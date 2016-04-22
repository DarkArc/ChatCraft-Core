package com.nearce.chatcraft.standalone;
import com.nearce.chatcraft.ChatMessage;
import com.nearce.chatcraft.ChatParticipant;
import com.nearce.chatcraft.GameServer;
import com.nearce.chatcraft.WebSocketHandler;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

public class ChatCraftStandalone {
    public static void main(String[] args) throws UnknownHostException {
        WebSocketHandler webSocketHandler = new WebSocketHandler(new GameServer() {
            @Override
            public List<ChatParticipant> getLocalParticipants() {
                return Collections.emptyList();
            }

            @Override
            public String sanitize(String input) {
                return input.replaceAll("[^\\x00-\\x80]", "");
            }

            @Override
            public void remoteClientJoin(ChatParticipant client) {
                System.out.println(client.getName() + " has joined remote chat");
            }

            @Override
            public void remoteClientLeave(ChatParticipant client) {
                System.out.println(client.getName() + " has left remote chat");
            }

            @Override
            public void remoteClientSendMessage(ChatMessage message) {
                System.out.println("<" + message.getSender().getName() + "> " + message.getMessage());
            }
        });
        webSocketHandler.start();
    }
}
