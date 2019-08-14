package com.michaelkuc6.u2fsafe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
  private static final int UNLOCK_TAG = 1;
  private static final int U2F_TAG = 2;
  private TextView passwordSet, keyfileExists;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    passwordSet = findViewById(R.id.passwordSet_text);

    keyfileExists = findViewById(R.id.keyfilePresent_text);
    keyfileExists.setText(U2FActivity.keyfileExists(this) ? R.string.yes : R.string.no);

    Intent loginIntent = new Intent(this, LoginActivity.class);
    startActivityForResult(loginIntent, UNLOCK_TAG);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == UNLOCK_TAG) {
      if (resultCode == 0) {
        passwordSet.setText(R.string.yes);
        byte[] passhash = data.getExtras().getByteArray(LoginActivity.PASSWORD_KEY);
        Intent u2fIntent = new Intent(this, U2FActivity.class);
        u2fIntent.putExtra(U2FActivity.PASSHASH_KEY, passhash);
        startActivityForResult(u2fIntent, U2F_TAG);
      } else passwordSet.setText(R.string.no);
    }
  }
}
