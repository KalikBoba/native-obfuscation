package ru.kotopushka.rewriter.stream;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

import static ru.kotopushka.rewriter.global.GlobalConstants.PARSER_KEY;

public class ClassOutputStream {

    private final DataOutputStream dataOutputStream;

    private final ByteArrayOutputStream byteArrayOutputStream;


    public int _klass_key_1;
    public int _klass_key_2;
    public int _klass_key_3;
    public int _klass_key_4;
    public int _klass_key_5;
    public int _klass_key_6;
    public int _klass_key_7;
    public int _klass_key_8;
    public int _klass_key_9;
    public int _klass_key_10;

    public ClassOutputStream() {
        byteArrayOutputStream = new ByteArrayOutputStream();
        dataOutputStream = new DataOutputStream(new DataOutputStream(byteArrayOutputStream));
    }

    public void writeKeys() {
        _klass_key_1 = 0;//new SecureRandom().nextInt(1, 40);
        _klass_key_2 = 0;//new SecureRandom().nextInt(1, 40);
        _klass_key_3 = 0;//new SecureRandom().nextInt(1, 40);
        _klass_key_4 = 0;//new SecureRandom().nextInt(1, 40);
        _klass_key_5 =0;// new SecureRandom().nextInt(1, 40);
        _klass_key_6 = 0;//new SecureRandom().nextInt(1, 40);
        _klass_key_7 = 0;//new SecureRandom().nextInt(1, 40);
        _klass_key_8 = 0;//new SecureRandom().nextInt(1, 40);
        _klass_key_9 = 0;//new SecureRandom().nextInt(1, 40);
        _klass_key_10 = 0;//new SecureRandom().nextInt(1, 40);

        writeShort(_klass_key_1);
        writeShort(_klass_key_2);
        writeShort(_klass_key_3);
        writeShort(_klass_key_4);
        writeShort(_klass_key_5);
        writeShort(_klass_key_6);
        writeShort(_klass_key_7);
        writeShort(_klass_key_8);
        writeShort(_klass_key_9);
        writeShort(_klass_key_10);

    }


    public void writeByte(int value) {
        try {
            dataOutputStream.writeByte(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void rewriteShort(int value) {


        try {
            dataOutputStream.writeShort(value ^ 0x15);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeShort(int value) {
        try {
            dataOutputStream.writeShort(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeInt(int value) {
        try {
            dataOutputStream.writeInt(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeLong(long value) {
        try {
            dataOutputStream.writeLong(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    public void writeBytes(byte[] bytes) {
        try {
            dataOutputStream.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ByteArrayOutputStream getByteArrayOutputStream() {
        return byteArrayOutputStream;
    }
}
