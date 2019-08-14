package com.michaelkuc6.u2fsafe.crypto;

import android.app.Activity;
import android.content.SharedPreferences;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.kevalpatel2106.fingerprintdialog.CryptoObjectGenerator;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;

public class GenericCryptoObjectGenerator implements CryptoObjectGenerator {
  private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
  private final String keyName;
  private final KeyStore keyStore;
  private final KeyGenerator keyGenerator;
  private Cipher currentCipher;

  public GenericCryptoObjectGenerator(Activity activity, String keyName, KeyGenerator keyGenerator)
      throws GeneratorException {
    this.keyGenerator = keyGenerator;
    this.keyName = keyName;

    try {
      keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
      keyStore.load(null);
    } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
      Toast.makeText(
              activity,
              "Failed to open secure encryption key storage: cannot use fingerprint",
              Toast.LENGTH_LONG)
          .show();
      throw new GeneratorException(e);
    }

    try {
      if (!keyStore.containsAlias(keyName) && !keyGenerator.generateKey(keyName)) {
        Toast.makeText(
                activity,
                "Failed to generate key to encrypt: cannot use fingerprint",
                Toast.LENGTH_LONG)
            .show();
        throw new GeneratorException("Failed to generate key");
      }
    } catch (KeyStoreException e) {
      throw new GeneratorException(e);
    }

    currentCipher = keyGenerator.generateCipher();

    if (currentCipher == null) {
      Toast.makeText(
              activity,
              "Failed to generate cipher to encrypt: cannot use fingerprint",
              Toast.LENGTH_LONG)
          .show();
      throw new GeneratorException("Failed to generate cipher");
    }
  }

  @Override
  public FingerprintManager.CryptoObject getFingerprintCryptoObject() {
    return new FingerprintManager.CryptoObject(currentCipher);
  }

  @RequiresApi(api = Build.VERSION_CODES.P)
  @Override
  public BiometricPrompt.CryptoObject getBiometricCryptoObject() {
    return new BiometricPrompt.CryptoObject(currentCipher);
  }

  public Cipher getCipher() {
    return currentCipher;
  }

  public void setAuthObject(int mode, Activity activity) throws GeneratorException {
    Key key;
    try {
      key = keyStore.getKey(keyName, null);
    } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
      Toast.makeText(
              activity,
              "Failed to retrieve encryption key: cannot use fingerprint",
              Toast.LENGTH_LONG)
          .show();
      throw new GeneratorException(e);
    }

    switch (mode) {
      case Cipher.ENCRYPT_MODE:
      case Cipher.DECRYPT_MODE:
        if (!keyGenerator.initCipher(currentCipher, mode, key, activity, keyName)) {
          Toast.makeText(
                  activity,
                  "Failed to initialise ciphers: cannot use fingerprint",
                  Toast.LENGTH_LONG)
              .show();
          throw new GeneratorException("Failed to initialise ciphers");
        }
        break;
      default:
        throw new GeneratorException("Invalid auth object mode");
    }
  }

  public void save(SharedPreferences.Editor editor) {
    keyGenerator.saveEncSettings(editor, currentCipher, keyName);
  }

  public void deleteFromStore() {
    try {
      if (keyStore.containsAlias(keyName)) keyStore.deleteEntry(keyName);
    } catch (KeyStoreException e) {
      e.printStackTrace();
    }
  }

  public class GeneratorException extends RuntimeException {
    public GeneratorException(String message) {
      super(message);
    }

    public GeneratorException(Exception source) {
      super(source);
    }
  }

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {}

  private void readObject(java.io.ObjectInputStream in)
      throws IOException, ClassNotFoundException {}

  private void readObjectNoData() throws ObjectStreamException {}
}
