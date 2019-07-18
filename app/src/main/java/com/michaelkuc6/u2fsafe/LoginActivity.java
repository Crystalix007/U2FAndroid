package com.michaelkuc6.u2fsafe;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.michaelkuc6.u2fsafe.ui.FingerprintLoginFragment;
import com.michaelkuc6.u2fsafe.ui.LoginPrompt;
import com.michaelkuc6.u2fsafe.ui.PasswordGetFPFragment;
import com.michaelkuc6.u2fsafe.ui.PasswordSetFPFragment;
import com.michaelkuc6.u2fsafe.ui.TextLoginFragment;

public class LoginActivity extends FragmentActivity implements LoginPrompt.LoginHandler {
  private static final String FINGERPRINT_KEY = "com.michaelkuc6.U2FSafe.fingerprint";
  public static final String PASSWORD_KEY = "password";
  private Fragment activeLogin;

  @Override
  public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
    super.onCreate(savedInstanceState, persistentState);

    if (FingerprintLoginFragment.isFPCapable(this)) {
      if (FingerprintLoginFragment.passwordKeyExists(this, PASSWORD_KEY))
        // Should try to do decryption
        activeLogin = new PasswordGetFPFragment(FINGERPRINT_KEY, PASSWORD_KEY, this);
      else activeLogin = new PasswordSetFPFragment(FINGERPRINT_KEY, PASSWORD_KEY, this);
    } else
      activeLogin =
          TextLoginFragment.createInstance(
              "Enter password", "Please enter your password to decrypt U2F safe", "Decipher");

    getSupportFragmentManager().beginTransaction().add(activeLogin, "LOGIN_FRAGMENT").commit();
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
  }

  @Override
  public void handleLogin(byte[] passwordHash) {
    Intent result = new Intent();
    result.putExtra(PASSWORD_KEY, passwordHash);

    setResult(0, result);
    finish();
  }
}
