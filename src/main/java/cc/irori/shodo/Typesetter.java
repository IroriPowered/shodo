package cc.irori.shodo;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Typesetter {
    private final Object lock = new Object();

    private final int boxWidth;
    private final int boxHeight;
    private final FontData font;
    private final Runnable updateCallback;

    private final LinkedList<TextMessage> lines = new LinkedList<>();

    private ScheduledExecutorService scheduler;
    private volatile long lineLifespanMillis = 10_000;

    public Typesetter(int width, int height, FontData font, Runnable updateCallback) {
        this(width, height, font, updateCallback, 1);
    }

    public Typesetter(int width, int height, FontData font, Runnable updateCallback, int cleanupPeriodSeconds) {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Invalid box size");
        if (font == null) throw new IllegalArgumentException("FontData cannot be null");
        this.boxWidth = width;
        this.boxHeight = height;
        this.font = font;
        this.updateCallback = updateCallback;

        if (cleanupPeriodSeconds > 0) {
            this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "TextMessageExpiry");
                t.setDaemon(true);
                return t;
            });
            this.scheduler.scheduleAtFixedRate(this::pruneExpiredLines, cleanupPeriodSeconds,
                    cleanupPeriodSeconds, TimeUnit.SECONDS);
        }
    }

    public void setLineLifespanMillis(long lifespanMs) {
        if (lifespanMs < 0) throw new IllegalArgumentException("lifespan must be >= 0");
        this.lineLifespanMillis = lifespanMs;
    }

    public void addMessage(String text) {
        addMessage(text, null);
    }

    public void addMessage(String text, Color color) {
        if (text == null) return;
        long now = System.currentTimeMillis();
        List<List<RenderGlyph>> wrapped = wrapText(text, color);

        synchronized (lock) {
            for (int i = wrapped.size() - 1; i >= 0; i--) {
                List<RenderGlyph> lineGlyphs = wrapped.get(i);
                lines.addFirst(new TextMessage(lineGlyphs, color, now));
            }
            pruneLinesByCountAndHeight();
        }
    }

    public List<RenderGlyph> calculateRenderQueue(int startX, int startY) {
        List<RenderGlyph> draw = new ArrayList<>();
        double lineHeight = font.getLineHeight() * font.getScale();
        int maxY = startY + boxHeight;

        synchronized (lock) {
            double currentY = startY;
            for (TextMessage message : lines) {
                if (currentY + lineHeight > maxY) break;
                for (RenderGlyph g : message.glyphs) {
                    draw.add(new RenderGlyph(g.character(), g.meta(), startX + g.x(), currentY, g.color()));
                }
                currentY += lineHeight;
            }
        }
        return draw;
    }

    public void clear() {
        synchronized (lock) {
            lines.clear();
        }
    }

    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    public int getLineCount() {
        synchronized (lock) {
            return lines.size();
        }
    }

    private List<List<RenderGlyph>> wrapText(String text, Color color) {
        List<List<RenderGlyph>> wrapped = new ArrayList<>();
        if (text.isEmpty()) {
            wrapped.add(Collections.emptyList());
            return wrapped;
        }

        List<RenderGlyph> currentLine = new ArrayList<>();
        double currentLineWidth = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\n') {
                wrapped.add(currentLine);
                currentLine = new ArrayList<>();
                currentLineWidth = 0;
                continue;
            }

            GlyphMeta meta = font.getGlyph(c);
            double cw = (meta.width() + font.getSpacingWidth()) * font.getScale();
            if (currentLineWidth + cw > this.boxWidth && !currentLine.isEmpty()) {
                wrapped.add(currentLine);
                currentLine = new ArrayList<>();
                currentLineWidth = 0;
            }

            currentLine.add(new RenderGlyph(c, meta, currentLineWidth, 0, color));
            currentLineWidth += cw;
        }
        if (!currentLine.isEmpty()) wrapped.add(currentLine);
        return wrapped;
    }

    private void pruneLinesByCountAndHeight() {
        double lineHeight = font.getLineHeight() * font.getScale();
        double maxLines = Math.max(0, boxHeight / lineHeight);
        while (lines.size() > maxLines) {
            lines.removeLast();
        }
    }

    private void pruneExpiredLines() {
        long now = System.currentTimeMillis();
        long lifespan = lineLifespanMillis;
        if (lifespan <= 0) return;

        synchronized (lock) {
            boolean removed = false;
            while (!lines.isEmpty()) {
                TextMessage tail = lines.getLast();
                if (now - tail.createdAtMs >= lifespan) {
                    lines.removeLast();
                    removed = true;
                } else {
                    break;
                }
            }
            if (removed) {
                updateCallback.run();
                pruneLinesByCountAndHeight();
            }
        }
    }

    record TextMessage(List<RenderGlyph> glyphs, @Nullable Color color, long createdAtMs) {
    }
}
