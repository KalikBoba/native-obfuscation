package ru.kotopushka.j2c.translator.processor.cpp.impl;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import ru.kotopushka.j2c.translator.processor.cpp.protection.ReferenceSnippetGenerator;
import ru.kotopushka.j2c.translator.processor.cpp.utils.translate.BaseProcessor;
import ru.kotopushka.j2c.translator.processor.cpp.utils.translate.MethodContext;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ru.kotopushka.j2c.translator.processor.cpp.MethodProcessor.CPP_TYPES;


public class InvokeProcessor extends BaseProcessor {

    public InvokeProcessor() {
        super(INVOKEINTERFACE, INVOKESPECIAL, INVOKESTATIC, INVOKEVIRTUAL);
    }

    @Override
    public void translate(MethodContext classContext, AbstractInsnNode insnNode, MethodNode method) {
        if (insnNode instanceof MethodInsnNode mh) {
//            classContext.output().begin(method);
            Type[] args = Type.getArgumentTypes(mh.desc);
            List<Integer> argOffsets = new ArrayList<>();

            int stackOffset = classContext.getStackPointer().peek();
            for (Type argType : args) {
                stackOffset -= argType.getSize();
            }
            int argumentOffset = stackOffset;
            for (Type argType : args) {
                argOffsets.add(argumentOffset);
                argumentOffset += argType.getSize();
            }

            boolean isStatic = insnNode.getOpcode() == Opcodes.INVOKESTATIC;
            int objectOffset = isStatic ? 0 : 1;

            int invokeStackPointer = stackOffset - objectOffset;

            StringBuilder arg4Call = new StringBuilder();
            for (int i = 0; i < argOffsets.size(); i++) {
                Type arg = args[i];
                StringBuilder appender = new StringBuilder();
                appender.append(", cstack").append(argOffsets.get(i));
                switch (arg.getDescriptor()) {
                    case "I" -> appender.append(".i");
                    case "J" -> appender.append(".j");
                    case "F" -> appender.append(".f");
                    case "D" -> appender.append(".d");
                    default -> appender.append(".l");
                }
                arg4Call.append(appender);
            }
            boolean customVMP = false;
            if (!classContext.output().isVMP) {;
//                customVMP = true;
//                classContext.output().pushMethodLine("//VMProtectBeginUltra(\"%s\");".formatted(new SecureRandom().nextLong() * new SecureRandom().nextLong()));
            }
//            if (classContext.output().isVMP)
//            classContext.output().pushMethodLine("if(%s == classes_notEncrypted[%s])"
//
//                    .formatted(
//                            ReferenceSnippetGenerator.generateJavaClassReference(classContext, method, mh.owner),
//                            classContext.output().pushJavaClass(mh.owner).getId()
//                            )
//
//            );

            if (insnNode.getOpcode() == INVOKEVIRTUAL || insnNode.getOpcode() == INVOKEINTERFACE) {
                String returnType = Type.getReturnType(mh.desc).getDescriptor();
                switch (returnType) {
                    case "V" -> classContext.output().pushMethodLine("env->CallVoidMethod(cstack%s.l, %s%s);"
                            .formatted(invokeStackPointer, ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                    case "Z" ->
                            classContext.output().pushMethodLine("cstack%s.i = (jint) env->CallBooleanMethod(cstack%s.l, %s%s);"
                                    .formatted(invokeStackPointer, invokeStackPointer, ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                    case "C" ->
                            classContext.output().pushMethodLine("cstack%s.i = (jint) env->CallCharMethod(cstack%s.l, %s%s);"
                                    .formatted(invokeStackPointer, invokeStackPointer, ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                    case "B" ->
                            classContext.output().pushMethodLine("cstack%s.b = (jint) env->CallByteMethod(cstack%s.l, %s%s);"
                                    .formatted(invokeStackPointer, invokeStackPointer, ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC) , arg4Call));
                    case "S" ->
                            classContext.output().pushMethodLine("cstack%s.i = (jint) env->CallShortMethod(cstack%s.l, %s%s);"
                                    .formatted(invokeStackPointer, invokeStackPointer, ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                    case "I" ->
                            classContext.output().pushMethodLine("cstack%s.i = env->CallIntMethod(cstack%s.l, %s%s);"
                                    .formatted(invokeStackPointer, invokeStackPointer,  ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                    case "F" ->
                            classContext.output().pushMethodLine("cstack%s.f = env->CallFloatMethod(cstack%s.l, %s%s);"
                                    .formatted(invokeStackPointer, invokeStackPointer, ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                    case "J" ->
                            classContext.output().pushMethodLine("cstack%s.j = env->CallLongMethod(cstack%s.l, %s%s);"
                                    .formatted(invokeStackPointer, invokeStackPointer,  ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                    case "D" ->
                            classContext.output().pushMethodLine("cstack%s.d = env->CallDoubleMethod(cstack%s.l, %s%s);"
                                    .formatted(invokeStackPointer, invokeStackPointer, ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                    default ->
                            classContext.output().pushMethodLine("cstack%s.l = env->CallObjectMethod(cstack%s.l, %s%s);"
                                    .formatted(invokeStackPointer, invokeStackPointer,  ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                }
            }

            if (insnNode.getOpcode() == INVOKESTATIC) {
                String returnType = Type.getReturnType(mh.desc).getDescriptor();

                switch (returnType) {
                    case "V" -> classContext.output().pushMethodLine("env->CallStaticVoidMethod(%s, %s%s);"
                            .formatted(ReferenceSnippetGenerator.generateJavaClassReference(classContext, method, mh.owner), ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                    case "Z" -> classContext.output().pushMethodLine("cstack%s.i = (jint) env->CallStaticBooleanMethod(%s, %s%s);"
                                .formatted(invokeStackPointer, ReferenceSnippetGenerator.generateJavaClassReference(classContext, method, mh.owner), ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                    case "C" -> classContext.output().pushMethodLine("cstack%s.i = (jint) env->CallStaticCharMethod(%s, %s%s);"
                            .formatted(invokeStackPointer, ReferenceSnippetGenerator.generateJavaClassReference(classContext, method, mh.owner), ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                    case "B" -> classContext.output().pushMethodLine("cstack%s.b = (jint) env->CallStaticByteMethod(%s,%s%s);"
                            .formatted(invokeStackPointer, ReferenceSnippetGenerator.generateJavaClassReference(classContext, method, mh.owner), ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                    case "S" -> classContext.output().pushMethodLine("cstack%s.i = (jint) env->CallStaticShortMethod(%s, %s%s);"
                            .formatted(invokeStackPointer, ReferenceSnippetGenerator.generateJavaClassReference(classContext, method, mh.owner), ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                    case "I" -> classContext.output().pushMethodLine("cstack%s.i = env->CallStaticIntMethod(%s, %s%s);"
                            .formatted(invokeStackPointer, ReferenceSnippetGenerator.generateJavaClassReference(classContext, method, mh.owner), ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                    case "F" -> classContext.output().pushMethodLine("cstack%s.f = env->CallStaticFloatMethod(%s, %s%s);"
                            .formatted(invokeStackPointer, ReferenceSnippetGenerator.generateJavaClassReference(classContext, method, mh.owner), ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                    case "J" -> classContext.output().pushMethodLine("cstack%s.j = env->CallStaticLongMethod(%s, %s%s);"
                            .formatted(invokeStackPointer, ReferenceSnippetGenerator.generateJavaClassReference(classContext, method, mh.owner), ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                    case "D" -> classContext.output().pushMethodLine("cstack%s.d = env->CallStaticDoubleMethod(%s, %s%s);"
                            .formatted(invokeStackPointer, ReferenceSnippetGenerator.generateJavaClassReference(classContext, method, mh.owner), ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                    default -> classContext.output().pushMethodLine("cstack%s.l = env->CallStaticObjectMethod(%s, %s%s);"
                            .formatted(invokeStackPointer, ReferenceSnippetGenerator.generateJavaClassReference(classContext, method, mh.owner), ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                }
            }

            if (insnNode.getOpcode() == INVOKESPECIAL) {
                String returnType = Type.getReturnType(mh.desc).getDescriptor();

                switch (returnType) {
                    case "V" ->
                            classContext.output().pushMethodLine("env->CallNonvirtualVoidMethod(cstack%s.l, %s, %s%s);"
                                    .formatted(invokeStackPointer, ReferenceSnippetGenerator.generateJavaClassReference(classContext, method, mh.owner), ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                    case "Z" ->
                            classContext.output().pushMethodLine("cstack%s.i = (jint) env->CallNonvirtualBooleanMethod(cstack%s.l, %s, %s%s);"
                                    .formatted(invokeStackPointer, invokeStackPointer, ReferenceSnippetGenerator.generateJavaClassReference(classContext, method, mh.owner), ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                    case "C" ->
                            classContext.output().pushMethodLine("cstack%s.i = (jint) env->CallNonvirtualCharMethod(cstack%s.l, %s, %s%s);"
                                    .formatted(invokeStackPointer, invokeStackPointer, ReferenceSnippetGenerator.generateJavaClassReference(classContext, method, mh.owner), ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                    case "B" ->
                            classContext.output().pushMethodLine("cstack%s.b = (jint) env->CallNonvirtualByteMethod(cstack%s.l, %s, %s%s);"
                                    .formatted(invokeStackPointer, invokeStackPointer, ReferenceSnippetGenerator.generateJavaClassReference(classContext, method, mh.owner), ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                    case "S" ->
                            classContext.output().pushMethodLine("cstack%s.i = (jint) env->CallNonvirtualShortMethod(cstack%s.l, %s, %s%s);"
                                    .formatted(invokeStackPointer, invokeStackPointer, ReferenceSnippetGenerator.generateJavaClassReference(classContext, method, mh.owner), ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                    case "I" ->
                            classContext.output().pushMethodLine("cstack%s.i = env->CallNonvirtualIntMethod(cstack%s.l, %s, %s%s);"
                                    .formatted(invokeStackPointer, invokeStackPointer, ReferenceSnippetGenerator.generateJavaClassReference(classContext, method, mh.owner), ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                    case "F" ->
                            classContext.output().pushMethodLine("cstack%s.f = env->CallNonvirtualFloatMethod(cstack%s.l, %s, %s%s);"
                                    .formatted(invokeStackPointer, invokeStackPointer, ReferenceSnippetGenerator.generateJavaClassReference(classContext, method, mh.owner), ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                    case "J" ->
                            classContext.output().pushMethodLine("cstack%s.j = env->CallNonvirtualLongMethod(cstack%s.l, %s, %s%s);"
                                    .formatted(invokeStackPointer, invokeStackPointer, ReferenceSnippetGenerator.generateJavaClassReference(classContext, method, mh.owner), ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                    case "D" ->
                            classContext.output().pushMethodLine("cstack%s.d = env->CallNonvirtualDoubleMethod(cstack%s.l, %s, %s%s);"
                                    .formatted(invokeStackPointer, invokeStackPointer, ReferenceSnippetGenerator.generateJavaClassReference(classContext, method, mh.owner), ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                    default ->
                            classContext.output().pushMethodLine("cstack%s.l = env->CallNonvirtualObjectMethod(cstack%s.l, %s, %s%s);"
                                    .formatted(invokeStackPointer, invokeStackPointer, ReferenceSnippetGenerator.generateJavaClassReference(classContext, method, mh.owner), ReferenceSnippetGenerator.generateJavaMethodReference(classContext, method, mh, mh.getOpcode() == Opcodes.INVOKESTATIC), arg4Call));
                }
            }
            if (customVMP) {
//                classContext.output().pushMethodLine("//VMProtectEnd();");
            }
            if (insnNode.getOpcode() != INVOKESTATIC) {
                classContext.getStackPointer().pop();
            }

            if(method.exceptions != null && !method.exceptions.isEmpty()) {
                classContext.output().pushMethodLine("if(env->ExceptionOccurred()) {  env->ExceptionDescribe(); %s  }".formatted(

                        Type.getReturnType(method.desc).getSort() != Type.VOID
                        ? "return (%s) 0;".formatted(CPP_TYPES[Type.getReturnType(method.desc).getSort()])
                        : "return;"

                ));
            }

//            classContext.output().end(method);
            classContext.getStackPointer().pop(Arrays.stream(Type.getArgumentTypes(mh.desc)).mapToInt(Type::getSize).sum()).push(Type.getReturnType(mh.desc).getSize());
        }
    }

    @Override
    public int updateStackPointer(AbstractInsnNode insnNode, int currentPointer) {
        return currentPointer;
    }
}