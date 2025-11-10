package ru.kotopushka.j2c.translator.processor.cpp.impl;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import ru.kotopushka.j2c.translator.processor.cpp.protection.ReferenceSnippetGenerator;
import ru.kotopushka.j2c.translator.processor.cpp.utils.translate.MethodContext;
import ru.kotopushka.j2c.translator.processor.cpp.utils.translate.BaseProcessor;


public class NewProcessor extends BaseProcessor {

    public NewProcessor() {
        super(NEW);
    }

    @Override
    public void translate(MethodContext context, AbstractInsnNode insn, MethodNode method) {
        if (insn instanceof TypeInsnNode node) {
            context.output().begin(method);
            context.output().pushMethodLine("if (jobject obj = env->AllocObject(%s)) { cstack%s.l = obj; }"
                    .formatted(
                            ReferenceSnippetGenerator.generateJavaClassReference(context, method, node.desc),
                            context.getStackPointer().peek())
            );
            context.output().end(method);
        }
    }

    @Override
    public int updateStackPointer(AbstractInsnNode insnNode, int currentPointer) {
        return currentPointer + 1;
    }
}
