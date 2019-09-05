package com.michaelkuc6.u2fandroid.jni;

public class U2FDevice {
    public static native String handleTransactions(String serverSocketPath, String clientSocketPath, String cacheDir);
}
