package com.michaelkuc6.u2fsafe;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.michaelkuc6.u2fsafe.ui.FingerprintLoginFragment;

public class LoginActivity extends Activity {
    private Fragment activeLogin;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        activeLogin = new FingerprintLoginFragment();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
