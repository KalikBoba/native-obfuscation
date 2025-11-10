package ru.kotopushka.j2c.sdk.classes;

import ru.kotopushka.j2c.sdk.annotations.NativeExclude;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;
import ru.kotopushka.j2c.sdk.annotations.VMProtect;
import ru.kotopushka.j2c.sdk.enums.VMProtectType;

@VMProtect(type = VMProtectType.ULTRA)
@NativeInclude
public class Profile {

    private static String username = System.getenv("username");

    private static int uid = 1;

    private static String expire = "2038-06-06";

    private static String role = "Разработчик";

    @NativeExclude
    public static String getUsername() {
        return username;
    }
    @NativeExclude
    public static int getUid() {
        return uid;
    }
    @NativeExclude
    public static String getExpire() {
        return expire;
    }
    @NativeExclude
    public static String getRole() {
        return role;
    }
}
