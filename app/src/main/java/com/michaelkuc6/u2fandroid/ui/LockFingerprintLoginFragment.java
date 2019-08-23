package com.michaelkuc6.u2fandroid.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LockFingerprintLoginFragment extends FingerprintLoginFragment {
  private static final String ARG_PASSHASH = "PASSHASH";
  private static final String ARG_PASSWORD_KEY = "PASSWORD_KEY";

  private byte[] passhash;
  private String passwordKey;

  public LockFingerprintLoginFragment() {}

  LockFingerprintLoginFragment(
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
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (savedInstanceState != null) {
      passhash = savedInstanceState.getByteArray(ARG_PASSHASH);
      passwordKey = savedInstanceState.getString(ARG_PASSWORD_KEY);
    }
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putByteArray(ARG_PASSHASH, passhash);
    outState.putString(ARG_PASSWORD_KEY, passwordKey);
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
