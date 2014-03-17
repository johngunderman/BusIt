package com.example.busit;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.GoogleAuthUtil;

public class LoginActivity extends Activity {
    private static final String DEBUG_TAG = "LoginActivity";
    private Button loginButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        this.loginButton = (Button) findViewById(R.id.login_button);

        this.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(DEBUG_TAG, "checking logins...");
                AccountManager mAccountManager = AccountManager
                        .get(getApplicationContext());
                Account[] accounts = mAccountManager
                        .getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);

                if (accounts.length == 0) {
                    new AlertDialog.Builder(LoginActivity.this).setMessage(
                            "No Google Account Found :(").show();
                }

                for (int i = 0; i < accounts.length; i++) {
                    Log.d(DEBUG_TAG, accounts[i].name);
                }
            }
        });
    }
}