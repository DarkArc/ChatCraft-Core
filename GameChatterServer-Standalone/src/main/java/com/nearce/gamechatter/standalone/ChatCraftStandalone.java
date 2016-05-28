/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nearce.gamechatter.standalone;
import com.nearce.gamechatter.ChatMessage;
import com.nearce.gamechatter.ChatParticipant;
import com.nearce.gamechatter.GameServer;
import com.nearce.gamechatter.WebSocketHandler;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ChatCraftStandalone {
    public static void main(String[] args) throws UnknownHostException, InterruptedException {
        new ChatCraftStandalone().main();
    }

    private void main() throws UnknownHostException, InterruptedException {
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
            public void joinWhenLegal(String name, UUID clientID, Consumer<String> verification, Consumer<String> join) {
                join.accept(name);
            }

            @Override
            public void clientJoinToLocal(ChatParticipant client) {
                System.out.println(client.getName() + " has joined remote chat");
            }

            @Override
            public void clientLeaveToLocal(ChatParticipant client) {
                System.out.println(client.getName() + " has left remote chat");
            }

            @Override
            public void clientMessageToLocal(ChatMessage message) {
                System.out.println("<" + message.getSender().getName() + "> " + message.getMessage());
            }

            @Override
            public void clientPrivateMessageToLocal(ChatMessage message, String toName) {
                System.out.println("[" + message.getSender().getName() + " -> " + toName + "] " + message.getMessage());
            }
        });

        Thread thread = new Thread(webSocketHandler);
        thread.start();

        thread.join();
    }
}
