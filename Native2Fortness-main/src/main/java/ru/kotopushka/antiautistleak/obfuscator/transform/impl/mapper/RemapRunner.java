package ru.kotopushka.antiautistleak.obfuscator.transform.impl.mapper;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.JarEntry;
import java.util.regex.Pattern;

/**
 * Продвинутый ремапер с автоматическим поиском main-метода и ремапом полей
 */
public class RemapRunner {

    private static final String INPUT_PATH  = "input.jar";
    private static final String OUTPUT_PATH = "output.jar";

    public static void main(String[] args) throws Exception {
        Path inputJar = Path.of(INPUT_PATH);
        Path outputJar = Path.of(OUTPUT_PATH);

        if (!Files.exists(inputJar)) {
            System.err.println("Input JAR not found: " + inputJar);
            return;
        }

        // Читаем все entries
        List<ClassNode> classNodes = new ArrayList<>();
        Map<String, byte[]> otherEntries = new LinkedHashMap<>();
        String mainClassName = null;

        try (JarFile jf = new JarFile(inputJar.toFile())) {
            Enumeration<JarEntry> entries = jf.entries();
            while (entries.hasMoreElements()) {
                JarEntry je = entries.nextElement();
                try (InputStream is = jf.getInputStream(je)) {
                    byte[] data = is.readAllBytes();
                    String name = je.getName();
                    if (name.endsWith(".class")) {
                        ClassReader cr = new ClassReader(data);
                        ClassNode cn = new ClassNode();
                        cr.accept(cn, 0);
                        classNodes.add(cn);

                        // Проверяем наличие main метода
                        if (mainClassName == null && hasMainMethod(cn)) {
                            mainClassName = cn.name;
                        }
                    } else {
                        otherEntries.put(name, data);
                    }
                }
            }
        }

        System.out.println("=== Remapper Started ===");
        System.out.println("Loaded classes: " + classNodes.size());
        System.out.println("Loaded resources: " + otherEntries.size());

        if (mainClassName != null) {
            System.out.println("Found Main-Class: " + mainClassName);
        } else {
            System.out.println("WARNING: Main-Class not found!");
        }

        // Создаём mappings для классов
        Map<String, String> classRemap = new HashMap<>();
        List<String> classNames = new ArrayList<>();

        for (ClassNode cn : classNodes) {
            classNames.add(cn.name);
        }

        Collections.shuffle(classNames);

        for (String original : classNames) {
            if (shouldMapClass(original)) {
                String newName = MapperTransformer.getRandomClassName();
                while (classRemap.containsValue(newName)) {
                    newName = MapperTransformer.getRandomClassName();
                }
                classRemap.put(original, newName);
            }
        }

        System.out.println("Class remaps: " + classRemap.size());

        // Создаём mappings для полей
        Map<String, Map<String, String>> fieldRemap = new HashMap<>();

        for (ClassNode cn : classNodes) {
            if (shouldMapClass(cn.name)) {
                Map<String, String> fields = new HashMap<>();

                if (cn.fields != null) {
                    for (FieldNode fn : cn.fields) {
                        if (shouldMapField(fn.name)) {
                            String newFieldName = getRandomFieldName();
                            while (fields.containsValue(newFieldName)) {
                                newFieldName = getRandomFieldName();
                            }
                            fields.put(fn.name, newFieldName);
                        }
                    }
                }

                if (!fields.isEmpty()) {
                    fieldRemap.put(cn.name, fields);
                }
            }
        }

        int totalFieldRemaps = fieldRemap.values().stream()
                .mapToInt(Map::size)
                .sum();
        System.out.println("Field remaps: " + totalFieldRemaps);

        // Применяем mappings
        List<ClassNode> renamed = applyMappings(classNodes, classRemap, fieldRemap);

        // Подготовка output
        if (outputJar.getParent() != null) {
            Files.createDirectories(outputJar.getParent());
        }

        // Определяем новое имя main-класса для манифеста
        String newMainClassName = mainClassName != null && classRemap.containsKey(mainClassName)
                ? classRemap.get(mainClassName)
                : mainClassName;

        // Записываем результат
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(outputJar.toFile()))) {
            // Обрабатываем манифест
            if (otherEntries.containsKey("META-INF/MANIFEST.MF")) {
                byte[] manBytes = otherEntries.remove("META-INF/MANIFEST.MF");
                String manifest = new String(manBytes);

                System.out.println("=== Original Manifest ===");
                System.out.println(manifest);
                System.out.println("=========================");

                // Обновляем Main-Class в манифесте
                if (newMainClassName != null && mainClassName != null) {
                    String oldMainClass = mainClassName.replace("/", ".");
                    String newMainClass = newMainClassName.replace("/", ".");

                    System.out.println("Replacing: " + oldMainClass + " -> " + newMainClass);

                    // Ищем и заменяем Main-Class (с учетом разных вариантов написания)
                    if (manifest.contains("Main-Class")) {
                        // Заменяем любое упоминание старого класса после Main-Class:
                        manifest = manifest.replaceAll(
                                "(Main-Class:)\\s*" + Pattern.quote(oldMainClass) + "([\\s\\r\\n])",
                                "$1 " + newMainClass + "$2"
                        );
                        System.out.println("Updated Main-Class: " + oldMainClass + " -> " + newMainClass);
                    } else {
                        // Если Main-Class не найден, добавляем его
                        if (!manifest.trim().endsWith("\n")) {
                            manifest += "\r\n";
                        }
                        manifest += "Main-Class: " + newMainClass + "\r\n";
                        System.out.println("Added Main-Class: " + newMainClass);
                    }

                    System.out.println("=== Updated Manifest ===");
                    System.out.println(manifest);
                    System.out.println("========================");

                    manBytes = manifest.getBytes();
                } else if (newMainClassName != null) {
                    // Если манифест есть, но Main-Class не был найден ранее
                    if (!manifest.contains("Main-Class:")) {
                        if (!manifest.trim().endsWith("\n")) {
                            manifest += "\r\n";
                        }
                        manifest += "Main-Class: " + newMainClassName.replace("/", ".") + "\r\n";
                        manBytes = manifest.getBytes();
                        System.out.println("Added Main-Class: " + newMainClassName.replace("/", "."));
                    }
                }

                JarEntry me = new JarEntry("META-INF/MANIFEST.MF");
                jos.putNextEntry(me);
                jos.write(manBytes);
                jos.closeEntry();
            } else if (newMainClassName != null) {
                // Если манифеста вообще не было, создаём новый
                String manifest = "Manifest-Version: 1.0\r\n" +
                        "Main-Class: " + newMainClassName.replace("/", ".") + "\r\n";

                JarEntry me = new JarEntry("META-INF/MANIFEST.MF");
                jos.putNextEntry(me);
                jos.write(manifest.getBytes());
                jos.closeEntry();
                System.out.println("Created manifest with Main-Class: " + newMainClassName.replace("/", "."));
            }

            // Записываем классы
            for (ClassNode cn : renamed) {
                org.objectweb.asm.ClassWriter cw = new org.objectweb.asm.ClassWriter(0);
                cn.accept(cw);
                byte[] classBytes = cw.toByteArray();
                String entryName = cn.name + ".class";
                JarEntry e = new JarEntry(entryName);
                jos.putNextEntry(e);
                jos.write(classBytes);
                jos.closeEntry();
            }

            // Записываем остальные ресурсы
            for (Map.Entry<String, byte[]> en : otherEntries.entrySet()) {
                JarEntry e = new JarEntry(en.getKey());
                jos.putNextEntry(e);
                jos.write(en.getValue());
                jos.closeEntry();
            }
        }

        System.out.println("=== Remapping Complete ===");
        System.out.println("Output: " + outputJar.toAbsolutePath());
    }

    /**
     * Проверяет наличие main метода: public static void main(String[] args)
     */
    private static boolean hasMainMethod(ClassNode cn) {
        if (cn.methods == null) return false;

        for (MethodNode mn : cn.methods) {
            // Правильная сигнатура: ([Ljava/lang/String;)V
            if ("main".equals(mn.name)
                    && "([Ljava/lang/String;)V".equals(mn.desc)
                    && (mn.access & 0x0009) == 0x0009) { // public + static
                return true;
            }
        }
        return false;
    }

    /**
     * Определяет, нужно ли ремапить класс
     */
    private static boolean shouldMapClass(String className) {
        if (className == null) return false;

        // Не трогаем системные пакеты
        if (className.startsWith("java/")) return false;
        if (className.startsWith("javax/")) return false;
        if (className.startsWith("sun/")) return false;
        if (className.startsWith("jdk/")) return false;
        if (className.startsWith("com/sun/")) return false;

        // Не трогаем библиотеки обфускации
        if (className.startsWith("org/objectweb/asm/")) return false;
        if (className.startsWith("kotlin/")) return false;
        if (className.startsWith("kotlinx/")) return false;

        // Можно добавить свои исключения
        // if (className.startsWith("ru/kotopushka")) return false;

        return true;
    }

    /**
     * Определяет, нужно ли ремапить поле
     */
    private static boolean shouldMapField(String fieldName) {
        if (fieldName == null) return false;

        // Не трогаем спецполя
        if (fieldName.startsWith("this$")) return false;
        if (fieldName.startsWith("val$")) return false;
        if (fieldName.equals("serialVersionUID")) return false;
        if (fieldName.equals("INSTANCE")) return false; // Kotlin singleton

        return true;
    }

    /**
     * Генерирует случайное имя поля
     */
    private static String getRandomFieldName() {
        Random r = new Random();
        int len = 3 + r.nextInt(8); // 3-10 символов
        StringBuilder sb = new StringBuilder();

        // Первый символ - буква
        sb.append((char)('a' + r.nextInt(26)));

        // Остальные - буквы или цифры
        for (int i = 1; i < len; i++) {
            if (r.nextBoolean()) {
                sb.append((char)('a' + r.nextInt(26)));
            } else {
                sb.append((char)('0' + r.nextInt(10)));
            }
        }

        return sb.toString();
    }

    /**
     * Применяет маппинги классов и полей
     */
    private static List<ClassNode> applyMappings(
            List<ClassNode> classes,
            Map<String, String> classRemap,
            Map<String, Map<String, String>> fieldRemap) {

        // Создаём новый список для обработанных классов
        List<ClassNode> result = new ArrayList<>();

        for (ClassNode cn : classes) {
            ClassReader cr = new ClassReader(getClassBytes(cn));
            ClassNode newNode = new ClassNode();

            // Используем RemappingClassAdapter для применения маппингов
            org.objectweb.asm.commons.ClassRemapper remapper =
                    new org.objectweb.asm.commons.ClassRemapper(newNode,
                            new CustomRemapper(classRemap, fieldRemap));

            cr.accept(remapper, 0);
            result.add(newNode);
        }

        return result;
    }

    /**
     * Получает байткод класса
     */
    private static byte[] getClassBytes(ClassNode cn) {
        org.objectweb.asm.ClassWriter cw = new org.objectweb.asm.ClassWriter(0);
        cn.accept(cw);
        return cw.toByteArray();
    }

    /**
     * Кастомный Remapper для обработки классов и полей
     */
    private static class CustomRemapper extends org.objectweb.asm.commons.Remapper {
        private final Map<String, String> classMap;
        private final Map<String, Map<String, String>> fieldMap;

        public CustomRemapper(Map<String, String> classMap, Map<String, Map<String, String>> fieldMap) {
            this.classMap = classMap;
            this.fieldMap = fieldMap;
        }

        @Override
        public String map(String internalName) {
            return classMap.getOrDefault(internalName, internalName);
        }

        @Override
        public String mapFieldName(String owner, String name, String descriptor) {
            // Сначала смотрим оригинальное имя owner
            Map<String, String> fields = fieldMap.get(owner);
            if (fields != null && fields.containsKey(name)) {
                return fields.get(name);
            }

            // Если owner был ремапнут, пробуем найти по новому имени
            String mappedOwner = classMap.get(owner);
            if (mappedOwner != null) {
                fields = fieldMap.get(mappedOwner);
                if (fields != null && fields.containsKey(name)) {
                    return fields.get(name);
                }
            }

            return name;
        }
    }
}