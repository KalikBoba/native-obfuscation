package ru.kotopushka.j2c.translator.processor.cpp.utils.translate;

import lombok.Getter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import ru.kotopushka.VMPState;
import ru.kotopushka.j2c.sdk.enums.VMProtectType;
import ru.kotopushka.j2c.translator.Compiler;
import ru.kotopushka.j2c.translator.processor.cpp.protection.ConstantPool;
import ru.kotopushka.j2c.translator.processor.cpp.protection.ReferenceNode;
import ru.kotopushka.j2c.translator.processor.cpp.protection.ReferenceTable;
import ru.kotopushka.j2c.translator.processor.cpp.protection.vmp.InstructionRecordHandler;
import ru.kotopushka.j2c.translator.utils.BaseUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static ru.kotopushka.j2c.translator.utils.clazz.parser.ClassFilter.*;

@Getter
public class MethodContext {
    private final LabelPool labelPool = new LabelPool();
    private final StackPointer stackPointer = new StackPointer();
    private final List<Integer> locals = new ArrayList<>();
    private final ClassNode classNode;
    private final ContextBuilder contextBuilder;
    private final ConstantPool constantPool;
    private int currentWriterIndex;

    public MethodContext(ClassNode classNode, ConstantPool constantPool, int currentWriterIndex) {
        this.classNode = classNode;
        this.constantPool = constantPool;
        this.contextBuilder = new ContextBuilder(this.classNode, this, currentWriterIndex);
        this.currentWriterIndex = currentWriterIndex;
    }

    public ContextBuilder output() {
        return this.contextBuilder;
    }

    public boolean notClinit(MethodNode methodNode) {
        return !methodNode.name.contains("$Clinit");
    }

    @SuppressWarnings("all")
    public static class ContextBuilder {
        private final MethodContext methodContext;
        private final StringBuilder output = new StringBuilder();
        private final StringBuilder classReferenceBuilder;
        private final StringBuilder methodReferenceBuilder;
        private final StringBuilder fieldReferenceBuilder;
        private List<ReferenceNode> classes = new CopyOnWriteArrayList<>();
        private List<ReferenceNode> strings = new CopyOnWriteArrayList<>();
        private @Getter List<ReferenceNode> methods = new CopyOnWriteArrayList<>();
        private @Getter List<ReferenceNode> fields = new CopyOnWriteArrayList<>();
        @Getter
        private static List<ReferenceNode> writers = new CopyOnWriteArrayList<>();

        private final ClassNode classNode;

        private StringBuilder globalReferences = new StringBuilder();
        @Getter
        public ReferenceNode currentWriter;

        public ContextBuilder(ClassNode classNode, MethodContext methodContext, int currentWriterIndex) {
            this.classNode = classNode;
            this.methodContext = methodContext;
            this.pushLine().pushString("// %s".formatted(classNode.name));
            currentWriter = new ReferenceNode(classNode.name, currentWriterIndex);
            writers.add(currentWriter);
            classReferenceBuilder = new StringBuilder();
//            classReferenceBuilder.append(("VMProtectBeginUltra(\"%s\");".formatted(
//                    String.valueOf(new Random().nextLong()*new Random().nextLong()+new Random().nextLong()+new Random().nextLong())
//            )));
            methodReferenceBuilder = new StringBuilder();
            fieldReferenceBuilder = new StringBuilder();

/*            methodReferenceBuilder.append(("VMProtectBeginUltra(\"%s\");".formatted(
                    String.valueOf(new Random().nextLong()*new Random().nextLong()+new Random().nextLong()+new Random().nextLong())
            )));

            fieldReferenceBuilder.append(("VMProtectBeginUltra(\"%s\");".formatted(
                    String.valueOf(new Random().nextLong()*new Random().nextLong()+new Random().nextLong()+new Random().nextLong())
            )));*/

        }

        public boolean isVMP;

