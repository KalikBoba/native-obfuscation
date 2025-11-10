package ru.kotopushka.j2c.translator.processor.instructions.interfaces;

import org.objectweb.asm.tree.ClassNode;

public interface InstructionProcessor {
    void process(ClassNode classNode);
}
