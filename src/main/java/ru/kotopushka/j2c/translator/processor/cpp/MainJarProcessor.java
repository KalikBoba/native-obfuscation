package ru.kotopushka.j2c.translator.processor.cpp;

import lombok.Setter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import ru.kotopushka.VMPState;
import ru.kotopushka.j2c.translator.Compiler;
import ru.kotopushka.j2c.translator.configuration.TranslatorConfiguration;
import ru.kotopushka.j2c.translator.processor.cpp.protection.ConstantPool;
import ru.kotopushka.j2c.translator.processor.cpp.protection.ReferenceTable;
import ru.kotopushka.j2c.translator.processor.cpp.utils.translate.MethodContext;
import ru.kotopushka.j2c.translator.processor.instructions.InsnProcessorManager;
import ru.kotopushka.j2c.translator.utils.BaseUtils;
import ru.kotopushka.j2c.translator.utils.StringUtils;
import ru.kotopushka.j2c.translator.utils.clazz.ClassEntry;
import ru.kotopushka.j2c.translator.utils.clazz.parser.ClassFilter;
import ru.kotopushka.j2c.translator.processor.cpp.utils.NativeLinker;
import ru.kotopushka.j2c.loader.Protection;
import ru.kotopushka.j2c.translator.utils.resource.ResourceUtils;
import software.coley.cafedude.InvalidClassException;
import software.coley.cafedude.classfile.ClassFile;
import software.coley.cafedude.classfile.Modifiers;
import software.coley.cafedude.io.ClassFileReader;
import software.coley.cafedude.io.ClassFileWriter;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static ru.kotopushka.j2c.translator.processor.cpp.utils.translate.BaseProcessor.RETURN;
import static ru.kotopushka.j2c.translator.utils.clazz.parser.ClassFilter.NATIVE_ANNOTATION_DESC;

@Setter
public class MainJarProcessor implements Modifiers {
    private ClassFilter classFilter;