        public void begin(MethodNode methodNode) {
            if (isVMP) return;
            boolean classIsMarked = hasNativeAnnotation(classNode);
            boolean methodIsMarked = hasNativeAnnotation(methodNode);

            if (classIsMarked || methodIsMarked) {
                isVMP = true;
                Optional<AnnotationNode> vmProtectAnnotation = Optional.ofNullable(methodNode.visibleAnnotations)
                        .flatMap(annotations -> annotations.stream()
                                .filter(annotation -> annotation.desc.equals(VMPROTECT_ANNOTATION_DESC))
                                .findFirst())
                        .or(() -> Optional.ofNullable(methodNode.invisibleAnnotations)
                                .flatMap(annotations -> annotations.stream()
                                        .filter(annotation -> annotation.desc.equals(VMPROTECT_ANNOTATION_DESC))
                                        .findFirst()));

                if (vmProtectAnnotation.isPresent()) {

                    AnnotationNode annotationNode = vmProtectAnnotation.get();
                    Object typeValue = null;

                    if (annotationNode.values != null) {
                        for (int i = 0; i < annotationNode.values.size(); i += 2) {
                            if ("type".equals(annotationNode.values.get(i))) {
                                typeValue = annotationNode.values.get(i + 1);
                                break;
                            }
                        }
                    }

                    if (typeValue != null) {
                        if (InstructionRecordHandler.getLastOpcode().getOpcode() == 153) return;
                        if (InstructionRecordHandler.getLastOpcode().getOpcode() > 153 && InstructionRecordHandler.getLastOpcode().getOpcode() < Opcodes.GOTO) return;
                        if (InstructionRecordHandler.getLastOpcode().getNext().getOpcode() == Opcodes.GOTO) {
                            Compiler.COMPILE_LOGGER.info("Marker not compiled (because next instruction goto)");
                            return;
                        }
                        VMProtectType vmProtectType = VMProtectType.valueOf((String) ((String[]) typeValue)[1]);
                        if (VMPState.vmp)
                        pushMethodLine("VMProtectBegin%s(\"%s\");".formatted(
                                Character.toUpperCase(vmProtectType.name().charAt(0)) + vmProtectType.name().substring(1).toLowerCase(),
                                String.valueOf(new Random().nextLong() * new Random().nextLong() + new Random().nextLong() + new Random().nextLong() * new Random().nextLong() * new Random().nextLong() + new Random().nextLong() + new Random().nextLong() ^ new Random().nextLong() * new Random().nextLong() + new Random().nextLong() + new Random().nextLong() % new Random().nextLong() * new Random().nextLong() + new Random().nextLong() + new Random().nextLong() * 4 * new Random().nextLong() * new Random().nextLong() + new Random().nextLong() + new Random().nextLong())
                        ));

                    }
                }
            }
        }

        public void end(MethodNode methodNode) {
            if (!isVMP) return;
            if (!VMPState.vmp) return;
            boolean classIsMarked = hasNativeAnnotation(classNode);
            boolean methodIsMarked = hasNativeAnnotation(methodNode);

            if (classIsMarked || methodIsMarked) {
                isVMP = true;
                Optional<AnnotationNode> vmProtectAnnotation = Optional.ofNullable(methodNode.visibleAnnotations)
                        .flatMap(annotations -> annotations.stream()
                                .filter(annotation -> annotation.desc.equals(VMPROTECT_ANNOTATION_DESC))
                                .findFirst())
                        .or(() -> Optional.ofNullable(methodNode.invisibleAnnotations)
                                .flatMap(annotations -> annotations.stream()
                                        .filter(annotation -> annotation.desc.equals(VMPROTECT_ANNOTATION_DESC))
                                        .findFirst()));

                if (vmProtectAnnotation.isPresent()) {

                    AnnotationNode annotationNode = vmProtectAnnotation.get();
                    Object typeValue = null;

                    if (annotationNode.values != null) {
                        for (int i = 0; i < annotationNode.values.size(); i += 2) {
                            if ("type".equals(annotationNode.values.get(i))) {
                                typeValue = annotationNode.values.get(i + 1);
                                break;
                            }
                        }
                    }

                    if (typeValue != null) {
//                        pushMethodLine("__int64 bumBam = (__int64)&env ^ %s;".formatted(new SecureRandom().nextLong()));
                        pushMethodLine("VMProtectEnd();");
                        isVMP = false;
                    }
                }
            }
        }
//
//        public void endV2(MethodNode methodNode) {
//            if (isVMP) {
//                isVMP = false;
//                pushMethodLine("//VMProtectEnd();");
//            }
//        }

