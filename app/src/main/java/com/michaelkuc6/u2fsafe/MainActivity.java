package com.michaelkuc6.u2fsafe;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.michaelkuc6.u2fsafe.utils.PrivateKeys;

public class MainActivity extends AppCompatActivity implements LifecycleObserver {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);
    ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
  }

  @Override
  protected void onPause() {
    super.onPause();
    PrivateKeys.closeInterface(this);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  public void onMoveToForeground() {
    PrivateKeys.requestPassHash(this);
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  public void onMoveToBackground() {}
}
