package cc.irori.shodo;

import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;

import java.awt.*;

// TODO: move chat feature to its own subplugin
public class TextBox {

    private static final float SHADOW_COLOR_SCALE = 0.25f;

    private final boolean hasShadow;
    private final Typesetter typesetter;
    private final Color defaultColor;
    private final FontData font;

    private TextBox(int width, int height, Color defaultColor, boolean hasShadow, FontData font, Runnable updateCallback, int cleanupPeriodSeconds) {
        this.typesetter = new Typesetter(width, height, font, updateCallback, cleanupPeriodSeconds);
        this.hasShadow = hasShadow;
        this.defaultColor = defaultColor;
        this.font = font;
    }

    public void render(UICommandBuilder uiCommandBuilder, String selector) {
        if (hasShadow) {
            for (RenderGlyph glyph : typesetter.calculateRenderQueue(0, 0)) {
                drawGlyph(uiCommandBuilder, selector, glyph, glyph.x(), glyph.y(), true);
            }
        }
        for (RenderGlyph glyph : typesetter.calculateRenderQueue(0, 0)) {
            drawGlyph(uiCommandBuilder, selector, glyph, glyph.x(), glyph.y(), false);
        }
    }

    public Typesetter typesetter() {
        return typesetter;
    }

    private void drawGlyph(UICommandBuilder uiCommandBuilder, String selector, RenderGlyph glyph, double x, double y, boolean asShadow) {
        Color color = glyph.color() != null ? glyph.color() : defaultColor;
        if (asShadow) {
            drawGlyphWithColor(uiCommandBuilder, selector, glyph, x + font.getScale(), y + font.getScale(), getShadowColor(color));
            return;
        }
        drawGlyphWithColor(uiCommandBuilder, selector, glyph, x, y, color);
    }

    private void drawGlyphWithColor(UICommandBuilder uiCommandBuilder, String selector, RenderGlyph glyph, double x, double y, Color color) {
        if (glyph.meta().atlas() == null) {
            uiCommandBuilder.appendInline(selector, String.format(
                    "Group { Anchor: (Left: %f, Top: %f, Width: %f, Height: %f); Background: (TexturePath: \"Shodo/Glyphs/invalid.png\", Color: #%02x%02x%02x); }",
                    x,
                    y,
                    font.getTileSize() * font.getScale(),
                    font.getTileSize() * font.getScale(),
                    color.getRed(),
                    color.getGreen(),
                    color.getBlue()
            ));
        } else {
            uiCommandBuilder.appendInline(selector, String.format(
                    "Group { Anchor: (Left: %f, Top: %f, Width: %f, Height: %f); Background: PatchStyle(TexturePath: \"Shodo/Glyphs/%s.png\", Area: (Left: %d, Top: %d, Right: %d, Bottom: %d), Color: #%02x%02x%02x); }",
                    x,
                    y,
                    font.getTileSize() * font.getScale(),
                    font.getTileSize() * font.getScale(),
                    glyph.meta().atlas(),
                    glyph.meta().imageX(),
                    glyph.meta().imageY(),
                    font.getAtlasSize() - glyph.meta().imageX() - font.getTileSize(),
                    font.getAtlasSize() - glyph.meta().imageY() - font.getTileSize(),
                    color.getRed(),
                    color.getGreen(),
                    color.getBlue()
            ));
        }
    }

    private static Color getShadowColor(Color color) {
        return new Color(
                (int) Math.clamp(color.getRed() * SHADOW_COLOR_SCALE, 0, 255),
                (int) Math.clamp(color.getGreen() * SHADOW_COLOR_SCALE, 0, 255),
                (int) Math.clamp(color.getBlue() * SHADOW_COLOR_SCALE, 0, 255),
                color.getAlpha()
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private int width = 800;
        private int height = 300;
        private Color defaultColor = Color.WHITE;
        private boolean hasShadow = true;
        private FontData font = BuiltInFontData.INSTANCE.ofScale(1.3);
        private Runnable updateCallback = () -> {};
        private int cleanupPeriodSeconds = 1;

        private Builder() {
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setDefaultColor(Color defaultColor) {
            this.defaultColor = defaultColor;
            return this;
        }

        public Builder setShadow(boolean hasShadow) {
            this.hasShadow = hasShadow;
            return this;
        }

        public Builder setFont(FontData font) {
            this.font = font;
            return this;
        }

        public Builder setUpdateCallback(Runnable updateCallback) {
            this.updateCallback = updateCallback;
            return this;
        }

        public Builder setCleanupPeriodSeconds(int cleanupPeriodSeconds) {
            this.cleanupPeriodSeconds = cleanupPeriodSeconds;
            return this;
        }

        public TextBox build() {
            return new TextBox(width, height, defaultColor, hasShadow, font, updateCallback, cleanupPeriodSeconds);
        }
    }
}
