package ru.kotopushka.rewriter.entrypoint;

import ru.kotopushka.j2c.translator.Compiler;
import ru.kotopushka.rewriter.global.GlobalConstants;

import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class Main {

    private static JarFile jarFileInputStream;

    private static JarOutputStream jarOutputStream;

    public static void main(String[] args) throws Exception {
        GlobalConstants globalConstants = new GlobalConstants();

        jarFileInputStream = new JarFile("input.jar");

        jarOutputStream = new JarOutputStream(new FileOutputStream("spi4ka.jar"));


        JarEntry jarEntry;
        Iterator<JarEntry> iterator = jarFileInputStream.entries().asIterator();
        while (iterator.hasNext() && (jarEntry = iterator.next()) != null) {
            ZipEntry zipEntry = jarFileInputStream.getEntry(jarEntry.getRealName());
            byte[] stream = jarFileInputStream.getInputStream(zipEntry).readAllBytes();
            if (jarEntry.getRealName().endsWith("class") && !jarEntry.getRealName().startsWith("com/google/thirdparty")) {
                Compiler.COMPILE_LOGGER.info("Rewriting %s...", jarEntry.getRealName());

//                ClassRewriter classRewriter = new ClassRewriter(new ClassInputStream(stream));
//                stream = classRewriter.getClassOutputStream().getByteArrayOutputStream().toByteArray();

//                for (int i = 0; i < stream.length; i++) {
//                     stream[i] ^= 0x15;
//                }

                jarOutputStream.putNextEntry(new ZipEntry(jarEntry.getRealName()));
                jarOutputStream.write(stream);
                jarOutputStream.closeEntry();
            } else {
                jarOutputStream.putNextEntry(zipEntry);
                jarOutputStream.write(stream);
                jarOutputStream.closeEntry();
            }
        }

        jarOutputStream.close();

    }
}