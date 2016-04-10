package com.nearce.chatcraft.sponge;
import com.nearce.chatcraft.ChatMessage;
import com.nearce.chatcraft.ChatParticipant;
import com.nearce.chatcraft.WebSocketHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Plugin(id = "com.nearce.chatcraft.sponge", name = "ChatCraft", version = "1.0", description = "Chat via websockets!")
public class ChatCraftPlugin {

    private static WebSocketHandler webSocketHandler;

    public static Collection<ChatParticipant> getConnectedParticipants() {
        return webSocketHandler.getConnectedParticipants();
    }

    private List<ChatParticipant> participants = new CopyOnWriteArrayList<>();

    @Listener
    public void onStart(GameStartedServerEvent event) throws UnknownHostException {
        webSocketHandler = new WebSocketHandler() {
            @Override
            public List<ChatParticipant> getLocalParticipants() {
                return participants;
            }

            @Override
            public void remoteClientJoin(ChatParticipant client) {
                MessageChannel.TO_ALL.send(Text.of(TextColors.YELLOW, client.getName(), " has joined remote chat"));
            }

            @Override
            public void remoteClientLeave(ChatParticipant client) {
                MessageChannel.TO_ALL.send(Text.of(TextColors.YELLOW, client.getName(), " has left remote chat"));
            }

            @Override
            public void remoteClientSendMessage(ChatMessage message) {
                MessageChannel.TO_ALL.send(Text.of("<", message.getSender().getName(), "> ", message.getMessage()));
            }
        };
        webSocketHandler.start();

        Sponge.getCommandManager().removeMapping(Sponge.getCommandManager().get("list").get());
        Sponge.getCommandManager().register(this, ChatCraftListCommand.aquireSpec(), "list");
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        ChatParticipant participant = new ChatParticipant(player.getUniqueId(), player.getName());
        participants.add(participant);
        webSocketHandler.clientJoin(participant);
    }

    @Listener
    public void onPlayerLeave(ClientConnectionEvent.Disconnect event) {
        Player player = event.getTargetEntity();
        ChatParticipant participant = new ChatParticipant(player.getUniqueId(), player.getName());
        participants.remove(participant);
        webSocketHandler.clientLeave(participant);
    }

    @Listener
    public void onUserChat(MessageChannelEvent.Chat event) {
        String message = event.getRawMessage().toPlain();
        Optional<Player> optSender= event.getCause().first(Player.class);
        if (optSender.isPresent()) {
            Player sender = optSender.get();
            ChatParticipant participant = new ChatParticipant(sender.getUniqueId(), sender.getName());
            webSocketHandler.clientSendMessage(participant, message);
        }
    }
}
