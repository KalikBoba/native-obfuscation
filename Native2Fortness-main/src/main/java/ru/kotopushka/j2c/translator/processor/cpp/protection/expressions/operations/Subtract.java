package ru.kotopushka.j2c.translator.processor.cpp.protection.expressions.operations;

import ru.kotopushka.j2c.translator.processor.cpp.protection.expressions.AbstractOperation;

public class Subtract extends AbstractOperation implements Operation {

    public Subtract() {
    }

    @Override
    public long evaluate(long value) {
        return value + key;
    }

    @Override
    public String formatter() {
        return "(%s - " + key + ")";
    }
}