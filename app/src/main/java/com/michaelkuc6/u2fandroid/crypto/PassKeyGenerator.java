package com.michaelkuc6.u2fandroid.crypto;

public class PassKeyGenerator extends GenericKeyGenerator {
  @Override
  public boolean generateKey(String keyName) {
    // Always can generate a key from string
    return true;
  }
}
