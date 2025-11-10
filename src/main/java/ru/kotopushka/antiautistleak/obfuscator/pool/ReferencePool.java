package ru.kotopushka.antiautistleak.obfuscator.pool;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ReferencePool {
    private static List<String> references;

    static  {
        references = new CopyOnWriteArrayList<>();
    }

    public static List<String> getReferences() {
        return references;
    }
}
