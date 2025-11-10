package ru.kotopushka.antiautistleak.obfuscator.transform.impl.jnicalls.methods;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import ru.kotopushka.antiautistleak.obfuscator.main.Main;
import ru.kotopushka.antiautistleak.obfuscator.transform.Transformer;

public class MethodMathOperationsTransformer extends Transformer {
    @Override
    public void transform(Main obfuscator) throws Exception {
//        System.out.println("[$] Math Operations");

        obfuscator.classes().forEach(classNode -> {

            classNode.methods.forEach(methodNode -> {

                if (shouldProcessNative(methodNode) || shouldProcessNative(classNode)) {

//                if(classNode.name.contains("Nemida")) {
//                    System.out.println(methodNode.name + " " + methodNode.desc);
//                }
                    xor(methodNode);
                    add(methodNode);
//                    sub(methodNode);
                }
            });

        });

    }

    public void add(MethodNode methodNode) {

        methodNode.instructions.forEach(abstractInsnNode -> {

            switch (abstractInsnNode.getOpcode()) {
                case IADD -> {

                    methodNode.instructions.set(abstractInsnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "ru/kotopushka/antiautistleak/obfuscator/NemidaSDK", "addInt", "(II)I"));

                    break;
                }

                case DADD -> {
                    methodNode.instructions.set(abstractInsnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "ru/kotopushka/antiautistleak/obfuscator/NemidaSDK", "addDouble", "(DD)D"));
                    break;
                }

                case FADD -> {
                    methodNode.instructions.set(abstractInsnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "ru/kotopushka/antiautistleak/obfuscator/NemidaSDK", "addDouble", "(FF)F"));
                    break;
                }

                case LADD -> {
                    methodNode.instructions.set(abstractInsnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "ru/kotopushka/antiautistleak/obfuscator/NemidaSDK", "addLong", "(JJ)J"));
                    break;
                }

            }
        });

    }

    public void sub(MethodNode methodNode) {

        methodNode.instructions.forEach(abstractInsnNode -> {

            switch (abstractInsnNode.getOpcode()) {
                case ISUB -> {
                    methodNode.instructions.set(abstractInsnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "ru/kotopushka/antiautistleak/obfuscator/NemidaSDK", "subInt", "(II)I"));

                    break;
                }
                case DSUB -> {
                    methodNode.instructions.set(abstractInsnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "ru/kotopushka/antiautistleak/obfuscator/NemidaSDK", "subDouble", "(DD)D"));
                    break;
                }

                case FSUB -> {
                    methodNode.instructions.set(abstractInsnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "ru/kotopushka/antiautistleak/obfuscator/NemidaSDK", "subDouble", "(FF)F"));
                    break;
                }

                case LSUB -> {
                    methodNode.instructions.set(abstractInsnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "ru/kotopushka/antiautistleak/obfuscator/NemidaSDK", "subLong", "(JJ)J"));
                    break;
                }

            }

            //      System.out.println(abstractInsnNode.getOpcode() + " " + abstractInsnNode.getClass().getSimpleName());

        });

    }

    public void xor(MethodNode methodNode) {

        methodNode.instructions.forEach(abstractInsnNode -> {

            switch (abstractInsnNode.getOpcode()) {
                case IXOR -> {
                    methodNode.instructions.set(abstractInsnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "ru/kotopushka/antiautistleak/obfuscator/NemidaSDK", "xorInt", "(II)I"));

                    break;
                }
                case LXOR -> {
                    methodNode.instructions.set(abstractInsnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "ru/kotopushka/antiautistleak/obfuscator/NemidaSDK", "xorLong", "(JJ)J"));
                    break;
                }

            }

            if (abstractInsnNode.getOpcode() > 0) {
                //      System.out.println(abstractInsnNode.getOpcode() + " " + abstractInsnNode.getClass().getSimpleName());
            }

        });

    }

}
