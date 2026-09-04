package com.ojt_22.mmspg.entity;

import java.security.SecureRandom;
import java.util.UUID;

/** Generates RFC 9562 UUID version 7 values (time-ordered UUIDs). */
public final class UuidV7 {
    private static final SecureRandom RANDOM = new SecureRandom();

    private UuidV7() { }

    public static UUID generate() {
        long epochMillis = System.currentTimeMillis();
        long mostSignificantBits = (epochMillis << 16) | 0x7000L | RANDOM.nextInt(1 << 12);
        long leastSignificantBits = 0x8000000000000000L | (RANDOM.nextLong() & 0x3fff_ffff_ffff_ffffL);
        return new UUID(mostSignificantBits, leastSignificantBits);
    }
}
