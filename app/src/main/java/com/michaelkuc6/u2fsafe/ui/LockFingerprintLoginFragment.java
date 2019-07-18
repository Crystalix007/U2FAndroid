package com.michaelkuc6.u2fsafe.ui;

import android.content.Context;
import android.util.Base64;

public class LockFingerprintLoginFragment extends FingerprintLoginFragment {
  private byte[] passhash;
  private String passwordKey;

  public LockFingerprintLoginFragment(
      byte[] passhash,
      String fingerprintKey,
      String passwordKey,
      String title,
      String subtitle,
      String description,
      String negativeButton) {
    super(LoginMode.LOCK, fingerprintKey, title, subtitle, description, negativeButton);
    this.passhash = passhash;
    this.passwordKey = passwordKey;
  }

  @Override
  public byte[] getPassword() {
    return passhash;
  }

  @Override
  public void handleCipheredPassword(byte[] cipheredPassword) {
    getActivity()
        .getPreferences(Context.MODE_PRIVATE)
        .edit()
        .putString(passwordKey, Base64.encodeToString(cipheredPassword, Base64.DEFAULT))
        .commit();
  }
}
