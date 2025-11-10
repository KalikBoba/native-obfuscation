package ru.kotopushka.j2c.translator.processor.cpp.impl;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import ru.kotopushka.j2c.translator.processor.cpp.protection.ReferenceSnippetGenerator;
import ru.kotopushka.j2c.translator.processor.cpp.utils.translate.MethodContext;
import ru.kotopushka.j2c.translator.processor.cpp.utils.translate.BaseProcessor;

public class ThrowProcessor extends BaseProcessor {
    public ThrowProcessor() {
        super(ATHROW);
    }

    @Override
    public void translate(MethodContext context, AbstractInsnNode insn, MethodNode method) {
        if (insn instanceof InsnNode) {
            context.output().begin(method);
                context.output().pushMethodLine("if (cstack%s.l == nullptr) env->ThrowNew(%s, %s); else env->Throw((jthrowable) cstack%s.l);"
                        .formatted(context.getStackPointer().peek() - 1,
                                ReferenceSnippetGenerator.generateJavaClassReference(context, method, context.getClassNode().name),
                                context.getConstantPool().pushUtf("\"\""),
                                context.getStackPointer().peek() - 1));
            context.output().end(method);
        }
    }

    @Override
    public int updateStackPointer(AbstractInsnNode insnNode, int currentPointer) {
        return currentPointer - 1;
    }
}
