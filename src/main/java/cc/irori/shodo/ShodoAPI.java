package cc.irori.shodo;

import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nullable;
import java.awt.*;

public class ShodoAPI {

    private static ShodoAPI instance;

    private final Shodo shodo;

    protected ShodoAPI(Shodo shodo) {
        instance = this;
        this.shodo = shodo;
    }

    public void broadcastMessage(String message) {
        broadcastMessage(message, null);
    }

    public void broadcastMessage(String message, @Nullable Color color) {
        for (TextBoxUI textBoxUI : shodo.getTextBoxUIs()) {
            textBoxUI.addMessage(message, color);
        }
    }

    public void sendMessage(PlayerRef playerRef, String message) {
        sendMessage(playerRef, message, null);
    }

    public void sendMessage(PlayerRef playerRef, String message, @Nullable Color color) {
        TextBoxUI textBoxUI = shodo.getTextBoxUI(playerRef);
        if (textBoxUI != null) {
            textBoxUI.addMessage(message, color);
        }
    }

    public static ShodoAPI getInstance() {
        return instance;
    }
}
