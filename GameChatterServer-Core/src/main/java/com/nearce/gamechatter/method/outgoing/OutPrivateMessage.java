/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nearce.gamechatter.method.outgoing;

public class OutPrivateMessage {
    private String sender;
    private String target;
    private String message;

    private OutPrivateMessage(String sender, String target, String message) {
        this.sender = sender;
        this.target = target;
        this.message = message;
    }

    public static OutMethod getRequest(String sender, String target, String message) {
        return new OutMethod("psend", new OutPrivateMessage(sender, target, message));
    }
}
