#ifndef _Included_ru_kotopushka_j2c_loader_Protection
#define _Included_ru_kotopushka_j2c_loader_Protection
#ifdef __cplusplus
extern "C" {
#endif
    JNIEXPORT void JNICALL Java_ru_kotopushka_j2c_loader_Protection_initialize(JNIEnv* env, jclass klass) {
        encryption_key = "get_random_string(16)"[0];
        jclass clazz = env->FindClass(xorstr_("[Z"));
        boolean_array_class = (jclass)env->NewGlobalRef(clazz);

        methodWriter = new Writer(get_random_string(32), get_random_string(32));

//        writers = (Writer*)(malloc(sizeof(Writer)*%s));

//        data = split_string(request(xorstr_("https://meowdlc.fun/datapool")), xorstr_("<MXNEXT>"));

            %s

    }
#ifdef __cplusplus
}
#endif
#endif