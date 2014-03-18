package com.example.busit;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;

public class LoginActivity extends Activity {
    private static final String DEBUG_TAG = "LoginActivity";
    private static final int GOOGLE_AUTH_CODE = 102938102;
    private static final int GOOGLE_GOT_AUTH_CODE = 89230589;
    private Button loginButton;
    private final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";

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

                Intent intent = AccountPicker.newChooseAccountIntent(null,
                        null, new String[] { "com.google" }, false, null, null,
                        null, null);
                LoginActivity.this.startActivityForResult(intent,
                        GOOGLE_AUTH_CODE);

                // for (int i = 0; i < accounts.length; i++) {
                // Log.d(DEBUG_TAG, accounts[i].name);
                // }
            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode,
            final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_AUTH_CODE) {
            String accountName = data
                    .getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            Log.d(DEBUG_TAG, "Logged in as: " + accountName);
            new GetTokenTask().execute(accountName);
        }
        if (requestCode == GOOGLE_GOT_AUTH_CODE) {
            Log.d(DEBUG_TAG, "GOT AUTH TOKEN WOOHOOO: " + data.toString());
        }
    }

    private class GetTokenTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... arg0) {
            try {
                return GoogleAuthUtil.getToken(LoginActivity.this, arg0[0],
                        SCOPE);
            } catch (UserRecoverableAuthException e) {
                LoginActivity.this.startActivityForResult(e.getIntent(),
                        GOOGLE_GOT_AUTH_CODE);
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String r) {
            Log.d(DEBUG_TAG, "got an auth token: " + r);
            LoginActivity.this.startActivity(new Intent(LoginActivity.this
                    .getApplicationContext(), MainActivity.class));
        }
    }
}