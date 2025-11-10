package ru.kotopushka.antiautistleak.obfuscator.transform.impl.jnicalls.fields;

import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import ru.kotopushka.antiautistleak.obfuscator.main.Main;
import ru.kotopushka.antiautistleak.obfuscator.transform.Transformer;

import java.util.Arrays;

public class FieldInsnNodeTransformer extends Transformer {
    @Override
    public void transform(Main obfuscator) throws Exception {

        obfuscator.classes().forEach(classNode -> {

            classNode.methods.forEach(methodNode -> {
                Arrays.stream(methodNode.instructions.toArray()).forEach(abstractInsnNode -> {

                    if (abstractInsnNode instanceof FieldInsnNode) {
                        FieldInsnNode insn = ((FieldInsnNode) abstractInsnNode);

                        switch (abstractInsnNode.getOpcode()) {
                            case GETSTATIC -> {
                                methodNode.maxStack += 5;
                                methodNode.instructions.set(abstractInsnNode, abstractInsnNode = new LdcInsnNode(insn.owner));
                                methodNode.instructions.insert(abstractInsnNode, abstractInsnNode = new LdcInsnNode(insn.name));
                                methodNode.instructions.insert(abstractInsnNode, abstractInsnNode = new LdcInsnNode(insn.desc));
                                methodNode.instructions.insert(abstractInsnNode, abstractInsnNode = new MethodInsnNode(INVOKESTATIC, "ru/kotopushka/antiautistleak/obfuscator/NemidaSDK", "getStaticObject", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;"));
                                methodNode.instructions.insert(abstractInsnNode, abstractInsnNode = new TypeInsnNode(CHECKCAST, insn.desc.replace("L", "").replace(";", "")));
                                break;
                            }
                             default -> System.out.println(abstractInsnNode.getOpcode());
                        }
                    }

                });

            });

        });

    }
}
