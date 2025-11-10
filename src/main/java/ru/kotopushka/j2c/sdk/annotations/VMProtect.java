package ru.kotopushka.j2c.sdk.annotations;

import ru.kotopushka.j2c.sdk.enums.VMProtectType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface VMProtect {
    VMProtectType type();
}
