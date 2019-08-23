package com.michaelkuc6.u2fsafe;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
  private static final int UNLOCK_TAG = 1;
  private static final int U2F_TAG = 2;
  private TextView passwordSet, keyfileExists;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    extractBinaries();

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
        u2fIntent.putExtra(U2FActivity.EXECUTABLE_DIR_KEY, getFilesDir().getAbsolutePath());
        u2fIntent.putExtra(U2FActivity.CACHE_DIR_KEY, getCacheDir().getAbsolutePath());
        startActivityForResult(u2fIntent, U2F_TAG);
      } else passwordSet.setText(R.string.no);
    }
  }

  private String getExecutableDirectory() {
    String abi = Build.SUPPORTED_ABIS[0];
    String folder = null;

    if (abi.contains("armeabi-v7a")) {
      folder = "armeabi-v7a";
    } else if (abi.contains("x86_64")) {
      folder = "x86_64";
    } else if (abi.contains("x86")) {
      folder = "x86";
    } else if (abi.contains("armeabi")) {
      folder = "armeabi";
    } else if (abi.contains("arm64-v8a")) {
      folder = "arm64-v8a";
    } else {
      Toast.makeText(this, "Unknown ABI - please issue a pull request", Toast.LENGTH_LONG).show();
      finish();
    }

    return folder;
  }

  private boolean extractBinaries() {
    String folder = getExecutableDirectory();

    if (folder == null) return false;

    AssetManager assetManager = getAssets();

    for (String binary : getBinaries()) {
      try {
        InputStream in = assetManager.open(folder + "/" + binary);
        File out = new File(getFilesDir(), binary);
        FileOutputStream outStream = new FileOutputStream(out, false);

        int nRead;
        byte[] buff = new byte[1024];

        while ((nRead = in.read(buff)) != -1) {
          outStream.write(buff, 0, nRead);
        }

        outStream.flush();
        outStream.close();

        String[] command =
            new String[] {"su", "-c", "chmod +x " + getFilesDir().getAbsolutePath() + "/" + binary};
        Process chmodProcess = Runtime.getRuntime().exec(command);
        chmodProcess.waitFor();
      } catch (IOException | InterruptedException e) {
        Toast.makeText(
                this,
                "Failed to extract binary \'"
                    + binary
                    + "\' to executing folder: \""
                    + e.getMessage()
                    + "\"",
                Toast.LENGTH_LONG)
            .show();
        finish();
        return false;
      }
    }

    return true;
  }

  private String[] getBinaries() {
    return new String[] {"U2FAndroid_Read", "U2FAndroid_Write"};
  }
}
