package ru.kotopushka.antiautistleak.obfuscator.transform.impl.objector;

import org.objectweb.asm.ClassWriter;
import ru.kotopushka.antiautistleak.obfuscator.cpp.NativeDll;
import ru.kotopushka.antiautistleak.obfuscator.main.Main;
import ru.kotopushka.antiautistleak.obfuscator.cpp.NativePool;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import ru.kotopushka.antiautistleak.obfuscator.pool.ReferencePool;
import ru.kotopushka.antiautistleak.obfuscator.transform.Transformer;
import ru.kotopushka.antiautistleak.obfuscator.transform.impl.jnicalls.methods.MethodInsnNodeTransformer;
import ru.kotopushka.antiautistleak.obfuscator.util.StringUtil;
import ru.kotopushka.antiautistleak.obfuscator.util.model.ValueModel;
import ru.kotopushka.j2c.loader.Protection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.Reference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Ref;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipEntry;

public class NativeObjectTransformer extends Transformer {
    private static int index = 0;

    private static int poolIndex = 0;

    private static FileOutputStream fileOutputStream;

    private static NativePool nativePool;

    private static void createCMake() throws IOException {
        File nativedir = new File("native/");
        if (nativedir.exists()) {
            nativedir.delete();
        }
        nativedir.mkdirs();
        File file = new File("native/CMakeLists.txt");
        file.createNewFile();
        String cmakeContent = new String(Files.readAllBytes(new File("assets/CMakeLists.txt").toPath()));
        cmakeContent = cmakeContent.replaceAll("solution", "AntiAutistLeak");
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(cmakeContent);
        fileWriter.close();
    }

    private static void createSkCrypt() throws IOException {
        write("skCrypter.h");
        write("VMProtectSDK.h");
//        write("VMProtectSDK64.lib");
//        write("VMProtectSDK64.dll");
//        write("build/lib/vmp.exe");
    }

    private static void write(String name) throws IOException {
        FileWriter fileWriter = new FileWriter("native/" + name);
        fileWriter.write(getResourceAsString("assets/" + name));
        fileWriter.close();
    }

    private static void createNativeDll(NativePool nativePool) throws IOException {
        createSkCrypt();
        NativeDll nativeDll = new NativeDll(nativePool);
        nativeDll.writeDll();
    }

    public static String getResourceAsString(String name) throws IOException {
        return new String(Files.readAllBytes(new File(name).toPath()));
    }


