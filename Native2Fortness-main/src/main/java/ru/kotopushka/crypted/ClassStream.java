package ru.kotopushka.crypted;


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ClassStream {
    private final DataInputStream reader;

    public ClassStream(byte[] buf) {
        this.reader = new DataInputStream(new ByteArrayInputStream(buf));
    }

    public int readByte() throws IOException {
        return reader.read();
    }

    public int readShort() throws IOException {
        return this.reader.readShort();
    }

    public int readInt() throws IOException {
        return this.reader.readInt();
    }

    public long readLong() throws IOException {
        return this.reader.readLong();
    }

    public float readFloat() throws IOException {
        return this.reader.readFloat();
    }

    public double readDouble() throws IOException {
        return this.reader.readDouble();
    }

    public String readUTF() throws IOException {
        final byte[] array = new byte[this.reader.readUnsignedShort()];
        this.reader.readFully(array);
        return new String(array, StandardCharsets.UTF_8);
    }

    public DataInputStream getReader() {
        return reader;
    }
}