#include <jni.h>
#include <sstream>
#include "../U2FDevice/Storage.hpp"

using namespace std;

extern "C" {
    JNIEXPORT void JNICALL Java_com_michaelkuc6_u2fandroid_jni_Storage_init(JNIEnv *env, jclass, jstring keys) {
        const char *path = env->GetStringUTFChars(keys, nullptr);

        stringstream keyStrStream{string{path}};

        Storage::init(keyStrStream);

        env->ReleaseStringUTFChars(keys, path);
    }
}

