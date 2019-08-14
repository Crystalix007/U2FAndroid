package com.michaelkuc6.u2fsafe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

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

  private boolean passwordExists() {
    return FingerprintLoginFragment.passwordKeyExists(this, PASSWORD_KEY);
  }

  @Override
  protected void onResume() {
    super.onResume();

    setResult(-1);

    if (FingerprintLoginFragment.isFPCapable(this)) {
      if (passwordExists()) {
        // Should try to do decryption
        PasswordGetFPFragment getPasswordFragment =
            new PasswordGetFPFragment(FINGERPRINT_KEY, PASSWORD_KEY);
        getPasswordFragment.setLoginHandler(this);
        activeLogin = getPasswordFragment;
      } else activeLogin = new PasswordSetFPFragment(FINGERPRINT_KEY, PASSWORD_KEY, this);
    } else
      activeLogin =
          TextLoginFragment.createInstance(
              "Enter password", "Please enter your password to decrypt U2F safe", "Decipher");

    getSupportFragmentManager().beginTransaction().add(activeLogin, "LOGIN_FRAGMENT").commit();
  }

  @Override
  protected void onPause() {
    super.onPause();
    getSupportFragmentManager().beginTransaction().remove(activeLogin).commit();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
  }

  @Override
  public void handleLogin(byte[] passwordHash) {
    Toast.makeText(
            this,
            "Got a hash: " + Base64.encodeToString(passwordHash, Base64.DEFAULT),
            Toast.LENGTH_LONG)
        .show();

    Intent result = new Intent();
    result.putExtra(PASSWORD_KEY, passwordHash);

    setResult(0, result);
    finish();
  }
}
