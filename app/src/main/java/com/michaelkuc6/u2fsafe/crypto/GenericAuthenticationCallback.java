package com.michaelkuc6.u2fsafe.crypto;

import androidx.annotation.Nullable;

import com.kevalpatel2106.fingerprintdialog.AuthenticationCallback;

public abstract class GenericAuthenticationCallback implements AuthenticationCallback {
  private AuthenticationAlternativeCallback alternativeCallback;

  public GenericAuthenticationCallback(AuthenticationAlternativeCallback authenticationAlternativeCallback) {
    this.alternativeCallback = authenticationAlternativeCallback;
  }

  @Override
  public void fingerprintAuthenticationNotSupported() {
    alternativeCallback.doAlternative();
  }

  @Override
  public void hasNoFingerprintEnrolled() {
    alternativeCallback.doAlternative();
  }

  @Override
  public void onAuthenticationError(int errorCode, @Nullable CharSequence errString) {}

  @Override
  public void onAuthenticationHelp(int helpCode, @Nullable CharSequence helpString) {}

  @Override
  public void authenticationCanceledByUser() {
    alternativeCallback.doAlternative();
  }

  @Override
  public void onAuthenticationFailed() {
    alternativeCallback.doAlternative();
  }

  public interface AuthenticationAlternativeCallback {
    void doAlternative();
  }
}
