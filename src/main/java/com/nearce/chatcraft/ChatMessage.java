package com.nearce.chatcraft;

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
