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

    private final Map<UUID, ChatBoxHud> playerChatUIs = new ConcurrentHashMap<>();

    private final ExecutorService chatExecutor = Executors.newCachedThreadPool();

    public Shodo(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void start() {
        new ShodoAPI(this);

        getEventRegistry().register(PlayerConnectEvent.class, event -> {
            if (playerChatUIs.containsKey(event.getPlayerRef().getUuid())) {
                return;
            }
            PlayerRef ref = event.getPlayerRef();
            Player player = event.getPlayer();

            AnchorBuilder anchor = new AnchorBuilder()
                    .setLeft(Value.of(70))
                    .setTop(Value.of(20))
                    .setWidth(Value.of(800))
                    .setHeight(Value.of(300));

            ChatBoxHud chatBoxHud = new ChatBoxHud(player, ref, anchor, BuiltInFontData.INSTANCE.ofScale(1.3));
            playerChatUIs.put(ref.getUuid(), chatBoxHud);
            chatBoxHud.updateHud();
        });

        getEventRegistry().register(PlayerDisconnectEvent.class, event -> {
            UUID playerUUID = event.getPlayerRef().getUuid();
            ChatBoxHud removed = playerChatUIs.remove(playerUUID);
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

    public ChatBoxHud getTextBoxUI(PlayerRef playerRef) {
        return playerChatUIs.get(playerRef.getUuid());
    }

    public Set<ChatBoxHud> getTextBoxUIs() {
        return Set.copyOf(playerChatUIs.values());
    }
}
