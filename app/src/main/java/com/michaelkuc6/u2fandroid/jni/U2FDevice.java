package com.michaelkuc6.u2fandroid.jni;

public class U2FDevice {
    public static native boolean handleTransaction(boolean hasAuthorisation);
    public static native String getResult();
}
