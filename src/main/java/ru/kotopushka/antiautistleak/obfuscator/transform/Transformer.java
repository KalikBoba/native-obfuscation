package ru.kotopushka.antiautistleak.obfuscator.transform;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import ru.kotopushka.antiautistleak.obfuscator.includes.annotations.auth.ReleaseNativeAuth;
import ru.kotopushka.antiautistleak.obfuscator.includes.annotations.compile.PopCompileToNativeCalls;
import ru.kotopushka.antiautistleak.obfuscator.includes.annotations.compile.ReleaseCompileToNativeCalls;
import ru.kotopushka.antiautistleak.obfuscator.main.Main;
import org.objectweb.asm.Opcodes;

public abstract class Transformer implements Opcodes {

    public static final String NATIVE_ANNOTATION_DESC = Type.getDescriptor(ReleaseCompileToNativeCalls.class);
    public static final String NOT_NATIVE_ANNOTATION_DESC = Type.getDescriptor(PopCompileToNativeCalls.class);
    public static final String NATIVEAUTH_ANNOTATION_DESC = Type.getDescriptor(ReleaseNativeAuth.class);

    public abstract void transform(Main obfuscator) throws Exception;

    public String name() {
        return this.getClass().getSimpleName();
    }

    public static boolean shouldProcessNative(ClassNode methodNode) {
        return methodNode.invisibleAnnotations != null &&
                methodNode.invisibleAnnotations.stream().anyMatch(annotationNode ->
                        annotationNode.desc.contains(NATIVEAUTH_ANNOTATION_DESC)) || methodNode.invisibleAnnotations != null &&
                methodNode.invisibleAnnotations.stream().anyMatch(annotationNode ->
                        annotationNode.desc.contains(NATIVE_ANNOTATION_DESC)) || methodNode.visibleAnnotations != null &&
                methodNode.visibleAnnotations.stream().anyMatch(annotationNode ->
                        annotationNode.desc.contains(NATIVE_ANNOTATION_DESC));
    }

    public static boolean shouldProcessNative(MethodNode methodNode) {
		return methodNode.invisibleAnnotations != null &&
				methodNode.invisibleAnnotations.stream().anyMatch(annotationNode ->
						annotationNode.desc.contains(NATIVEAUTH_ANNOTATION_DESC)) || methodNode.invisibleAnnotations != null &&
				methodNode.invisibleAnnotations.stream().anyMatch(annotationNode ->
						annotationNode.desc.contains(NATIVE_ANNOTATION_DESC)) || methodNode.visibleAnnotations != null &&
				methodNode.visibleAnnotations.stream().anyMatch(annotationNode ->
						annotationNode.desc.contains(NATIVE_ANNOTATION_DESC));
//        return true;
    }

}
