package ru.kotopushka.rewriter.constants;

public interface ClassFileMethodHandleReferences {
    int JVM_REF_getField        =   1,
    JVM_REF_getStatic           =   2,
    JVM_REF_putField            =   3,
    JVM_REF_putStatic           =   4,
    JVM_REF_invokeVirtual       =   5,
    JVM_REF_invokeStatic        =   6,
    JVM_REF_invokeSpecial       =   7,
    JVM_REF_newInvokeSpecial    =   8,
    JVM_REF_invokeInterface     =   9,
    KVM_REF_getField            =   10,
    KVM_REF_getStatic           =   11,
    KVM_REF_putField            =   12,
    KVM_REF_putStatic           =   13,
    KVM_REF_invokeVirtual       =   14,
    KVM_REF_invokeStatic        =   15,
    KVM_REF_invokeSpecial       =   16,
    KVM_REF_newInvokeSpecial    =   17,
    KVM_REF_invokeInterface     =   18;
}