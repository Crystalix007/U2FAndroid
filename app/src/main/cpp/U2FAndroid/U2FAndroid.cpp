#include "../U2FDevice/Controller.hpp"
#include "../U2FDevice/Storage.hpp"
#include "Storage.hpp"
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
Java_com_michaelkuc6_u2fandroid_jni_U2FDevice_handleTransaction(JNIEnv* env, jclass type, jboolean hasAuthorisation) {
	static Controller ch{ 0xF1D00000 };

	const auto pending{ std::move(getPending()) };
	U2FMessage message;

	// By default ignore authorisation until we know what we are authorising (the pending op.)
	AuthorisationLevel auth{ AuthorisationLevel::Unspecified };

	if (pending) {
		message = *pending;

		auth = static_cast<bool>(hasAuthorisation) ? AuthorisationLevel::Authorised : AuthorisationLevel::Unauthorised;
	}
	else {
		auto potentialMessage = U2FMessage::readNonBlock();

		if (!potentialMessage)
			return static_cast<jboolean>(true);

		message = *potentialMessage;
	}

	try {
		if (!ch.handleTransaction(message, auth) && !pending) {
			getPending() = make_unique<U2FMessage>(message);
			return static_cast<jboolean>(false);
		}
	} catch (const runtime_error& e) {
		__android_log_print(ANDROID_LOG_INFO, "U2FDevice", "%s", e.what());
	}

	return static_cast<jboolean>(true);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_michaelkuc6_u2fandroid_jni_U2FDevice_getResult(JNIEnv *env, jclass clazz) {
	return env->NewStringUTF(getSave().c_str());
}
