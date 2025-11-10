package ru.kotopushka.j2c.translator.processor.cpp.protection;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import ru.kotopushka.j2c.translator.processor.cpp.protection.expressions.GenerativeExpression;
import ru.kotopushka.j2c.translator.utils.RandomUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static ru.kotopushka.j2c.translator.processor.cpp.impl.constant.LdcProcessor.escapeString;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConstantPool {
    private final List<String> utf8;
    private final FileOutputStream fileOutputStream;
    private int arrayIndex;
    final List<Method> methods;
    final StringBuilder codeBuilder;
    int methodsIndex;

    @Data
    public static class Method {
        private GenerativeExpression generativeExpression;
        private String name;
        private long seed;
        private long key;
        private int index;

        public Method(String name, int index) {
            this.name = name;
            this.index = index;
            seed = ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);

            seed = new Random().nextLong();

            long serverSeed = (seed ^ (0x4b91a ^ (new Random(0x56854564435B34L).nextLong())));

            Random random = new Random(serverSeed);
            random.nextLong(785647534);
            random.nextLong(785447534);
            random.nextLong(78564534);
            key = random.nextLong();


            generativeExpression = new GenerativeExpression(random);


            generativeExpression = new GenerativeExpression(random);
        }

    }

    public ConstantPool() {
        utf8 = new CopyOnWriteArrayList<>();

        try {
            fileOutputStream = new FileOutputStream("section.data");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        arrayIndex = 0;
        codeBuilder = new StringBuilder();
        methods = new CopyOnWriteArrayList<>();

    }

    public String compileNativeMethod(String name) {

//        if (true) return name;

        Method method = new Method(name, methodsIndex++);

        String snippet = "((__int64)methodWriter->address_at(%s))".formatted(
                method.getIndex()
        );

        snippet = "(void*)((%s ^ methodWriter->private_key()[1] ^ methodWriter->hwid()[5]))"
                .formatted(
                        method.getGenerativeExpression().format(snippet)
                );


        codeBuilder.append("""
                    
                            { // linking-method reference
                    
                                    methodWriter->writeInt64((__int64)%s ^ %s);
                                    methodWriter->writeInt64(%s /*^ methodWriter->hwid()[0] ^ methodWriter->private_key()[2]*/);
                    
                            }
                    
                    """.formatted(
                name,
                method.getKey(),
                method.getSeed()
        ));

        return snippet;
    }


    public String code() {
        return codeBuilder.toString();
    }

    public String pushInt(String utf) {

        try {
            fileOutputStream.write(utf.getBytes());
            fileOutputStream.write("<MXNEXT>".getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "\""+escapeString(utf)+"\"";//"std::stoi(data[%s])".formatted(arrayIndex++);
    }

    public String pushUtf(String utf) {

        try {
            fileOutputStream.write((utf).getBytes());
            fileOutputStream.write("<MXNEXT>".getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "\""+escapeString(utf)+"\"";//"data[%s].c_str()".formatted(arrayIndex++);

    }

}
