package ru.kotopushka.antiautistleak.obfuscator.main;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.kotopushka.antiautistleak.obfuscator.pool.NemidaPool;
import ru.kotopushka.antiautistleak.obfuscator.transform.impl.jnicalls.methods.MethodMathOperationsTransformer;
import ru.kotopushka.antiautistleak.obfuscator.transform.impl.mapper.MapperTransformer;
import ru.kotopushka.antiautistleak.obfuscator.transform.impl.objector.NativeObjectTransformer;
import ru.kotopushka.antiautistleak.obfuscator.util.ClassUtil;
import ru.kotopushka.antiautistleak.obfuscator.NemidaSDK;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import ru.kotopushka.antiautistleak.obfuscator.transform.Transformer;
import ru.kotopushka.antiautistleak.obfuscator.config.Config;
import ru.kotopushka.j2c.loader.Protection;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static ru.kotopushka.antiautistleak.obfuscator.transform.impl.objector.NativeObjectTransformer.addNode;

public class Main {

//    private static final //LOGGER //LOGGER = LogManager.get//LOGGER(Main.class);

    @Setter
    private Map<String, ClassNode> classes = new ConcurrentHashMap<>();
    private final Map<String, ClassNode> originalClasses = new ConcurrentHashMap<>();
    private final Map<String, byte[]> files = new ConcurrentHashMap<>();

    private final List<Transformer> transformers = new ArrayList<>();

    private final Path input;
    private final Path output;
    private final boolean consoleDebug;
    private boolean cfm;
    @Getter
    private static Main instance;
    @Getter
    private String nativePath;
    @Getter
    private String[] remapperPaths;
    @Getter
    private String protectionPath;

    private Main(Builder builder) throws FileNotFoundException {
        if (!builder.input.toFile().exists())
            throw new FileNotFoundException(builder.input.toString());

        this.input = builder.input;
        this.output = builder.output;
        this.cfm = builder.cfm;
        this.nativePath = builder.nativePath;
        this.protectionPath = builder.protectionPath;
        this.remapperPaths = builder.remapperPaths;
        this.transformers.addAll(builder.transformers);
        this.consoleDebug = true;
        instance = this;
    }


