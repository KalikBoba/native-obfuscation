package ru.kotopushka.antiautistleak.obfuscator.transform.impl.cfm;

import org.objectweb.asm.tree.*;
import ru.kotopushka.antiautistleak.obfuscator.main.Main;
import ru.kotopushka.antiautistleak.obfuscator.transform.Transformer;


public class OpcodeTransformer extends Transformer {


    @Override
    public void transform(Main obfuscator) throws Exception {
        obfuscator.classes().forEach(classNode -> {

            classNode.methods.forEach(methodNode -> {

                methodNode.instructions.forEach(abstractInsnNode -> {

                    if (abstractInsnNode instanceof MethodInsnNode) {
                        if (((MethodInsnNode) abstractInsnNode).name.equals("void_signature")) {
                            ((MethodInsnNode) abstractInsnNode).desc = "()M";
                        }

                    }




                    switch (abstractInsnNode.getOpcode()) {
                        case ACONST_NULL -> {
                            System.out.println("ACONST_NULL " + classNode.name);
                            methodNode.instructions.set(abstractInsnNode, new InsnNode(239));
                            break;
                        }

                        case ICONST_0 -> {
                            System.out.println("ICONST_0 " + classNode.name);
                            methodNode.instructions.set(abstractInsnNode, new InsnNode(240));
                            break;
                        }

                        case POP -> {
                            System.out.println("POP " + classNode.name);
                            methodNode.instructions.set(abstractInsnNode, new InsnNode(241));
                            break;
                        }

                    }

                });
            });
        });
    }

}