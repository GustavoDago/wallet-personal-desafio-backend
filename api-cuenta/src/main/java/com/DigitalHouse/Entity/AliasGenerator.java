package com.DigitalHouse.Entity;

import java.util.Random;

public class AliasGenerator {
    private static final String[] WORDS = {
            "azul", "río", "montaña", "sol", "estrella", "mar", "cielo", "lago", "piedra", "flor"
    };

    public static String generateAlias() {
        Random random = new Random();
        String first = WORDS[random.nextInt(WORDS.length)];
        String second = WORDS[random.nextInt(WORDS.length)];
        String third = WORDS[random.nextInt(WORDS.length)];
        return String.format("%s.%s.%s", first, second, third);
    }
}
