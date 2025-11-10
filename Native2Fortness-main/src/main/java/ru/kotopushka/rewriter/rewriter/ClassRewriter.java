package ru.kotopushka.rewriter.rewriter;

import ru.kotopushka.rewriter.constants.ClassFileConstants;
import ru.kotopushka.rewriter.stream.ClassInputStream;
import ru.kotopushka.rewriter.stream.ClassOutputStream;

import java.util.Random;

import static ru.kotopushka.rewriter.global.GlobalConstants.*;

public class ClassRewriter implements ClassFileConstants {
    private final ClassOutputStream classOutputStream;

    public ClassRewriter(final ClassInputStream classInputStream) {
        classOutputStream = new ClassOutputStream();

        write_class(classInputStream);

    }

    private String[] utf8;

    private void write_class(final ClassInputStream classInputStream) {

        int magic = classInputStream.readInt();

        if (magic != 0xCAFEBABE) {
            throw new ClassFormatError(String.format("Invalid magical number 0x%s", Integer.toHexString(magic).toUpperCase()));
        }

        magic = 0xDEADBABE ^ new Random().nextInt(60)+1;
        classOutputStream.writeInt(magic);

//        classOutputStream.writeBytes(keys);


        int minorVersion = classInputStream.readShort();
        int majorVersion = classInputStream.readShort();


        classOutputStream.writeShort((minorVersion));
        classOutputStream.writeShort((majorVersion));

        int constantPoolSize = classInputStream.readShort();

        utf8 = new String[constantPoolSize];

        classOutputStream.rewriteShort((constantPoolSize ^ _CONSTANT_POOL_KEY_1 ^ (magic)));

        classOutputStream.writeKeys();

        writeConstantPool(constantPoolSize, classInputStream);

        int access_flags = classInputStream.readShort();

        classOutputStream.rewriteShort(access_flags);

        int this_class_index = classInputStream.readShort();

        classOutputStream.rewriteShort(this_class_index);

        int super_class_index = classInputStream.readShort();

        classOutputStream.rewriteShort(super_class_index);

        int itfs_length = classInputStream.readShort();

        classOutputStream.rewriteShort(itfs_length);

        writeInterfaces(itfs_length, classInputStream);

        // u2 fields_count;
        // field_info fields[fields_count];
        // u2 methods_count;
        // method_info methods[methods_count];
        // u2 attributes_count;
        // attribute_info attributes[attributes_count];

        final int fields_count = classInputStream.readShort();

        classOutputStream.rewriteShort(fields_count);

        writeFields(fields_count, classInputStream);

        int methods_count = classInputStream.readShort();

        classOutputStream.rewriteShort(methods_count);

        writeMethods(methods_count, classInputStream);

        final int attributes_count = classInputStream.readShort();

        classOutputStream.rewriteShort(attributes_count);

        if (attributes_count > 0) {
            for (int attribute_index = 0; attribute_index < attributes_count; attribute_index++) {
                writeAttribute(classInputStream);
            }
        }

//        classOutputStream.writeBytes(classInputStream.readAllBytes());

    }

    private void writeMethods(int methods_count, final ClassInputStream classInputStream) {
        if (methods_count > 0) {
            for (int i = 0; i  < methods_count; i++) {
                //    u2             access_flags;
                //    u2             name_index;
                //    u2             descriptor_index;
                //    u2             attributes_count;
                //    attribute_info attributes[attributes_count];

                final int access_flags = classInputStream.readShort();
                final int name_index = classInputStream.readShort();
                final int descriptor_index = classInputStream.readShort();
                final int attributes_count = classInputStream.readShort();

                classOutputStream.rewriteShort(access_flags);
                classOutputStream.rewriteShort(name_index);
                classOutputStream.rewriteShort(descriptor_index);
                classOutputStream.rewriteShort(attributes_count);

                if (attributes_count > 0) {
                    for (int attribute_index = 0; attribute_index < attributes_count; attribute_index++) {

                        int attribute_name_index = classInputStream.readShort();
                        int attribute_length = classInputStream.readInt();

                        classOutputStream.writeShort(attribute_name_index);
                        classOutputStream.writeInt(attribute_length);

                        if (utf8[attribute_name_index].equals("Code")) {
                            int max_stack = classInputStream.readShort();
                            int max_locals = classInputStream.readShort();
                            int code_length = classInputStream.readInt();
                            classOutputStream.writeShort(max_stack ^ (classOutputStream._klass_key_8));
                            classOutputStream.writeShort(max_locals ^ (classOutputStream._klass_key_8));
                            classOutputStream.writeInt(code_length);


                            // bytecode instructions
                            for (int opcodeIndex = 0; opcodeIndex < code_length; opcodeIndex++) {
                                int value = classInputStream.readByte();

                                classOutputStream.writeByte(value ^ PARSER_KEY);
                            }

                            // exception table
                            int exception_table_length = classInputStream.readShort();
                            classOutputStream.writeShort(exception_table_length);

                            for (int exception_index = 0; exception_index < exception_table_length; exception_index++) {
                                for (int j = 0; j < 4; j++) {
                                    classOutputStream.writeShort(classInputStream.readShort());
                                }
                            }

                            // attributes
                            int code_attributes_count = classInputStream.readShort();
                            classOutputStream.writeShort(code_attributes_count);

                            for (int code_attribute_index = 0; code_attribute_index < code_attributes_count; code_attribute_index++) {
                                int attributeNameIndex = classInputStream.readShort();
                                int attributeLength = classInputStream.readInt();
                                classOutputStream.writeShort(attributeNameIndex);
                                classOutputStream.writeInt(attributeLength);
                                for (int j = 0; j < attributeLength; j++) {
                                    classOutputStream.writeByte(classInputStream.readByte());
                                }
                            }
                        } else {
                            byte[] info = classInputStream.readNBytes(attribute_length);

                            classOutputStream.writeBytes(info);
                        }
                    }
                }

            }
        }
    }

