package com.michaelkuc6.u2fandroid.jni;

public class Storage {
    public static native void init(String keys, String serverSocketPath, String clientSocketPath, String cacheDir);
    public static native void start();
    public static native void stop();
}
