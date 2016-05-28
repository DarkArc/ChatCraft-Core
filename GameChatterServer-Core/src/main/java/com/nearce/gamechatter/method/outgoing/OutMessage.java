/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nearce.gamechatter.method.outgoing;

public class OutMessage {
    private String sender;
    private String message;

    private OutMessage(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public static OutMethod getRequest(String sender, String message) {
        return new OutMethod("send", new OutMessage(sender, message));
    }
}
