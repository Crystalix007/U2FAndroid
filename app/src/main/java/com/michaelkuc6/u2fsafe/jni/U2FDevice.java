package com.michaelkuc6.u2fsafe.jni;

public class U2FDevice {
    public static native String handleTransactions(String executableDirectory, String cacheDir);
}
