#include "../U2FDevice/Controller.hpp"
#include "../U2FDevice/Storage.hpp"
#include "../U2FDevice/IO.hpp"
#include <string>
#include <unistd.h>
#include <iostream>
#include <sstream>
#include <jni.h>
#include <android/log.h>

using namespace std;

string getSave()
{
    stringstream result{};
    Storage::save(result);
    return result.str();
}

extern "C" JNIEXPORT jstring JNICALL Java_com_michaelkuc6_u2fsafe_jni_U2FDevice_handleTransactions(JNIEnv *env,
                                                                                        jclass type, jstring executableDirectory, jstring cacheDir)
{
    Controller ch{ 0xF1D00000 };

    const char* execDirCStr = env->GetStringUTFChars(executableDirectory, nullptr);
    binaryDirectory = execDirCStr;
    __android_log_print(ANDROID_LOG_DEBUG, "U2FAndroid", "Using \'%s\' as binary directory", binaryDirectory.c_str());
    env->ReleaseStringUTFChars(executableDirectory, execDirCStr);

    const char* cacheDirCStr = env->GetStringUTFChars(cacheDir, nullptr);
    cacheDirectory = cacheDirCStr;
    env->ReleaseStringUTFChars(cacheDir, cacheDirCStr);

    jclass U2FActivityClass = env->FindClass("com/michaelkuc6/u2fsafe/U2FActivity");
    jfieldID fieldID = env->GetStaticFieldID(U2FActivityClass, "shouldContinue", "Z");

    while ((env->GetStaticBooleanField(U2FActivityClass, fieldID) == JNI_TRUE))
    {
        try
        {
            ch.handleTransaction();
        }
        catch (const runtime_error &e)
        {
            __android_log_print(ANDROID_LOG_INFO, "U2FDevice", "%s", e.what());
            return env->NewStringUTF(getSave().c_str());
        }
        usleep(10000);
    }

    return env->NewStringUTF(getSave().c_str());
}
