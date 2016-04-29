package com.nearce.chatcraft.sponge;
import com.nearce.chatcraft.ChatParticipant;
import com.nearce.chatcraft.WebSocketHandler;
import com.nearce.chatcraft.db.DatabaseConfigLoader;
import com.nearce.chatcraft.sponge.command.ChatCraftListCommand;
import com.nearce.chatcraft.sponge.command.ChatCraftVerifyCommand;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;

import java.net.UnknownHostException;
import java.util.Collection;

@Plugin(id = "com.nearce.chatcraft.sponge", name = "ChatCraft", version = "1.0", description = "Chat via websockets!")
public class ChatCraftPlugin {
    private static ChatCraftPlugin inst;

    public ChatCraftPlugin() {
        inst = this;
    }

    public static ChatCraftPlugin inst() {
        return inst;
    }

    private WebSocketHandler webSocketHandler;

    public Collection<ChatParticipant> getConnectedParticipants() {
        return webSocketHandler.getConnectedParticipants();
    }

    private SpongeGameServer gameServer;

    public SpongeGameServer getGameServer() {
        return gameServer;
    }

    @Listener
    public void onStart(GameStartedServerEvent event) throws UnknownHostException {
        new DatabaseConfigLoader().init();

        gameServer = new SpongeGameServer();

        webSocketHandler = new WebSocketHandler(gameServer);
        webSocketHandler.start();

        SpongeIntegrationListener integrationListener = new SpongeIntegrationListener(webSocketHandler);

        Sponge.getEventManager().registerListeners(this, gameServer);
        Sponge.getEventManager().registerListeners(this, integrationListener);

        Sponge.getCommandManager().removeMapping(Sponge.getCommandManager().get("list").get());
        Sponge.getCommandManager().register(this, ChatCraftListCommand.aquireSpec(), "list");
        Sponge.getCommandManager().register(this, ChatCraftVerifyCommand.aquireSpec(), "verifyremotechat", "vrc");
    }
}
