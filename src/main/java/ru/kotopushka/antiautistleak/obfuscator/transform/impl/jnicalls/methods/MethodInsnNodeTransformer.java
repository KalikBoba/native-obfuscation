package ru.kotopushka.antiautistleak.obfuscator.transform.impl.jnicalls.methods;

import org.objectweb.asm.tree.*;
import ru.kotopushka.antiautistleak.obfuscator.main.Main;
import ru.kotopushka.antiautistleak.obfuscator.pool.ReferencePool;
import ru.kotopushka.antiautistleak.obfuscator.transform.Transformer;

public class MethodInsnNodeTransformer extends Transformer {

    @Override
    public void transform(Main main) {
		for (ClassNode classNode : main.classes()) {
//				/*
//				 * MethodNode mnode = new MethodNode(ACC_PUBLIC + ACC_STATIC, "mur", "()V",
//				 * null, null); mnode.maxStack = 10; mnode.maxLocals = 10;
//				 * mnode.instructions.add(new LdcInsnNode(ICONST_2)); mnode.instructions.add(new
//				 * LdcInsnNode("we/winner/annotations/obfuscator/Penis"));
//				 * mnode.instructions.add(new LdcInsnNode(new String("meow")));
//				 * mnode.instructions.add(new LdcInsnNode("()V")); mnode.instructions.add(new
//				 * MethodInsnNode(INVOKESTATIC, "we/winner/annotations/obfuscator/Penis",
//				 * "call",
//				 * "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;"
//				 * )); mnode.instructions.aFdd(new InsnNode(RETURN));
//				 * classNode.methods.add(mnode);
//				 */
			for (MethodNode mnode : classNode.methods) {
				if (shouldProcessNative(mnode) || shouldProcessNative(classNode) && !classNode.name.contains("NemidaSDK")) {
					for (AbstractInsnNode node : mnode.instructions) {
						if (node instanceof MethodInsnNode && node.getOpcode() == INVOKESTATIC && !mnode.name.equals("<clinit>")) {

							if (((MethodInsnNode) node).desc.startsWith("()L")) {
								mnode.maxStack += 5;
								MethodInsnNode minsn = ((MethodInsnNode) node);
								mnode.instructions.set(node, node = new LdcInsnNode(2));
								int indexNah = -1;
								for (int i = 0; i < ReferencePool.getReferences().size(); i++) {
									if (ReferencePool.getReferences().get(i).equals(minsn.owner)) {
										indexNah = i;
										break;
									}
								}

								if (indexNah == -1) {
									System.out.println("ну пиздец");
									System.out.println(minsn.owner);
									System.exit(-1);
								}

								mnode.instructions.insert(node, node = new IntInsnNode(BIPUSH,indexNah));
								mnode.instructions.insert(node, node = new LdcInsnNode(minsn.name));
								mnode.instructions.insert(node, node = new LdcInsnNode(minsn.desc));

								mnode.instructions.insert(node, node = new MethodInsnNode(INVOKESTATIC, "ru/kotopushka/antiautistleak/obfuscator/NemidaSDK", "call", "(IILjava/lang/String;Ljava/lang/String;)Ljava/lang/Object;"));
								mnode.instructions.insert(node, node = new TypeInsnNode(CHECKCAST, minsn.desc.replace("()L", "").replace(";", "")));
							} else if (((MethodInsnNode) node).desc.startsWith("()I")) {
								mnode.maxStack += 5;
								MethodInsnNode minsn = ((MethodInsnNode) node);
								mnode.instructions.set(node, node = new LdcInsnNode(1));
								int indexNah = -1;
								for (int i = 0; i < ReferencePool.getReferences().size(); i++) {
									if (ReferencePool.getReferences().get(i).equals(minsn.owner)) {
										indexNah = i;
										break;
									}
								}

								if (indexNah == -1) {
									System.out.println("ну пиздец");
									System.out.println(minsn.owner);
									System.exit(-1);
								}

								mnode.instructions.insert(node, node = new IntInsnNode(BIPUSH,indexNah));
								mnode.instructions.insert(node, node = new LdcInsnNode(minsn.name));
								mnode.instructions.insert(node, node = new LdcInsnNode(minsn.desc));

								mnode.instructions.insert(node, node = new MethodInsnNode(INVOKESTATIC, "ru/kotopushka/antiautistleak/obfuscator/NemidaSDK", "call", "(IILjava/lang/String;Ljava/lang/String;)Ljava/lang/Object;"));
								mnode.instructions.insert(node, node = new TypeInsnNode(CHECKCAST, "java/lang/Integer"));
								mnode.instructions.insert(node, node = new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I"));
							}
						}
					}
				}
			}
		}
	}
}
