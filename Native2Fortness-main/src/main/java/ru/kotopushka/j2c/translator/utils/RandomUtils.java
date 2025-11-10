package ru.kotopushka.j2c.translator.utils;

import lombok.experimental.UtilityClass;

import java.util.Random;

@UtilityClass
public class RandomUtils {

    public long nextLong(final Random random, long bound) {

        if (bound < 0) bound = -bound;

        return random.nextLong(bound);

    }

}
