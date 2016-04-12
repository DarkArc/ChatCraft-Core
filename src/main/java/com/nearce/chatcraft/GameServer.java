package com.nearce.chatcraft;

import java.util.List;

public interface GameServer {
    List<ChatParticipant> getLocalParticipants();

    String sanitize(String input);

    void remoteClientJoin(ChatParticipant client);

    void remoteClientLeave(ChatParticipant client);

    void remoteClientSendMessage(ChatMessage message);
}
