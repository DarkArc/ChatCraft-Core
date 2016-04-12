package com.nearce.chatcraft.sponge;
import com.nearce.chatcraft.ChatMessage;
import com.nearce.chatcraft.ChatParticipant;
import com.nearce.chatcraft.GameServer;
import com.nearce.chatcraft.WebSocketHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.achievement.GrantAchievementEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
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

@Plugin(id = "com.nearce.chatcraft.sponge", name = "ChatCraft", version = "1.0", description = "Chat via websockets!")
public class ChatCraftPlugin {

    private static WebSocketHandler webSocketHandler;

    public static Collection<ChatParticipant> getConnectedParticipants() {
        return webSocketHandler.getConnectedParticipants();
    }

    private List<ChatParticipant> participants = new CopyOnWriteArrayList<>();

    @Listener
    public void onStart(GameStartedServerEvent event) throws UnknownHostException {
        webSocketHandler = new WebSocketHandler(new GameServer() {
            @Override
            public List<ChatParticipant> getLocalParticipants() {
                return participants;
            }

            @Override
            public String sanitize(String input) {
                return input.replaceAll("[^\\x00-\\x80]", "");
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
        });
        webSocketHandler.start();

        Sponge.getCommandManager().removeMapping(Sponge.getCommandManager().get("list").get());
        Sponge.getCommandManager().register(this, ChatCraftListCommand.aquireSpec(), "list");
    }

    @Listener(order = Order.POST)
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        ChatParticipant participant = new ChatParticipant(player.getUniqueId(), player.getName());
        participants.add(participant);
        webSocketHandler.clientJoin(participant);
    }

    @Listener(order = Order.POST)
    public void onPlayerLeave(ClientConnectionEvent.Disconnect event) {
        Player player = event.getTargetEntity();
        ChatParticipant participant = new ChatParticipant(player.getUniqueId(), player.getName());
        participants.remove(participant);
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
