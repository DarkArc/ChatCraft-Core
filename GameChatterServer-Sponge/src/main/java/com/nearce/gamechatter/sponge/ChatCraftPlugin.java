/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nearce.gamechatter.sponge;
import com.nearce.gamechatter.ChatParticipant;
import com.nearce.gamechatter.WebSocketHandler;
import com.nearce.gamechatter.db.DatabaseConfigLoader;
import com.nearce.gamechatter.sponge.command.ChatCraftListCommand;
import com.nearce.gamechatter.sponge.command.ChatCraftVerifyCommand;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;

import java.net.UnknownHostException;
import java.util.Collection;

@Plugin(id = "com.nearce.gamechatter.sponge", name = "ChatCraft", version = "1.0", description = "Chat via websockets!")
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
