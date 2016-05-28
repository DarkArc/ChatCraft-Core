/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nearce.gamechatter;

import com.google.gson.*;
import com.nearce.gamechatter.method.outgoing.OutMethod;
import com.nearce.gamechatter.method.incoming.InJoin;
import com.nearce.gamechatter.method.incoming.InMessage;
import com.nearce.gamechatter.method.incoming.InPrivateMessage;
import com.nearce.gamechatter.method.outgoing.*;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class WebSocketHandler extends WebSocketServer {
    private Map<InetSocketAddress, WebSocket> pendingSockets = new HashMap<>();
    private Map<InetSocketAddress, RemoteChatUser> participantMap = new HashMap<>();

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

    public Collection<ChatParticipant> getConnectedParticipants() {
        return participantMap.values().stream().map(RemoteChatUser::getParticipant).collect(Collectors.toList());
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

    private void sendToSocket(WebSocket socket, OutMethod request) {
        String renderedRequest = new Gson().toJson(request);
        sendToSocket(socket, renderedRequest);
    }

    private void sendToSocket(WebSocket socket, String renderedRequest) {
        socket.send(renderedRequest);
    }

    private void sendToRemoteClients(OutMethod request) {
        String renderedRequest = new Gson().toJson(request);
        for (RemoteChatUser user : participantMap.values()) {
            sendToSocket(user.getSocket(), renderedRequest);
        }
    }

    private void sendToRemoteClientsWhere(OutMethod request, Predicate<RemoteChatUser> predicate) {
        String renderedRequest = new Gson().toJson(request);
        participantMap.values().stream().filter(predicate).forEach(user -> sendToSocket(user.getSocket(), renderedRequest));
    }

    private void join(InetSocketAddress address, UUID identifier, JsonObject params) {
        InJoin joinRequest = new Gson().fromJson(params, InJoin.class);
        String requestedName = gameServer.sanitize(joinRequest.getName());

        WebSocket pendingSocket = pendingSockets.remove(address);

        // If the name is not valid, close their connection
        if (requestedName.isEmpty()) {
            pendingSocket.close();
            return;
        }

        gameServer.joinWhenLegal(
                requestedName,
                identifier,
                (code) -> sendToSocket(pendingSocket, OutVerificationCode.getRequest(code)),
                (name) -> {
                    ChatParticipant participant = new ChatParticipant(name);
                    RemoteChatUser user = new RemoteChatUser(
                            pendingSocket, participant
                    );

                    participantMap.put(address, user);

                    clientJoinToRemote(participant, true);
                    gameServer.clientJoinToLocal(participant);

                    sendParticipants(user);
                }
        );
    }

    private void sendParticipants(RemoteChatUser targetParticipant) {
        sendToSocket(targetParticipant.getSocket(), OutParticipantList.getRequest(gameServer.getLocalParticipants(), getConnectedParticipants()));
    }

    private void leave(InetSocketAddress address, JsonObject params) {
        if (pendingSockets.remove(address) != null) {
            return;
        }

        RemoteChatUser user = participantMap.remove(address);
        if (user != null) {
            ChatParticipant participant = user.getParticipant();
            clientLeaveToRemote(participant, true);
            gameServer.clientLeaveToLocal(participant);
        }
    }

    private void sendMessage(InetSocketAddress address, JsonObject params) {
        RemoteChatUser user = participantMap.get(address);
        if (user != null) {
            ChatParticipant participant = user.getParticipant();

            InMessage messageRequest = new Gson().fromJson(params, InMessage.class);
            String message = gameServer.sanitize(messageRequest.getMessage()).trim();
            if (message.isEmpty()) {
                return;
            }

            clientMessageToRemote(participant, message);

            gameServer.clientMessageToLocal(new ChatMessage(participant, message));
        }
    }

    private void sendPrivateMessage(InetSocketAddress address, JsonObject params) {
        RemoteChatUser user = participantMap.get(address);
        if (user != null) {
            InPrivateMessage privateMessage = new Gson().fromJson(params, InPrivateMessage.class);
            String target = gameServer.sanitize(privateMessage.getTarget()).trim();
            String message = gameServer.sanitize(privateMessage.getMessage()).trim();
            if (target.isEmpty() || message.isEmpty()) {
                return;
            }

            clientPrivateMessageToRemote(user.getParticipant(), target, message);

            gameServer.clientPrivateMessageToLocal(new ChatMessage(user.getParticipant(), message), target);
        }
    }

    public void systemMessageToRemote(String message) {
        sendToRemoteClients(OutSystemMessage.getRequest(message));
    }

    public void clientJoinToRemote(ChatParticipant participant) {
        clientJoinToRemote(participant, false);
    }

    private void clientJoinToRemote(ChatParticipant participant, boolean remote) {
        sendToRemoteClients(OutJoin.getRequest(participant.getName(), remote));
    }

    public void clientLeaveToRemote(ChatParticipant participant) {
        clientLeaveToRemote(participant, false);
    }

    private void clientLeaveToRemote(ChatParticipant participant, boolean remote) {
        sendToRemoteClients(OutLeave.getRequest(participant.getName(), remote));
    }

    public void clientMessageToRemote(ChatParticipant participant, String message) {
        sendToRemoteClients(OutMessage.getRequest(participant.getName(), message));
    }

    public void clientPrivateMessageToRemote(ChatParticipant participant, String toName, String message) {
        sendToRemoteClientsWhere(
                OutPrivateMessage.getRequest(participant.getName(), toName, message),
                user -> {
                    String participantName = user.getParticipant().getName();
                    boolean isFrom = participantName.equals(participant.getName());
                    boolean isTo = participantName.equals(toName);
                    return isFrom || isTo;
                }
        );
    }
}
