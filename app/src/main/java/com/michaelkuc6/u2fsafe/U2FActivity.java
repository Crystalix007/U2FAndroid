package com.michaelkuc6.u2fsafe;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.security.keystore.KeyProperties;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class U2FActivity extends FragmentActivity {
  private static final int IV_BYTE_LENGTH = 16;
  private static final String KEYFILE_NAME = "keys";
  public static final int RESULT_FAILURE = -1;
  public static final int RESULT_KEY_BAD = -2;
  public static final int RESULT_KEYFILE_SAVE_FAILED = -3;
  public static final int RESULT_SUCCESS = 0;
  public static final String PASSHASH_KEY = "passhash";
  private static final String ALGORITHM =
      KeyProperties.KEY_ALGORITHM_AES
          + "/"
          + KeyProperties.BLOCK_MODE_CBC
          + "/"
          + KeyProperties.ENCRYPTION_PADDING_PKCS7;
  private byte[] passhash;
  private TextView u2fDecrypted;

  public static boolean keyfileExists(Activity activity) {
    return new File(activity.getFilesDir(), KEYFILE_NAME).exists();
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    passhash = null;

    if (savedInstanceState == null) passhash = getIntent().getByteArrayExtra(PASSHASH_KEY);
    else passhash = savedInstanceState.getByteArray(PASSHASH_KEY);

    if (passhash == null) {
      setResult(RESULT_FAILURE);
      finish();
      return;
    }

    setContentView(R.layout.fragment_u2f);

    u2fDecrypted = findViewById(R.id.u2fDecrypted_edit);
    String decrypted = decryptFile();
    u2fDecrypted.setText(decrypted);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putByteArray(PASSHASH_KEY, passhash);
    encryptFile(u2fDecrypted.getText().toString());
  }

  private Key genKey() {
    return new SecretKeySpec(passhash, ALGORITHM);
  }

  private Cipher genCipher() {
    Cipher cipher;

    try {
      cipher = Cipher.getInstance(ALGORITHM);
    } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
      setResult(RESULT_FAILURE);
      finish();
      return null;
    }

    return cipher;
  }

  private byte[] getFile() {
    File file = new File(getFilesDir(), KEYFILE_NAME);
    FileInputStream inStream;

    try {
      inStream = new FileInputStream(file);
    } catch (FileNotFoundException e) {
      return new byte[] {};
    }

    int byteLength = (int) file.length();
    byte[] fileContent = new byte[byteLength];

    try {
      inStream.read(fileContent, 0, byteLength);
    } catch (IOException e) {
      Toast.makeText(this, "Keyfile unable to read", Toast.LENGTH_LONG).show();
      setResult(RESULT_FAILURE);
      finish();
      return null;
    }

    return fileContent;
  }

  private void writeFile(byte[] iv, byte[] data) throws IOException {
    if (data == null) {
      return;
    }

    FileOutputStream outStream;

    try {
      outStream = openFileOutput(KEYFILE_NAME, Context.MODE_PRIVATE);
    } catch (FileNotFoundException e) {
      Toast.makeText(this, "Keyfile could not be opened for saving", Toast.LENGTH_LONG).show();
      throw e;
    }

    try {
      outStream.write(iv);
      outStream.write(data);
      outStream.close();
    } catch (IOException e) {
      Toast.makeText(this, "Keyfile could not be saved", Toast.LENGTH_LONG).show();
      throw e;
    }
  }

  private String decryptFile() {
    Cipher dCipher = genCipher();

    try {
      byte[] fileContents = getFile();

      if (fileContents == null) return null;
      if (fileContents.length < IV_BYTE_LENGTH) return (fileContents.length == 0) ? "" : null;

      byte[] ivBytes = Arrays.copyOfRange(fileContents, 0, IV_BYTE_LENGTH);
      byte[] message = Arrays.copyOfRange(fileContents, IV_BYTE_LENGTH, fileContents.length);

      IvParameterSpec iv = new IvParameterSpec(ivBytes);
      dCipher.init(Cipher.DECRYPT_MODE, genKey(), iv);

      return new String(dCipher.doFinal(message));
    } catch (IllegalBlockSizeException
        | BadPaddingException
        | InvalidAlgorithmParameterException
        | InvalidKeyException e) {
      Toast.makeText(this, "Unable to decrypt keyfile", Toast.LENGTH_LONG).show();
      setResult(RESULT_KEY_BAD);
      finish();
      return null;
    }
  }

  private void encryptFile(String text) {
    Cipher eCipher = genCipher();

    try {
      eCipher.init(Cipher.ENCRYPT_MODE, genKey());
      byte[] finalResult = eCipher.doFinal(text.getBytes());
      writeFile(eCipher.getIV(), finalResult);
    } catch (IllegalBlockSizeException
        | BadPaddingException
        | IOException
        | InvalidKeyException e) {
      Toast.makeText(this, "Unable to encrypt keyfile", Toast.LENGTH_LONG).show();
      setResult(RESULT_KEYFILE_SAVE_FAILED);
      finish();
    }
  }
}
