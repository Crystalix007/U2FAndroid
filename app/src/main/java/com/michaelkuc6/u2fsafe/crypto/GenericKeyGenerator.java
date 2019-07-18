package com.michaelkuc6.u2fsafe.crypto;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

public class GenericKeyGenerator implements GenericCryptoObjectGenerator.KeyGenerator {
  private static final String IV_SUFFIX = "_IV";

  @Override
  public boolean generateKey(String keyName, String provider) {
    KeyGenerator keyGenerator;

    try {
      keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, provider);
    } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
      return false;
    }

    try {
      keyGenerator.init(
          new KeyGenParameterSpec.Builder(
                  keyName, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
              .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
              .setUserAuthenticationRequired(true)
              .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
              .build());
      keyGenerator.generateKey();
    } catch (InvalidAlgorithmParameterException e) {
      return false;
    }

    return true;
  }

  @Override
  public Cipher generateCipher() {
    try {
      return Cipher.getInstance(
          KeyProperties.KEY_ALGORITHM_AES
              + "/"
              + KeyProperties.BLOCK_MODE_CBC
              + "/"
              + KeyProperties.ENCRYPTION_PADDING_PKCS7);
    } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
      return null;
    }
  }

  private AlgorithmParameterSpec getDecryptParameterSpec(Activity activity, String keyName) {
    String b64IVs =
        activity.getPreferences(Context.MODE_PRIVATE).getString(getIVKeyName(keyName), null);

    if (b64IVs == null) return null;

    byte[] ivs = Base64.decode(b64IVs, Base64.DEFAULT);
    return new IvParameterSpec(ivs);
  }

  private static String getIVKeyName(String keyName) {
    return keyName + IV_SUFFIX;
  }

  @Override
  public void saveEncSettings(SharedPreferences.Editor editor, Cipher cipher, String keyName) {
    editor.putString(getIVKeyName(keyName), Base64.encodeToString(cipher.getIV(), Base64.DEFAULT));
  }

  @Override
  public boolean initCipher(Cipher cipher, int opmode, Key key, Activity activity, String keyName) {
    try {
      switch (opmode) {
        case Cipher.ENCRYPT_MODE:
          cipher.init(opmode, key);
          SharedPreferences.Editor prefEditor = activity.getPreferences(Context.MODE_PRIVATE).edit();
          saveEncSettings(prefEditor, cipher, keyName);
          prefEditor.commit();
          return true;
        case Cipher.DECRYPT_MODE:
          cipher.init(opmode, key, getDecryptParameterSpec(activity, keyName));
          return true;
        default:
          return false;
      }
    } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
      return false;
    }
  }
}