    public static ClassNode addNode(Class<?> clazz) {

//        ClassReader classReader = null;
//        try {
//            classReader = new ClassReader(Files.readAllBytes(Path.of("c:/clients/Protection.class")));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        ClassNode classNode = new ClassNode();
//        classReader.accept(classNode, 0);


        try {
            ClassReader classReader = new ClassReader(clazz.getName());
            ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);
            return classNode;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void executeCommand(String[] command, File directory) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command)
                .directory(directory)
                .inheritIO()
                .start();
        process.waitFor();
    }

    public void build() throws IOException, InterruptedException {
        executeCommand(new String[]{"C:\\Program Files\\CMake\\bin\\cmake.exe", "."}, new File("native\\"));
        executeCommand(new String[]{"C:\\Program Files\\CMake\\bin\\cmake.exe", "--build", ".", "--config", "Release"}, new File("native\\"));
        executeCommand(new String[]{new File("native\\build\\lib\\").getAbsolutePath()+ "\\vmp.exe", "AntiAutistLeak.dll", "AALProtection.dll"}, new File(new File("native\\build\\lib\\").getAbsolutePath()));
    }

    public void writeObj(Object object) {
        try {
            fileOutputStream.write(String.valueOf(object).getBytes());
            fileOutputStream.write("\n".getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String escapeString(String input) {
        StringBuilder escapedString = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case '\n' -> escapedString.append("\\n");
                case '\r' -> escapedString.append("\\r");
                case '\t' -> escapedString.append("\\t");
                case '\"' -> escapedString.append("\\\"");
                case '\\' -> escapedString.append("\\\\");
                default -> escapedString.append(c);
            }
        }
        return escapedString.toString();
    }

    public static String xorstr(String data) {

        byte[] arr = data.getBytes();

        for (int i = 0; i < arr.length; i++) {
            arr[i] ^= 0x15;
        }

        return new String(arr);
    }

    public void collectStrings(List<ClassNode> classNodes) {
        for (ClassNode classNode : classNodes) {
            List<ValueModel<String>> values = new CopyOnWriteArrayList<>();
            for (MethodNode method : classNode.methods) {
                if ((shouldProcessNative(method) || shouldProcessNative(classNode) || shouldProcessHandshake(method)) && !classNode.name.contains("NemidaSDK")) {
                    for (AbstractInsnNode insn : method.instructions) {
                        if (insn instanceof LdcInsnNode && !method.name.equals("<clinit>") && ((LdcInsnNode) insn).cst instanceof String && !((String) ((LdcInsnNode) insn).cst).contains("AntiAutistLeak")) {
                            if (((LdcInsnNode) insn).cst instanceof String && ((String) ((LdcInsnNode) insn).cst).length() < 300) {
                                String functionName = StringUtil.getRandomString(8);

                                ValueModel<String> valueModel = new ValueModel<>(classNode, functionName, escapeString((String) ((LdcInsnNode) insn).cst), poolIndex);
//                                valueModel.setIndex(poolIndex);
//                                poolIndex++;
                                auth(valueModel, method);

                                /// escapeString((String) ((LdcInsnNode) insn).cst)
                                nativePool.writeString(valueModel);
//                                writeObj(((String) ((LdcInsnNode) insn).cst));
                                ((LdcInsnNode) insn).cst = index++;
                                values.add(new ValueModel<>(classNode, functionName, functionName));
//                              method.instructions.insert(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;"));
                                method.instructions.set(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, classNode.name, functionName, "()Ljava/lang/String;"));
                            }
                        }
                    }
                } else if (!classNode.name.contains("NemidaSDK")) {
//                    List<AbstractInsnNode> originalInstructions = new ArrayList<>(List.of(method.instructions.toArray()));
//                    for (AbstractInsnNode insn : originalInstructions) {
//                        if (insn instanceof LdcInsnNode) {
//                            if (((LdcInsnNode) insn).cst instanceof String) {
//                                String stringVal = escapeString(xorstr((String) ((LdcInsnNode) insn).cst));
//                                nativePool.write(stringVal);
//                                ((LdcInsnNode) insn).cst = index++;
//                                method.instructions.insert(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;"));
//                                method.instructions.insert(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "ru/kotopushka/antiautistleak/obfuscator/NemidaSDK", "getFromPool", "(I)Ljava/lang/Object;"));
//                            }
//                        }
//                    }
                }
            }
            for (ValueModel<String> valueModel : values) {
                classNode.methods.add(new MethodNode(ACC_PUBLIC + ACC_STATIC + ACC_NATIVE, valueModel.getName(), "()Ljava/lang/String;", null, null));
            }
        }
    }

    public static boolean shouldProcessHandshake(MethodNode methodNode) {

        return methodNode.invisibleAnnotations != null &&
                methodNode.invisibleAnnotations.stream().anyMatch(annotationNode ->
                        annotationNode.desc.contains(NATIVEAUTH_ANNOTATION_DESC)) || methodNode.visibleAnnotations != null &&
                methodNode.visibleAnnotations.stream().anyMatch(annotationNode ->
                        annotationNode.desc.contains(NATIVEAUTH_ANNOTATION_DESC));
    }


    public void collectInts(List<ClassNode> classNodes) {
        for (ClassNode classNode : classNodes) {
            if (!classNode.name.contains("ru/kotopushka/antiautistleak/obfuscator")) {

                List<ValueModel<Integer>> values = new ArrayList<>();

                for (MethodNode method : classNode.methods) {
                    if ((shouldProcessNative(method) || shouldProcessNative(classNode) || shouldProcessHandshake(method))) {
                        List<AbstractInsnNode> originalInstructions = new ArrayList<>(List.of(method.instructions.toArray()));

                        for (AbstractInsnNode insn : originalInstructions) {
                            if (insn instanceof IntInsnNode) {
                                if (insn.getOpcode() == SIPUSH || insn.getOpcode() == BIPUSH) {
                                    String functionName = StringUtil.getRandomString(8);
                                    int pushValue = ((IntInsnNode) insn).operand;

                                    if (Double.isInfinite(pushValue)) {

                                        pushValue = 0;

                                    }
                                    ValueModel<Integer> valueModel = new ValueModel<>(classNode, functionName, pushValue, poolIndex);
                                    valueModel.setIndex(poolIndex);
                                    poolIndex++;
                                    auth(valueModel, method);
                                    writeObj(valueModel.getValue());
                                    nativePool.writeInteger(valueModel);
                                    values.add(valueModel);
                                    method.instructions.set(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, classNode.name, functionName, "()I"));
                                }
                            }
                            if ((insn.getOpcode() == Opcodes.ICONST_0 || insn.getOpcode() == Opcodes.ICONST_1 || insn.getOpcode() == Opcodes.ICONST_2 || insn.getOpcode() == Opcodes.ICONST_3 || insn.getOpcode() == Opcodes.ICONST_4 || insn.getOpcode() == Opcodes.ICONST_5)) {
                                int pushValue = insn.getOpcode() == ICONST_0 ? 0 : ICONST_1 == insn.getOpcode() ? 1 : insn.getOpcode() == ICONST_2 ? 2 : insn.getOpcode() == ICONST_3 ? 3 : insn.getOpcode() == ICONST_4 ? 4 : 5;
                                String functionName = StringUtil.getRandomString(8);

                                if (Double.isInfinite(pushValue)) {

                                    pushValue = 0;

                                }

                                ValueModel<Integer> valueModel = new ValueModel<>(classNode, functionName, pushValue, poolIndex);
                                valueModel.setIndex(poolIndex);
                                poolIndex++;
                                auth(valueModel, method);
                                writeObj(valueModel.getValue());
                                nativePool.writeInteger(valueModel);
                                values.add(valueModel);
                                method.instructions.set(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, classNode.name, functionName, "()I"));
                            }
                        }
                    }
                }

                for (ValueModel<Integer> valueModel : values) {
                    classNode.methods.add(new MethodNode(ACC_PUBLIC + ACC_STATIC + ACC_NATIVE, valueModel.getName(), "()I", null, null));
                }

            }
        }
    }

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
                if (!clinit) {
                    MethodNode method = new MethodNode(ACC_STATIC, "<clinit>", "()V", null, null);
                    method.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, "ru/kotopushka/antiautistleak/obfuscator/NemidaSDK", "initialize", "()V"));
                    method.instructions.add(new InsnNode(RETURN));
                    classNode.methods.add(method);
                }
            }
        }
    }

    private void auth(ValueModel<?> valueModel, MethodNode method) {
    	valueModel.setHandshake(shouldProcessHandshake(method));
    }

    private void collectIntsAll(List<ClassNode> classNodes) {
        int count = 0;
        for (ClassNode classNode : classNodes) {
            if (!classNode.name.contains("ru/kotopushka/antiautistleak/obfuscator")) {

                List<ValueModel<Integer>> values = new ArrayList<>();

                for (MethodNode method : classNode.methods) {
                    {
                        List<AbstractInsnNode> originalInstructions = new ArrayList<>(List.of(method.instructions.toArray()));

                        for (AbstractInsnNode insn : originalInstructions) {
                            if (insn instanceof IntInsnNode) {
                                if (insn.getOpcode() == SIPUSH || insn.getOpcode() == BIPUSH) {
                                    int pushValue = ((IntInsnNode) insn).operand;
                                    writeObj(pushValue);
                                    if (count == 0) {
                                        nativePool.setStartPoint(poolIndex);
                                    }
                                    nativePool.write(poolIndex++);
                                    nativePool.setIntLength(poolIndex);
                                    method.instructions.insert(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "ru/kotopushka/antiautistleak/obfuscator/NemidaSDK", "cast", "(I)I"));
                                    method.instructions.set(insn, new IntInsnNode(SIPUSH, index++));
                                    count++;

                                    if(count == 500) {
                                        System.out.println("[$] limit!");
                                        return;
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void createReferences(List<ClassNode> nodes) {
//        nodes.forEach(classNode -> ReferencePool.getReferences().add(classNode.name));

        for (ClassNode classNode : nodes) {
            for (MethodNode mnode : classNode.methods) {
                if (shouldProcessNative(mnode) || shouldProcessNative(classNode)) {
                    for (AbstractInsnNode node : mnode.instructions) {
                        if (node instanceof MethodInsnNode && node.getOpcode() == INVOKESTATIC && !mnode.name.equals("<clinit>")) {

                            if (((MethodInsnNode) node).desc.startsWith("()L")) {
                                mnode.maxStack += 5;
                                MethodInsnNode minsn = ((MethodInsnNode) node);
                                int indexNah = -1;
                                for (int i = 0; i < ReferencePool.getReferences().size(); i++) {
                                    if (ReferencePool.getReferences().get(i).equals(minsn.owner)) {
                                        indexNah = i;
                                        break;
                                    }
                                }

                                if (indexNah == -1) {
                                    System.out.println(minsn.owner);
                                    ReferencePool.getReferences().add(minsn.owner);
                                }

                            } else if (((MethodInsnNode) node).desc.startsWith("()I")) {
                                mnode.maxStack += 5;
                                MethodInsnNode minsn = ((MethodInsnNode) node);
                                int indexNah = -1;
                                for (int i = 0; i < ReferencePool.getReferences().size(); i++) {
                                    if (ReferencePool.getReferences().get(i).equals(minsn.owner)) {
                                        indexNah = i;
                                        break;
                                    }
                                }

                                if (indexNah == -1) {
                                    System.out.println(minsn.owner);
                                    ReferencePool.getReferences().add(minsn.owner);
                                }

                            }
                        }
                    }
                }
            }
        }

    }
    
    @Override
    public void transform(Main main) throws Exception {
        File file = new File("data.pool");
        file.delete();
        file.createNewFile();
        fileOutputStream = new FileOutputStream(file);
        createCMake();
        nativePool = new NativePool();
//        System.out.println("[$] Native Objector");
        List<ClassNode> nodes = new CopyOnWriteArrayList<>(main.classes());
        addInitialize(nodes);

        collectInts(nodes);

        collectIntsAll(nodes);
//        collectStrings(nodes);

        //reference creator

//        createReferences(nodes);

        fileOutputStream.close();

        createNativeDll(nativePool);
        build();
    }
}