        public void pushMethod(MethodNode methodNode, String nativeName,
                               List<String> argNames, String[] CPP_TYPES, Type[] args,
                               boolean clinit, boolean isStatic) {
            this.pushLine().pushString("%s%s JNICALL  Java_%s(JNIEnv *env, "
                    .formatted(clinit ? "extern \"C\" JNIEXPORT " : "",
                            CPP_TYPES[Type.getReturnType(methodNode.desc).getSort()],
                            nativeName)
            );

            this.pushString(BaseUtils.getFlag(methodNode.access, Opcodes.ACC_STATIC) ? "jclass clazz" : "jobject obj");

            for (int i = 0; i < args.length; ++i) {
                this.pushString(", %s arg%d".formatted(CPP_TYPES[args[i].getSort()], i));
            }
            this.pushString(") {");
        }

        public ReferenceNode findString(String string) {
            for (ReferenceNode referenceNode : strings) {
                if (referenceNode.getClassName().equals(string)) {
                    return referenceNode;
                }
            }
            return null;
        }

        public ReferenceNode findClass(String klassName) {
            for (ReferenceNode referenceNode : classes) {
                if (referenceNode.getClassName().equals(klassName)) {
                    return referenceNode;
                }
            }
            return null;
        }

        public ReferenceNode pushJavaString(String klassName) {

            for (ReferenceNode referenceNode : strings) {
                if (referenceNode.getClassName().equals(klassName)) {
                    return referenceNode;
                }
            }

            int index = ReferenceTable.getClassIndex();
            ReferenceTable.pushClass(klassName, index);

            //not found, we should allocate

            ReferenceNode classRef = new ReferenceNode(klassName, "NULL", "NULL", true, writeIndex++, 1);
            if (classRef.getWriter() == null) {
                classRef.setWriter(getCurrentWriter());
            }
//            classReferenceBuilder.append("    VMProtectBeginUltra(\"classes_%s\");\n".formatted(
//                    String.valueOf(new Random().nextLong()*new Random().nextLong()+new Random().nextLong()+new Random().nextLong())
//            ));

            ReferenceNode referenceNode = findReferenceByWriterOrPush(methodContext, classRef);

            this.globalReferences.append("""
                            { // string reference
                            
                                    jstring localReference = env->NewStringUTF(%s);
                            
                                    jstring reference = (jstring)env->NewGlobalRef(localReference);
                                    env->DeleteLocalRef(localReference);
                     
                                    writers[%s].writeInt64((__int64)reference ^ %s);
                                    writers[%s].writeInt64(%s);
                    
                            }\n                    
                    """.formatted(
                    klassName,
                    referenceNode.getWriter().getId(),
//                    index,
//                    methodContext.getConstantPool().pushUtf(klassName),
//
//                    index,
////                    methodContext.getConstantPool().pushUtf("%s/decrypt?value=".formatted(TranslatorConfiguration.IMP.MAIN.URL_PROTECTION)),
////                    index,
//                    index,//methodContext.getConstantPool().pushUtf(String.valueOf(referenceNode.getSeed())),
                    referenceNode.getClinit(),


//                    referenceNode.getWriter().getId(),
//                    index,
                    referenceNode.getWriter().getId(),
                    (referenceNode.getSeed())
            ));

//            this.classReferenceBuilder.append("    classes_notEncrypted[%s] = (jclass)env->NewGlobalRef(env->FindClass((%s)));\n"
//                    .formatted(
//                            index,
//                            methodContext.getConstantPool().pushUtf(klassName),
//                            methodContext.getConstantPool().pushUtf(String.valueOf(referenceNode.getSeed()))
//                    ));
//            this.classReferenceBuilder.append("    classes[%s] = (jclass)std::stoll(request(std::format(\"{}{}&seed={}&rtdsc={}\", %s, (__int64)classes_notEncrypted[%s] ^ %s, %s, rtdsc)));\n"
//                    .formatted(index,
//                            methodContext.getConstantPool().pushUtf("%s/decrypt?value=".formatted(TranslatorConfiguration.IMP.MAIN.URL_PROTECTION)),
//                            index,
//                            referenceNode.getClinit(),
//                            methodContext.getConstantPool().pushUtf(String.valueOf(referenceNode.getSeed()))
//                    ));

            strings.add(referenceNode);

            return referenceNode;
        }

