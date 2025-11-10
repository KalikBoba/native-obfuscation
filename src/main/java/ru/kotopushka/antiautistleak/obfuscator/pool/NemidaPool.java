package ru.kotopushka.antiautistleak.obfuscator.pool;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NemidaPool {
    private List<Object> pool;

    public NemidaPool(){
        pool = new ArrayList<>();
    }

}
