package cc.irori.shodo;

import com.buuz135.mhud.MultipleHUD;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nullable;
import java.awt.*;

public class ChatBoxHud extends CustomUIHud {

    private final Player player;
    private final PlayerRef playerRef;

    private final AnchorBuilder anchor;
    private final TextBox textBox;

    protected ChatBoxHud(Player player, PlayerRef playerRef, AnchorBuilder anchor, FontData font) {
        this(player, playerRef, anchor, font, 1);
    }

    protected ChatBoxHud(Player player, PlayerRef playerRef, AnchorBuilder anchor, FontData font, int cleanupPeriodSeconds) {
        super(playerRef);
        if (anchor.getWidth() == null || anchor.getHeight() == null) {
            throw new IllegalArgumentException("Anchor must have both width and height defined.");
        }

        this.player = player;
        this.playerRef = playerRef;

        this.anchor = anchor;
        this.textBox = TextBox.builder()
                .setWidth(anchor.getWidth().getValue())
                .setHeight(anchor.getHeight().getValue())
                .setUpdateCallback(this::updateHud)
                .build();
    }

    @Override
    protected void build(@NonNullDecl UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("Shodo/TextBox.ui");
        uiCommandBuilder.setObject("#TextBox.Anchor", anchor.build());

        textBox.render(uiCommandBuilder, "#TextBox");
    }

    public void addMessage(String message) {
        addMessage(message, null);
    }

    public void addMessage(String message, @Nullable Color color) {
        textBox.typesetter().addMessage(message, color);
        updateHud();
    }

    public void shutdown() {
        textBox.typesetter().shutdown();
    }

    public void clear() {
        textBox.typesetter().clear();
    }

    public void updateHud() {
        MultipleHUD.getInstance().setCustomHud(player, playerRef, "Shodo_" + this.hashCode(), this);
    }
}
