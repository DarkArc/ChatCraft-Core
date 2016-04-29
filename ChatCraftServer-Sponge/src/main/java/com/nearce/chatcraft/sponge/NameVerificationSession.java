package com.nearce.chatcraft.sponge;

import java.util.UUID;
import java.util.function.Consumer;

class NameVerificationSession {
    private final long creationTime = System.currentTimeMillis();

    private String name;
    private UUID clientID;
    private Consumer<String> join;

    public NameVerificationSession(String name, UUID clientID, Consumer<String> join) {
        this.name = name;
        this.clientID = clientID;
        this.join = join;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getClientID() {
        return clientID;
    }

    public void setClientID(UUID clientID) {
        this.clientID = clientID;
    }

    public Consumer<String> getJoin() {
        return join;
    }
}
