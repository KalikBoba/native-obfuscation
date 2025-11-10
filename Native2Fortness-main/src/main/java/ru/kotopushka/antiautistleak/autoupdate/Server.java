package ru.kotopushka.antiautistleak.autoupdate;

import ru.kotopushka.antiautistleak.obfuscator.main.Main;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

public class Server {
    
    public static void main(String[] args) throws Exception {
        try {
            try {
                ServerSocket server;
                server = new ServerSocket(4004);
                while (true) {
                    Socket clientSocket;
                    System.out.println("Сервер запущен!");

                    clientSocket = server.accept();

                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));


                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                    DataInputStream dIn = new DataInputStream(clientSocket.getInputStream());

                    String cirk = in.readLine();

                    String[] split = cirk.split("<mx>");

                    String nativePath = split[0];

                    String name = "archives/"+split[1];

                    out.write("blyaaa\n");

                    out.flush();

                    byte[] message = read(dIn);

                    FileOutputStream input = new FileOutputStream(name);
                    input.write(Objects.requireNonNull(message));
                    input.close();

//                    Main.main(new String[]{name, name+"-out.jar", nativePath});

                    FileInputStream fileOutputStream = new FileInputStream(name+"-out.jar");

                    write(new DataOutputStream(clientSocket.getOutputStream()), fileOutputStream.readAllBytes());

                    out.flush();
                }
            } finally {
                System.out.println("Сервер закрыт!");
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void write(final DataOutputStream dataOutputStream, final byte[] message) {
        try {
            dataOutputStream.writeInt(message.length);
            dataOutputStream.write(message);
            dataOutputStream.flush();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static byte[] read(final DataInputStream dataInputStream) {
        try {
            int length = dataInputStream.readInt();
            byte[] message = new byte[length];
            if (length > 0) {
                dataInputStream.readFully(message, 0, message.length);
            }
            return message;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

}