package cc.irori.shodo;

import com.buuz135.mhud.MultipleHUD;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Locale;

public class TextBoxUI extends CustomUIHud {

    private static final float SHADOW_COLOR_SCALE = 0.25f;

    private final Player player;
    private final PlayerRef playerRef;

    private final AnchorBuilder anchor;
    private final Typesetter typesetter;
    private final FontData font;

    private Color defaultColor = Color.WHITE;

    public TextBoxUI(Player player, PlayerRef playerRef, AnchorBuilder anchor, FontData font) {
        this(player, playerRef, anchor, font, 1);
    }

    public TextBoxUI(Player player, PlayerRef playerRef, AnchorBuilder anchor, FontData font, int cleanupPeriodSeconds) {
        super(playerRef);
        if (anchor.getWidth() == null || anchor.getHeight() == null) {
            throw new IllegalArgumentException("Anchor must have both width and height defined.");
        }

        this.player = player;
        this.playerRef = playerRef;

        this.anchor = anchor;
        this.font = font;
        this.typesetter = new Typesetter(anchor.getWidth().getValue(), anchor.getHeight().getValue(), font, this::updateHud, cleanupPeriodSeconds);
    }

    @Override
    protected void build(@NonNullDecl UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("Shodo/TextBox.ui");
        uiCommandBuilder.setObject("#TextBox.Anchor", anchor.build());

        for (RenderGlyph glyph : typesetter.calculateRenderQueue(0, 0)) {
            drawGlyph(uiCommandBuilder, glyph, glyph.x(), glyph.y(), true);
        }
        for (RenderGlyph glyph : typesetter.calculateRenderQueue(0, 0)) {
            drawGlyph(uiCommandBuilder, glyph, glyph.x(), glyph.y(), false);
        }
    }

    private void drawGlyph(UICommandBuilder uiCommandBuilder, RenderGlyph glyph, double x, double y, boolean asShadow) {
        Color color = glyph.color() != null ? glyph.color() : defaultColor;
        if (asShadow) {
            drawGlyphWithColor(uiCommandBuilder, glyph, x + font.getScale(), y + font.getScale(), getShadowColor(color));
            return;
        }
        drawGlyphWithColor(uiCommandBuilder, glyph, x, y, color);
    }

    private void drawGlyphWithColor(UICommandBuilder uiCommandBuilder, RenderGlyph glyph, double x, double y, Color color) {
        if (glyph.meta().atlas() == null) {
            uiCommandBuilder.appendInline("#TextBox", String.format(
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
            uiCommandBuilder.appendInline("#TextBox", String.format(
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

    public void setDefaultColor(Color color) {
        this.defaultColor = color;
    }

    public void addMessage(String message) {
        addMessage(message, null);
    }

    public void addMessage(String message, @Nullable Color color) {
        typesetter.addMessage(message, color);
        updateHud();
    }

    public void shutdown() {
        typesetter.shutdown();
    }

    public void clear() {
        typesetter.clear();
        updateHud();
    }

    public void updateHud() {
        MultipleHUD.getInstance().setCustomHud(player, playerRef, "Shodo_" + this.hashCode(), this);
    }

    private static Color getShadowColor(Color color) {
        return new Color(
                (int) Math.clamp(color.getRed() * SHADOW_COLOR_SCALE, 0, 255),
                (int) Math.clamp(color.getGreen() * SHADOW_COLOR_SCALE, 0, 255),
                (int) Math.clamp(color.getBlue() * SHADOW_COLOR_SCALE, 0, 255),
                color.getAlpha()
        );
    }
}
