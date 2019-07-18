package com.michaelkuc6.u2fsafe.ui;

public class PasswordGetFPFragment extends UnlockFingerprintLoginFragment
    implements FingerprintLoginFragment.FailureHandler, LoginPrompt.LoginHandler {

  public PasswordGetFPFragment(
      String fingerprintKey, String passwordKey, LoginPrompt.LoginHandler loginHandler) {
    super(
                    fingerprintKey,
                    passwordKey,
                    "Fingerprint unlock",
                    "Unlock U2F safe",
                    "Please unlock the U2F safe using your fingerprint",
                    "Cancel");
    setFailureHandler(failureHandler);
    setLoginHandler(this);
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
