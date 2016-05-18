/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nearce.gamechatter;

import java.util.UUID;

public abstract class ChatParticipant {
    private final UUID identifier;
    private String name;

    public ChatParticipant(UUID identifier, String name) {
        this.identifier = identifier;
        this.name = name;
    }

    public UUID getUniqueId() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract void sendMessage(String message);

    @Override
    public boolean equals(Object object) {
        return object instanceof ChatParticipant && ((ChatParticipant) object).getUniqueId().equals(getUniqueId());
    }
}
