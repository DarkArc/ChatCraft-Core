package com.nearce.chatcraft;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;

public abstract class WebSocketHandler {
    private Map<InetSocketAddress, WebSocket> activeSockets = new HashMap<>();
    private Map<InetSocketAddress, ChatParticipant> participantMap = new HashMap<>();

    private WebSocketServer server = new WebSocketServer(new InetSocketAddress(8080)) {
        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            activeSockets.put(conn.getRemoteSocketAddress(), conn);
            System.out.println("Accepting connection");
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            if (activeSockets.remove(conn.getRemoteSocketAddress()) != null) {
                ChatParticipant participant = participantMap.remove(conn.getRemoteSocketAddress());
                if (participant != null) {
                    clientLeave(participant);
                }
            }
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            System.out.println("Accepting message: " + message);
            process(conn.getRemoteSocketAddress(), message);
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {

        }
    };

    public WebSocketHandler() throws UnknownHostException {
    }

    public void start() {
        server.start();
    }

    public void stop() throws IOException, InterruptedException {
        server.stop();
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
            case "send":
                sendMessage(address, params);
                break;
            case "leave":
                activeSockets.get(address).close();
                break;
        }
    }

    private void join(InetSocketAddress address, UUID identifier, JsonObject params) {
        ChatParticipant participant = new ChatParticipant(identifier, params.getAsJsonPrimitive("name").getAsString());
        participantMap.put(address, participant);
        clientJoin(participant);
    }

    private void sendMessage(InetSocketAddress address, JsonObject params) {
        ChatParticipant participant = participantMap.get(address);
        if (participant != null) {
            String message = params.getAsJsonPrimitive("message").getAsString();
            sendMessage(participant, message);

            receiveMessage(new ChatMessage(participant, message));
        }
    }

    public void sendMessage(ChatParticipant participant, String message) {
        JsonObject requestParams = new JsonObject();
        requestParams.addProperty("sender", participant.getName());
        requestParams.addProperty("message", message);

        JsonObject request = new JsonObject();
        request.addProperty("method", "send");
        request.add("params", requestParams);

        for (WebSocket webSocket : activeSockets.values()) {
            webSocket.send(request.toString());
        }
    }

    public abstract void clientJoin(ChatParticipant client);

    public abstract void clientLeave(ChatParticipant client);

    public abstract void receiveMessage(ChatMessage message);
}
