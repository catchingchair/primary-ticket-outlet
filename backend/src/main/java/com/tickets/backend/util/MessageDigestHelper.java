package com.tickets.backend.util;

public final class MessageDigestHelper {

    private MessageDigestHelper() {
    }

    public static boolean isEqual(byte[] expected, byte[] provided) {
        if (expected == null || provided == null) {
            return false;
        }
        if (expected.length != provided.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < expected.length; i++) {
            result |= expected[i] ^ provided[i];
        }
        return result == 0;
    }
}

