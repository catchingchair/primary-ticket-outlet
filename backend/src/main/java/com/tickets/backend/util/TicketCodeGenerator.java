package com.tickets.backend.util;

import java.security.SecureRandom;

public final class TicketCodeGenerator {

    private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int CODE_LENGTH = 12;

    private TicketCodeGenerator() {
    }

    public static String generateCode() {
        char[] buffer = new char[CODE_LENGTH];
        for (int i = 0; i < CODE_LENGTH; i++) {
            buffer[i] = ALPHABET[RANDOM.nextInt(ALPHABET.length)];
        }
        return new String(buffer);
    }
}

