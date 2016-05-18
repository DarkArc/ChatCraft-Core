/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nearce.gamechatter;

import org.java_websocket.WebSocket;

import java.util.UUID;

public class RemoteChatParticipant extends ChatParticipant {
    private WebSocket socket;

    public RemoteChatParticipant(WebSocket socket, UUID identifier, String name) {
        super(identifier, name);
        this.socket = socket;
    }

    @Override
    public void sendMessage(String message) {
        socket.send(message);
    }
}
