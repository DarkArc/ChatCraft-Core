package com.nearce.chatcraft.sponge;

import com.nearce.chatcraft.ChatParticipant;
import com.nearce.chatcraft.WebSocketHandler;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.achievement.GrantAchievementEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.Collection;
import java.util.Optional;

public class SpongeIntegrationListener {

    private WebSocketHandler webSocketHandler;

    public SpongeIntegrationListener(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Listener(order = Order.POST)
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        ChatParticipant participant = new ChatParticipant(player.getUniqueId(), player.getName());
        webSocketHandler.clientJoin(participant);
    }

    @Listener(order = Order.POST)
    public void onPlayerLeave(ClientConnectionEvent.Disconnect event) {
        Player player = event.getTargetEntity();
        ChatParticipant participant = new ChatParticipant(player.getUniqueId(), player.getName());
        webSocketHandler.clientLeave(participant);
    }

    @Listener(order = Order.POST)
    public void onUserChat(MessageChannelEvent.Chat event) {
        String message = event.getRawMessage().toPlain();
        Optional<Player> optSender= event.getCause().first(Player.class);
        if (optSender.isPresent()) {
            Player sender = optSender.get();
            ChatParticipant participant = new ChatParticipant(sender.getUniqueId(), sender.getName());
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
