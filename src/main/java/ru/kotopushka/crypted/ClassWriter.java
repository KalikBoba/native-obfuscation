package ru.kotopushka.crypted;

import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class ClassWriter {
    private final ByteArrayOutputStream stream;
    private final DataOutputStream writer;

    public ClassWriter() {
        this.stream = new ByteArrayOutputStream();
        this.writer = new DataOutputStream(stream);
    }

    @SneakyThrows
    public void writeByte(final int b) throws IOException {
        this.writer.write(b);
    }

    @SneakyThrows
    public void writeTag(int b) throws IOException {
        this.writer.writeByte(b);
    }

    @SneakyThrows
    public void writeShort(final int v) throws IOException {
        this.writer.writeShort(v);
    }

    @SneakyThrows
    public void write(final int v) throws IOException {
        this.writer.writeInt(v);
    }

    @SneakyThrows
    public void write(final long v) throws IOException {
        this.writer.writeLong(v);
    }

    @SneakyThrows
    public void write(final float v) throws IOException {
        this.writer.writeFloat(v);
    }

    @SneakyThrows
    public void write(final double v) throws IOException {
        this.writer.writeDouble(v);
    }

    @SneakyThrows
    public void writeUTF(String string) throws IOException {
        byte[] data = string.getBytes(StandardCharsets.UTF_8);
        int baseKey = (string.hashCode() ^ 0x5F37A21B) & 0xFF;

        for (int i = 0; i < data.length; i++) {
            int b = data[i] & 0xFF;

            // Step 1: XOR with position-dependent key
            b ^= (baseKey + i * 13);

            // Step 2: Add position-based offset
            b = (b + (i * 7) - 0x3C) & 0xFF;

            // Step 3: XOR with high bits shifted down
            b ^= (b >> 3);

            data[i] = (byte) b;
        }

        writer.writeByte(baseKey);
        writer.writeShort(data.length);
        writer.write(data);
    }

    @SneakyThrows
    public void writeclass(ClassWriter classWriter) throws IOException {
        writer.write(classWriter.getByteArrayOutputStream().toByteArray());
    }

    @SneakyThrows
    public void writebytes(byte[] bytes) throws IOException {
        writer.write(bytes);
    }

    public ByteArrayOutputStream getByteArrayOutputStream() {
        return this.stream;
    }
}