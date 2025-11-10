package ru.kotopushka.j2c.translator.processor.cpp.protection.expressions.operations;

import java.io.Serializable;

public interface Operation extends Serializable {
    long evaluate(long value);

    String formatter();
}
