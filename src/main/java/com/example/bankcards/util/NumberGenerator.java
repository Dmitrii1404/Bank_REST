package com.example.bankcards.util;

import java.security.SecureRandom;
import java.util.Random;

public class NumberGenerator {

    public static String generateNumber() {
        StringBuilder number = new StringBuilder();
        Random random = new SecureRandom();

        for (int i = 0; i < 16; i++) {
            number.append(random.nextInt(10));
        }

        return number.toString();
    }
}
