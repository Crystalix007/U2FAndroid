package com.michaelkuc6.u2fandroid.ui;

public interface LoginPrompt {
  void setLoginHandler(LoginHandler loginHandler);

  interface LoginHandler {
    void handleLogin(byte[] passwordHash);
  }
}
