#include "../U2FDevice/u2f.hpp"
#include <android/log.h>
#include <cerrno>
#include <csignal>
#include <cstdio>
#include <fcntl.h>
#include <memory>
#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/un.h>
#include <poll.h>
#include <unistd.h>

volatile bool killed = false;

void signalCallback(int signum);

#define HID_DEV "/dev/hidg2"

using namespace std;

// Usage:
//  sudo U2FAndroid_Pipe <path to pipe>
//
// Error codes:
//  1 - incorrect number of arguments
//  2 - unable to make socket
//  3 - unable to open app-side socket for listening
//  4 - unable to open kernel-side pipe
//  5 - unspecified error reading from kernel-side pipe
//  6 - unspecified error reading from app-side socket
//  7 - unable to delete existing file at socket location
//  8 - unable to open file-descriptor for app socket
//  9 - unable to make the app-side socket world read-writeable
// 10 - unable to accept client connection to app-side socket
int main(int argc, char* argv[]) {
	const constexpr timespec intervalDelay{ 0, 10'000'000 };

	if (argc != 2)
		return 1;

	// Does pipe exist at argv[1] path
	if (access(argv[1], F_OK) != -1) {
		__android_log_print(ANDROID_LOG_DEBUG, "U2FAndroid_Socket", "Deleting existing file at %s",
		                    argv[1]);
		// Attempt to clean up pipe that hung around from last time
		// may need
		// unlink(argv[1]); // Overwrite existing connection
		// instead
		if (remove(argv[1]) != 0)
			return 7;
	}

	int appServerFD;

	if ((appServerFD = socket(AF_UNIX, SOCK_SEQPACKET | SOCK_CLOEXEC, 0)) == -1)
		return 8;

	sockaddr_un appServerAddr{};
	appServerAddr.sun_family = AF_UNIX;
	strncpy(appServerAddr.sun_path, argv[1], sizeof(appServerAddr.sun_path) - 1);

	if (bind(appServerFD, (sockaddr*)&appServerAddr, sizeof(appServerAddr)) == -1)
		return 2;

	if (chmod(argv[1], 0666) == -1) {
		return 9;
	}

	if (listen(appServerFD, 1) == -1)
		return 3;

	__android_log_print(ANDROID_LOG_DEBUG, "U2FAndroid_Socket", "Waiting for connections");

	int appClientFD;

	appClientFD = accept4(appServerFD, nullptr, nullptr, SOCK_CLOEXEC);

	if (appClientFD == -1)
		return 10;

	// Use custom deleter for exception safety
	const auto appServerSocketDeleter = [&argv, appClientFD](const int* fd) {
		close(*fd);
		close(appClientFD);
		unlink(argv[1]);
		delete fd;
	};
	unique_ptr<int, decltype(appServerSocketDeleter)> appPipe{ new int{ appServerFD },
		                                                       appServerSocketDeleter };

	const auto kernelPipeDeleter = [](int* fd) {
		if (*fd != -1)
			close(*fd);

		delete fd;
	};
	unique_ptr<int, decltype(kernelPipeDeleter)> kernelPipe{
		new int{ open(HID_DEV, O_RDWR | O_APPEND) }, kernelPipeDeleter
	};

	if (*kernelPipe == -1) {
		return 4;
	}

	uint8_t appReadBuffer[HID_RPT_SIZE];
	uint8_t kernelReadBuffer[HID_RPT_SIZE];
	uint8_t krbSize = 0;
	uint8_t arbSize = 0;

	__android_log_print(ANDROID_LOG_DEBUG, "U2FAndroid_Socket",
	                    "Starting U2F server, communicating via %s", argv[1]);

	signal(SIGTERM, signalCallback);
	signal(SIGINT, signalCallback);

	pollfd readFDs[2] = {
			{ *kernelPipe, POLLIN, 0 },
			{ appClientFD, POLLIN, 0 } };

	while (!killed) {
		int ready = ppoll(readFDs, 2, &intervalDelay, nullptr);

		int readCount;

		if (readFDs[0].revents & POLLIN) {
			if ((readCount = read(*kernelPipe, kernelReadBuffer + krbSize,
								  HID_RPT_SIZE - krbSize)) ==
				-1) {
				if (errno != EAGAIN)
					return 5;
			} else {
				krbSize += readCount;

				if (krbSize == HID_RPT_SIZE) {
					// Technically, not all bytes must be written, but if not, just drop
					// the rest
					int writeCount = send(appClientFD, kernelReadBuffer, HID_RPT_SIZE, 0);
					if (writeCount < 0) {
						__android_log_print(ANDROID_LOG_WARN, "U2FAndroid_Socket",
											"Failed to write to app with errno %d", errno);
					}
					krbSize = 0;
				}
			}
		}

		if (readFDs[1].revents & POLLIN) {
			if ((readCount = recv(appClientFD, appReadBuffer + arbSize, HID_RPT_SIZE - arbSize,
								  0)) ==
				-1) {
				if (errno != EAGAIN)
					return 6;
			} else {
				arbSize += readCount;

				if (arbSize == HID_RPT_SIZE) {
					// Technically, not all bytes must be written, but if not, just drop
					// the rest
					int writeCount = write(*kernelPipe, appReadBuffer, HID_RPT_SIZE);
					if (writeCount < 0) {
						__android_log_print(ANDROID_LOG_WARN, "U2FAndroid_Socket",
											"Failed to write to kernel with errno %d", errno);
					}
					arbSize = 0;
				}
			}
		}
	}

	__android_log_print(ANDROID_LOG_DEBUG, "U2FAndroid_Socket", "Closing U2F server");
}

void signalCallback([[maybe_unused]] int signum) {
	__android_log_print(ANDROID_LOG_DEBUG, "U2FAndroid_Socket", "U2F server kill request");
	killed = true;
}
