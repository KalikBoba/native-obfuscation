package ru.kotopushka.j2c.translator.processor.instructions;

import org.objectweb.asm.tree.ClassNode;
import ru.kotopushka.j2c.translator.Compiler;
import ru.kotopushka.j2c.translator.processor.instructions.interfaces.InstructionProcessor;
import ru.kotopushka.j2c.translator.utils.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;

public class InsnProcessorManager {
    private final static List<InstructionProcessor> PROCESSORS = new ArrayList<>();

    static {
        PROCESSORS.addAll(ReflectionUtils.getClasses("ru.kotopushka.j2c.translator.processor.instructions.impl", InstructionProcessor.class));
        Compiler.LOGGER.info("Loaded {} instruction processors before compilation", PROCESSORS.size());
    }

    public static void process(ClassNode classNode) {
        PROCESSORS.forEach(processor -> processor.process(classNode));
    }
}
