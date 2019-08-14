package com.michaelkuc6.u2fsafe.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
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
import com.michaelkuc6.u2fsafe.crypto.BioKeyGenerator;
import com.michaelkuc6.u2fsafe.crypto.GenericAuthenticationCallback;
import com.michaelkuc6.u2fsafe.crypto.GenericCryptoObjectGenerator;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

public abstract class FingerprintLoginFragment extends Fragment implements LoginPrompt {
  private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
  private static final String ARG_LOGIN_MODE = "LOGIN_MODE";
  private static final String ARG_FINGERPRINT_KEY = "FINGERPRINT_KEY";
  private static final String ARG_TITLE = "TITLE";
  private static final String ARG_SUBTITLE = "SUBTITLE";
  private static final String ARG_DESCRIPTION = "DESCRIPTION";
  private static final String ARG_NEGATIVE_BUTTON = "NEGATIVE_BUTTON";

  private LoginHandler loginHandler;
  private String fingerprintKey;
  private FailureHandler failureHandler;
  private String title, subtitle, description, negativeButton;
  private LoginMode loginMode;

  public FingerprintLoginFragment() {}

  FingerprintLoginFragment(
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

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (savedInstanceState != null) {
      loginMode = LoginMode.values()[savedInstanceState.getInt(ARG_LOGIN_MODE)];
      fingerprintKey = savedInstanceState.getString(ARG_FINGERPRINT_KEY);
      title = savedInstanceState.getString(ARG_TITLE);
      subtitle = savedInstanceState.getString(ARG_SUBTITLE);
      description = savedInstanceState.getString(ARG_DESCRIPTION);
      negativeButton = savedInstanceState.getString(ARG_NEGATIVE_BUTTON);
    }
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
              getActivity(), fingerprintKey, new BioKeyGenerator(KEYSTORE_PROVIDER));
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

  @Override
  public void onPause() {
    super.onPause();
    FingerprintDialogBuilder.close(getFragmentManager());
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(ARG_LOGIN_MODE, loginMode.ordinal());
    outState.putString(ARG_FINGERPRINT_KEY, fingerprintKey);
    outState.putString(ARG_TITLE, title);
    outState.putString(ARG_SUBTITLE, subtitle);
    outState.putString(ARG_DESCRIPTION, description);
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