        public ReferenceNode pushJavaClass(String klassName) {

            for (ReferenceNode referenceNode : classes) {
                if (referenceNode.getClassName().equals(klassName)) {
                    return referenceNode;
                }
            }

            int index = ReferenceTable.getClassIndex();
            ReferenceTable.pushClass(klassName, index);

            //not found, we should allocate

            ReferenceNode classRef = new ReferenceNode(klassName, "NULL", "NULL", true, writeIndex++, 1);
            if (classRef.getWriter() == null) {
                classRef.setWriter(getCurrentWriter());
            }
//            classReferenceBuilder.append("    VMProtectBeginUltra(\"classes_%s\");\n".formatted(
//                    String.valueOf(new Random().nextLong()*new Random().nextLong()+new Random().nextLong()+new Random().nextLong())
//            ));

            ReferenceNode referenceNode = findReferenceByWriterOrPush(methodContext, classRef);

            this.globalReferences.append("""
                            { // class reference
                            
                                    jclass localReference = env->FindClass(%s);
                    
                                    jclass reference = (jclass)(env->NewGlobalRef(localReference));
                                    env->DeleteLocalRef(localReference);
                            
                                    writers[%s].writeInt64((__int64)reference ^ %s);
                            
                                    writers[%s].writeInt64(%s);
                    
                            }\n                   
                    """.formatted(
//                    index,
                    methodContext.getConstantPool().pushUtf(klassName),

//                    index,
//                    methodContext.getConstantPool().pushUtf("%s/decrypt?value=".formatted(TranslatorConfiguration.IMP.MAIN.URL_PROTECTION)),
//                    index,
//                    index,//methodContext.getConstantPool().pushUtf(String.valueOf(referenceNode.getSeed())),
//                    referenceNode.getClinit(),


                    referenceNode.getWriter().getId(),
//                    index,
                    referenceNode.getClinit(),
                    referenceNode.getWriter().getId(),
                    (referenceNode.getSeed())
            ));

//            this.classReferenceBuilder.append("    classes_notEncrypted[%s] = (jclass)env->NewGlobalRef(env->FindClass((%s)));\n"
//                    .formatted(
//                            index,
//                            methodContext.getConstantPool().pushUtf(klassName),
//                            methodContext.getConstantPool().pushUtf(String.valueOf(referenceNode.getSeed()))
//                    ));
//            this.classReferenceBuilder.append("    classes[%s] = (jclass)std::stoll(request(std::format(\"{}{}&seed={}&rtdsc={}\", %s, (__int64)classes_notEncrypted[%s] ^ %s, %s, rtdsc)));\n"
//                    .formatted(index,
//                            methodContext.getConstantPool().pushUtf("%s/decrypt?value=".formatted(TranslatorConfiguration.IMP.MAIN.URL_PROTECTION)),
//                            index,
//                            referenceNode.getClinit(),
//                            methodContext.getConstantPool().pushUtf(String.valueOf(referenceNode.getSeed()))
//                    ));

            classes.add(referenceNode);

            return referenceNode;
        }

        private ReferenceNode findReference(ReferenceNode toFind) {
                for (ReferenceNode referenceNode : currentWriter.getReferences()) {
                    if (referenceNode.getClassName().equals(toFind.getClassName())) {
                        if (referenceNode.getName().equals(toFind.getName())) {
                            if (referenceNode.getSignature().equals(referenceNode.getSignature())) {
                                if (referenceNode.isStaticVal() == referenceNode.isStaticVal()) {
                                    referenceNode.setWriter(getCurrentWriter());
                                    return referenceNode;
                                }
                            }
                        }
                    }
            }
            return null;
        }

        private ReferenceNode findReferenceByWriterOrPush(MethodContext context, ReferenceNode reference) {

            ReferenceNode finded = findReference(reference);

            if (finded != null) {
                return finded;
            }

            if (finded == null) {
                ReferenceNode writer = null;
                if (context.getContextBuilder() == this) {
                    writers.get(context.getCurrentWriterIndex());
                }

                reference.setWriter(getCurrentWriter());

                getCurrentWriter().getReferences().add(reference);

            }

            return reference;
        }

        private int writeIndex = 0;

