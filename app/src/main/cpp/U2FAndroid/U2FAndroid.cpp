#include "../U2FDevice/Architecture.hpp"
#include "../U2FDevice/Controller.hpp"
#include "../U2FDevice/IO.hpp"
#include "../U2FDevice/Storage.hpp"
#include <android/log.h>
#include <iostream>
#include <jni.h>
#include <sstream>
#include <string>
#include <unistd.h>

using namespace std;

string getSave() {
	stringstream result{};
	Storage::save(result);
	return result.str();
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_michaelkuc6_u2fandroid_jni_U2FDevice_handleTransactions(JNIEnv* env, jclass type,
                                                                 jstring serverSocketPath,
                                                                 jstring clientSocketPath,
                                                                 jstring cacheDir) {
	Controller ch{ 0xF1D00000 };

	const char* serverSocketPathCStr = env->GetStringUTFChars(serverSocketPath, nullptr);
	hidDev = serverSocketPathCStr;
	env->ReleaseStringUTFChars(serverSocketPath, serverSocketPathCStr);

	const char* clientSocketPathCStr = env->GetStringUTFChars(clientSocketPath, nullptr);
	clientSocket = clientSocketPathCStr;
	env->ReleaseStringUTFChars(clientSocketPath, clientSocketPathCStr);

#ifdef DEBUG_STREAMS
	const char* cacheDirCStr = env->GetStringUTFChars(cacheDir, nullptr);
	cacheDirectory = cacheDirCStr;
	env->ReleaseStringUTFChars(cacheDir, cacheDirCStr);
#endif

	jclass U2FActivityClass = env->FindClass("com/michaelkuc6/u2fandroid/U2FActivity");
	jfieldID fieldID = env->GetStaticFieldID(U2FActivityClass, "shouldContinue", "Z");

	while ((env->GetStaticBooleanField(U2FActivityClass, fieldID) == JNI_TRUE)) {
		try {
			ch.handleTransaction();
		} catch (const runtime_error& e) {
			__android_log_print(ANDROID_LOG_INFO, "U2FDevice", "%s", e.what());
			return env->NewStringUTF(getSave().c_str());
		}
		usleep(10000);
	}

	return env->NewStringUTF(getSave().c_str());
}
