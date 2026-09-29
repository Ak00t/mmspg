package com.ojt_22.mmspg;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder();

        String password = "Admin123";

        String hash = encoder.encode(password);

        System.out.println("HASH = " + hash);
        System.out.println("MATCH = " +
                encoder.matches(password, hash));
    }
}