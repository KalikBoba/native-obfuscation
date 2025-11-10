package ru.kotopushka.antiautistleak.obfuscator.transform.impl.jnicalls.fields;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import ru.kotopushka.antiautistleak.obfuscator.main.Main;
import ru.kotopushka.antiautistleak.obfuscator.transform.Transformer;
import ru.kotopushka.antiautistleak.obfuscator.util.Counter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FieldToNativeCallsTransformer extends Transformer {

    private Counter OBJECTS = new Counter();

    private Counter INTEGER = new Counter();

    private Counter LONG = new Counter();

    public void addInitialize(List<ClassNode> classNodes) {
        for (ClassNode classNode : classNodes) {
            if (!classNode.name.contains("ru/kotopushka/antiautistleak/obfuscator")) {
                boolean clinit = false;
                for (MethodNode method : classNode.methods) {
                    if (method.name.equals("<clinit>")) {
                        clinit = true;
                        method.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, "ru/kotopushka/antiautistleak/obfuscator/NemidaSDK", "initialize", "()V"));
                        break;
                    }
                }
//                if (!clinit) {
//                    MethodNode method = new MethodNode(ACC_STATIC, "<clinit>", "()V", null, null);
//                    method.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, "ru/kotopushka/java2cpp/NemidaSDK", "initialize", "()V"));
//                    method.instructions.add(new InsnNode(RETURN));
//                    classNode.methods.add(method);
//                }
            }
        }
    }

    @Override
    public void transform(Main obfuscator) throws Exception {

        obfuscator.classes().forEach(classNode -> {
            if (!classNode.name.contains("Nemida")) {

                if (!classNode.name.contains("ru/kotopushka/antiautistleak/obfuscator") && shouldProcessNative(classNode)) {
                    boolean clinit = false;
                    for (MethodNode method : classNode.methods) {
                        if (method.name.equals("<clinit>")) {
                            clinit = true;
                            method.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, "ru/kotopushka/antiautistleak/obfuscator/NemidaSDK", "initialize", "()V"));
                            break;
                        }
                    }
//                if (!clinit) {
//                    MethodNode method = new MethodNode(ACC_STATIC, "<clinit>", "()V", null, null);
//                    method.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, "ru/kotopushka/java2cpp/NemidaSDK", "initialize", "()V"));
//                    method.instructions.add(new InsnNode(RETURN));
//                    classNode.methods.add(method);
//                }
                }


                List<FieldNode> fieldNodes = new CopyOnWriteArrayList<>();
                classNode.fields.forEach(fieldNode -> {

                    if ((fieldNode.access & Opcodes.ACC_STATIC) != 0 && (fieldNode.desc.equals("J") || fieldNode.desc.equals("I"))) {
                        fieldNodes.add(fieldNode);
                    } else {
                       // System.out.println(classNode.name+fieldNode.desc+fieldNode.name + " not static");
                    }

                });

                for (FieldNode fieldNode : fieldNodes) {
                    classNode.fields.remove(fieldNode);
                }

                classNode.methods.forEach(methodNode -> {

                    methodNode.maxStack += 5;

                    methodNode.instructions.forEach(abstractInsnNode -> {

                        if (abstractInsnNode instanceof FieldInsnNode) {
                            FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNode;

                            for (FieldNode fieldNode : fieldNodes) {
                                if (fieldNode.name.equals(fieldInsnNode.name)
                                                && fieldNode.desc.equals(fieldInsnNode.desc)
                                                    && classNode.name.equals(fieldInsnNode.owner)) {
                                    if (fieldInsnNode.getOpcode() == GETSTATIC) {

                                        switch (fieldInsnNode.desc) {
                                            case "I" -> {
                                                methodNode.instructions.insertBefore(abstractInsnNode, new IntInsnNode(BIPUSH, INTEGER.inc(fieldInsnNode.owner+fieldNode.desc+fieldNode.name)));
                                                methodNode.instructions.set(abstractInsnNode, abstractInsnNode = new MethodInsnNode(INVOKESTATIC, "ru/kotopushka/antiautistleak/obfuscator/NemidaSDK", "getIntField", "(I)I"));
                                                break;
                                            }
                                            case "J" -> {
                                                methodNode.instructions.insertBefore(abstractInsnNode, new IntInsnNode(BIPUSH, LONG.inc(fieldInsnNode.owner+fieldNode.desc+fieldNode.name)));
                                                methodNode.instructions.set(abstractInsnNode, abstractInsnNode = new MethodInsnNode(INVOKESTATIC, "ru/kotopushka/antiautistleak/obfuscator/NemidaSDK", "getLongField", "(I)J"));
                                                break;
                                            }

//                                            default -> {
//                                                if(!(fieldNode.desc.contains("F") || fieldNode.desc.contains("D"))) {
//                                                    methodNode.instructions.insertBefore(abstractInsnNode, new IntInsnNode(BIPUSH, OBJECTS.inc(fieldInsnNode.owner+fieldNode.desc+fieldNode.name)));
//                                                    methodNode.instructions.set(abstractInsnNode, abstractInsnNode = new MethodInsnNode(INVOKESTATIC, "ru/kotopushka/antiautistleak/obfuscator/NemidaSDK", "getObjectField", "(I)Ljava/lang/Object;"));
//                                                }
//                                            }
                                        }

                                    } else if (fieldInsnNode.getOpcode() == PUTSTATIC){


                                        switch (fieldInsnNode.desc) {
                                            case "I" -> {
                                                methodNode.instructions.insertBefore(abstractInsnNode, new IntInsnNode(BIPUSH, INTEGER.getCounter(fieldInsnNode.owner+fieldNode.desc+fieldNode.name)));
                                                methodNode.instructions.set(abstractInsnNode, abstractInsnNode = new MethodInsnNode(INVOKESTATIC, "ru/kotopushka/antiautistleak/obfuscator/NemidaSDK", "setIntField", "(II)V"));
                                                break;
                                            }
                                            case "J" -> {
                                                methodNode.instructions.insertBefore(abstractInsnNode, new IntInsnNode(BIPUSH, LONG.getCounter(fieldInsnNode.owner+fieldNode.desc+fieldNode.name)));
                                                methodNode.instructions.set(abstractInsnNode, abstractInsnNode = new MethodInsnNode(INVOKESTATIC, "ru/kotopushka/antiautistleak/obfuscator/NemidaSDK", "setLongField", "(IJ)V"));
                                                break;
                                            }

//                                            default -> {
//                                                if(!(fieldNode.desc.contains("F") || fieldNode.desc.contains("D"))) {
//                                                    methodNode.instructions.insertBefore(abstractInsnNode, new IntInsnNode(BIPUSH, LONG.getCounter(fieldInsnNode.owner+fieldNode.desc+fieldNode.name)));
//                                                    methodNode.instructions.set(abstractInsnNode, abstractInsnNode = new MethodInsnNode(INVOKESTATIC, "ru/kotopushka/antiautistleak/obfuscator/NemidaSDK", "setObjectField", "(ILjava/lang/Object;)V"));
//                                                }
//                                            }
                                        }

                                    }
                                }
                            }

                        }

                    });

                });
            }

        });

    }
}
