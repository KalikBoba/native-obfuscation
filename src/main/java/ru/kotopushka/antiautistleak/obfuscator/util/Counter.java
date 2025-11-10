package ru.kotopushka.antiautistleak.obfuscator.util;

import java.util.HashMap;

public class Counter {

    private HashMap<String, Integer> hashMap = new HashMap<>();

    private int counter;

    public Counter() {
        counter = 0;
    }

    public int inc(String key) {
        hashMap.put(key, counter);
        return counter++;
    }

    public int getCounter(String key) {
        return hashMap.get(key);
    }
}
