package ru.kotopushka.j2c.translator.processor.cpp.protection.expressions;

import java.util.Random;

public class Main {
    public static void main(String[] args) {

        Random random = new Random(58845);

        GenerativeExpression generativeExpression = new GenerativeExpression(random);
        System.out.println(generativeExpression.format("%s"));
    }
}
