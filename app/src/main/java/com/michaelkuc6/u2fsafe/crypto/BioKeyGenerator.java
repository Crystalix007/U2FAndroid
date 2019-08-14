package com.michaelkuc6.u2fsafe.crypto;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.KeyGenerator;

public class BioKeyGenerator extends GenericKeyGenerator {
  private final String provider;

  public BioKeyGenerator(String keystoreProvider) {
    super();
    this.provider = keystoreProvider;
  }

  @Override
  public boolean generateKey(String keyName) {
    javax.crypto.KeyGenerator keyGenerator;

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
}
