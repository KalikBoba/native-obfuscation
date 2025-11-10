package ru.kotopushka.j2c.translator.processor.cpp.utils;


public class SnippetGenerator {

    public static String reinterpretArray(String type) {
        if (type.equals("Array")) return "Object";
        return type;
    }

}
