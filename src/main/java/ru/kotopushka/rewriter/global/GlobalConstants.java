package ru.kotopushka.rewriter.global;

import ru.kotopushka.j2c.translator.configuration.TranslatorConfiguration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;

public class GlobalConstants {
    public static int PARSER_KEY;
    public static int _CONSTANT_POOL_KEY_1;
    public static int _CONSTANT_POOL_KEY_2;
    public static int _CONSTANT_POOL_KEY_3;
    public static int _CONSTANT_POOL_KEY_4;
    public static int _CONSTANT_POOL_KEY_5;
    public static int _CONSTANT_POOL_KEY_6;

    public static String getHTML(String urlToRead) {
        try {
            StringBuilder result = new StringBuilder();
            URL url = new URL(urlToRead);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                for (String line; (line = reader.readLine()) != null;) {
                    result.append(line);
                }
            }
            return result.toString();
        } catch (Exception e) {
            System.out.println("WOW");
            return "";
        }
    }

    static {
        String path = "%s/setncryption?key="
                .formatted(TranslatorConfiguration.IMP.MAIN.URL_PROTECTION);
        String path_cp = "%s/setConstantPool?key="
                .formatted(TranslatorConfiguration.IMP.MAIN.URL_PROTECTION);;

        PARSER_KEY = new SecureRandom().nextInt(1, 40);
        _CONSTANT_POOL_KEY_1 = new SecureRandom().nextInt(1, 40);
        _CONSTANT_POOL_KEY_2 = new SecureRandom().nextInt(1, 40);
        _CONSTANT_POOL_KEY_3 = new SecureRandom().nextInt(1, 40);
        _CONSTANT_POOL_KEY_4 = new SecureRandom().nextInt(1, 40);
        _CONSTANT_POOL_KEY_5 = new SecureRandom().nextInt(1, 40);
        _CONSTANT_POOL_KEY_6 = new SecureRandom().nextInt(1, 40);


        try {
            System.out.println(getHTML(path + PARSER_KEY));
            System.out.println(getHTML(path_cp + _CONSTANT_POOL_KEY_1 + "&index=" + 1));
            System.out.println(getHTML(path_cp + _CONSTANT_POOL_KEY_2 + "&index=" + 2));
            System.out.println(getHTML(path_cp + _CONSTANT_POOL_KEY_3 + "&index=" + 3));
            System.out.println(getHTML(path_cp + _CONSTANT_POOL_KEY_4 + "&index=" + 4));
            System.out.println(getHTML(path_cp + _CONSTANT_POOL_KEY_5 + "&index=" + 5));
            System.out.println(getHTML(path_cp + _CONSTANT_POOL_KEY_6 + "&index=" + 6));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
