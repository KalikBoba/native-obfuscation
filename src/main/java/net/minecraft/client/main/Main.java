package net.minecraft.client.main;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        ClassReader classReader = null;
        try {
            classReader = new ClassReader(Files.readAllBytes(Path.of("1.class")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
    }
}
