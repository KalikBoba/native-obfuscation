package ru.kotopushka.j2c;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.jar.*;
import java.util.zip.ZipEntry;

public class ReleaseCompileToNativeCalls {

    public static void main(String[] args) {
        String inputJar = "input.jar";
        String outputJar = "output.jar";

        try {
            Path tempDir = Files.createDirectory(Paths.get("temp_release_native"));

            // Распаковываем input.jar
            unzipJar(inputJar, tempDir);

            // Создаём аннотации
            createAnnotationFiles(tempDir.toFile());

            // Упаковываем всё обратно в output.jar
            zipJar(tempDir, outputJar);

            // Удаляем временные файлы
            deleteDirectory(tempDir);

            System.out.println("✅ Аннотации PopCompileToNativeCalls добавлены и сохранены в " + outputJar);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void unzipJar(String jarPath, Path outputDir) throws IOException {
        try (JarFile jar = new JarFile(jarPath)) {
            jar.stream().forEach(entry -> {
                try {
                    Path outPath = outputDir.resolve(entry.getName());
                    if (entry.isDirectory()) {
                        Files.createDirectories(outPath);
                    } else {
                        Files.createDirectories(outPath.getParent());
                        try (InputStream is = jar.getInputStream(entry)) {
                            Files.copy(is, outPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }

    private static void zipJar(Path sourceDir, String jarPath) throws IOException {
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarPath))) {
            Files.walk(sourceDir)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        String entryName = sourceDir.relativize(path).toString().replace("\\", "/");
                        try (InputStream is = Files.newInputStream(path)) {
                            jos.putNextEntry(new ZipEntry(entryName));
                            is.transferTo(jos);
                            jos.closeEntry();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }
    }

    private static void createAnnotationFiles(File baseDir) throws IOException {
        File dir = new File(baseDir, "ru/kotopushka/j2c/sdk/annotations");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Не удалось создать директорию: " + dir.getAbsolutePath());
        }

        // ReleaseCompileToNativeCalls
        writeFile(new File(dir, "ReleaseCompileToNativeCalls.java"),
                "package ru.kotopushka.j2c.sdk.annotations;\n\n" +
                        "import java.lang.annotation.Retention;\n" +
                        "import java.lang.annotation.RetentionPolicy;\n\n" +
                        "@Retention(RetentionPolicy.RUNTIME)\n" +
                        "public @interface ReleaseCompileToNativeCalls {}");

        // PopCompileToNativeCalls
        writeFile(new File(dir, "PopCompileToNativeCalls.java"),
                "package ru.kotopushka.j2c.sdk.annotations;\n\n" +
                        "import java.lang.annotation.Retention;\n" +
                        "import java.lang.annotation.RetentionPolicy;\n\n" +
                        "@Retention(RetentionPolicy.RUNTIME)\n" +
                        "public @interface PopCompileToNativeCalls {}");

        // VMProtect (оставим для совместимости)
        writeFile(new File(dir, "VMProtect.java"),
                "package ru.kotopushka.j2c.sdk.annotations;\n\n" +
                        "import ru.kotopushka.j2c.sdk.enums.VMProtectType;\n" +
                        "import java.lang.annotation.Retention;\n" +
                        "import java.lang.annotation.RetentionPolicy;\n\n" +
                        "@Retention(RetentionPolicy.RUNTIME)\n" +
                        "public @interface VMProtect {\n" +
                        "    VMProtectType type();\n" +
                        "}");

        // Компиляция аннотаций
        compileAnnotations(dir);

        // Инжектируем PopCompileToNativeCalls во все классы
        injectPopCompileAnnotation(baseDir);
    }

    private static void injectPopCompileAnnotation(File dir) throws IOException {
        if (!dir.isDirectory()) return;
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                injectPopCompileAnnotation(file);
            } else if (file.getName().endsWith(".class")) {
                byte[] classBytes = Files.readAllBytes(file.toPath());
                ClassNode classNode = new ClassNode();
                ClassReader reader = new ClassReader(classBytes);
                reader.accept(classNode, 0);

                List<MethodNode> methods = classNode.methods;
                for (MethodNode method : methods) {
                    if (method.visibleAnnotations == null) {
                        method.visibleAnnotations = new java.util.ArrayList<>();
                    }
                    AnnotationNode annotationNode = new AnnotationNode("Lru/kotopushka/j2c/sdk/annotations/ReleaseNativeAuth;");
                    method.visibleAnnotations.add(annotationNode);
                }

                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                classNode.accept(writer);
                Files.write(file.toPath(), writer.toByteArray());
            }
        }
    }

    private static void compileAnnotations(File dir) throws IOException {
        File[] javaFiles = dir.listFiles((d, name) -> name.endsWith(".java"));
        if (javaFiles == null || javaFiles.length == 0) return;

        String[] files = new String[javaFiles.length];
        for (int i = 0; i < javaFiles.length; i++) files[i] = javaFiles[i].getAbsolutePath();

        javax.tools.JavaCompiler compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
        int result = compiler.run(null, null, null, files);
        if (result != 0) {
            throw new IOException("Не удалось скомпилировать аннотации");
        }
    }

    private static void writeFile(File file, String content) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }

    private static void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {}
                    });
        }
    }
}
