package com.nearce.chatcraft;

import java.util.UUID;

public class ChatParticipant {
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

    @Override
    public boolean equals(Object object) {
        return object instanceof ChatParticipant && ((ChatParticipant) object).getUniqueId().equals(getUniqueId());
    }
}
