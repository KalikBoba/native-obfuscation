package ru.kotopushka.crypted;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Main {
    private static final ConcurrentHashMap<String, byte[]> files = new ConcurrentHashMap<>();


    public static void main(String[] args) throws IOException {

        ZipFile zip = new ZipFile("input.jar");
        zip.entries().asIterator().forEachRemaining((entry) -> {
            try {
                files.put(entry.getName(), zip.getInputStream(entry).readAllBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        zip.close();

        files.forEach((name, data) -> {
            if (isClassFileFormat(name, data)) {
                ClassWriter classWriter = new ClassWriter();
                ClassParser classFile = new ClassParser(new ClassStream(data), classWriter);
                System.out.println("Processing " + name);
                try {
                    classFile.parse_stream();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                byte[] buffer = classWriter.getByteArrayOutputStream().toByteArray();
                files.put(name, buffer);
            }
        });

        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream("output.jar"));
        zos.setLevel(9);
        files.forEach((name, data) -> {
            try {
                zos.putNextEntry(new ZipEntry(name));
                zos.write(data);
                zos.closeEntry();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });


        zos.close();
    }

    private static boolean isClassFileFormat(String name, byte[] data) {
        return data.length >= 8 && name.endsWith(".class");
    }
}