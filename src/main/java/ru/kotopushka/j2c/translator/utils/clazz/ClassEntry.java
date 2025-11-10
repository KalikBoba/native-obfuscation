package ru.kotopushka.j2c.translator.utils.clazz;


import org.objectweb.asm.tree.ClassNode;

import java.util.jar.JarEntry;

public record ClassEntry(ClassNode classNode, JarEntry jarEntry, byte[] classBytes) {
}
