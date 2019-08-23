#include <unistd.h>
#include <array>
#include <iostream>
#include "../U2FDevice/Streams.hpp"
#include "../U2FDevice/u2f.hpp"
#include <android/log.h>

using namespace std;

// Usage: su U2FAndroid_Read
//  i.e.: stdout is HID output
int main() {
    auto hostDescriptor = *getHostDescriptor();
    array<uint8_t, HID_RPT_SIZE> bytes{};
    ssize_t readByteCount;

    do {
        readByteCount = read(hostDescriptor, bytes.data(), HID_RPT_SIZE);

        if (readByteCount > 0 && readByteCount != HID_RPT_SIZE) {
            //Failed to copy an entire packet in, so log this packet
#ifdef DEBUG_MSGS
            cerr << "Only retrieved " << readByteCount << " bytes from expected full packet." << endl;
#endif
        }

        if (readByteCount > 0) {
            __android_log_print(ANDROID_LOG_DEBUG, "U2FAndroid_Read", "Read a packet of size %zu", readByteCount);
            fwrite(bytes.data(), 1, readByteCount, stdout);
        }
    } while (readByteCount > 0);

    return 0;
}
