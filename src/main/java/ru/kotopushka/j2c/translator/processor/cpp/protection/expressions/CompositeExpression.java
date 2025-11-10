package ru.kotopushka.j2c.translator.processor.cpp.protection.expressions;

import ru.kotopushka.j2c.translator.processor.cpp.protection.expressions.operations.Operation;

public class CompositeExpression implements Expression {
    private final AbstractOperation[] operations;

    public CompositeExpression(AbstractOperation... operations) {
        this.operations = operations;
    }

    @Override
    public long evaluate(long value) {
        for (int i = operations.length - 1; i >= 0; i--)
            value = ((Operation) operations[i]).evaluate(value);
        return value;
    }

    @Override
    public String format(String formatter) {
        for (AbstractOperation operation : operations)
            formatter = String.format(((Operation) operation).formatter(), formatter);
        return formatter;
    }
}
