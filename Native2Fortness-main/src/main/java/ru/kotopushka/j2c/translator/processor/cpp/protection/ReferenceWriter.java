package ru.kotopushka.j2c.translator.processor.cpp.protection;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ReferenceWriter {
//    List<ReferenceNode> classes;
//    List<ReferenceNode> methods;
//    List<ReferenceNode> fields;

    public ReferenceWriter() {
//        classes = new CopyOnWriteArrayList<>();
//        methods = new CopyOnWriteArrayList<>();
//        fields = new CopyOnWriteArrayList<>();
    }

}