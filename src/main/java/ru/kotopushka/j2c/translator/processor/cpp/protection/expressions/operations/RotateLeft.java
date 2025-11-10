package ru.kotopushka.j2c.translator.processor.cpp.protection.expressions.operations;

import ru.kotopushka.j2c.translator.processor.cpp.protection.expressions.AbstractOperation;

public class RotateLeft extends AbstractOperation implements Operation {

    @Override
    public long evaluate(long value) {
        return Long.rotateRight(value, (int)key);
    }

    @Override
    public String formatter() {
        return "_rotl64(%s, " + key + ")";
    }
}
