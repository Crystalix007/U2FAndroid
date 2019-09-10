# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

if (NOT EXISTS "${CMAKE_CURRENT_SOURCE_DIR}/U2FDevice/Certificates.cpp")
    message(SEND_ERROR "Please run `GenCertificates.sh` in app/src/main/cpp/U2FDevice first")
endif()

set(ENABLE_PROGRAMS OFF CACHE BOOL "" FORCE)
set(ENABLE_TESTING OFF CACHE BOOL "" FORCE)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++14 -Wall -Werror -Wno-unused-variable")

add_subdirectory("${CMAKE_CURRENT_SOURCE_DIR}/mbed-crypto" "${CMAKE_CURRENT_SOURCE_DIR}/ext/${CMAKE_ANDROID_ARCH_ABI}/mbed-crypto/")

add_custom_command(OUTPUT "${CMAKE_CURRENT_SOURCE_DIR}/U2FDevice/cpp-base64/CMakeLists.txt"
        COMMAND cp "${CMAKE_CURRENT_SOURCE_DIR}/U2FDevice/Scripts/cpp-base64-CMakeLists.txt" "${CMAKE_CURRENT_SOURCE_DIR}/U2FDevice/cpp-base64/CMakeLists.txt")

add_subdirectory("${CMAKE_CURRENT_SOURCE_DIR}/U2FDevice/cpp-base64/" "${CMAKE_CURRENT_SOURCE_DIR}/ext/${CMAKE_ANDROID_ARCH_ABI}/b64/")

add_custom_command(OUTPUT "${CMAKE_CURRENT_SOURCE_DIR}/U2FDevice/micro-ecc/CMakeLists.txt"
        COMMAND cp "${CMAKE_CURRENT_SOURCE_DIR}/U2FDevice/Scripts/uECC-CMakeLists.txt" "${CMAKE_CURRENT_SOURCE_DIR}/U2FDevice/micro-ecc/CMakeLists.txt")

add_subdirectory("${CMAKE_CURRENT_SOURCE_DIR}/U2FDevice/micro-ecc/" "${CMAKE_CURRENT_SOURCE_DIR}/ext/${CMAKE_ANDROID_ARCH_ABI}/uECC/")

add_library(U2FAndroid SHARED
        U2FDevice/Architecture.cpp
        U2FDevice/Certificates.cpp
        U2FDevice/Channel.cpp
        U2FDevice/Controller.cpp
        U2FDevice/Field.cpp
        U2FDevice/IO.cpp
        U2FDevice/LED.cpp
        U2FDevice/Packet.cpp
        U2FDevice/Signature.cpp
        U2FDevice/Streams.cpp
        U2FDevice/Storage.cpp
        U2FDevice/U2F_Authenticate_APDU.cpp
        U2FDevice/U2F_CMD.cpp
        U2FDevice/U2F_Init_CMD.cpp
        U2FDevice/U2F_Msg_CMD.cpp
        U2FDevice/U2F_Ping_CMD.cpp
        U2FDevice/U2F_Register_APDU.cpp
        U2FDevice/U2F_Version_APDU.cpp
        U2FDevice/U2FMessage.cpp
        U2FAndroid/Storage.cpp
        U2FAndroid/U2FAndroid.cpp)

add_executable(U2FAndroid_Socket
        U2FDevice/Streams.cpp
        U2FDevice/IO.cpp
        U2FDevice/Architecture.cpp
        U2FAndroid/U2FAndroid_Socket.cpp)

target_include_directories(U2FAndroid PRIVATE mbed-crypto/include)

find_library(log log-lib)

target_link_libraries(U2FAndroid
        log
        android
        mbedcrypto
        cppb64
        uECC)

target_link_libraries(U2FAndroid_Socket
        log)

set_target_properties(U2FAndroid_Socket
        PROPERTIES
        RUNTIME_OUTPUT_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/../assets/${CMAKE_ANDROID_ARCH_ABI}")
