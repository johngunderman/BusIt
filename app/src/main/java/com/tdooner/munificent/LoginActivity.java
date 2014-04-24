package com.tdooner.munificent;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.AccountPicker;
import com.tdooner.munificent.R;
import com.tdooner.munificent.api.BusItConnection.OnDoneCallback;
import com.tdooner.munificent.auth.GoogleAuth;

public class LoginActivity extends Activity {
    private static final String DEBUG_TAG = "LoginActivity";
    private static final int RESULT_ACCOUNT_PICKER = 102938102;
    private Button loginButton;
    private GoogleAuth googleAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        this.loginButton = (Button) findViewById(R.id.login_button);
        this.googleAuth = new GoogleAuth(this.getApplicationContext());

        this.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String preferredEmail = LoginActivity.this.googleAuth.getSavedEmail();

                if (preferredEmail.equals("")) {
                    Account[] accounts = LoginActivity.this.googleAuth.getAccounts();
                    if (accounts.length == 1) {
                        preferredEmail = accounts[0].name;
                        LoginActivity.this.googleAuth.setPreferredEmail(preferredEmail);
                        LoginActivity.this.googleAuth.getAuthToken(LoginActivity.this, preferredEmail, new FinishLogin());
                    } else {
                        Intent intent = AccountPicker.newChooseAccountIntent(
                                null, null, new String[]{"com.google"}, false, null, null, null, null);
                        LoginActivity.this.startActivityForResult(intent, RESULT_ACCOUNT_PICKER);
                    }
                } else {
                    LoginActivity.this.googleAuth.getAuthToken(LoginActivity.this, preferredEmail, new FinishLogin());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_ACCOUNT_PICKER:
                String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                this.googleAuth.setPreferredEmail(accountName);
                LoginActivity.this.googleAuth.getAuthToken(LoginActivity.this, accountName, new FinishLogin());
                break;
            case GoogleAuth.RESULT_GOOGLE_GOT_PERMISSIONS:
                LoginActivity.this.googleAuth.getAuthToken(LoginActivity.this, this.googleAuth.getSavedEmail(), new FinishLogin());
                break;
        }
    }

    private class FinishLogin implements OnDoneCallback<String> {
        @Override
        public void onDone(String accessToken) {
            // todo send the accessToken back in the bundle
            LoginActivity.this.setResult(RESULT_OK, new Intent().putExtra("accessToken", accessToken));
            LoginActivity.this.finish();
        }
    }
}