package ru.kotopushka.antiautistleak.obfuscator.config;

import lombok.AllArgsConstructor;
import lombok.Data;

public class Config {
    private final String nativePath;
    private final String protectionPath;
    private final Transformer[] transformers;
    private final String[] remapperPaths;

    public Config(String nativePath, String protectionPath, Transformer[] transformers, String[] remapperPaths) {
        this.nativePath = nativePath;
        this.protectionPath = protectionPath;
        this.transformers = transformers;
        this.remapperPaths = remapperPaths;
    }

    public String getNativePath() {
        return nativePath;
    }

    public String getProtectionPath() {
        return protectionPath;
    }

    public String[] getRemapperPaths() {
        return remapperPaths;
    }

    public Transformer[] getTransformers() {
        return transformers;
    }


    public static class Transformer {
        private final String name;
        private final boolean toggled;

        public Transformer(String name, boolean toggled) {
            this.name = name;
            this.toggled = toggled;
        }


        public boolean isToggled() {
            return toggled;
        }

        public String getName() {
            return name;
        }
    }

}
