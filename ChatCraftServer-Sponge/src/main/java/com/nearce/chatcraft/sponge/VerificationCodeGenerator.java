package com.nearce.chatcraft.sponge;

import java.util.UUID;

public class VerificationCodeGenerator {
    public static String generate() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
