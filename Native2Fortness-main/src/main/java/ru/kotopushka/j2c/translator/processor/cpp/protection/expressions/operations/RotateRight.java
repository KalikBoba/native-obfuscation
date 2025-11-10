package ru.kotopushka.j2c.translator.processor.cpp.protection.expressions.operations;

import ru.kotopushka.j2c.translator.processor.cpp.protection.expressions.AbstractOperation;

public class RotateRight extends AbstractOperation implements Operation {

    public RotateRight() {
    }

    @Override
    public long evaluate(long value) {
        return Long.rotateLeft(value, (int)key);
    }

    @Override
    public String formatter() {
        return "_rotr64(%s, " + key + ")";
    }
}