package com.michaelkuc6.u2fandroid.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;

public class PasswordGetFPFragment extends UnlockFingerprintLoginFragment
    implements FingerprintLoginFragment.FailureHandler, LoginPrompt.LoginHandler {
  private LoginHandler loginHandler;

  public PasswordGetFPFragment() {}

  public PasswordGetFPFragment(String fingerprintKey, String passwordKey) {
    super(
        fingerprintKey,
        passwordKey,
        "Fingerprint unlock",
        "Unlock U2F safe",
        "Please unlock the U2F safe using your fingerprint",
        "Cancel");
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setFailureHandler(this);
  }

  @Override
  public void setLoginHandler(LoginHandler loginHandler) {
    super.setLoginHandler(this);
    this.loginHandler = loginHandler;
  }

  @Override
  public void onFingerprintFailure() {}

  @Override
  public void onFingerprintNeedsReset() {}

  @Override
  public void onPasswordFailure() {}

  // Handle the deciphered passhash
  @Override
  public void handleCipheredPassword(byte[] cipheredPassword) {
    super.handleCipheredPassword(cipheredPassword);
    loginHandler.handleLogin(cipheredPassword);
  }

  // Do nothing with the passhash retrieved before deciphering it
  @Override
  public void handleLogin(byte[] passwordHash) {}
}
