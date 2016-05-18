/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nearce.gamechatter.sponge;

import com.nearce.gamechatter.ChatParticipant;
import com.nearce.gamechatter.WebSocketHandler;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.achievement.GrantAchievementEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.channel.MessageChannel;

import java.util.Optional;

public class SpongeIntegrationListener {

    private WebSocketHandler webSocketHandler;

    public SpongeIntegrationListener(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Listener(order = Order.POST)
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        ChatParticipant participant = new SpongeChatParticipant(player.getMessageChannel(), player.getUniqueId(), player.getName());
        webSocketHandler.clientJoin(participant);
    }

    @Listener(order = Order.POST)
    public void onPlayerLeave(ClientConnectionEvent.Disconnect event) {
        Player player = event.getTargetEntity();
        ChatParticipant participant = new SpongeChatParticipant(player.getMessageChannel(), player.getUniqueId(), player.getName());
        webSocketHandler.clientLeave(participant);
    }

    @Listener(order = Order.POST)
    public void onUserChat(MessageChannelEvent.Chat event) {
        String message = event.getFormatter().getBody().toText().toPlain();
        Optional<Player> optSender= event.getCause().first(Player.class);
        if (optSender.isPresent()) {
            Player sender = optSender.get();
            ChatParticipant participant = new SpongeChatParticipant(sender.getMessageChannel(), sender.getUniqueId(), sender.getName());
            webSocketHandler.clientSendMessage(participant, message);
        }
    }

    @Listener(order = Order.POST)
    public void onUserDeath(DestructEntityEvent.Death event) {
        Living target = event.getTargetEntity();
        if (target instanceof Player) {
            webSocketHandler.systemMessage(event.getMessage().toPlain());
        }
    }

    @Listener(order = Order.POST)
    public void onAchievementGrant(GrantAchievementEvent.TargetPlayer event) {
        webSocketHandler.systemMessage(event.getMessage().toPlain());
    }

}
