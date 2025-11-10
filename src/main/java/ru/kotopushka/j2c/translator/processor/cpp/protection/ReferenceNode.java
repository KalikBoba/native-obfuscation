package ru.kotopushka.j2c.translator.processor.cpp.protection;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import ru.kotopushka.j2c.translator.processor.cpp.protection.expressions.GenerativeExpression;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class ReferenceNode {
    private final String className;
    private String name;
    private boolean isStatic;
    private int id;
    private String signature;
    private long seed;
    long clinit;
    List<ReferenceNode> references;
    @Setter @Getter
    ReferenceNode writer;

    private GenerativeExpression generativeExpression;

    public ReferenceNode(String className, String name, String signature, boolean isStatic, int id, int meow) {
        this.className = className;
        this.name = name;
        this.isStatic = isStatic;
        this.id = id;
        this.signature = signature;
        seed = new Random().nextInt(4096*4);

        long serverSeed = (seed ^ (0x4b91a ^ (new Random(0x56854564435B34L).nextLong())));

        Random random = new Random(serverSeed);
        random.nextLong(785647534);
        random.nextLong(785447534);
        random.nextLong(78564534);
        clinit = random.nextLong();


        generativeExpression = new GenerativeExpression(random);
    }

    public ReferenceNode(String className, int id) {
        this.className = className;
        this.id = id;

        references = new CopyOnWriteArrayList<>();
    }


    public String isStatic() {
        return isStatic ? "Static" : "";
    }

    public boolean isStaticVal() {
        return isStatic;
    }

}
