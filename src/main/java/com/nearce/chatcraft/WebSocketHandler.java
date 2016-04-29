package com.nearce.chatcraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;

public class WebSocketHandler extends WebSocketServer {
    private Map<InetSocketAddress, WebSocket> activeSockets = new HashMap<>();
    private Map<InetSocketAddress, ChatParticipant> participantMap = new HashMap<>();

    private GameServer gameServer;

    public WebSocketHandler(GameServer gameServer) throws UnknownHostException {
        super(new InetSocketAddress(8080));
        this.gameServer = gameServer;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        activeSockets.put(conn.getRemoteSocketAddress(), conn);
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
        }
    }

    private void sendToRemoteClients(JsonObject message) {
        for (InetSocketAddress address : participantMap.keySet()) {
            activeSockets.get(address).send(message.toString());
        }
    }

    private void join(InetSocketAddress address, UUID identifier, JsonObject params) {
        String requestedName = gameServer.sanitize(params.getAsJsonPrimitive("name").getAsString());

        // If the name is not valid, close their connection
        if (requestedName.isEmpty()) {
            activeSockets.remove(address).close();
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

                    activeSockets.get(address).send(request.toString());
                },
                (name) -> {
                    ChatParticipant participant = new ChatParticipant(identifier, name);
                    participantMap.put(address, participant);

                    clientJoin(participant, true);
                    gameServer.remoteClientJoin(participant);

                    sendParticipants(address);
                }
        );
    }

    private void sendParticipants(InetSocketAddress address) {
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

        activeSockets.get(address).send(request.toString());
    }

    private void leave(InetSocketAddress address, JsonObject params) {
        if (activeSockets.remove(address) != null) {
            ChatParticipant participant = participantMap.remove(address);
            if (participant != null) {
                clientLeave(participant, true);
                gameServer.remoteClientLeave(participant);
            }
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
}
