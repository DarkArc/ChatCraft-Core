/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nearce.gamechatter.method.outgoing;

public class OutSystemMessage {
    private String message;

    private OutSystemMessage(String message) {
        this.message = message;
    }

    public static OutMethod getRequest(String message) {
        return new OutMethod("ssend", new OutSystemMessage(message));
    }
}
