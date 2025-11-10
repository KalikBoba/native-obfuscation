package ru.kotopushka.j2c.translator.processor.cpp.protection.vmp;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.objectweb.asm.tree.AbstractInsnNode;

@Data
public class InstructionRecordHandler {
    @Setter
    @Getter
    private static AbstractInsnNode lastOpcode = null;
}
