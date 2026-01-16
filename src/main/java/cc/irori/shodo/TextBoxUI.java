package cc.irori.shodo;

import com.buuz135.mhud.MultipleHUD;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.Locale;

public class TextBoxUI extends CustomUIHud {

    private final Player player;
    private final PlayerRef playerRef;

    private final AnchorBuilder anchor;
    private final Typesetter typesetter;
    private final FontData font;

    private String mainColor = "#ffffff";

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

        addGlyphs(uiCommandBuilder, "#2A2A2A", font.getScale(), font.getScale());
        addGlyphs(uiCommandBuilder, mainColor, 0, 0);
    }

    private void addGlyphs(UICommandBuilder uiCommandBuilder, String color, double xOffset, double yOffset) {
        for (RenderGlyph glyph : typesetter.calculateRenderQueue(0, 0)) {
            String hex = String.format("%04x", (int) glyph.character()).toUpperCase(Locale.ROOT);
            uiCommandBuilder.appendInline("#TextBox", String.format(
                    "Group { Anchor: (Left: %f, Top: %f, Width: %f, Height: %f); Background: (TexturePath: \"Shodo/Glyphs/u%s.png\", Color: %s); }",
                    glyph.x() + xOffset,
                    glyph.y() + yOffset,
                    16 * font.getScale(),
                    16 * font.getScale(),
                    hex,
                    color
            ));
        }
    }

    public void setMainColor(String mainColor) {
        this.mainColor = mainColor;
    }

    public void addMessage(String message) {
        typesetter.addMessage(message);
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
}
