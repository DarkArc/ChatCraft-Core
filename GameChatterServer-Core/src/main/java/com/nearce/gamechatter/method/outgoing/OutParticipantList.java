/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nearce.gamechatter.method.outgoing;

import com.nearce.gamechatter.ChatParticipant;

import java.util.Collection;

public class OutParticipantList {
    private Collection<ChatParticipant> server;
    private Collection<ChatParticipant> remote;

    private OutParticipantList(Collection<ChatParticipant> server, Collection<ChatParticipant> remote) {
        this.server = server;
        this.remote = remote;
    }

    public static OutMethod getRequest(Collection<ChatParticipant> server, Collection<ChatParticipant> remote) {
        return new OutMethod("list", new OutParticipantList(server, remote));
    }
}
