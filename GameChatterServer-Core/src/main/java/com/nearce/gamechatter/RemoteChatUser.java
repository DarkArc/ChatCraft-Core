/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nearce.gamechatter;

import org.java_websocket.WebSocket;

public class RemoteChatUser {
    private final ChatParticipant participant;
    private final WebSocket socket;

    public RemoteChatUser(WebSocket socket, String name) {
        this(socket, new ChatParticipant(name));
    }

    public RemoteChatUser(WebSocket socket, ChatParticipant participant) {
        this.participant = participant;
        this.socket = socket;
    }

    public ChatParticipant getParticipant() {
        return participant;
    }

    public WebSocket getSocket() {
        return socket;
    }
}