    public void process(Path inputJarPath, Path outputJarPath) throws IOException {
        StringBuilder cppCode = new StringBuilder();
        MethodProcessor methodProcessor = new MethodProcessor();

        Path outputDir;
        String outputName;

// Проверяем, что путь не null
        if (outputJarPath == null) {
            throw new IllegalArgumentException("outputJarPath cannot be null");
        }

// Если путь указывает на существующую директорию
        if (Files.exists(outputJarPath) && Files.isDirectory(outputJarPath)) {
            // используем эту директорию как outputDir и создаём внутри файл output.jar
            outputDir = outputJarPath;
            outputName = "output.jar"; // Можно поменять на любое имя
        } else {
            // Иначе путь указывает на файл
            outputDir = outputJarPath.getParent();
            outputName = outputJarPath.getFileName().toString();

            if (Files.exists(outputJarPath) && Files.isDirectory(outputJarPath)) {
                throw new IOException("Output path is a directory: " + outputJarPath);
            }
        }

// Создаём директорию, если её нет
        if (outputDir != null) {
            Files.createDirectories(outputDir);
        } else {
            // Если родительская директория null — используем текущую
            outputDir = Path.of("");
        }

// Определяем итоговый путь файла
        Path outputFile = outputDir.resolve(outputName);

// Если файл не существует — создаём его
        if (!Files.exists(outputFile)) {
            Files.createFile(outputFile);
        }

        Compiler.LOGGER.info("Output directory: {}", outputDir.toAbsolutePath());
        Compiler.LOGGER.info("Output file: {}", outputFile.toAbsolutePath());

// Создаём поддиректорию cpp внутри outputDir
        Path cppDir = outputDir.resolve("cpp");
        Files.createDirectories(cppDir);

//        BaseUtils.copyFromDisk("C:\\protection\\catjdk\\build\\windows-x86_64-server-release\\jdk\\include\\jni.h", cppDir);

        List<String> resources = List.of("assets/jni.h","assets/vmp/VMProtectSDK64.dll", "assets/vmp/VMProtectSDK32.dll", "assets/vmp/VMProtect_Ext64.dll", "assets/vmp/VMProtect_Ext32.dll", "assets/vmp/VMProtectSDK.h", "assets/jni_md.h", "assets/xorstr.h", "assets/CMakeLists.txt");
        resources.forEach(name -> {
            try {
                BaseUtils.copyResource(name, cppDir);
            } catch (IOException e) {
                Compiler.LOGGER.info("An error has occurred in copying resource {}", name, e);
            }
        });

        Files.createDirectories(Path.of("%s/build/lib".formatted(cppDir.toFile().getAbsolutePath())));

        //"assets/VMProtectSDK64.dll", "assets/VMProtectSDK64", "assets/VMProtectSDK32", "assets/VMProtectSDK32.dll", "assets/VMProtect_Ext64.dll", "assets/VMProtect_Ext32.dll"
        BaseUtils.copyBin("assets/vmp.exe", new File("%s/build/lib/".formatted(cppDir.toFile().getAbsolutePath())).toPath(), "vmp.exe");
        BaseUtils.copyBin("assets/vmp/VMProtectSDK64", cppDir, "VMProtectSDK64.lib");
        BaseUtils.copyBin("assets/vmp/VMProtectSDK32", cppDir, "VMProtectSDK32.lib");

        Path dllMainPath = cppDir.resolve("dllmain.cpp");
        Path constants = cppDir.resolve("constants.h");
        FileOutputStream fileOutputStream = new FileOutputStream(constants.toFile());
        fileOutputStream.write("""
#pragma once

#define PAD_0 0x15\s
#define PAD_1 0x15\s
#define PAD_2 0x15\s
#define PAD_3 0x15
                
""".getBytes());
// 12:56:59
// 01 2 34 5 67

        //#define PAD_0 (__TIME__[0] ^ (__TIME__[1] ^ (__LINE__) ^ 0x754D) ^ 0xe4)s
        //#define PAD_1 (__TIME__[2] ^ (__TIME__[3] ^ (__LINE__) ^ 0x74D) ^ 0x583)s
        //#define PAD_2 (((__LINE__) ^ __TIME__[4] ^ (__TIME__[2] ^ 0x15 ^ 0x7D) ^ 0x6e4) ^ __TIME__[6])s
        //#define PAD_3 (((((__LINE__ ^ __TIME__[0]) ^ __TIME__[6] ^ (__TIME__[4] ^ 0x15 ^ 0xBBB) ^ 0x6e) ^ __TIME__[6]) ^ 32))
        //

        fileOutputStream.close();

        try (BufferedWriter mainWriter = Files.newBufferedWriter(dllMainPath);
             ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(outputFile));
             JarFile jar = new JarFile(inputJarPath.toFile())) {

            mainWriter.write("\n%s".formatted(ResourceUtils.getStringFromResource("/assets/snippets/start.cpp")));
            ConstantPool constantPool = new ConstantPool();
//            addProtectionClass(out);
            processJarEntries(jar, out, cppCode, methodProcessor, constantPool);
            mainWriter.append(cppCode.toString());
            mainWriter.write("\n%s".formatted(ResourceUtils.getStringFromResource("/assets/snippets/initialization.cpp")
                    .formatted(
                            ReferenceTable.getWriterIndex()+1,
                            constantPool.code()
                    )

            ));
            Optional.ofNullable(jar.getManifest()).ifPresent(manifest -> writeManifest(out, manifest));

            try (BufferedWriter tableWriter = Files.newBufferedWriter(cppDir.resolve("table.h"))) {
                tableWriter.write(ResourceUtils.getStringFromResource("/assets/table.h"));

                tableWriter.write("%s".formatted(("#include \"constants.h\"\n" +
                                                  "#include \"xorstr.h\"\n" +
                                                  "#include <iostream>\n" +
                                                  "#include <windows.h>\n" +
                                                  "#include <wininet.h>\n" +
                                                  "#include <vector>\n" +
                                                  "#include <cstdint>\n" +
                                                  "#include <random>\n" +
                                                  "\n" +
                                                  "#pragma comment(lib, \"wininet.lib\")\n" +
                                                  "\n" +
                                                  "static int encryption_key;\n" +
                                                  "\n" +
                                                  "std::vector<char> decryptBuffer(std::vector<char>, __int64);\n" +
                                                  "\n" +
                                                  "class Writer {\n" +
                                                  "private:\n" +
                                                  "    char pad0[PAD_0];\n" +
                                                  "    std::vector<char> data;\n" +
                                                  "    char pad1[PAD_1];\n" +
                                                  "    std::vector<__int64> addresses;\n" +
                                                  "    char pad2[PAD_2];\n" +
                                                  "    std::string _hwid;\n" +
                                                  "    char pad3[PAD_3];\n" +
                                                  "    std::string _private_key;\n" +
                                                  "\n" +
                                                  "public:\n" +
                                                  "    Writer() {}\n" +
                                                  "    Writer(std::string hwid, std::string private_key) : _hwid(hwid), _private_key(private_key){}\n" +
                                                  "\n" +
                                                  "    __int64 address_at(int index) {\n" +
                                                  "        return addresses[index];\n" +
                                                  "    }\n" +
                                                  "\n" +
                                                  "    std::string hwid() {\n" +
                                                  "        return _hwid;\n" +
                                                  "    }\n" +
                                                  "    std::string private_key() {\n" +
                                                  "        return _private_key;\n" +
                                                  "    }\n" +
                                                  "\n" +
                                                  "    void writeInt64(__int64 value) {\n" +
                                                  "\n" +
                                                  "        for (size_t i = 0; i < sizeof(value); ++i) {\n" +
                                                  "            data.push_back(static_cast<char>((value >> (i * 8)) & 0xFF));\n" +
                                                  "        }\n" +
                                                  "    }\n" +
                                                  "\n" +
                                                  "    void writeString(std::string value) {\n" +
                                                  "\n" +
                                                  "        for (size_t i = 0; i < sizeof(value); ++i) {\n" +
                                                  "            data.push_back(static_cast<char>(value.c_str()[i]));\n" +
                                                  "            std::cout << std::hex << (static_cast<int>(value[i]) & 0xFF) << \"\";\n" +
                                                  "        }\n" +
                                                  "        std::cout << std::endl;\n" +
                                                  "    }\n" +
                                                  "\n" +
                                                  "    __forceinline void push() {\n" +
                                                  "\n" +
                                                  "            HINTERNET hInternet = InternetOpen(xorstr_(\"ByteSender\"), INTERNET_OPEN_TYPE_DIRECT, NULL, NULL, 0);\n" +
                                                  "            if (hInternet == NULL) {\n" +
                                                  "                std::cerr << \"InternetOpen failed: \" << GetLastError() << std::endl;\n" +
                                                  "                return;\n" +
                                                  "            }\n" +
                                                  "\n" +
                                                  "            HINTERNET hConnect = InternetConnect(hInternet, xorstr_(\"%s\"), 443, NULL, NULL, INTERNET_SERVICE_HTTP, 0, NULL);\n".formatted(TranslatorConfiguration.IMP.MAIN.URL_PROTECTION) +
                                                  "            if (hConnect == NULL) {\n" +
                                                  "                std::cerr << \"InternetConnect failed: \" << GetLastError() << std::endl;\n" +
                                                  "                InternetCloseHandle(hInternet);\n" +
                                                  "                return;\n" +
                                                  "            }\n" +
                                                  "\n" +
                                                  "            std::string url = xorstr_(\"/api/bytes?hwid=\") + _hwid + xorstr_(\"&k=\") + _private_key;\n" +
                                                  "            HINTERNET hRequest = HttpOpenRequest(hConnect, \"POST\", url.c_str(), NULL, NULL, NULL, INTERNET_FLAG_RELOAD | INTERNET_FLAG_SECURE, NULL);\n" +
                                                  "            if (hRequest == NULL) {\n" +
                                                  "                std::cerr << \"HttpOpenRequest failed: \" << GetLastError() << std::endl;\n" +
                                                  "                InternetCloseHandle(hConnect);\n" +
                                                  "                InternetCloseHandle(hInternet);\n" +
                                                  "                return;\n" +
                                                  "            }\n" +
                                                  "\n" +
                                                  "            size_t hwidLength = hwid().size();\n" +
                                                  "            size_t privateKeyLength = private_key().size();\n" +
                                                  "\n" +
                                                  "            for (size_t i = 0; i < data.size(); i++) {\n" +
                                                  "                data[i] ^= hwid()[i % hwidLength];\n" +
                                                  "                data[i] ^= private_key()[i % privateKeyLength];\n" +
                                                  "                data[i] ^= (private_key()[i % privateKeyLength] ^ (hwid()[i % hwidLength] ^ (hwidLength) ^ 0xAB) ^ 0x9A) ^ 0xAA;" +
                                                  "            }\n" +
                                                  "\n" +
                                                  "            const char* headers = xorstr_(\"Content-Type: application/octet-stream\");\n" +
                                                  "            BOOL result = HttpSendRequest(hRequest, headers, strlen(headers), (LPVOID)data.data(), data.size());\n" +
                                                  "            if (!result) {\n" +
                                                  "                std::cerr << xorstr_(\"HttpSendRequest failed: \") << GetLastError() << std::endl;\n" +
                                                  "            }\n" +
                                                  "            else {\n" +
                                                  "                //std::cout << \"Data sent successfully!\" << std::endl;\n" +
                                                  "\n" +
                                                  "                DWORD bytesRead;\n" +
                                                  "                char buffer[4096];\n" +
                                                  "                std::vector<char> responseData;\n" +
                                                  "\n" +
                                                  "                while (InternetReadFile(hRequest, buffer, sizeof(buffer), &bytesRead) && bytesRead > 0) {\n" +
                                                  "                    responseData.insert(responseData.end(), buffer, buffer + bytesRead);\n" +
                                                  "                }\n" +
                                                  "\n" +
                                                  "                responseData = decryptBuffer(responseData, ((__int64)this ^ encryption_key));\n" +
                                                  "\n" +
                                                  "                for (size_t i = 0; i < responseData.size(); i += sizeof(__int64)) {\n" +
                                                  "                    if (i + sizeof(__int64) <= responseData.size()) {\n" +
                                                  "                        __int64 value = 0;\n" +
                                                  "                        for (size_t j = 0; j < sizeof(__int64); ++j) {\n" +
                                                  "                            value |= (static_cast<__int64>(static_cast<unsigned char>((responseData[i + j]))) << (j * 8));\n" +
                                                  "                        }\n" +
                                                  "                        addresses.push_back(value);\n" +
                                                  "                    }\n" +
                                                  "                }\n" +
                                                  "            }\n" +
                                                  "\n" +
                                                  "            InternetCloseHandle(hRequest);\n" +
                                                  "            InternetCloseHandle(hConnect);\n" +
                                                  "            InternetCloseHandle(hInternet);\n" +
                                                  "        }\n" +
                                                  "};\n" +
                                                  "\n" +
                                                  "__forceinline std::vector<char> decryptBuffer(std::vector<char> buffer, __int64 writerInt64) {\n" +
                                                  "    Writer* writer = (Writer*)(writerInt64 ^ encryption_key);\n" +
                                                  "\n" +
                                                  "    for (int i = 0; i < buffer.size(); i++) {\n" +
                                                  "        buffer[i] ^= (writer->hwid()[0] ^ (writer->hwid().length() ^ (writer->private_key()[1] ^ (writer->private_key().length()) ^ writer->private_key()[2]) ^ writer->private_key()[3]) ^ writer->private_key()[4]);\n" +
                                                  "    }\n" +
                                                  "\n" +
                                                  "    return buffer;\n" +
                                                  "}\n" +
                                                  "\n" +
                                                  "std::vector<std::string> split_string(const std::string& str, const std::string& delim) {\n" +
                                                  "    std::vector<std::string> tokens;\n" +
                                                  "    size_t prev = 0, pos = 0;\n" +
                                                  "\n" +
                                                  "    do {\n" +
                                                  "        pos = str.find(delim, prev);\n" +
                                                  "        if (pos == std::string::npos) pos = str.length();\n" +
                                                  "        std::string token = str.substr(prev, pos - prev);\n" +
                                                  "        if (!token.empty()) tokens.push_back(token);\n" +
                                                  "        prev = pos + delim.length();\n" +
                                                  "\n" +
                                                  "    } while (pos < str.length() && prev < str.length());\n" +
                                                  "\n" +
                                                  "    return tokens;\n" +
                                                  "}\n")));

//                tableWriter.append("\njclass classes_notEncrypted[%s];".formatted(ReferenceTable.getClassIndex()+1));
//                tableWriter.append("\njclass classes[%s];".formatted(ReferenceTable.getClassIndex()));
//                tableWriter.append("\njmethodID methods[%s];".formatted(ReferenceTable.getMethodIndex()+1));
//                tableWriter.append("\njfieldID fields[%s];".formatted(ReferenceTable.getFieldIndex()+1));
                tableWriter.append("\nWriter* methodWriter;");
                tableWriter.append("\nWriter writers[%s];".formatted(ReferenceTable.getWriterIndex()));
            }

            mainWriter.close();

            try {
                executeCommand((new String[]{"cmake", "."}), cppDir.toFile());
                executeCommand((new String[]{"cmake", "--build", ".", "--config", "Release"}), cppDir.toFile());
                if (VMPState.vmp) executeCommand((new String[]{cppDir.toFile()+"/build/lib/vmp.exe", "AntiAutistLeak.dll", "AALProtection.dll"}), new File(cppDir.toFile()+"/build/lib/"));
            } catch (Exception e) {
                System.err.println("Error during CMake execution: " + e.getMessage());
            }

            if(!VMPState.vmp) {
                addProtectionClass(out);
            }

            out.putNextEntry(new ZipEntry("⚜richessssstafffs⚜/AntiAutistLeak.dll"));
            out.write(Files.readAllBytes(new File((VMPState.vmp ? "%s/build/lib/AALProtection.dll" : "%s/build/lib/AntiAutistLeak.dll").formatted(cppDir.toFile().getAbsolutePath())).toPath()));


        }
        Compiler.LOGGER.info("Created output file (path={})", outputJarPath.toAbsolutePath());

//        Files.copy(outputFile, Path.of("%s/bin/libs/1.16.jar".formatted(Definition.getPath())));
    }

    private List<String> createCommand(String... args) {
        List<String> command = new ArrayList<>();
        command.add("C:\\Program Files\\CMake\\bin\\cmake.exe");
        command.addAll(Arrays.asList(args));
        return command;
    }



    private void executeCommand(String[] command, File directory) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command, null, directory);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Compiler.LOGGER.info(line);
            }
        }

        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = errorReader.readLine()) != null) {
                Compiler.LOGGER.error(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Process exited with code: " + exitCode);
        }
    }

    protected static ClassNode getOwner(FieldNode f, List<ClassNode> classes) {
        return findFirst(classes, c -> c.fields.contains(f));
    }

    protected static ClassNode getOwner(MethodNode f, List<ClassNode> classes) {
        return findFirst(classes, c -> c.methods.contains(f));
    }

    public static List<ClassEntry> applyMappings(List<ClassEntry> classMap, Map<String, String> remap) {
        SimpleRemapper remapper = new SimpleRemapper(remap);
        List<ClassEntry> newClassMap = new CopyOnWriteArrayList<>();

        for (ClassEntry entry : classMap) {
            ClassNode node = entry.classNode();
            ClassNode copy = new ClassNode();
            ClassRemapper adapter = new ClassRemapper(copy, remapper);
            node.accept(adapter);
            copy.access = node.access;
            copy.sourceFile = "AntiAutistLeak";
            copy.sourceDebug = "AntiAutistLeak";
            copy.visitSource("AAL(AntiAutistLeak)", "AAL(AntiAutistLeak)");
            newClassMap.add(new ClassEntry(copy, new JarEntry(copy.name+".class"), entry.classBytes()));
        }
        classMap = newClassMap;
        return newClassMap;
    }


    private static boolean isInterface(ClassNode classNode) {
        return (classNode.access & ACC_INTERFACE) != 0;
    }

    protected static ClassNode getClassNode(String name, List <ClassEntry> classMap) {

        for (ClassEntry classEntry : classMap) {
            if (classEntry.classNode().name.equals(name)) {
                return classEntry.classNode();
            }

        }

        return null;//findFirst(classMap, c -> c.name.equals(name));
    }

    protected static <T> T findFirst(Collection < T > collection, Predicate<T> predicate) {
        for (T t: collection)
            if (predicate.test(t))
                return t;
        return null;
    }


    private static final ArrayList <String> chars = new ArrayList <>();

    protected static String getRandomName() {
        if (chars.isEmpty()) {
            String str = "ZOVZ0VZVZOVZZ0VO0";

            for (char c: str.toCharArray()) {
                chars.add(String.valueOf(c));
            }
        }
        int characters = 36;
        StringBuilder name = new StringBuilder();

        for (int i = 0; i < characters; i++) {
            String ch = chars.get(new Random().nextInt(chars.size()-4));
            name.append(ch);
        }

        return name.toString();
    }
    protected static String getRandomClassName() {
        char[] chars = new char[]{'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z', 'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};

        //        name.append("aal");
//        for (int i = 0; i < 4; i++) {
//            String ch = ""+chars[(new Random().nextInt(chars.length-2))];
//            name.append(ch).append("/");
//        }

//        name.append("d");

        return "REBORN_"+ StringUtils.getString(8)
//        name.append("d");
                ;
    }


    private void processJarEntries(JarFile jar, ZipOutputStream out, StringBuilder cppCode, MethodProcessor methodProcessor, ConstantPool constantPool) throws IOException {

        var ref = new Object() {
            final List<ClassEntry> classEntries = new CopyOnWriteArrayList<>();
        };
//        ClassNode protectionClass = addNode(Protection.class);
//        ref.classEntries.add(new ClassEntry(protectionClass, new JarEntry(protectionClass.name+".class"), classToBytes(protectionClass, ClassWriter.COMPUTE_MAXS)));

        jar.stream()
                .filter(jarEntry -> !jarEntry.getName().equals("META-INF/MANIFEST.MF"))
                .forEach(entry -> {

            if (entry.getName().endsWith(".class")) {
                try {
                    byte[] classBytes = jar.getInputStream(entry).readAllBytes();
                    if (!this.isClassFile(classBytes)) {
                        BaseUtils.writeEntry(out, entry.getName(), classBytes);
                        return;
                    }

                    ClassNode classNode = loadClassNode(classBytes);

                    ref.classEntries.add(new ClassEntry(classNode, entry, classBytes));

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("WTF");
                    System.exit(-1);
                }
            } else {
                try {
                    BaseUtils.writeEntry(jar, out, entry);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        });

//        Map<String, String> remap = new HashMap<>();
//
//        List<String> keys = ref.classEntries.stream().map(c -> c.classNode().name).collect(Collectors.toList());
//        Collections.shuffle(keys);
//
//        for (String key: keys) {
//            ClassNode cn = getClassNode(key, ref.classEntries);
//            if (canMap(Objects.requireNonNull(cn).name)) {
//                remap.put(cn.name, cn.name.contains("ru/kotopushka") || cn.name.contains("classes/Profile") || cn.name.contains("net/minecraft/client/main/Main") ? cn.name : getRandomClassName());
//            }
//        }

//        ref.classEntries = applyMappings(ref.classEntries, remap);

//        classEntries.forEach(classNode -> main.getClasses().put(classNode.name, classNode));

        ref.classEntries.forEach(classEntry -> {

            final ClassNode classNode = classEntry.classNode();

            final JarEntry entry = classEntry.jarEntry();

            try {

                if (this.shouldProcessClass(classEntry.classNode())) {

                    this.ensureClinitMethod(classNode);

                    InsnProcessorManager.process(classNode);
                    this.processClass(classNode, methodProcessor, cppCode, constantPool);
                    this.classFilter.cleanAnnotations(classNode);

                    System.out.println(entry.getName());

                    BaseUtils.writeEntry(out, entry.getName(), classToBytes(classNode, ClassWriter.COMPUTE_MAXS));

                } else {
                    BaseUtils.writeEntry(out, classEntry.jarEntry().getName(), classEntry.classBytes());
                }

            } catch (Exception e) {
                Compiler.LOGGER.error("An error occurred in processing entry {}", entry.getName(), e);
                System.out.println(e.getMessage());
            }
        });


//        jar.stream()
//                .filter(entry -> !entry.getName().equals("META-INF/MANIFEST.MF"))
//                .forEach(entry -> processEntry(jar, entry, out, cppCode, methodProcessor, constantPool));
    }
//    private void processEntry(JarFile jar, ZipEntry entry, ZipOutputStream out, StringBuilder cppCode, MethodProcessor methodProcessor, ConstantPool constantPool) {
//        try {
//            JarEntry jarEntry = (JarEntry) entry;
////            if (!entry.getName().endsWith(".class")) {
////                BaseUtils.writeEntry(jar, out, jarEntry);
////                return;
////            }
//
//            byte[] classBytes = jar.getInputStream(entry).readAllBytes();
//            if (!this.isClassFile(classBytes)) {
//                BaseUtils.writeEntry(out, entry.getName(), classBytes);
//                return;
//            }
//
////            ClassNode classNode = loadClassNode(classBytes);
////            if (!this.shouldProcessClass(classNode)) {
////                BaseUtils.writeEntry(out, entry.getName(), classBytes);
////                return;
////            }
//            this.ensureClinitMethod(classNode);
//
//            InsnProcessorManager.process(classNode);
//            this.processClass(classNode, methodProcessor, cppCode, constantPool);
//            this.classFilter.cleanAnnotations(classNode);
//
//            BaseUtils.writeEntry(out, entry.getName(), classToBytes(classNode, ClassWriter.COMPUTE_MAXS));
//        } catch (Exception ex) {
//            TranslatorMain.LOGGER.error("An error occurred in processing entry {}", entry.getName(), ex);
//        }
//    }

    private void processClass(ClassNode classNode, MethodProcessor methodProcessor, StringBuilder cppCode, ConstantPool constantPool) {
        NativeLinker linker = new NativeLinker(classNode, constantPool);
        int currentIndex = ReferenceTable.getWriterIndex();

        MethodContext context = new MethodContext(classNode, constantPool, currentIndex);

        classNode.methods.stream()
                .filter(method -> this.shouldProcessMethod(method, classNode))
                .filter(method -> {

                    boolean methodIsMarked = Optional.ofNullable(method.invisibleAnnotations)
                            .map(annotations -> annotations.stream()
            .anyMatch(annotation -> annotation.desc.equals(NATIVE_ANNOTATION_DESC)))
                            .orElse(false) || Optional.ofNullable(method.visibleAnnotations)
                            .map(annotations -> annotations.stream()
            .anyMatch(annotation -> annotation.desc.equals(NATIVE_ANNOTATION_DESC)))
                            .orElse(false);

                    return !this.classFilter.useAnnotations || (methodIsMarked || method.name.contains("$Clinit"));
                })
                .forEach(method -> methodProcessor.process(context, method, linker));
        cppCode.append(context.output().toString());
    }

    public boolean canMap(String name) {

        for (String string : TranslatorConfiguration.IMP.MAIN.MAPPINGS) {
            if (name.startsWith(string)) {
                return true;
            }
        }

        return false;
    }

    public String retransform(String name) {

        StringBuilder lol = new StringBuilder();

        for(int i = 1; i < name.length()-1; i++) {
            lol.append(name.charAt(i));
        }

        return lol.toString();

    }

    private ClassNode loadClassNode(byte[] classBytes) throws InvalidClassException {
        return loadClass(classBytes, ClassReader.EXPAND_FRAMES);
    }

    public static ClassNode loadClass(byte[] bytes, int readerMode) throws InvalidClassException {
        return loadClass(bytes, readerMode, true);
    }

    public static ClassNode loadClass(byte[] bytes, int readerMode, boolean fix) throws InvalidClassException {
        ClassNode classNode;
        try {
            classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, readerMode);
        } catch (Exception e) {
            classNode = fix ? loadClass(fixClass(bytes), readerMode, false) : null;
        }

        return classNode;
    }

    public static byte[] fixClass(byte[] bytes) throws InvalidClassException {
        ClassFileReader classFileReader = new ClassFileReader();
        ClassFile classFile = classFileReader.read(bytes);
        bytes = new ClassFileWriter().write(classFile);

        return bytes;
    }

    private void ensureClinitMethod(ClassNode classNode) {
        if (classNode.methods.stream().noneMatch(method -> method.name.equals("<clinit>"))) {
            MethodNode clinit = new MethodNode(ACC_STATIC, "<clinit>", "()V", null, null);
            clinit.instructions.add(new InsnNode(RETURN));
            classNode.methods.add(clinit);
        }
    }

    private boolean shouldProcessMethod(MethodNode method, ClassNode classNode) {
        return method.name.contains("$Clinit") || !method.name.equals("<clinit>") &&
               !method.name.equals("<init>") &&
               !BaseUtils.hasFlag(method.access, Opcodes.ACC_ABSTRACT) &&
               (!method.name.contains("_proxy") && !method.name.contains("hello")) && classFilter.shouldProcess(classNode, method);
    }

    private void writeManifest(ZipOutputStream out, Manifest manifest) {
        try {
            out.putNextEntry(new ZipEntry(JarFile.MANIFEST_NAME));
            manifest.write(out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void addProtectionClass(ZipOutputStream out) {
        try {
            ClassNode protectionClass = addNode(Protection.class);
            out.putNextEntry(new ZipEntry(protectionClass.name + ".class"));
            out.write(classToBytes(protectionClass, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static byte[] classToBytes(ClassNode classNode, int writerMode) {
        var classWriter = new ClassWriter(writerMode);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    public static ClassNode addNode(Class<?> clazz) {
        try {
            ClassReader classReader = new ClassReader(clazz.getName());
            ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);
            return classNode;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean shouldProcessClass(ClassNode classNode) {
        return classFilter.shouldProcess(classNode) &&
               classNode.methods.stream()
                       .anyMatch(method -> ClassFilter.shouldProcess(method) && classFilter.shouldProcess(classNode, method));
    }

    private boolean isClassFile(byte[] bytes) {
        return BaseUtils.byteArrayToInt(Arrays.copyOfRange(bytes, 0, 4)) == 0xCAFEBABE;
    }

    public ClassFilter getFilter() {
        return this.classFilter;
    }
}