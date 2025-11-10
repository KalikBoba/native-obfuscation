package ru.kotopushka.antiautistleak.obfuscator.util.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.objectweb.asm.tree.ClassNode;

@Getter
@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ValueModel<T> {
    final ClassNode classNode;
	final String name;
	final T value;
    @Setter
    boolean handshake;
	@Setter
    int index;
    
    public ValueModel(ClassNode classNode, String name, T value) {
    	this.classNode = classNode;
    	this.name = name;
    	this.value = value;
    }

	public ValueModel(ClassNode classNode, String name, T value, int index) {
		this.classNode = classNode;
		this.name = name;
		this.value = value;
		this.index = index;
	}

}