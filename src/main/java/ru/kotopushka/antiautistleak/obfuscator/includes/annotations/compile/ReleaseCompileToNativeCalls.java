package ru.kotopushka.antiautistleak.obfuscator.includes.annotations.compile;

import ru.kotopushka.antiautistleak.obfuscator.includes.annotations.compile.types.VMProtect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ReleaseCompileToNativeCalls {
    VMProtect vmp = VMProtect.VIRTUALIZATION;
}