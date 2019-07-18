package com.michaelkuc6.u2fsafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleObserver;

public class MainActivity extends AppCompatActivity
/* implements LifecycleObserver */ {
  private static final int UNLOCK_TAG = 1;
  private byte[] passhash;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);
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
        passhash = data.getExtras().getByteArray(LoginActivity.PASSWORD_KEY);
      }
    }
  }

  public void login(View view) {
    Intent loginIntent = new Intent(this, LoginActivity.class);
    //startActivityForResult(loginIntent, UNLOCK_TAG);
    startActivity(loginIntent);
  }

  /*
  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  public void onMoveToForeground() {
    PrivateKeys.requestPassHash(this);
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  public void onMoveToBackground() {}
  */
}