    private void writeFields(int fields_count, final ClassInputStream classInputStream) {
        if (fields_count > 0) {
            for (int i = 0; i < fields_count; i++) {
                //    u2             access_flags;
                //    u2             name_index;
                //    u2             descriptor_index;
                //    u2             attributes_count;
                //    attribute_info attributes[attributes_count];

                final int access_flags = classInputStream.readShort();
                final int name_index = classInputStream.readShort();
                final int descriptor_index = classInputStream.readShort();
                final int attributes_count = classInputStream.readShort();

                classOutputStream.rewriteShort(access_flags);
                classOutputStream.rewriteShort(name_index);
                classOutputStream.rewriteShort(descriptor_index);
                classOutputStream.rewriteShort(attributes_count);

                if (attributes_count > 0) {

                    for (int attribute_index = 0; attribute_index < attributes_count; attribute_index++) {
                        writeAttribute(classInputStream);
                    }

                }

            }
        }
    }

    private void writeAttribute(final ClassInputStream classInputStream) {

        //attribute_info {
        //    u2 attribute_name_index;
        //    u4 attribute_length;
        //    u1 info[attribute_length];
        //}

        int attribute_name_index = classInputStream.readShort();
        int attribute_length = classInputStream.readInt();

        classOutputStream.writeShort(attribute_name_index);
        classOutputStream.writeInt(attribute_length);

        byte[] info = classInputStream.readNBytes(attribute_length);

        classOutputStream.writeBytes(info);

    }

    private void writeInterfaces(int itfs_length, final ClassInputStream classInputStream) {

        if (itfs_length > 0) {
            for (int i = 0; i < itfs_length; i++) {
                int interface_index = classInputStream.readShort();
                classOutputStream.rewriteShort(interface_index);
            }
        }

    }

    private void writeConstantPoolTag(int tag) {

        switch (tag) {
//            case JVM_CONSTANT_Integer -> tag = 21;
//            case JVM_CONSTANT_NameAndType -> tag = 22;
//            case JVM_CONSTANT_MethodHandle -> tag = 23;
            case JVM_CONSTANT_MethodType -> tag = 48;
//            case JVM_CONSTANT_InvokeDynamic -> tag = 25;
//            case JVM_CONSTANT_Dynamic -> tag = 26;
//            case JVM_CONSTANT_Long -> tag = 27;
//            case JVM_CONSTANT_Float -> tag = 28;
//            case JVM_CONSTANT_Double -> tag = 29;
        }

        classOutputStream.writeByte(tag);

    }

