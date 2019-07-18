package com.michaelkuc6.u2fsafe.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.fragment.app.Fragment;

import com.kevalpatel2106.fingerprintdialog.AuthenticationCallback;
import com.kevalpatel2106.fingerprintdialog.FingerprintDialogBuilder;
import com.michaelkuc6.u2fsafe.crypto.GenericAuthenticationCallback;
import com.michaelkuc6.u2fsafe.crypto.GenericCryptoObjectGenerator;
import com.michaelkuc6.u2fsafe.crypto.GenericKeyGenerator;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

public abstract class FingerprintLoginFragment extends Fragment implements LoginPrompt {
  protected LoginHandler loginHandler;
  protected final String fingerprintKey;
  protected FailureHandler failureHandler;
  protected final String title, subtitle, description, negativeButton;
  protected final LoginMode loginMode;

  public FingerprintLoginFragment(
      LoginMode loginMode,
      String fingerprintKey,
      String title,
      String subtitle,
      String description,
      String negativeButton) {
    this.loginMode = loginMode;
    this.fingerprintKey = fingerprintKey;
    this.title = title;
    this.subtitle = subtitle;
    this.description = description;
    this.negativeButton = negativeButton;
  }

  @Override
  public void setLoginHandler(LoginHandler loginHandler) {
    this.loginHandler = loginHandler;
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    final View view = super.onCreateView(inflater, container, savedInstanceState);

    GenericCryptoObjectGenerator cryptoGenerator;

    try {
      cryptoGenerator =
          new GenericCryptoObjectGenerator(
              getActivity(), fingerprintKey, new GenericKeyGenerator());
    } catch (GenericCryptoObjectGenerator.GeneratorException ignored) {
      failureHandler.onFingerprintFailure();
      return view;
    }

    try {
      cryptoGenerator.setAuthObject(loginMode.cipherMode(), getActivity());
    } catch (GenericCryptoObjectGenerator.GeneratorException ignored) {
      failureHandler.onFingerprintNeedsReset();
      return view;
    }

    final GenericCryptoObjectGenerator generator = cryptoGenerator;

    FingerprintDialogBuilder fingerprintDialogBuilder =
        new FingerprintDialogBuilder(getActivity(), cryptoGenerator)
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButton(negativeButton);

    AuthenticationCallback callback =
        new GenericAuthenticationCallback(
            new GenericAuthenticationCallback.AuthenticationAlternativeCallback() {
              @Override
              public void onFailure() {
                failureHandler.onFingerprintFailure();
              }
            }) {

          @Override
          public void onAuthenticationSucceeded() {
            Cipher cipher = generator.getCipher();
            byte[] password = getPassword();

            try {
              byte[] cipheredPassword = cipher.doFinal(password);
              handleCipheredPassword(cipheredPassword);
              loginHandler.handleLogin(password);
            } catch (BadPaddingException | IllegalBlockSizeException e) {
              Toast.makeText(getActivity(), "Unable to crypt password", Toast.LENGTH_LONG).show();
              failureHandler.onPasswordFailure();
            }
          }
        };

    fingerprintDialogBuilder.show(getActivity().getSupportFragmentManager(), callback);

    return view;
  }

  public abstract byte[] getPassword();

  public void handleCipheredPassword(byte[] cipheredPassword) {}

  public void setFailureHandler(FailureHandler failureHandler) {
    this.failureHandler = failureHandler;
  }

  public static boolean passwordKeyExists(Activity activity, String passwordKey) {
    String encPassword = activity.getPreferences(Context.MODE_PRIVATE).getString(passwordKey, null);

    return encPassword != null;
  }

  public static boolean isFPCapable(Activity activity) {
    FingerprintManagerCompat fingerprintManager = FingerprintManagerCompat.from(activity);

    if (!fingerprintManager.isHardwareDetected()) return false;

    if (!fingerprintManager.hasEnrolledFingerprints()) {
      Toast.makeText(activity, "This device has no fingerprints registered", Toast.LENGTH_SHORT)
          .show();
      return false;
    }

    return true;
  }

  public interface FailureHandler {
    void onFingerprintFailure();

    void onFingerprintNeedsReset();

    void onPasswordFailure();
  }

  public enum LoginMode {
    LOCK,
    UNLOCK;

    public int cipherMode() {
      switch (this) {
        case LOCK:
          return Cipher.ENCRYPT_MODE;
        case UNLOCK:
          return Cipher.DECRYPT_MODE;
        default:
          return -1;
      }
    }
  }
}
