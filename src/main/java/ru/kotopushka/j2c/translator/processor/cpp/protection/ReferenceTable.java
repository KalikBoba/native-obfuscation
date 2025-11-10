package ru.kotopushka.j2c.translator.processor.cpp.protection;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class ReferenceTable {
    @Getter
    private static final Map<String, Integer> classes = new HashMap<>();
    private static int classIndex = 0;
    private static int methodIndex = 0;
    private static int fieldIndex = 0;
    private static int writerIndex = 0;

    public static void pushClass(String klassName, int classIndex) {
        classes.putIfAbsent(klassName, classIndex);
    }

    public static int getClassIndex() {
        return classIndex++;
    }

    public static int getMethodIndex() {
        return classIndex++;
    }

    public static int getWriterIndex() {
        return writerIndex++;
    }

    public static int getFieldIndex() {
        return classIndex++;
    }
    public static int getNotIncrementIndex() {
        return classIndex;
    }
}
