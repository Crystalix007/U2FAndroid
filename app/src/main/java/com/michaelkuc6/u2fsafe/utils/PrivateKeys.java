package com.michaelkuc6.u2fsafe.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.widget.Toast;

import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.kevalpatel2106.fingerprintdialog.AuthenticationCallback;
import com.kevalpatel2106.fingerprintdialog.FingerprintDialogBuilder;
import com.michaelkuc6.u2fsafe.R;
import com.michaelkuc6.u2fsafe.crypto.GenericAuthenticationCallback;
import com.michaelkuc6.u2fsafe.crypto.GenericCryptoObjectGenerator;
import com.michaelkuc6.u2fsafe.crypto.GenericKeyGenerator;
import com.michaelkuc6.u2fsafe.ui.TextLoginFragment;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

public class PrivateKeys {
  private static final String FINGERPRINT_KEY = "com.michaelkuc6.U2FSafe.fingerprint";
  private static final String PASSWORD_KEY = "password";
  private static boolean uiIsActive = false;

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

  private static boolean getPasswordFP(final FragmentActivity activity) {
    if (uiIsActive) return false;

    if (!isFPCapable(activity)) return false;

    GenericCryptoObjectGenerator cryptoGenerator;

    try {
      cryptoGenerator =
          new GenericCryptoObjectGenerator(activity, FINGERPRINT_KEY, new GenericKeyGenerator());
    } catch (GenericCryptoObjectGenerator.GeneratorException ignored) {
      return false;
    }

    try {
      cryptoGenerator.setAuthObject(Cipher.DECRYPT_MODE, activity);
    } catch (GenericCryptoObjectGenerator.GeneratorException ignored) {
      cryptoGenerator.deleteFromStore();
      activity.getPreferences(Context.MODE_PRIVATE).edit().remove(PASSWORD_KEY).commit();
      setPassword(activity);
      return true;
    }

    uiIsActive = true;

    final GenericCryptoObjectGenerator generator = cryptoGenerator;

    FingerprintDialogBuilder fingerprintDialogBuilder =
        new FingerprintDialogBuilder(activity, cryptoGenerator)
            .setTitle("Unlock U2F keys")
            .setSubtitle("Authenticate to unlock the U2F safe")
            .setDescription("Press your finger onto the fingerprint sensor to unlock")
            .setNegativeButton("Cancel");

    AuthenticationCallback callback =
        new GenericAuthenticationCallback(
            new GenericAuthenticationCallback.AuthenticationAlternativeCallback() {
              @Override
              public void doAlternative() {
                uiIsActive = false;
                getPasswordText(activity);
              }
            }) {

          @Override
          public void onAuthenticationSucceeded() {
            Cipher cipher = generator.getCipher();
            String encPassword =
                activity.getPreferences(Context.MODE_PRIVATE).getString(PASSWORD_KEY, null);

            if (encPassword == null) {
              Toast.makeText(activity, "No known password stored", Toast.LENGTH_LONG).show();
              setPassword(activity);
              return;
            }

            uiIsActive = false;
            try {
              byte[] decipheredPassword = cipher.doFinal(Base64.decode(encPassword, Base64.DEFAULT));
              onPassHash(activity, decipheredPassword);
            } catch (BadPaddingException | IllegalBlockSizeException e) {
              Toast.makeText(activity, "Unable to decrypt password", Toast.LENGTH_LONG).show();
              getPasswordText(activity);
            }
          }
        };

    fingerprintDialogBuilder.show(activity.getSupportFragmentManager(), callback);
    return true;
  }

  private static void setPassword(final FragmentActivity activity) {
    if (uiIsActive) return;

    uiIsActive = true;

    final TextLoginFragment loginFragment =
        TextLoginFragment.createInstance(
            "Set Password", "Set the password used to encrypt your U2F keys", "Encrypt");
    TextLoginFragment.LoginHandler handler =
        new TextLoginFragment.LoginHandler() {
          @Override
          public void handleLogin(final byte[] passhash) {
            if (isFPCapable(activity)) {
              final GenericCryptoObjectGenerator cryptoGenerator =
                  new GenericCryptoObjectGenerator(
                      activity, FINGERPRINT_KEY, new GenericKeyGenerator());
              cryptoGenerator.setAuthObject(Cipher.ENCRYPT_MODE, activity);
              FingerprintDialogBuilder fingerprintDialogBuilder =
                  new FingerprintDialogBuilder(activity, cryptoGenerator)
                      .setTitle("Lock U2F keys")
                      .setSubtitle("Authenticate to encrypt the U2F safe")
                      .setDescription("Press your finger onto the fingerprint sensor to lock")
                      .setNegativeButton("Cancel");

              AuthenticationCallback callback =
                  new GenericAuthenticationCallback(
                      new GenericAuthenticationCallback.AuthenticationAlternativeCallback() {
                        @Override
                        public void doAlternative() {}
                      }) {

                    @Override
                    public void onAuthenticationSucceeded() {
                      SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
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
            uiIsActive = false;
          }
        };
    loginFragment.setLoginHandler(handler);
    loginFragment.show(activity.getSupportFragmentManager(), "PASSWORD_SET");
  }

  private static void getPasswordText(final FragmentActivity activity) {
    if (uiIsActive) return;
    uiIsActive = true;

    final TextLoginFragment login =
        TextLoginFragment.createInstance(
            activity.getString(R.string.vault_title),
            activity.getString(R.string.vault_subtitle),
            activity.getString(R.string.vault_button));
    login.setLoginHandler(
        new TextLoginFragment.LoginHandler() {
          @Override
          public void handleLogin(byte[] passhash) {
            onPassHash(activity, passhash);
            activity.getSupportFragmentManager().beginTransaction().remove(login).commit();
            uiIsActive = false;
          }
        });
    login.show(activity.getSupportFragmentManager(), "PASSWORD_GET");
  }

  public static void requestPassHash(final FragmentActivity activity) {
    if (!getPasswordFP(activity)) getPasswordText(activity);
  }

  private static void onPassHash(final Context context, byte[] passhash) {
    Toast.makeText(
            context,
            "Got passhash: " + Base64.encodeToString(passhash, Base64.DEFAULT),
            Toast.LENGTH_LONG)
        .show();
  }

  public static void closeInterface(FragmentActivity activity) {
    FragmentManager manager = activity.getSupportFragmentManager();

    Fragment passwordGet = manager.findFragmentByTag("PASSWORD_GET"),
        passwordSet = manager.findFragmentByTag("PASSWORD_SET");
    FragmentTransaction transaction = manager.beginTransaction();

    if (passwordGet != null) transaction.remove(passwordGet);

    if (passwordSet != null) transaction.remove(passwordSet);

    transaction.commit();
  }
}
