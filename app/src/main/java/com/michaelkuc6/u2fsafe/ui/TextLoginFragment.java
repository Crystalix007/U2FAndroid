package com.michaelkuc6.u2fsafe.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.michaelkuc6.u2fsafe.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TextLoginFragment extends DialogFragment implements LoginPrompt {
  private static final String ARG_TITLE_TEXT = "titleText";
  private static final String ARG_SUBTITLE_TEXT = "subtitleText";
  private static final String ARG_BUTTON_TEXT = "buttonText";
  private String titleText, subtitleText, buttonText;
  private EditText passwordField;
  private LoginHandler loginHandler;

  public TextLoginFragment() {}

  public static TextLoginFragment createInstance(String title, String subtitle, String buttonText) {
    TextLoginFragment instance = new TextLoginFragment();
    Bundle arguments = new Bundle();
    arguments.putString(ARG_TITLE_TEXT, title);
    arguments.putString(ARG_SUBTITLE_TEXT, subtitle);
    arguments.putString(ARG_BUTTON_TEXT, buttonText);
    instance.setArguments(arguments);
    return instance;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() == null) {
      titleText = savedInstanceState.getString(ARG_TITLE_TEXT);
      subtitleText = savedInstanceState.getString(ARG_SUBTITLE_TEXT);
      buttonText = savedInstanceState.getString(ARG_BUTTON_TEXT);
    } else {
      titleText = getArguments().getString(ARG_TITLE_TEXT);
      subtitleText = getArguments().getString(ARG_SUBTITLE_TEXT);
      buttonText = getArguments().getString(ARG_BUTTON_TEXT);
    }
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_login, container, false);
    passwordField = view.findViewById(R.id.password_edit);
    passwordField.setText("");

    Button loginButton = view.findViewById(R.id.login_button);
    loginButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            handleLogin();
          }
        });

    TextView title = view.findViewById(R.id.login_title_text);
    TextView subtitle = view.findViewById(R.id.login_subtitle_text);

    title.setText(titleText);
    subtitle.setText(subtitleText);
    loginButton.setText(buttonText);

    return view;
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(ARG_TITLE_TEXT, titleText);
    outState.putString(ARG_SUBTITLE_TEXT, subtitleText);
    outState.putString(ARG_BUTTON_TEXT, buttonText);
  }

  @Override
  public void setLoginHandler(LoginHandler loginHandler) {
    this.loginHandler = loginHandler;
  }

  private void handleLogin() {
    loginHandler.handleLogin(hashPassword(passwordField.getText().toString()));
  }

  @Override
  public void onPause() {
    super.onPause();
    this.dismissAllowingStateLoss();
  }

  private static byte[] hashPassword(final String password) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
      messageDigest.update(password.getBytes());
      return messageDigest.digest();
    } catch (NoSuchAlgorithmException e) {
      return null;
    }
  }
}
