package ru.kotopushka.j2c.translator.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.reflections.Reflections;

public class ReflectionUtils {
    public static <T> List<? extends T> getClasses(String prefix, Class<T> classType) {
        return new Reflections(prefix).getSubTypesOf(classType).stream()
                .filter(c -> c.getName().startsWith(prefix))
                .map(c -> {
                    try {
                        return c.getConstructor().newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
    }
}
