package com.michaelkuc6.u2fsafe.crypto;

import android.app.Activity;
import android.content.SharedPreferences;

import java.security.Key;

import javax.crypto.Cipher;

public interface KeyGenerator {
  boolean generateKey(String keyName);

  Cipher generateCipher();

  boolean initCipher(Cipher cipher, int opmode, Key key, Activity activity, String keyName);

  void saveEncSettings(SharedPreferences.Editor editor, Cipher cipher, String keyName);
}
