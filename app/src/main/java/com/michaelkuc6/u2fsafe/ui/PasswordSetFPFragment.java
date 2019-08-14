package com.michaelkuc6.u2fsafe.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kevalpatel2106.fingerprintdialog.FingerprintDialogBuilder;

public class PasswordSetFPFragment extends Fragment
    implements FingerprintLoginFragment.FailureHandler, LoginPrompt.LoginHandler {
  private static final String ARG_FINGERPRINT_KEY = "FINGERPRINT";
  private static final String ARG_PASSWORD_KEY = "PASSWORD";

  private TextLoginFragment loginFragment;
  private String fingerprintKey;
  private String passwordKey;
  private FingerprintLoginFragment.LoginHandler loginHandler;

  public PasswordSetFPFragment() {}

  public PasswordSetFPFragment(
      String fingerprintKey, String passwordKey, LoginPrompt.LoginHandler loginHandler) {
    loginFragment =
        TextLoginFragment.createInstance(
            "Set Password", "Set the password used to encrypt your U2F keys", "Encrypt");
    this.fingerprintKey = fingerprintKey;
    this.passwordKey = passwordKey;
    this.loginHandler = loginHandler;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (savedInstanceState != null) {
      fingerprintKey = savedInstanceState.getString(ARG_FINGERPRINT_KEY);
      passwordKey = savedInstanceState.getString(ARG_PASSWORD_KEY);
    }

    final FingerprintLoginFragment.FailureHandler failureHandler = this;
    final LoginPrompt.LoginHandler loginHandler = this;

    final TextLoginFragment.LoginHandler handler =
        new TextLoginFragment.LoginHandler() {
          @Override
          public void handleLogin(final byte[] passhash) {
            LockFingerprintLoginFragment fingerprintLoginFragment =
                new LockFingerprintLoginFragment(
                    passhash,
                    fingerprintKey,
                    passwordKey,
                    "Fingerprint lock",
                    "Set fingerprint lock",
                    "Press your finger to the sensor to lock the U2F private keys",
                    "Cancel");
            fingerprintLoginFragment.setFailureHandler(failureHandler);
            fingerprintLoginFragment.setLoginHandler(loginHandler);

            getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .add(fingerprintLoginFragment, "FINGERPRINT_LOGIN")
                .commit();
          }
        };
    loginFragment.setLoginHandler(handler);
    loginFragment.show(getActivity().getSupportFragmentManager(), "PASSWORD_SET");
  }

  @Override
  public void onPause() {
    FingerprintDialogBuilder.close(getActivity().getSupportFragmentManager());

    if (loginFragment != null) {
      getActivity().getSupportFragmentManager().beginTransaction().remove(loginFragment).commit();
    }

    super.onPause();
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putString(ARG_FINGERPRINT_KEY, fingerprintKey);
    outState.putString(ARG_PASSWORD_KEY, passwordKey);
  }

  @Override
  public void onFingerprintFailure() {}

  @Override
  public void onFingerprintNeedsReset() {}

  @Override
  public void onPasswordFailure() {}

  @Override
  public void handleLogin(byte[] passwordHash) {
    loginHandler.handleLogin(passwordHash);
    getActivity().getSupportFragmentManager().beginTransaction().remove(loginFragment).commit();
  }
}
