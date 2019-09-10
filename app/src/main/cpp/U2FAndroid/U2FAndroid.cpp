#include "../U2FDevice/Controller.hpp"
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

extern "C" JNIEXPORT jboolean JNICALL
Java_com_michaelkuc6_u2fandroid_jni_U2FDevice_handleTransaction(JNIEnv* env, jclass type) {
	static Controller ch{ 0xF1D00000 };

	try {
		ch.handleTransaction();
	} catch (const runtime_error& e) {
		__android_log_print(ANDROID_LOG_INFO, "U2FDevice", "%s", e.what());
		return static_cast<jboolean>(false);
	}

	return static_cast<jboolean>(true);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_michaelkuc6_u2fandroid_jni_U2FDevice_getResult(JNIEnv *env, jclass clazz) {
	return env->NewStringUTF(getSave().c_str());
}
