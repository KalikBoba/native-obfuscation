package ru.kotopushka.j2c.translator.utils.resource;

import ru.kotopushka.j2c.translator.Compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ResourceUtils {
    public static String getStringFromResource(String resourcePath) throws IOException {
        InputStream inputStream = ResourceUtils.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            Compiler.LOGGER.error("Resource not found (path={})", resourcePath);
            return "";
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        }
        return content.toString();
    }

}
