package ru.kotopushka.antiautistleak.obfuscator.transform.impl.mapper;

import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.FieldRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.*;
import ru.kotopushka.antiautistleak.obfuscator.main.Main;
import ru.kotopushka.antiautistleak.obfuscator.transform.Transformer;
import ru.kotopushka.antiautistleak.obfuscator.util.StringUtil;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MapperTransformer extends Transformer {

    protected static ClassNode getOwner(FieldNode f, List<ClassNode> classes) {
        return findFirst(classes, c -> c.fields.contains(f));
    }

    protected static ClassNode getOwner(MethodNode f, List<ClassNode> classes) {
        return findFirst(classes, c -> c.methods.contains(f));
    }

    public static List<ClassNode> applyMappings(List<ClassNode> classMap, Map<String, String> remap) {
        SimpleRemapper remapper = new SimpleRemapper(remap);
        List<ClassNode> newClassMap = new ArrayList<>();

        for (ClassNode node : classMap) {
            ClassNode copy = new ClassNode();
            ClassRemapper adapter = new ClassRemapper(copy, remapper);
            node.accept(adapter);
            copy.access = node.access;
            copy.sourceFile = "SilvaGuard";
            copy.sourceDebug = "SilvaGuard";
            copy.visitSource("SilvaGuard(best guard)", "SilvaGuard(pls no crack)");
            newClassMap.add(copy);
        }
        return newClassMap;
    }


    private static boolean isInterface(ClassNode classNode) {
        return (classNode.access & ACC_INTERFACE) != 0;
    }

    protected static ClassNode getClassNode(String name, List <ClassNode> classMap) {
        return findFirst(classMap, c -> c.name.equals(name));
    }

    protected static <T> T findFirst(Collection < T > collection, Predicate<T> predicate) {
        for (T t: collection)
            if (predicate.test(t))
                return t;
        return null;
    }


    private static final ArrayList <String> chars = new ArrayList <>();

    protected static String getRandomName() {
        if (chars.isEmpty()) {
            String str = "ZOVZ0VZVZOVZZ0VO0";

            for (char c: str.toCharArray()) {
                chars.add(String.valueOf(c));
            }
        }
        int characters = 36;
        StringBuilder name = new StringBuilder();

        for (int i = 0; i < characters; i++) {
            String ch = chars.get(new Random().nextInt(chars.size()-4));
            name.append(ch);
        }

        return name.toString();
    }
    protected static String getRandomClassName() {
        char[] chars = new char[]{'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z', 'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};



        return "SILVAGUARD"+StringUtil.getString(8)
//        name.append("d");
                ;
    }

    private Map<String, String> fieldRemap = new HashMap<>();

    public static boolean hasFlag(int access, int flag) {
        return (access & flag) != 0;
    }

    public void remapFields(List<ClassNode> classes) {
        // Первый проход: переименование полей
        classes.forEach(classNode -> {
            if (!(hasFlag(classNode.access, ACC_ENUM)))
            if (!classNode.name.startsWith("ru/kotopushka")) {
                classNode.fields.forEach(fieldNode -> {
                    String newFieldName = "NEW_%s".formatted(fieldNode.name);
                    fieldRemap.put(getFieldKey(classNode.name, fieldNode.name + fieldNode.desc), newFieldName);
                    fieldNode.name = newFieldName;
                });
            }
        });

        // Второй проход: замена ссылок на поля в методах
        classes.forEach(classNode -> {
            classNode.methods.forEach(methodNode -> {
                methodNode.instructions.forEach(instruction -> {
                    if (instruction instanceof FieldInsnNode fieldInsn) {
                        String fieldKey = getFieldKey(classNode.name, fieldInsn.name + fieldInsn.desc);
                        if (fieldRemap.containsKey(fieldKey)) {
                            fieldInsn.name = fieldRemap.get(fieldKey);
                            System.out.println("REPLACED field " + fieldInsn.name);
                        }
                    }
                });
            });
        });
    }

    private String getFieldKey(String className, String fieldNameWithDesc) {
        return className + ":" + fieldNameWithDesc;
    }

    @Override
    public void transform(Main main) {

        Map<String, String> remap = new HashMap<>();

        List<String> keys = main.classes().stream().map(c -> c.name).collect(Collectors.toList());
        Collections.shuffle(keys);

        List<ClassNode> classNodes = new CopyOnWriteArrayList<>(main.classes());

        //        classNodes = remapMethods(classNodes);

        for (String key: keys) {
            ClassNode cn = getClassNode(key, classNodes);

            /*
            || cn.name.contains("com/google/internal") || cn.name.contains("com/google/gson/reflect") || cn.name.contains("com/google/gson/stream")
             */

            if (canMap(cn.name)) {

                remap.put(cn.name, getRandomClassName());
            }
        }

        classNodes = applyMappings(classNodes, remap);

        main.classes().clear();

        classNodes.forEach(classNode -> main.getClasses().put(classNode.name, classNode));

//        Map<String, String> fieldRemap = new HashMap<>();
////        Map<String, String> methodRemap = new HashMap<>();
////        Map<String, String> annotationMethodMap = new HashMap<>();
//
//        main.classes().forEach(classNode -> {
//            if (!classNode.name.startsWith("ru/kotopushka"))
//                classNode.fields.forEach(fieldNode -> {
//                    String newFieldName = "NEW_%s".formatted(fieldNode.name);
//                    fieldRemap.put(getFieldKey(classNode.name, fieldNode.name+fieldNode.desc), newFieldName);
//                    fieldNode.name = newFieldName;
//
//                    SimpleRemapper remapper = new SimpleRemapper(remap);
//
//                    FieldRemapper fieldRemapper = new FieldRemapper(fieldNode, remapper);
//
//                    fieldRemapper.
//
//                });
////                if (!classNode.name.contains("Nemida"))
////                    classNode.methods.forEach(methodNode -> {
////                        String newMethodName = remapMethodName(methodNode.name);
////                        methodRemap.put(getMethodKey(classNode.name, methodNode.name, methodNode.desc), newMethodName);
////
////                        if ((classNode.access & ACC_ANNOTATION) != 0) {
////                            annotationMethodMap.put(getMethodKey(classNode.name, methodNode.name), newMethodName);
////                        }
////
////                        methodNode.name = newMethodName;
////                    });
////            }
//        });
//
//
////
//        main.classes().forEach(classNode -> {
//////            if (classNode.name.startsWith("omg/toffi")) {
//                classNode.methods.forEach(methodNode -> {
//                    methodNode.instructions.forEach(instruction -> {
////
//                        if (instruction.getOpcode() == GETSTATIC) {
//                            FieldInsnNode fieldInsn = (FieldInsnNode) instruction;
//                            String fieldKey = getFieldKey(classNode.name, fieldInsn.name + fieldInsn.desc);
//                            boolean LIN3X = false;
//                            if(fieldInsn.name.contains("LINUX")) {
//                                System.out.printf("LINUX? KEY -> %s%n", fieldKey);
//                                LIN3X = true;
//                            }
//
//                            if (fieldRemap.containsKey(fieldKey)) {
//                                fieldInsn.name = fieldRemap.get(fieldKey);
//                                if (LIN3X) System.out.println("WOWOWOWOWOW LIN3X REPLACED...");
////                                else
////                                System.out.println("REPLACED to " + fieldInsn.name);
//                            }
//                        }
//
//                        if (instruction instanceof FieldInsnNode fieldInsn) {
//                            String fieldKey = getFieldKey(classNode.name, fieldInsn.name + fieldInsn.desc);
//                            if (fieldRemap.containsKey(fieldKey)) {
//                                fieldInsn.name = fieldRemap.get(fieldKey);
//                                System.out.println("REPLACED to " + fieldInsn.name);
//                            }
//                        }
//                        //else if (instruction instanceof MethodInsnNode) {
//////                            MethodInsnNode methodInsn = (MethodInsnNode) instruction;
//////                            String methodKey = getMethodKey(methodInsn.owner, methodInsn.name, methodInsn.desc);
//////                            if (methodRemap.containsKey(methodKey)) {
//////                                methodInsn.name = methodRemap.get(methodKey);
//////                            }
////                        } else if (instruction instanceof InvokeDynamicInsnNode) {
//////                            InvokeDynamicInsnNode invokeDynamicInsnNode = (InvokeDynamicInsnNode) instruction;
//////                            for (Object bsmArg : invokeDynamicInsnNode.bsmArgs) {
//////                            System.out.println(bsmArg + "CLASS: " + bsmArg.getClass().getSimpleName());
////
//////                                if (bsmArg instanceof Handle) {
//////
//////                                    Handle handle = (Handle) bsmArg;
//////
//////                                    if (getMethodKey(handle.getOwner(), handle.getName(), handle.getDesc()) != null) {
//////                                        handle.name = methodRemap.get(getMethodKey(handle.getOwner(), handle.getName(), handle.getDesc()));
//////                                    }
//////                                }
//////                            }
////                        }
//                    });
//                });
//            });
//        }
//
//        nativeObfuscator.classes().forEach(classNode -> {
//
////                if (classNode.invisibleAnnotations != null)
////                    classNode.invisibleAnnotations.forEach(annotationNode -> remapMethodNames(annotationNode, annotationMethodMap, classNode));
////
////                if (classNode.visibleAnnotations != null)
////                    classNode.visibleAnnotations.forEach(annotationNode -> remapMethodNames(annotationNode, annotationMethodMap, classNode));
//        });

//        remapFields(classNodes);

    }

    public boolean canMap(String name) {

        for (String string : Main.getInstance().getRemapperPaths()) {
            if (name.startsWith(string)) {
                return !name.contains("ru/kotopushka/j2c")
                        && !name.contains("ai/")
                        && !name.contains("net/minecraft/realms/")
                        && !name.contains("fun/rockstarity/api/scripts/")
                        && !name.contains("net/minecraft/item/Items")
                        && !name.contains("net/minecraft/network/play/client/")
                        && !name.contains("net/minecraft/network/play/server/")
                        && !name.contains("fun/rockstarity/api/events/list/")
                        ;
            }
        }

        return false;
    }

    public String retransform(String name) {

        StringBuilder lol = new StringBuilder();

        for(int i = 1; i < name.length()-1; i++) {
            lol.append(name.charAt(i));
        }

        return lol.toString();

    }

    public void remapMethodNames(AnnotationNode annotationNode, Map<String, String> methodRemap, ClassNode classNode) {
        if (annotationNode.values != null)
        for (int i = 0; i < annotationNode.values.size(); i += 2) {
            String key = (String) annotationNode.values.get(i);

            if (key != null) {
                if (methodRemap.containsKey((getMethodKey(retransform(annotationNode.desc), key)))) {
                    annotationNode.values.set(i, methodRemap.get((getMethodKey(retransform(annotationNode.desc), key))));
                }
            }
        }
    }

    private int counter = 0;

    private String remapMethodName(String originalName) {

        if (originalName.contains("main") || originalName.startsWith("<") || originalName.contains("lambda")) {
            return originalName;
        }

        return "AAL_" + originalName;
    }

    private String getMethodKey(String className, String methodName) {
        return className + "." + methodName;
    }

    private String getMethodKey(String className, String methodName, String methodDesc) {
        return className + "." + methodName + methodDesc;
    }
}