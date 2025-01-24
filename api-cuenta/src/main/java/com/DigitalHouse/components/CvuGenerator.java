package com.DigitalHouse.components;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class CvuGenerator {
    public String generateCvu() {
        StringBuilder cvu = new StringBuilder();
        for (int i = 0; i < 22; i++) {
            cvu.append(new Random().nextInt(10));
        }
        return cvu.toString();
    }
}
