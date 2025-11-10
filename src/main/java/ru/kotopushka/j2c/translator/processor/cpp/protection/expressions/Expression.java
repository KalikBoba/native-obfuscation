package ru.kotopushka.j2c.translator.processor.cpp.protection.expressions;

import java.io.Serializable;

public interface Expression extends Serializable {
    long evaluate(long value);

    String format(String name);
}
