/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nearce.gamechatter.sponge;
import com.nearce.gamechatter.ChatParticipant;
import com.nearce.gamechatter.RemoteChatParticipant;
import com.nearce.gamechatter.WebSocketHandler;
import com.nearce.gamechatter.db.DatabaseConfigLoader;
import com.nearce.gamechatter.sponge.command.GameChatterListCommand;
import com.nearce.gamechatter.sponge.command.GameChatterVerifyCommand;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.net.UnknownHostException;
import java.util.Collection;

@Plugin(id = "gamechatter", name = "Game Chatter", version = "1.0", description = "Chat via websockets!")
public class GameChatterPlugin {
    private static GameChatterPlugin inst;

    public GameChatterPlugin() {
        inst = this;
    }

    public static GameChatterPlugin inst() {
        return inst;
    }

    private WebSocketHandler webSocketHandler;

    public Collection<RemoteChatParticipant> getConnectedParticipants() {
        return webSocketHandler.getConnectedParticipants();
    }

    private SpongeGameServer gameServer;

    public SpongeGameServer getGameServer() {
        return gameServer;
    }

    public void sendSystemMessage(String message) {
        webSocketHandler.systemMessage(message);
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
        Sponge.getCommandManager().register(this, GameChatterListCommand.aquireSpec(), "list");
        Sponge.getCommandManager().register(this, GameChatterVerifyCommand.aquireSpec(), "verifyremotechat", "vrc");
    }
}
