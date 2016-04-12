package com.nearce.chatcraft;

import java.util.List;

public interface GameServer {
    List<ChatParticipant> getLocalParticipants();

    void remoteClientJoin(ChatParticipant client);

    void remoteClientLeave(ChatParticipant client);

    void remoteClientSendMessage(ChatMessage message);
}
