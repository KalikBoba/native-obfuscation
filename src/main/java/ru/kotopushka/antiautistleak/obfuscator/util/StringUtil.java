package ru.kotopushka.antiautistleak.obfuscator.util;

import java.util.Random;

public class StringUtil {

    /**
     * Генерирует случайную строку из арабских символов (диапазон U+0600–U+06FF)
     */
    public static String getString(int length) {
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        int start = 0x0600; // начало диапазона арабских символов
        int end = 0x06FF;   // конец диапазона

        for (int i = 0; i < length; i++) {
            char c = (char) (start + random.nextInt(end - start + 1));
            sb.append(c);
        }

        return sb.toString();
    }

    /**
     * Генерирует строку, начинающуюся с "نميدة" (аналог nemida, но арабскими),
     * а затем добавляет случайные арабские символы.
     */
    public static String getRandomString(int length) {
        StringBuilder sb = new StringBuilder("نميدة"); // арабская версия слова "nemida"
        Random random = new Random();
        int start = 0x0600;
        int end = 0x06FF;

        while (sb.length() < length) {
            char c = (char) (start + random.nextInt(end - start + 1));
            sb.append(c);
        }

        return sb.toString();
    }
}
