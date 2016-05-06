/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nearce.gamechatter;

public class ChatMessage {
    private final ChatParticipant sender;
    private String message;

    public ChatMessage(ChatParticipant sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public ChatParticipant getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
