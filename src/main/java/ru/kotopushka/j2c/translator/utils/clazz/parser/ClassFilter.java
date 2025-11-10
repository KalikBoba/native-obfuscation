package ru.kotopushka.j2c.translator.utils.clazz.parser;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import ru.kotopushka.j2c.sdk.annotations.NativeExclude;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;
import ru.kotopushka.j2c.sdk.annotations.VMProtect;
import ru.kotopushka.j2c.translator.configuration.matcher.AntPathMatcher;
import ru.kotopushka.j2c.translator.utils.BaseUtils;

import java.util.List;
import java.util.Optional;

public class ClassFilter {
    public static final String VMPROTECT_ANNOTATION_DESC = Type.getDescriptor(VMProtect.class);
    public static final String NATIVE_ANNOTATION_DESC = Type.getDescriptor(NativeInclude.class);
    public static final String NOT_NATIVE_ANNOTATION_DESC = Type.getDescriptor(NativeExclude.class);

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final List<String> blackList;
    private final List<String> whiteList;
    public final boolean useAnnotations;

    public ClassFilter(List<String> blackList, List<String> whiteList, boolean useAnnotations) {
        this.blackList = blackList;
        this.whiteList = whiteList;
        this.useAnnotations = useAnnotations;
    }

    public void cleanAnnotations(ClassNode classNode) {
        Optional.ofNullable(classNode.invisibleAnnotations)
                .ifPresent(annotations -> annotations.removeIf(annotation -> annotation.desc.equals(NATIVE_ANNOTATION_DESC)));

        classNode.methods.stream()
                .filter(method -> method.invisibleAnnotations != null)
                .forEach(method -> method.invisibleAnnotations.removeIf(annotation ->
                        annotation.desc.equals(NATIVE_ANNOTATION_DESC) || annotation.desc.equals(NOT_NATIVE_ANNOTATION_DESC)));
    }

    public static boolean shouldProcess(MethodNode method) {
        return !method.name.contains("onEnable") && !method.name.contains("onDisable") && !BaseUtils.getFlag(method.access, 1024) && !BaseUtils.getFlag(method.access, 256) && !method.name.equals("<init>");
    }

    public boolean shouldProcess(ClassNode classNode) {
        if (isInterface(classNode) || isEnum(classNode) || isInvalidJavaClassName(classNode)) {
            return false;
        }

        if (blackList.get(0).isBlank() && !whiteList.get(0).isBlank()) {
            return isWhitelisted(classNode);
        } else if (!blackList.get(0).isBlank() && whiteList.get(0).isBlank()) {
            return !isBlacklisted(classNode);
        } else if (!blackList.get(0).isBlank()) {
            return !isBlacklisted(classNode) && isWhitelisted(classNode);
        }

        return useAnnotations && hasNativeAnnotation(classNode) ||
               classNode.methods.stream().anyMatch(method -> shouldProcess(classNode, method));
    }

    public boolean shouldProcess(ClassNode classNode, MethodNode methodNode) {
        if (classNode != null)
        if (isBlacklisted(classNode, methodNode)) {
            return false;
        }
        if (classNode != null)
        if (isWhitelisted(classNode, methodNode)) {
            return true;
        }

        for (MethodNode methodNode1 : classNode.methods) {
            boolean methodIsMarked = Optional.ofNullable(methodNode1.invisibleAnnotations)
                    .map(annotations -> annotations.stream()
                            .anyMatch(annotation -> annotation.desc.equals(NATIVE_ANNOTATION_DESC)))
                    .orElse(false) || Optional.ofNullable(methodNode1.visibleAnnotations)
                    .map(annotations -> annotations.stream()
                            .anyMatch(annotation -> annotation.desc.equals(NATIVE_ANNOTATION_DESC)))
                    .orElse(false);

            if (methodIsMarked) {
                return true;
            }
        }

        if (useAnnotations) {
            if (classNode != null) {
                boolean classIsMarked = hasNativeAnnotation(classNode);

                boolean methodIsMarked = Optional.ofNullable(methodNode.invisibleAnnotations)
                        .map(annotations -> annotations.stream()
                                .anyMatch(annotation -> annotation.desc.equals(NATIVE_ANNOTATION_DESC)))
                        .orElse(false) || Optional.ofNullable(methodNode.visibleAnnotations)
                        .map(annotations -> annotations.stream()
                                .anyMatch(annotation -> annotation.desc.equals(NATIVE_ANNOTATION_DESC)))
                        .orElse(false);

                boolean methodIsExcluded = Optional.ofNullable(methodNode.invisibleAnnotations)
                        .map(annotations -> annotations.stream()
                                .anyMatch(annotation -> annotation.desc.equals(NOT_NATIVE_ANNOTATION_DESC)))
                        .orElse(false);

                return methodIsMarked || (classIsMarked && !methodIsExcluded);
            } else {
                boolean methodIsMarked = Optional.ofNullable(methodNode.invisibleAnnotations)
                        .map(annotations -> annotations.stream()
                                .anyMatch(annotation -> annotation.desc.equals(NATIVE_ANNOTATION_DESC)))
                        .orElse(false) || Optional.ofNullable(methodNode.visibleAnnotations)
                        .map(annotations -> annotations.stream()
                                .anyMatch(annotation -> annotation.desc.equals(NATIVE_ANNOTATION_DESC)))
                        .orElse(false);

                boolean methodIsExcluded = Optional.ofNullable(methodNode.invisibleAnnotations)
                        .map(annotations -> annotations.stream()
                                .anyMatch(annotation -> annotation.desc.equals(NOT_NATIVE_ANNOTATION_DESC)))
                        .orElse(false);
                return methodIsMarked && !methodIsExcluded;
            }
        }

        return true;
    }

