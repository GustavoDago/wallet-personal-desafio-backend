package com.DigitalHouse.components;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class AliasGenerator {
    private static final List<String> WORDS = Arrays.asList("Cuenta", "Personal", "Banco", "Argentina", "Digital", "Money", "House", "Bank", "Account", "Cartera", "Wallet", "Pago", "Pay", "Rapido", "Seguro");
    private static final int LENGTH = 3;

    public String generateAlias() {
        StringBuilder alias = new StringBuilder();
        for (int i = 0; i < LENGTH; i++) {
            alias.append(WORDS.get(new Random().nextInt(WORDS.size())));
            if (i < LENGTH - 1) {
                alias.append(".");
            }
        }
        return alias.toString();
    }
}