    private void writeConstantPool(int constantPoolSize,
                                     final ClassInputStream classInputStream) {

        int constant_pool_global_key = (4 * _CONSTANT_POOL_KEY_6 / _CONSTANT_POOL_KEY_2);

        //because parsing from 1 index in hotspot
        for (int constantPoolIndex = 1; constantPoolIndex < constantPoolSize; constantPoolIndex++) {
            int tag = classInputStream.readByte();

            writeConstantPoolTag(tag);

            switch (tag) {

                case JVM_CONSTANT_Utf8 -> {

                    int length = classInputStream.readShort();

                    byte[] buffer = classInputStream.readUTF(length);

                    utf8[constantPoolIndex] = new String(buffer);

//                    for (int index = 0; index < buffer.length; index++) {
//                        buffer[index] ^= keys[0];
//                    }

                    classOutputStream.writeShort(length);
                    classOutputStream.writeBytes(buffer);

                    break;
                }

                case JVM_CONSTANT_Integer -> {
                    int value = classInputStream.readInt();
                    classOutputStream.writeInt(value);
                    break;
                }

                case JVM_CONSTANT_Float -> {
                    float value = classInputStream.readFloat();
                    classOutputStream.writeInt(Float.floatToIntBits(value));
                    break;
                }

                case JVM_CONSTANT_Long -> {
                    long value = classInputStream.readLong();
                    classOutputStream.writeLong(value);
                    ++constantPoolIndex;
                    break;
                }

                case JVM_CONSTANT_Double -> {
                    double value = classInputStream.readDouble();
                    classOutputStream.writeLong(Double.doubleToLongBits(value));
                    ++constantPoolIndex;
                    break;
                }

                case JVM_CONSTANT_Class -> {
                    int value = classInputStream.readShort();
                    classOutputStream.writeShort(value ^ classOutputStream._klass_key_4 ^ (_CONSTANT_POOL_KEY_3 ^ (classOutputStream._klass_key_6 ^ (classOutputStream._klass_key_7 ^ (classOutputStream._klass_key_8 ^ (classOutputStream._klass_key_9 ^ (classOutputStream._klass_key_1)) ^ classOutputStream._klass_key_10) ^ _CONSTANT_POOL_KEY_6) ^ _CONSTANT_POOL_KEY_4) ^ _CONSTANT_POOL_KEY_2) ^ _CONSTANT_POOL_KEY_3);
                    break;
                }

                case JVM_CONSTANT_String -> {
                    int value = classInputStream.readShort();
                    classOutputStream.writeShort(value ^ classOutputStream._klass_key_3);
                    break;
                }

                case JVM_CONSTANT_Fieldref -> {
                    final int class_index = classInputStream.readShort();
                    final int name_and_type_index = classInputStream.readShort();
                    classOutputStream.writeShort(class_index);
                    classOutputStream.writeShort(name_and_type_index);
                    break;
                }

                case JVM_CONSTANT_Methodref -> {
                    final int class_index = classInputStream.readShort();
                    final int name_and_type_index = classInputStream.readShort();
                    classOutputStream.writeShort(class_index);
                    classOutputStream.writeShort(name_and_type_index);
                    break;
                }

                case JVM_CONSTANT_InterfaceMethodref -> {
                    final int class_index = classInputStream.readShort();
                    final int name_and_type_index = classInputStream.readShort();
                    classOutputStream.writeShort(class_index);
                    classOutputStream.writeShort(name_and_type_index);
                    break;
                }

                case JVM_CONSTANT_NameAndType -> {
                    final int name_index = classInputStream.readShort();
                    final int signature_index = classInputStream.readShort();
                    classOutputStream.writeShort(name_index ^ classOutputStream._klass_key_2);
                    classOutputStream.writeShort(signature_index ^ classOutputStream._klass_key_2);
                    break;
                }

                case JVM_CONSTANT_MethodHandle -> {
                    final int ref_kind = classInputStream.readByte();
                    final int method_index = classInputStream.readShort();
                    classOutputStream.writeByte(ref_kind ^ classOutputStream._klass_key_1 ^ (_CONSTANT_POOL_KEY_5 ^ (_CONSTANT_POOL_KEY_3 / _CONSTANT_POOL_KEY_4 * (_CONSTANT_POOL_KEY_2 - _CONSTANT_POOL_KEY_1 % (_CONSTANT_POOL_KEY_5 + (_CONSTANT_POOL_KEY_6 % 4))))));
                    classOutputStream.writeShort(method_index ^ classOutputStream._klass_key_1 ^ (_CONSTANT_POOL_KEY_5 ^ (_CONSTANT_POOL_KEY_3 / _CONSTANT_POOL_KEY_4 * (_CONSTANT_POOL_KEY_2 - _CONSTANT_POOL_KEY_1 % (_CONSTANT_POOL_KEY_5 + (_CONSTANT_POOL_KEY_6 % 4))))));
                    break;
                }

                case JVM_CONSTANT_MethodType -> {
                    int signature_index = classInputStream.readShort();
                    signature_index = signature_index ^ 0xCC ^((_CONSTANT_POOL_KEY_6 ^ _CONSTANT_POOL_KEY_4 ^ 0xDE) ^ 0xBB) ^ constant_pool_global_key;
                    classOutputStream.writeShort(signature_index ^ 5);
                    break;
                }

                case JVM_CONSTANT_Dynamic -> {
                    final int bootstrap_specifier_index = classInputStream.readShort();
                    final int name_and_type_index = classInputStream.readShort();
                    classOutputStream.writeShort(bootstrap_specifier_index);
                    classOutputStream.writeShort(name_and_type_index);
                    break;
                }

                case JVM_CONSTANT_InvokeDynamic -> {
                    final int bootstrap_specifier_index = classInputStream.readShort();
                    final int name_and_type_index = classInputStream.readShort();
                    classOutputStream.writeShort(bootstrap_specifier_index);
                    classOutputStream.writeShort(name_and_type_index );
                    break;
                }

                case JVM_CONSTANT_Module, JVM_CONSTANT_Package -> classOutputStream.writeShort(classInputStream.readShort());

                default -> {
                    System.out.println(String.format("%02x",classInputStream.readByte()));
                    System.out.println(new String(classInputStream.readAllBytes()));
                    throw new ClassFormatError(String.format("Invalid tag %s", tag));
                }
            }

        }

    }

    public ClassOutputStream getClassOutputStream() {
        return classOutputStream;
    }
}
