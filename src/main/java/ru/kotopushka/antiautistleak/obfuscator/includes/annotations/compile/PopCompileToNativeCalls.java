package ru.kotopushka.antiautistleak.obfuscator.includes.annotations.compile;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
public @interface PopCompileToNativeCalls {
}
