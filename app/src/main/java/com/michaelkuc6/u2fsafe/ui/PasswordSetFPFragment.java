package com.michaelkuc6.u2fsafe.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PasswordSetFPFragment extends Fragment
    implements FingerprintLoginFragment.FailureHandler, LoginPrompt.LoginHandler {
  private final TextLoginFragment loginFragment;
  private final String fingerprintKey;
  private final String passwordKey;
  private final FingerprintLoginFragment.LoginHandler loginHandler;

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

            /*
                final GenericCryptoObjectGenerator cryptoGenerator =
                        new GenericCryptoObjectGenerator(
                                getActivity(), fingerprintKey, new GenericKeyGenerator());
                cryptoGenerator.setAuthObject(Cipher.ENCRYPT_MODE, getActivity());
                FingerprintDialogBuilder fingerprintDialogBuilder =
                        new FingerprintDialogBuilder(getActivity(), cryptoGenerator)
                                .setTitle("Lock U2F keys")
                                .setSubtitle("Authenticate to encrypt the U2F safe")
                                .setDescription("Press your finger onto the fingerprint sensor to lock")
                                .setNegativeButton("Cancel");

                AuthenticationCallback callback =
                        new GenericAuthenticationCallback(
                                new GenericAuthenticationCallback.AuthenticationAlternativeCallback() {
                                  @Override
                                  public void onFailure() {}
                                }) {

                          @Override
                          public void onAuthenticationSucceeded() {
                            SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                            Cipher cipher = cryptoGenerator.getCipher();
                            try {
                              String encPassword =
                                      Base64.encodeToString(cipher.doFinal(passhash), Base64.DEFAULT);
                              preferences.edit().putString(PASSWORD_KEY, encPassword).commit();
                            } catch (BadPaddingException | IllegalBlockSizeException ignored) {
                            }

                            uiIsActive = false;
                          }
                        };

                fingerprintDialogBuilder.show(activity.getSupportFragmentManager(), callback);
              }
                activity.getSupportFragmentManager().beginTransaction().remove(loginFragment).commit();
            }
            */
          }
        };
    loginFragment.setLoginHandler(handler);
    loginFragment.show(getActivity().getSupportFragmentManager(), "PASSWORD_SET");
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
