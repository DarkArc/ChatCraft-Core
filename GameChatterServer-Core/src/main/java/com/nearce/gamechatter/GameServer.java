/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nearce.gamechatter;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public interface GameServer {
    List<ChatParticipant> getLocalParticipants();

    String sanitize(String input);

    void joinWhenLegal(String name, UUID clientID, Consumer<String> verification, Consumer<String> join);

    void clientJoinToLocal(ChatParticipant client);

    void clientLeaveToLocal(ChatParticipant client);

    void clientMessageToLocal(ChatMessage message);
    void clientPrivateMessageToLocal(ChatMessage message, String toName);

}
