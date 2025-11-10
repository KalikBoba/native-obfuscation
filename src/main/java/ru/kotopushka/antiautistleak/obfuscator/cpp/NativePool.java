package ru.kotopushka.antiautistleak.obfuscator.cpp;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.kotopushka.antiautistleak.obfuscator.util.model.ValueModel;

import java.util.ArrayList;
import java.util.List;

@Data
//@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NativePool {
    List<ValueModel<String>> strings;
    List<ValueModel<Integer>> integers;
    List<ValueModel<Double>> doubles;
    private List<String> stringsOld;
    private List<Integer> intsOld;
    private List<Double> doublesOld;
    private int intLength;
    private int startPoint;

    public NativePool() {
        strings = new ArrayList<>();
        integers = new ArrayList<>();
        doubles = new ArrayList<>();
        stringsOld = new ArrayList<>();
        intsOld = new ArrayList<>();
        doublesOld = new ArrayList<>();
        intLength = 0;
    }

    public int getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(int startPoint) {
        this.startPoint = startPoint;
    }

    public void setIntLength(int intLength) {
        this.intLength = intLength;
    }

    public int getIntLength() {
        return intLength;
    }

    public void write(Object string) {
        stringsOld.add((String) string);
    }

    public void write(String string) {
        stringsOld.add(string);
    }

    public void write(double string) {
        doublesOld.add(string);
    }

    public void write(int string) {
        intsOld.add(string);
    }

    public List<Double> getDoublesOld() {
        return doublesOld;
    }

    public List<String> getStringsOld() {
        return stringsOld;
    }

    public List<Integer> getIntsOld() {
        return intsOld;
    }

    public List<ValueModel<String>> getStrings() {
		return strings;
	}
    
    public List<ValueModel<Integer>> getIntegers() {
		return integers;
	}
    
    public List<ValueModel<Double>> getDoubles() {
		return doubles;
	}

    
    public void writeString(ValueModel<String> model) {
        strings.add(model);
    }
    
    public void writeDouble(ValueModel<Double> model) {
        doubles.add(model);
    }
    
    public void writeInteger(ValueModel<Integer> model) {
        integers.add(model);
    }
}
