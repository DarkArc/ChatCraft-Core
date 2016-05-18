/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nearce.gamechatter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.function.Predicate;

public class WebSocketHandler extends WebSocketServer {
    private Map<InetSocketAddress, WebSocket> pendingSockets = new HashMap<>();
    private Map<InetSocketAddress, RemoteChatParticipant> participantMap = new HashMap<>();

    private GameServer gameServer;

    public WebSocketHandler(GameServer gameServer) throws UnknownHostException {
        super(new InetSocketAddress(8080));
        this.gameServer = gameServer;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        pendingSockets.put(conn.getRemoteSocketAddress(), conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        leave(conn.getRemoteSocketAddress(), new JsonObject());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        process(conn.getRemoteSocketAddress(), message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    public Collection<RemoteChatParticipant> getConnectedParticipants() {
        return participantMap.values();
    }

    private void process(InetSocketAddress address, String message) {
        JsonObject object = new JsonParser().parse(message).getAsJsonObject();

        UUID identifier = UUID.fromString(object.getAsJsonPrimitive("user").getAsString());
        String method = object.getAsJsonPrimitive("method").getAsString();
        JsonObject params = object.getAsJsonObject("params");

        switch (method) {
            case "join":
                join(address, identifier, params);
                break;
            case "leave":
                leave(address, params);
                break;
            case "send":
                sendMessage(address, params);
                break;
            case "psend":
                sendPrivateMessage(address, params);
                break;
        }
    }

    private void sendToRemoteClients(JsonObject message) {
        for (RemoteChatParticipant participant : participantMap.values()) {
            participant.sendMessage(message.toString());
        }
    }

    private void sendToRemoteClientsWhere(JsonObject message, Predicate<RemoteChatParticipant> predicate) {
        participantMap.values().stream().filter(predicate).forEach(participant -> participant.sendMessage(message.toString()));
    }

    private void join(InetSocketAddress address, UUID identifier, JsonObject params) {
        String requestedName = gameServer.sanitize(params.getAsJsonPrimitive("name").getAsString());

        WebSocket pendingSocket = pendingSockets.remove(address);

        // If the name is not valid, close their connection
        if (requestedName.isEmpty()) {
            pendingSocket.close();
            return;
        }

        gameServer.joinWhenLegal(
                requestedName,
                identifier,
                (code) -> {
                    JsonObject requestParams = new JsonObject();
                    requestParams.addProperty("code", code);

                    JsonObject request = new JsonObject();
                    request.addProperty("method", "verify");
                    request.add("params", requestParams);

                    pendingSocket.send(request.toString());
                },
                (name) -> {
                    RemoteChatParticipant participant = new RemoteChatParticipant(
                            pendingSocket, identifier, name
                    );

                    participantMap.put(address, participant);

                    clientJoin(participant, true);
                    gameServer.remoteClientJoin(participant);

                    sendParticipants(participant);
                }
        );
    }

    private void sendParticipants(RemoteChatParticipant targetParticipant) {
        JsonObject requestParams = new JsonObject();

        JsonArray localParticipants = new JsonArray();
        gameServer.getLocalParticipants().stream().forEach(p -> {
            JsonObject participant = new JsonObject();
            participant.addProperty("name", p.getName());
            localParticipants.add(participant);
        });

        JsonArray remoteParticipants = new JsonArray();
        getConnectedParticipants().stream().forEach(p -> {
            JsonObject participant = new JsonObject();
            participant.addProperty("name", p.getName());
            remoteParticipants.add(participant);
        });

        requestParams.add("server", localParticipants);
        requestParams.add("remote", remoteParticipants);

        JsonObject request = new JsonObject();
        request.addProperty("method", "list");
        request.add("params", requestParams);

        targetParticipant.sendMessage(request.toString());
    }

    private void leave(InetSocketAddress address, JsonObject params) {
        if (pendingSockets.remove(address) != null) {
            return;
        }

        ChatParticipant participant = participantMap.remove(address);
        if (participant != null) {
            clientLeave(participant, true);
            gameServer.remoteClientLeave(participant);
        }
    }

    private void sendMessage(InetSocketAddress address, JsonObject params) {
        ChatParticipant participant = participantMap.get(address);
        if (participant != null) {
            String message = gameServer.sanitize(params.getAsJsonPrimitive("message").getAsString()).trim();
            if (message.isEmpty()) {
                return;
            }

            clientSendMessage(participant, message);

            gameServer.remoteClientSendMessage(new ChatMessage(participant, message));
        }
    }

    private void sendPrivateMessage(InetSocketAddress address, JsonObject params) {
        ChatParticipant participant = participantMap.get(address);
        if (participant != null) {
            String user = gameServer.sanitize(params.getAsJsonPrimitive("target").getAsString()).trim();
            String message = gameServer.sanitize(params.getAsJsonPrimitive("message").getAsString()).trim();
            if (user.isEmpty() || message.isEmpty()) {
                return;
            }

            clientSendPrivateMessage(participant, user, message);

            gameServer.remoteClientSendPrivateMessage(new ChatMessage(participant, message), user);
        }
    }

    public void systemMessage(String message) {
        JsonObject requestParams = new JsonObject();
        requestParams.addProperty("message", message);

        JsonObject request = new JsonObject();
        request.addProperty("method", "ssend");
        request.add("params", requestParams);

        sendToRemoteClients(request);
    }

    public void clientJoin(ChatParticipant participant) {
        clientJoin(participant, false);
    }

    private void clientJoin(ChatParticipant participant, boolean remote) {
        JsonObject requestParams = new JsonObject();
        requestParams.addProperty("name", participant.getName());
        requestParams.addProperty("remote", remote);

        JsonObject request = new JsonObject();
        request.addProperty("method", "join");
        request.add("params", requestParams);

        sendToRemoteClients(request);
    }

    public void clientLeave(ChatParticipant participant) {
        clientLeave(participant, false);
    }

    private void clientLeave(ChatParticipant participant, boolean remote) {
        JsonObject requestParams = new JsonObject();
        requestParams.addProperty("name", participant.getName());
        requestParams.addProperty("remote", remote);

        JsonObject request = new JsonObject();
        request.addProperty("method", "leave");
        request.add("params", requestParams);

        sendToRemoteClients(request);
    }

    public void clientSendMessage(ChatParticipant participant, String message) {
        JsonObject requestParams = new JsonObject();
        requestParams.addProperty("sender", participant.getName());
        requestParams.addProperty("message", message);

        JsonObject request = new JsonObject();
        request.addProperty("method", "send");
        request.add("params", requestParams);

        sendToRemoteClients(request);
    }

    public void clientSendPrivateMessage(ChatParticipant participant, String toName, String message) {
        JsonObject requestParams = new JsonObject();
        requestParams.addProperty("sender", participant.getName());
        requestParams.addProperty("target", toName);
        requestParams.addProperty("message", message);

        JsonObject request = new JsonObject();
        request.addProperty("method", "psend");
        request.add("params", requestParams);

        sendToRemoteClientsWhere(request, chatParticipant -> chatParticipant.getName().equals(toName));
    }
}
