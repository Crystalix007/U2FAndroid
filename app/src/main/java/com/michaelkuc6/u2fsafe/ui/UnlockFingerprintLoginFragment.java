package com.michaelkuc6.u2fsafe.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class UnlockFingerprintLoginFragment extends FingerprintLoginFragment {
  private static final String ARG_PASSWORD_KEY = "PASSWORD_KEY";

  private String passwordKey;

  public UnlockFingerprintLoginFragment() {}

  UnlockFingerprintLoginFragment(
      String fingerprintKey,
      String passwordKey,
      String title,
      String subtitle,
      String description,
      String negativeButton) {
    super(LoginMode.UNLOCK, fingerprintKey, title, subtitle, description, negativeButton);
    this.passwordKey = passwordKey;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (savedInstanceState != null) {
      passwordKey = savedInstanceState.getString(ARG_PASSWORD_KEY);
    }
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putString(ARG_PASSWORD_KEY, passwordKey);
  }

  @Override
  public byte[] getPassword() {
    return Base64.decode(
        getActivity().getPreferences(Context.MODE_PRIVATE).getString(passwordKey, null),
        Base64.DEFAULT);
  }
}
