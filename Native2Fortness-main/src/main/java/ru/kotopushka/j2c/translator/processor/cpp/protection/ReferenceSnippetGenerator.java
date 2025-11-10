package ru.kotopushka.j2c.translator.processor.cpp.protection;

import lombok.experimental.UtilityClass;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import ru.kotopushka.j2c.translator.processor.cpp.utils.translate.MethodContext;

@UtilityClass
public class ReferenceSnippetGenerator {

    public String generateStringReference(MethodContext methodContext,
                                             MethodNode method,
                                             String string) {
        ReferenceNode referenceNode = null;
        if (methodContext.notClinit(method) || ((referenceNode = methodContext.output().findString(string)) != null)) {

            if(referenceNode == null) {
                referenceNode = methodContext.output().pushJavaString(string);
            }

//            String snippet = "(((((((__int64)(classes[%s]) ^ %s) ^ %s) ^ %s) ^ %s) ^ %s) ^ %s ^ rtdsc)".formatted(referenceNode.getId(), referenceNode.getKluch2(), referenceNode.getKluch3(), referenceNode.getKluch4(), referenceNode.getKluch5(), referenceNode.getKluch6(), referenceNode.getKluch());


//((__int64)((writers[%s].address_at(%s) ^ writers[%s].private_key()[1] ^ writers[%s].hwid()[5])))
            String snippet = "((__int64)writers[%s].address_at(%s))".formatted(
                    referenceNode.getWriter().getId(),
                    referenceNode.getId()
            );

            snippet = "(jstring)((%s ^ writers[%s].private_key()[1] ^ writers[%s].hwid()[5]))"
                    .formatted(
                            referenceNode.getGenerativeExpression().format(snippet),
                            referenceNode.getWriter().getId(),
                            referenceNode.getWriter().getId()
                    );


//            return "env->FindClass(%s)".formatted(methodContext.getConstantPool().pushUtf(string));
            return snippet;

        } else return "env->NewStringUTF(%s);".formatted((string));
    }

    public String generateJavaClassReference(MethodContext methodContext,
                                                    MethodNode method,
                                                    String className) {
        ReferenceNode referenceNode = null;
        if (methodContext.notClinit(method) || ((referenceNode = methodContext.output().findClass(className)) != null)) {

            if(referenceNode == null) {
                referenceNode = methodContext.output().pushJavaClass(className);
            }

//            String snippet = "(((((((__int64)(classes[%s]) ^ %s) ^ %s) ^ %s) ^ %s) ^ %s) ^ %s ^ rtdsc)".formatted(referenceNode.getId(), referenceNode.getKluch2(), referenceNode.getKluch3(), referenceNode.getKluch4(), referenceNode.getKluch5(), referenceNode.getKluch6(), referenceNode.getKluch());


//((__int64)((writers[%s].address_at(%s) ^ writers[%s].private_key()[1] ^ writers[%s].hwid()[5])))
            String snippet = "((__int64)writers[%s].address_at(%s))".formatted(
                    referenceNode.getWriter().getId(),
                    referenceNode.getId()
            );

            snippet = "(jclass)((%s ^ writers[%s].private_key()[1] ^ writers[%s].hwid()[5]))"
                    .formatted(
                    referenceNode.getGenerativeExpression().format(snippet),
                    referenceNode.getWriter().getId(),
                    referenceNode.getWriter().getId()
            );


//            return "env->FindClass(%s)".formatted(methodContext.getConstantPool().pushUtf(className));
            return snippet;

        } else return "env->FindClass(%s)".formatted(methodContext.getConstantPool().pushUtf(className));
    }

    /*
        TODO: SUPPORT ONLY STATIC METHOD'S
     */

