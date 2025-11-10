package ru.kotopushka.rewriter.stream;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class ClassInputStream {
    private final DataInputStream dataInputStream;

    public ClassInputStream(final byte[] stream) {
        dataInputStream = new DataInputStream(new ByteArrayInputStream(stream));
    }

    public int readByte() {
        try {
            return dataInputStream.readByte();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int readShort() {
        try {
            return dataInputStream.readShort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int readInt() {
        try {
            return dataInputStream.readInt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public float readFloat() {
        try {
            return dataInputStream.readFloat();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long readLong() {
        try {
            return dataInputStream.readLong();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public double readDouble() {
        try {
            return dataInputStream.readDouble();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] readUTF(int length) {
        try {
            final byte[] array = new byte[length];
            dataInputStream.readFully(array);
            return array;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] readNBytes(int length) {
        try {
            return dataInputStream.readNBytes(length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] readAllBytes() {
        try {
            return dataInputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
