package com.nearce.chatcraft.sponge;
import com.nearce.chatcraft.ChatMessage;
import com.nearce.chatcraft.ChatParticipant;
import com.nearce.chatcraft.WebSocketHandler;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.net.UnknownHostException;
import java.util.Optional;

@Plugin(id = "com.nearce.chatcraft.sponge", name = "ChatCraft", version = "1.0", description = "Chat via websockets!")
public class ChatCraftPlugin {

    private WebSocketHandler webSocketHandler;

    @Listener
    public void onStart(GameStartingServerEvent event) throws UnknownHostException {
        webSocketHandler = new WebSocketHandler() {
            @Override
            public void clientJoin(ChatParticipant client) {
                MessageChannel.TO_ALL.send(Text.of(TextColors.YELLOW, client.getName(), " has joined remote chat."));
            }

            @Override
            public void clientLeave(ChatParticipant client) {
                MessageChannel.TO_ALL.send(Text.of(TextColors.YELLOW, client.getName(), " has left remote chat."));
            }

            @Override
            public void receiveMessage(ChatMessage message) {
                MessageChannel.TO_ALL.send(Text.of("<", message.getSender().getName(), "> ", message.getMessage()));
            }
        };
        webSocketHandler.start();
    }

    @Listener
    public void onPlayerChat(MessageChannelEvent.Chat event) {
        String message = event.getRawMessage().toPlain();
        Optional<Player> optSender= event.getCause().first(Player.class);
        if (optSender.isPresent()) {
            Player sender = optSender.get();
            ChatParticipant participant = new ChatParticipant(sender.getUniqueId(), sender.getName());
            webSocketHandler.sendMessage(participant, message);
        }
    }
}