    public String generateJavaMethodReference(MethodContext methodContext,
                                             MethodNode method,
                                             MethodInsnNode mh, boolean isStatic) {

        ReferenceNode referenceNode = null;

        if (methodContext.notClinit(method) || ((referenceNode = methodContext.output().findMethod(methodContext, mh.owner, mh.name, mh.desc, isStatic, method)) != null)) {
//            referenceNode = methodContext.output().findMethod(methodContext, mh.owner, mh.name, mh.desc, isStatic, method);
            if (referenceNode == null) {
                referenceNode = methodContext.output().allocateOrGetMethodNode(methodContext, mh.owner, mh.name, mh.desc, isStatic, method);
            }

            String snippet = "((__int64)writers[%s].address_at(%s))".formatted(
                    referenceNode.getWriter().getId(),
                    referenceNode.getId()
            );

            snippet = "(jmethodID)((%s ^ writers[%s].private_key()[1] ^ writers[%s].hwid()[5]))".formatted(referenceNode.getGenerativeExpression().format(snippet),
                    referenceNode.getWriter().getId(),
                    referenceNode.getWriter().getId(),
                    referenceNode.getWriter().getId(),
                    referenceNode.getWriter().getId()
            );
            return snippet;//"(jmethodID)(((((((__int64)(methods[%s]) ^ %s) ^ %s) ^ %s) ^ %s) ^ %s) ^ %s ^ rtdsc)"
//                    .formatted(referenceNode.getId(), referenceNode.getKluch2(), referenceNode.getKluch3(), referenceNode.getKluch4(), referenceNode.getKluch5(), referenceNode.getKluch6(), referenceNode.getKluch());
        } else
            return "env->Get%sMethodID(env->FindClass(%s), (%s), (%s))"
                    .formatted(isStatic ? "Static" : "",methodContext.getConstantPool().pushUtf(mh.owner), methodContext.getConstantPool().pushUtf(mh.name), methodContext.getConstantPool().pushUtf(mh.desc));
    }

    public String generateJavaFieldReference(MethodContext methodContext,
                                             MethodNode method,
                                             FieldInsnNode fn, boolean isStatic) {
        ReferenceNode referenceNode = null;
        if (methodContext.notClinit(method)  || ((referenceNode = methodContext.output().findField(methodContext,fn.owner, fn.name, fn.desc, isStatic, method)) != null)) {
//            ReferenceNode referenceNode = methodContext.output().allocateOrGetFieldNode(methodContext,fn.owner, fn.name, fn.desc, isStatic, method);
//            referenceNode = methodContext.output().findField(methodContext,fn.owner, fn.name, fn.desc, isStatic, method);
            if (referenceNode == null) {
                referenceNode =  methodContext.output().allocateOrGetFieldNode(methodContext,fn.owner, fn.name, fn.desc, isStatic, method);
            }

            String snippet = "((__int64)writers[%s].address_at(%s))".formatted(
                    referenceNode.getWriter().getId(),
                    referenceNode.getId()
            );

            snippet = "(jfieldID)((%s ^ writers[%s].private_key()[1] ^ writers[%s].hwid()[5]))".formatted(referenceNode.getGenerativeExpression().format(snippet),
                    referenceNode.getWriter().getId(),
                    referenceNode.getWriter().getId(),
                    referenceNode.getWriter().getId(),
                    referenceNode.getWriter().getId()
            );

//            return "env->Get%sFieldID(env->FindClass(%s), (%s), (%s))".formatted(isStatic ? "Static" : "",methodContext.getConstantPool().pushUtf(fn.owner), methodContext.getConstantPool().pushUtf(fn.name), methodContext.getConstantPool().pushUtf(fn.desc));
            return snippet;

//            return "(jfieldID)(((((((__int64)(fields[%s]) ^ %s) ^ %s) ^ %s) ^ %s) ^ %s) ^ %s ^ rtdsc)"
//                    .formatted(referenceNode.getId(), referenceNode.getKluch2(), referenceNode.getKluch3(), referenceNode.getKluch4(), referenceNode.getKluch5(), referenceNode.getKluch6(), referenceNode.getKluch());
        } else
            return "env->Get%sFieldID(env->FindClass(%s), (%s), (%s))".formatted(isStatic ? "Static" : "",methodContext.getConstantPool().pushUtf(fn.owner), methodContext.getConstantPool().pushUtf(fn.name), methodContext.getConstantPool().pushUtf(fn.desc));
    }

}
