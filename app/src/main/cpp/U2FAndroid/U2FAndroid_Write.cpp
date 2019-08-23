#include <array>
#include <cstdio>
#include <fcntl.h>
#include <unistd.h>
#include "../U2FDevice/IO.hpp"
#include "../U2FDevice/Streams.hpp"
#include "../U2FDevice/u2f.hpp"

using namespace std;

// Usage: su ./U2FAndroid_Write <<<"HID communication"
//  i.e.: stdin is written to HID kernel device
int main() {
    fcntl(STDIN_FILENO, F_SETFL, fcntl(STDIN_FILENO, F_GETFL) | O_NONBLOCK);

    array<uint8_t, HID_RPT_SIZE> buffer;
    const auto hostDescriptor = getHostDescriptor();
    size_t packetSize = 0;

    while (ssize_t readBytes = read(STDIN_FILENO, buffer.data() + packetSize, HID_RPT_SIZE - packetSize)) {
        packetSize += readBytes;

        if (packetSize == HID_RPT_SIZE) {
            write(*hostDescriptor, buffer.data(), HID_RPT_SIZE);
            packetSize = 0;
        }
    }

    return 0;
}
