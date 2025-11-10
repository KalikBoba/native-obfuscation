package ru.kotopushka.j2c.translator.processor.cpp.protection.expressions;

import lombok.Getter;
import ru.kotopushka.j2c.translator.processor.cpp.protection.expressions.operations.*;

import java.util.Random;

@Getter
public class GenerativeExpression extends CompositeExpression {

    private final Random random;

    private static final Class<?>[] OPERATIONS = {
            Xor.class,
            Add.class,
            Inverse.class,
            Subtract.class,
            RotateLeft.class,
            RotateRight.class,
    };

    public GenerativeExpression(Random random) {
        super(generateOperations(random.nextInt(30,100), random));
        random.nextLong();
        this.random = random;
    }

    private static AbstractOperation[] generateOperations(int operationsCount, Random random) {
        AbstractOperation[] operations = new AbstractOperation[operationsCount];
        for (int i = 0; i < operations.length; i++) {
            try {
                operations[i] = (AbstractOperation) OPERATIONS[random.nextInt(OPERATIONS.length)].newInstance();
                random.nextLong();
                if (operations[i] instanceof RotateLeft || operations[i] instanceof RotateRight) operations[i].key = (random.nextInt());
                else operations[i].key = (random.nextLong());
            } catch (InstantiationException | IllegalAccessException exception) {
                throw new RuntimeException(exception);
            }
        }
        return operations;
    }
}