    public boolean cfm() {
        return cfm;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void start() {
        try {
            loadInput();
//            classes.put(addNode(NemidaSDK.class).name, addNode(NemidaSDK.class));
//            classes.put(addNode(NemidaPool.class).name, addNode(NemidaPool.class));
            classes.put(addNode(Protection.class).name, addNode(Protection.class));
//            classes.put(addNode(Boostrap.class).name, addNode(Boostrap.class));
            transform(transformers);
            saveOutput();
        } catch (Exception e) {
            //LOGGER.error("Error occurred while obfuscation");
            //LOGGER.debug(e);

            if (consoleDebug)
                e.printStackTrace();
        }
    }

    private boolean isClass(String fileName, byte[] bytes) {
        return bytes.length >= 4 && String
                .format("%02X%02X%02X%02X", bytes[0], bytes[1], bytes[2], bytes[3]).equals("CAFEBABE") && (
                fileName.endsWith(".class") || fileName.endsWith(".class/"));
    }

    public static Map<String, byte[]> loadFilesFromZip(String file) {
        Map<String, byte[]> files = new HashMap<>();

        try (ZipFile zipFile = new ZipFile(file)) {
            zipFile.entries().asIterator().forEachRemaining(zipEntry -> {
                try {
                    files.put(zipEntry.getName(), zipFile.getInputStream(zipEntry).readAllBytes());
                } catch (Exception e) {
                    //LOGGER.error("Could not load ZipEntry: {}", zipEntry.getName());
                    //LOGGER.debug(e);
                }
            });
        } catch (Exception e) {
            //LOGGER.error("Could not load file: {}", file);
            //LOGGER.debug(e);
        }

        return files;
    }

    private void loadInput() {
        loadFilesFromZip(input.toString()).forEach((name, data) -> {
            try {
                if (isClass(name, data)) {
                    ClassNode classNode = ClassUtil.loadClass(data, ClassReader.EXPAND_FRAMES);
                    classes.put(classNode.name, classNode);
                    originalClasses.put(classNode.name, ClassUtil.copy(classNode)); //yes
                } else {
                    files.put(name, data);
                }
            } catch (Exception e) {
                //LOGGER.error("could not load class: {}, adding as file", name);
                //LOGGER.debug(e);
                files.put(name, data);

                if (consoleDebug)
                    e.printStackTrace();
            }
        });
    }

    public void transform(List<Transformer> transformers) {
        if (transformers == null || transformers.isEmpty())
            return;

        transformers.forEach(transformer -> {
            try {
                System.out.println(String.format("[$] %s", transformer.name()));
                transformer.transform(this);
            } catch (IOException e) {
                //LOGGER.error("! {}: {}", transformer.name(), e.getMessage());

                if (consoleDebug)
                    e.printStackTrace();
            } catch (Exception e) {
                //LOGGER.error("error occurred when transforming {}", transformer.name());
                System.out.println(e.getMessage());

                if (consoleDebug)
                    e.printStackTrace();
            }
            System.out.println("-------------------------------------\n");
        });
    }

    private void saveOutput() {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(output.toFile()))) {
            zipOutputStream.setLevel(9);

            classes.forEach((ignored, classNode) -> {
                try {
                    byte[] data = ClassUtil.classToBytes(classNode, 0);

                    if (cfm()) {

//                        ClassFileWriter classFileWriter = new ClassFileWriter();
//                        ClassFileParser classFileParser = new ClassFileParser(new ClassFileStream(data), classFileWriter);
//
//                        data = classFileWriter.getByteArrayOutputStream().toByteArray();

                    }

                    zipOutputStream.putNextEntry(new ZipEntry(classNode.name + ".class"));
                    zipOutputStream.write(data);
                } catch (Exception e) {
                    //LOGGER.error("could not save class, saving original class instead of deobfuscated: {}", classNode.name);
                    //LOGGER.debug(e);

                    if (consoleDebug)
                        e.printStackTrace();
                    try {
                        byte[] data = ClassUtil.classToBytes(originalClasses.get(classNode.name), 0);

                        zipOutputStream.putNextEntry(new ZipEntry(classNode.name + ".class"));
                        zipOutputStream.write(data);
                    } catch (Exception e2) {
                        //LOGGER.error("could not save original class: {}", classNode.name);
                        //LOGGER.debug( e2);

                        if (consoleDebug)
                            e2.printStackTrace();
                    }
                }

                originalClasses.remove(classNode.name);
                classes.remove(ignored);
            });

            files.forEach((name, data) -> {
                try {
                    zipOutputStream.putNextEntry(new ZipEntry(name));
                    zipOutputStream.write(data);
                } catch (Exception e) {
                    //LOGGER.error("could not save file: {}", name);
                    //LOGGER.debug(e);

                    if (consoleDebug)
                        e.printStackTrace();
                }

                files.remove(name);
            });
//            zipOutputStream.putNextEntry(new ZipEntry("⚜richessssstafffs⚜/AntiAutistLeak.dll"));
//            zipOutputStream.write(Files.readAllBytes(new File("native/build/lib/AALNativeLibrary.dll").toPath()));
        } catch (Exception e) {
            //LOGGER.error("could not save output file: {}", output);
            //LOGGER.debug(e);
            if (consoleDebug)
                e.printStackTrace();

        }

