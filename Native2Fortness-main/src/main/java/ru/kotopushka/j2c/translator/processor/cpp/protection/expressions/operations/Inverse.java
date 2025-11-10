package ru.kotopushka.j2c.translator.processor.cpp.protection.expressions.operations;

import ru.kotopushka.j2c.translator.processor.cpp.protection.expressions.AbstractOperation;

public class Inverse extends AbstractOperation implements Operation {

    @Override
    public long evaluate(long value) {
        return ~value;
    }

    @Override
    public String formatter() {
        return "~(%s)";
    }
}