        public ReferenceNode findMethod(MethodContext context, String className, String name, String signature, boolean isStatic, MethodNode methodNode) {

            for (ReferenceNode referenceNode : getMethods()) {
                if (referenceNode.getClassName().equals(className)) {
                    if (referenceNode.getName().equals(name)) {
                        if (referenceNode.getSignature().equals(signature)) {
                            if (referenceNode.isStaticVal() == isStatic) {
                                return referenceNode;
                            }
                        }
                    }
                }
            }

            return null;
        }

        private ReferenceNode referenceNodeAllocationTemplate(MethodContext context, String className, String name, String signature, boolean isStatic, List<ReferenceNode> references, StringBuilder referenceStringBuilder, String arrayName, String castType, MethodNode methodNode) {
            ReferenceNode classRef = findClass(className);
            if (classRef == null) {
                classRef = pushJavaClass(className);
            }
            for (ReferenceNode referenceNode : references) {
                if (referenceNode.getClassName().equals(className)) {
                    if (referenceNode.getName().equals(name)) {
                        if (referenceNode.getSignature().equals(signature)) {
                            if (referenceNode.isStaticVal() == isStatic) {
                                return referenceNode;
                            }
                        }
                    }
                }
            }

            int index = ReferenceTable.getClassIndex();

            //not found, we should allocate

            ReferenceNode referenceNode = new ReferenceNode(className, name, signature, isStatic, writeIndex++, 1);
            if (referenceNode.getWriter() == null) {
                referenceNode.setWriter(getCurrentWriter());
            }

            getCurrentWriter().getReferences().add(referenceNode);

//            ReferenceNode referenceNode = new ReferenceNode(className, name, signature, isStatic, index, 1);
//            referenceStringBuilder.append("    VMProtectBeginUltra(\"%s_%s\");\n".formatted(arrayName,
//                    String.valueOf(new Random().nextLong()*new Random().nextLong()+new Random().nextLong()+new Random().nextLong())
//            ));
//            referenceStringBuilder.append("%ss[%s] = env->Get%s%sID(%s, (%s), (%s))"
//                    .formatted(
//                            arrayName,
//                            referenceNode.getId(),
//                            referenceNode.isStatic(),
//                            (referenceNode.getName()),
//                            (referenceNode.getSignature())
//                    ));



            globalReferences.append("""
                            { // %s reference id: %s
                            
                                    jclass reference = env->FindClass(\"%s\");
                    
                                    writers[%s].writeInt64((__int64) ((__int64)env->Get%s%sID(reference, (%s), (%s)) ^ %s));
                                    writers[%s].writeInt64(%s);
                                    
                                    env->DeleteLocalRef(reference);
                    
                            }\n                    
                    """.formatted(
                            arrayName,
                            referenceNode.getId(),
                    className,
//                    arrayName,
//                    index,
//                    castType,
//                    methodContext.getConstantPool().pushUtf("%s/decrypt?value=".formatted(TranslatorConfiguration.IMP.MAIN.URL_PROTECTION)),
                    referenceNode.getWriter().getId(),
                    referenceNode.isStatic(),
                    Character.toUpperCase(arrayName.charAt(0)) + arrayName.substring(1),
                    context.getConstantPool().pushUtf(referenceNode.getName()),
                    context.getConstantPool().pushUtf(referenceNode.getSignature()),
                    referenceNode.getClinit(),
//                    methodContext.getConstantPool().pushUtf(String.valueOf(referenceNode.getSeed())),
                    referenceNode.getWriter().getId(),
//                    arrayName,
//                    index,
//                    referenceNode.getWriter().getId(),
                    referenceNode.getSeed()
            ));

//            referenceStringBuilder.append("    %ss[%s] = (%s)std::stoll(request(std::format(\"{}{}&seed={}&rtdsc={}\", %s, ((__int64)env->Get%s%sID(%s, (%s), (%s)) ^ %s), %s, rtdsc)));\n"
//                    .formatted(
//                            arrayName,
//                            referenceNode.getId(),
//                            castType,
//                            methodContext.getConstantPool().pushUtf("%s/decrypt?value=".formatted(TranslatorConfiguration.IMP.MAIN.URL_PROTECTION)),
//                            referenceNode.isStatic(),
//                            Character.toUpperCase(arrayName.charAt(0)) + arrayName.substring(1),
//                            ReferenceSnippetGenerator.generateJavaClassReference(context, new MethodNode(0,"$Clinit", "", null, null), className),
//                            context.getConstantPool().pushUtf(referenceNode.getName()),
//                            context.getConstantPool().pushUtf(referenceNode.getSignature()),
//                            referenceNode.getClinit(),
//                            methodContext.getConstantPool().pushUtf(String.valueOf(referenceNode.getSeed())),
//                            arrayName,
//                            castType
//                    ));
//            referenceStringBuilder.append("    //VMProtectEnd();\n");
             references.add(referenceNode);

            return referenceNode;
        }