        System.out.println("[$] saved");
        //LOGGER.info("output file saved");
    }

    public static void main(String[] args) {
        // Путь к директории с subota.json: можно передать первым аргументом
        String basePath = (args != null && args.length > 0) ? args[0] : "C:/clients";
        File file = new File(basePath, "subota.json");

        if (!file.exists()) {
            System.err.println("Конфиг не найден: " + file.getAbsolutePath());
            System.err.println("Поместите subota.json в директорию или передайте путь как аргумент.");
            return;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            String jsonText = new String(fis.readAllBytes());
            JSONObject jsonObject = new JSONObject(jsonText);

            // считываем строки nativePath / protectionPath
            String nativePath = jsonObject.optString("native-check", null);
            String protectionPath = jsonObject.optString("protection", null);

            // transformers
            JSONObject transformersJson = jsonObject.optJSONObject("transformers");
            Config.Transformer[] transformers;
            if (transformersJson != null) {
                transformers = new Config.Transformer[transformersJson.length()];
                int index = 0;
                for (String key : transformersJson.keySet()) {
                    boolean toggled = transformersJson.getBoolean(key);
                    transformers[index++] = new Config.Transformer(key, toggled);
                }
            } else {
                transformers = new Config.Transformer[0];
            }

            // remapper-paths
            JSONArray remapperPathsJson = jsonObject.optJSONArray("remapper-paths");
            String[] remapperPaths;
            if (remapperPathsJson != null) {
                remapperPaths = new String[remapperPathsJson.length()];
                for (int i = 0; i < remapperPathsJson.length(); i++) {
                    remapperPaths[i] = remapperPathsJson.getString(i);
                }
            } else {
                remapperPaths = new String[0];
            }

            // Создаём Config (используем реальные значения из JSON)
            Config config = new Config(nativePath, protectionPath, transformers, remapperPaths);

            // Собираем список трансформеров по имени
            List<ru.kotopushka.antiautistleak.obfuscator.transform.Transformer> transformerList = new CopyOnWriteArrayList<>();
            boolean cfm = false;

            for (Config.Transformer t : config.getTransformers()) {
                String name = t.getName();
                boolean enabled = t.isToggled();

                switch (name) {
                    case "Remapper" -> {
                        if (enabled) transformerList.add(new MapperTransformer());
                    }
                    case "Opcodes" -> {
                        // пример: если у вас есть OpcodeTransformer, то раскомментируйте
                        // if (enabled) transformerList.add(new OpcodeTransformer());
                    }
                    case "NativeObjector" -> {
                        if (enabled) transformerList.add(new NativeObjectTransformer());
                    }
                    case "CFM-Encryption" -> {
                        cfm = enabled;
                    }
                    case "NativeMathOperations" -> {
                        if (enabled) transformerList.add(new MethodMathOperationsTransformer());
                    }
                    default -> {
                        // если неизвестный трансформер — логируем, но не падаем жестко
                        System.err.println("Неизвестный трансформер в конфиге: " + name + " (пропускаю)");
                    }
                }
            }

            // Строим и запускаем Main
            // ВАЖНО: меняем input/output на папку basePath (как в вашем примере)
            Path inputJar = Path.of(basePath, "input.jar");
            Path outputJar = Path.of(basePath, "1_16.jar");

            Main.builder()
                    .input(inputJar)
                    .output(outputJar)
                    .transformers(transformerList) // ✅ корректно
                    .remapperPaths(remapperPaths)
                    .cfm(cfm)
                    .nativePath(nativePath)
                    .protectionPath(protectionPath)
                    .build()
                    .start();


            System.out.println("Запуск завершён (или процесс стартовал).");

        } catch (Exception e) {
            System.err.println("Ошибка при запуске: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Collection<ClassNode> classes() {
        return classes.values();
    }

    public Map<String, ClassNode> getClasses() {
        return classes;
    }

    public static class Builder {

        private Path input;
        private Path output;
        private boolean cfm;
        private String nativePath;
        private String protectionPath;
        private String[] remapperPaths;

        private List<Transformer> transformers;


        private Builder() {
        }

        public Builder input(Path input) {
            this.input = input;
            return this;
        }

        public Builder output(Path output) {
            this.output = output;
            return this;
        }

        public Builder transformers(Transformer... transformers) {
            this.transformers = Arrays.asList(transformers);
            return this;
        }

        public Builder transformers(List<Transformer> transformers) {
            this.transformers = transformers;
            return this;
        }

        public Builder protectionPath(String protectionPath) {
            this.protectionPath = protectionPath;
            return this;
        }

        public Builder nativePath(String nativePath) {
            this.nativePath = nativePath;
            return this;
        }

        public Builder remapperPaths(String[] remapperPaths) {
            this.remapperPaths = remapperPaths;
            return this;
        }


        public Builder cfm(boolean cfm) {
            this.cfm = cfm;
            return this;
        }

        public Main build() throws FileNotFoundException {
            return new Main(this);
        }
    }

}
