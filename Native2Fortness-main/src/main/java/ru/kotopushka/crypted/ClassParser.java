package ru.kotopushka.crypted;


import lombok.SneakyThrows;

import java.io.IOException;
import java.security.SecureRandom;

public class ClassParser {

    private final ClassStream classStream;
    private final ClassWriter classWriter;

    public ClassParser(ClassStream classStream, final ClassWriter classWriter) {
        this.classStream = classStream;
        this.classWriter = classWriter;
    }
    int magic;
    int magic2;
    int magic3;
    int magic4;
    @SneakyThrows
    public void parse_stream() throws IOException {
        // читаем (но затем заменим) первые 4 байта
        // (оставляем чтение, чтобы сместить поток, если нужно)
        this.classStream.readByte();
        this.classStream.readByte();
        this.classStream.readByte();
        this.classStream.readByte();

        // генерируем 4 случайных байта
        SecureRandom rnd = new SecureRandom();
        byte[] hdr = new byte[4];
        rnd.nextBytes(hdr);

        // добавляем ключ 0x15 к первому байту (magic0)
        int magic0 = (hdr[0] & 0xFF) + 0x15;
        magic0 &= 0xFF; // по модулю 256
        hdr[0] = (byte) magic0;

        // устанавливаем значение 50 во второй байт (magic2)
        hdr[1] = (byte) 50;

        // записываем сгенерированные 4 байта в writer
        ClassWriter magicwriter = new ClassWriter();
        magicwriter.writeByte(hdr[0] & 0xFF);
        magicwriter.writeByte(hdr[1] & 0xFF);
        magicwriter.writeByte(hdr[2] & 0xFF);
        magicwriter.writeByte(hdr[3] & 0xFF);

        // записываем magic в выходной класс
        classWriter.writeclass(magicwriter);

        // далее — minor и major версии
        classWriter.writeShort(classStream.readShort());
        classWriter.writeShort(classStream.readShort());

        // constant pool
        final int conspool = classStream.readShort();
        classWriter.writeShort(conspool);
        parseConstant(conspool);

        // flags, this_class, super_class
        classWriter.writeShort(classStream.readShort());
        classWriter.writeShort(classStream.readShort());
        classWriter.writeShort(classStream.readShort());

        // interfaces
        int interfacesSize = classStream.readShort();
        classWriter.writeShort(interfacesSize);
        for (int i = 0; i < interfacesSize; i++) {
            classWriter.writeShort(classStream.readShort());
        }

        // остальные байты
        classWriter.writebytes(classStream.getReader().readAllBytes());
    }

    @SneakyThrows
    void parseConstant(final int length) throws IOException {
        for (int i = 1; i < length; ++i) {
            final int consttag = classStream.readByte();
            classWriter.writeTag(consttag);

            switch (consttag) {
                case 7: // JVM_CONSTANT_Class
                    classWriter.writeShort(classStream.readShort());
                    break;
                case 8: // JVM_CONSTANT_String
                    classWriter.writeShort(classStream.readShort());
                    break;
                case 9: // JVM_CONSTANT_Fieldref
                    final int shortfield = classStream.readShort();
                    final int shortfield2 = classStream.readShort();
                    classWriter.writeShort(shortfield);
                    classWriter.writeShort(shortfield2);
                    break;
                case 11: // JVM_CONSTANT_InterfaceMethodref
                    final int shortinterface = classStream.readShort();
                    final int shortinterface2 = classStream.readShort();
                    classWriter.writeShort(shortinterface);
                    classWriter.writeShort(shortinterface2);
                    break;
                case 10: // JVM_CONSTANT_Methodref
                    final int shortmethod = classStream.readShort();
                    final int shortmethod2 = classStream.readShort();
                    classWriter.writeShort(shortmethod);
                    classWriter.writeShort(shortmethod2);
                    break;
                case 12: // JVM_CONSTANT_NameAndType
                    final int shortname = classStream.readShort();
                    final int shortname2 = classStream.readShort();
                    classWriter.writeShort(shortname);
                    classWriter.writeShort(shortname2);
                    break;
                case 15: // JVM_CONSTANT_MethodHandle
                    final int bytemethod = classStream.readByte();
                    final int shortmethodhanle = classStream.readShort();
                    classWriter.writeByte(bytemethod);
                    classWriter.writeShort(shortmethodhanle);
                    break;
                case 16: // JVM_CONSTANT_MethodType
                    classWriter.writeShort(classStream.readShort());
                    break;
                case 17: // JVM_CONSTANT_Dynamic
                    final int shortdynamic = classStream.readShort();
                    final int shortdynamic2 = classStream.readShort();
                    classWriter.writeShort(shortdynamic);
                    classWriter.writeShort(shortdynamic2);
                    break;
                case 18: // JVM_CONSTANT_InvokeDynamic
                    final int shortinvoke = classStream.readShort();
                    final int shortinvoke2 = classStream.readShort();
                    classWriter.writeShort(shortinvoke);
                    classWriter.writeShort(shortinvoke2);
                    break;
                case 3: // JVM_CONSTANT_Integer
                    classWriter.write(classStream.readInt());
                    break;
                case 4: // JVM_CONSTANT_Float
                    classWriter.write(classStream.readFloat());
                    break;
                case 5: // JVM_CONSTANT_Long
                    classWriter.write(classStream.readLong());
                    ++i; // Long занимает два индекса
                    break;
                case 6: // JVM_CONSTANT_Double
                    classWriter.write(classStream.readDouble());
                    ++i; // Double занимает два индекса
                    break;
                case 1: // JVM_CONSTANT_Utf8
                    String[] utf = new String[length];

                    String readUTF = classStream.readUTF();
                    utf[i] = readUTF;
                    classWriter.writeUTF(readUTF);
                    break;
                case 19: // JVM_CONSTANT_Module
                    classWriter.writeShort(classStream.readShort());
                    break;
                case 20: // JVM_CONSTANT_Package
                    classWriter.writeShort(classStream.readShort());
                    break;
                default:
                    System.out.println("Unknown constant pool tag: " + consttag);
            }
        }
    }
}