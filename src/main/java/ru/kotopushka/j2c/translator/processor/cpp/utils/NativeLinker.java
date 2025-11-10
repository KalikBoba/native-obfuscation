package ru.kotopushka.j2c.translator.processor.cpp.utils;

import lombok.Getter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import ru.kotopushka.VMPState;
import ru.kotopushka.j2c.translator.processor.cpp.protection.ConstantPool;

import java.util.Random;

public class NativeLinker {
    @Getter
    private final ClassNode classNode;
    private int count;
    @Getter
    private final StringBuilder stringBuilder;
    private final StringBuilder methods;
    private ConstantPool constantPool;

    public NativeLinker(ClassNode classNode, ConstantPool constantPool) {
        this.constantPool = constantPool;
        this.classNode = classNode;
        this.stringBuilder = new StringBuilder();
        this.count = 0;
        stringBuilder.append("jclass linkingClass = env->FindClass(\"%s\");\n"
                .formatted(classNode.name));

        this.methods = new StringBuilder();
        if (VMPState.vmp)
        methods.append("VMProtectBeginUltra(\"clinit_%s\");\n".formatted(
                String.valueOf(new Random().nextLong()*new Random().nextLong()+new Random().nextLong()+new Random().nextLong())
        ));
        methods.append("JNINativeMethod jniMethods[] = {\n");
    }

    public void pushMethod(MethodNode methodNode, String nativeName) {
        count++;
        methods.append("        (char*)(%s), (char*)(%s), &Java_%s,\n"
                .formatted(constantPool.pushUtf(methodNode.name), constantPool.pushUtf(methodNode.desc), nativeName));
    }

    public void end() {
        if (count > 0) {
            methods.append("    };\n");
            methods.append("    env->RegisterNatives(env->FindClass((%s)), jniMethods, sizeof(jniMethods) / sizeof(JNINativeMethod));\n"
                    .formatted(constantPool.pushUtf(classNode.name)));
            if (VMPState.vmp) methods.append("    VMProtectEnd();\n");
        }
    }

    public StringBuilder getMethods() {
        return (count != 0 ? new StringBuilder(methods.toString()) : new StringBuilder());
    }

}
