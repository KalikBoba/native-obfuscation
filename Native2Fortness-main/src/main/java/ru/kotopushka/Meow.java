package ru.kotopushka;

import java.io.File;
import java.util.Objects;

public class Meow {
    public static void main(String[] args) {
//        ch.qos.logback.core.util.Loader.systemClassloaderIfNull
        for (File file : Objects.requireNonNull(new File("C:\\DeltaClient\\libraries").listFiles())) {
            System.out.printf(";%s",file.getAbsolutePath().replaceAll("\\\\", "/"));
        }
        System.out.println();
    }
}