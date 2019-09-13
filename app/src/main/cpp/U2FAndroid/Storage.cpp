#include "../U2FDevice/Architecture.hpp"
#include "../U2FDevice/IO.hpp"
#include "../U2FDevice/Storage.hpp"
#include "../U2FDevice/Streams.hpp"
#include "Storage.hpp"
#include <jni.h>
#include <sstream>

using namespace std;

extern "C"
JNIEXPORT void JNICALL Java_com_michaelkuc6_u2fandroid_jni_Storage_init(JNIEnv* env, jclass,
                                                                        jstring keys, jstring serverSocketPath, jstring clientSocketPath, jstring cacheDir) {

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

	const char* path = env->GetStringUTFChars(keys, nullptr);
	stringstream keyStrStream{ string{ path } };
	env->ReleaseStringUTFChars(keys, path);

	Storage::init(keyStrStream);
}

extern "C" JNIEXPORT void JNICALL Java_com_michaelkuc6_u2fandroid_jni_Storage_start(JNIEnv *env, jclass clazz) {
	initStreams();
}

extern "C" JNIEXPORT void JNICALL Java_com_michaelkuc6_u2fandroid_jni_Storage_stop(JNIEnv *, jclass) {
	closeStreams();
	getPending().reset();
}

unique_ptr<U2FMessage>& getPending() {
	static unique_ptr<U2FMessage> pending{};
	return pending;
}
