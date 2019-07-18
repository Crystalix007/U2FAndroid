package com.michaelkuc6.u2fsafe.ui;

import android.content.Context;
import android.util.Base64;

public class UnlockFingerprintLoginFragment extends FingerprintLoginFragment {
  private String passwordKey;

  public UnlockFingerprintLoginFragment(
      String fingerprintKey,
      String passwordKey,
      String title,
      String subtitle,
      String description,
      String negativeButton) {
    super(LoginMode.LOCK, fingerprintKey, title, subtitle, description, negativeButton);
    this.passwordKey = passwordKey;
  }

  @Override
  public byte[] getPassword() {
    return Base64.decode(
        getActivity().getPreferences(Context.MODE_PRIVATE).getString(passwordKey, null),
        Base64.DEFAULT);
  }
}