        public ReferenceNode allocateOrGetMethodNode(MethodContext context, String className, String name, String signature, boolean isStatic, MethodNode methodNode) {
            ReferenceNode referenceNode = referenceNodeAllocationTemplate(context, className, name, signature, isStatic, getMethods(), methodReferenceBuilder, "method", "jmethodID", methodNode);
            getMethods().add(referenceNode);
            return referenceNode;
        }

        public ReferenceNode findField(MethodContext context, String className, String name, String signature, boolean isStatic, MethodNode methodNode) {

            for (ReferenceNode referenceNode : getFields()) {
                    if (referenceNode.getName().equals(name)) {
                        if (referenceNode.getSignature().equals(signature)) {
                            if (referenceNode.isStaticVal() == isStatic) {
                                return referenceNode;
                            }
                        }
                    }
                }

            return null;
        }

        public ReferenceNode allocateOrGetFieldNode(MethodContext context, String className, String name, String signature, boolean isStatic, MethodNode methodNode) {
            return referenceNodeAllocationTemplate(context, className, name, signature, isStatic, getFields(), fieldReferenceBuilder, "field", "jfieldID", methodNode);
        }

        public ContextBuilder pushLine() {
            output.append("\n");
            return this;
        }

        public ContextBuilder pushMethodLine(String str) {
            return this.pushLine().pushTab(2).pushTab().pushString(str);
        }

        public ContextBuilder pushMethodBlock(String... lines) {
            this.pushLine().pushTab().pushString("{");
            Arrays.stream(lines).forEach(line -> this.pushLine().pushTab(2).pushString(line));
            this.pushLine().pushTab().pushString("}");
            return this;
        }

        public ContextBuilder pushLineWithTab(int tabAmount, String str) {
            return this.pushLine().pushTab(tabAmount).pushString(str);
        }

        public ContextBuilder pushString(String str) {
            output.append(str);
            return this;
        }

        public ContextBuilder pushTab(int amount) {
            output.append("    ".repeat(amount));
            return this;
        }

        public ContextBuilder pushTab() {
            return this.pushTab(1);
        }

        public String toString() {
            return output.toString();
        }


        public String getClassReferences() {

//            if (globalReferences.size() > 1) {
            if (VMPState.vmp)
                return "%s\n%s        writers[%s].push();\n        VMProtectEnd();\n".formatted(("    VMProtectBeginUltra(\"clinit_references_%s\");\n".formatted(
                        String.valueOf(new Random().nextLong() * new Random().nextLong() + new Random().nextLong() + new Random().nextLong())
                )), this.globalReferences.toString(), methodContext.getCurrentWriterIndex());
//            }
            else {
                return "\n%s writers[%s].push();\n".formatted(this.globalReferences.toString(), methodContext.getCurrentWriterIndex());
            }
//            return "\n%s \n".formatted(this.globalReferences.toString());
        }
    }

    public static class StackPointer {
        private int pointer;

        public StackPointer() {
            this.pointer = 0;
        }

        public StackPointer push(int... count) {
            pointer += (count.length > 0) ? count[0] : 1;
            return this;
        }

        public StackPointer pop(int... count) {
            pointer -= (count.length > 0) ? count[0] : 1;
            return this;
        }

        public int peek() {
            return this.pointer;
        }

        public void set(int pointer) {
            this.pointer = pointer;
        }
    }

    public static class LabelPool {

        private final WeakHashMap<Label, Long> labels = new WeakHashMap<>();
        private long currentIndex = 0;

        public String getName(Label label) {
            return "L" + this.labels.computeIfAbsent(label, a -> ++currentIndex);
        }

        public void clear() {
            this.labels.clear();
            this.currentIndex = 0;
        }
    }
}