    private boolean isInterface(ClassNode classNode) {
        return (classNode.access & Opcodes.ACC_INTERFACE) != 0;
    }

    private boolean isAbstract(ClassNode classNode) {
        return (classNode.access & Opcodes.ACC_ABSTRACT) != 0;
    }

    private boolean isEnum(ClassNode classNode) {
        return (classNode.access & Opcodes.ACC_ENUM) != 0;
    }

    private boolean isInvalidJavaClassName(ClassNode classNode) {
        return BaseUtils.isValidJavaFullClassName(classNode.name.replaceAll("/", "."));
    }

    private boolean isBlacklisted(ClassNode classNode) {
        return blackList != null && blackList.stream()
                .filter(black -> !black.contains("#"))
                .anyMatch(black -> PATH_MATCHER.matchStart(black, classNode.name));
    }

    private boolean isWhitelisted(ClassNode classNode) {
        return whiteList != null && whiteList.stream()
                .filter(white -> !white.contains("#"))
                .anyMatch(white -> PATH_MATCHER.matchStart(white, classNode.name));
    }

    private boolean isBlacklisted(ClassNode classNode, MethodNode methodNode) {
        return blackList != null && blackList.stream()
                .filter(black -> black.contains("#"))
                .anyMatch(black -> {
                    String blackClass = black.split("#")[0];
                    String blackMethod = black.split("#")[1];
                    return PATH_MATCHER.matchStart(blackClass, classNode.name) &&
                           (blackMethod.contains("!") ?
                                   PATH_MATCHER.matchStart(blackMethod, methodNode.name + '!' + methodNode.desc) :
                                   PATH_MATCHER.matchStart(blackMethod, methodNode.name));
                });
    }

    private boolean isWhitelisted(ClassNode classNode, MethodNode methodNode) {
        return whiteList != null && whiteList.stream()
                .filter(white -> white.contains("#"))
                .anyMatch(white -> {
                    String whiteClass = white.split("#")[0];
                    String whiteMethod = white.split("#")[1];
                    return PATH_MATCHER.matchStart(whiteClass, classNode.name) &&
                           (whiteMethod.contains("!") ?
                                   PATH_MATCHER.matchStart(whiteMethod, methodNode.name + '!' + methodNode.desc) :
                                   PATH_MATCHER.matchStart(whiteMethod, methodNode.name));
                });
    }

    public static boolean hasNativeAnnotation(MethodNode classNode) {
        boolean hasInvisibleAnnotation = Optional.ofNullable(classNode.invisibleAnnotations)
                .map(annotations -> annotations.stream()
                        .anyMatch(annotation -> annotation.desc.equals(NATIVE_ANNOTATION_DESC)))
                .orElse(false);

        boolean hasVisibleAnnotation = Optional.ofNullable(classNode.visibleAnnotations)
                .map(annotations -> annotations.stream()
                        .anyMatch(annotation -> annotation.desc.equals(NATIVE_ANNOTATION_DESC)))
                .orElse(false);

        return hasInvisibleAnnotation || hasVisibleAnnotation;
    }

    public static boolean hasNativeAnnotation(ClassNode classNode) {
        boolean hasInvisibleAnnotation = Optional.ofNullable(classNode.invisibleAnnotations)
                .map(annotations -> annotations.stream()
                        .anyMatch(annotation -> annotation.desc.equals(NATIVE_ANNOTATION_DESC)))
                .orElse(false);

        boolean hasVisibleAnnotation = Optional.ofNullable(classNode.visibleAnnotations)
                .map(annotations -> annotations.stream()
                        .anyMatch(annotation -> annotation.desc.equals(NATIVE_ANNOTATION_DESC)))
                .orElse(false);

        return hasInvisibleAnnotation || hasVisibleAnnotation;
    }
}