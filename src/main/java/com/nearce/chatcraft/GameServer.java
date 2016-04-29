package com.nearce.chatcraft;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public interface GameServer {
    List<ChatParticipant> getLocalParticipants();

    String sanitize(String input);

    void joinWhenLegal(String name, UUID clientID, Consumer<String> verification, Consumer<String> join);

    void remoteClientJoin(ChatParticipant client);

    void remoteClientLeave(ChatParticipant client);

    void remoteClientSendMessage(ChatMessage message);
}
