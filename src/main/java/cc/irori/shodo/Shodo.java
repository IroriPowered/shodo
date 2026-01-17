package cc.irori.shodo;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.common.semver.SemverRange;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Shodo extends JavaPlugin {

    private static final PluginIdentifier SCAFFOLD_PLUGIN = new PluginIdentifier("IroriPowered", "Scaffold_Hytale");

    private final BuiltInFontdata fontData = new BuiltInFontdata(1.35);
    private final Map<UUID, TextBoxUI> playerTextUIs = new ConcurrentHashMap<>();

    private final ExecutorService chatExecutor = Executors.newCachedThreadPool();

    public Shodo(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void start() {
        new ShodoAPI(this);
        fontData.load();

        getEventRegistry().register(PlayerConnectEvent.class, event -> {
            if (playerTextUIs.containsKey(event.getPlayerRef().getUuid())) {
                return;
            }
            PlayerRef ref = event.getPlayerRef();
            Player player = event.getPlayer();

            AnchorBuilder anchor = new AnchorBuilder()
                    .setLeft(Value.of(70))
                    .setTop(Value.of(20))
                    .setWidth(Value.of(800))
                    .setHeight(Value.of(300));

            TextBoxUI textBoxUI = new TextBoxUI(player, ref, anchor, fontData);
            playerTextUIs.put(ref.getUuid(), textBoxUI);
            textBoxUI.updateHud();
        });

        getEventRegistry().register(PlayerDisconnectEvent.class, event -> {
            UUID playerUUID = event.getPlayerRef().getUuid();
            TextBoxUI removed = playerTextUIs.remove(playerUUID);
            if (removed != null) {
                removed.shutdown();
            }
        });

        getEventRegistry().registerAsyncGlobal(PlayerChatEvent.class, future -> future.thenApply(event -> {
            if (!HytaleServer.get().getPluginManager().hasPlugin(SCAFFOLD_PLUGIN, SemverRange.WILDCARD)) {
                ShodoAPI.getInstance().broadcastMessage(event.getSender().getUsername() + ": " + event.getContent());
            }
            return event;
        }));
    }

    @Override
    protected void shutdown() {
        chatExecutor.shutdownNow();
    }

    public TextBoxUI getTextBoxUI(PlayerRef playerRef) {
        return playerTextUIs.get(playerRef.getUuid());
    }

    public Set<TextBoxUI> getTextBoxUIs() {
        return Set.copyOf(playerTextUIs.values());
    }
}
