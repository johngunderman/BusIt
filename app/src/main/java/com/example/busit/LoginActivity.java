package com.example.busit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.busit.api.BusItConnection.OnDoneCallback;
import com.example.busit.auth.GoogleAuth;

public class LoginActivity extends Activity {
    private static final String DEBUG_TAG = "LoginActivity";
    private static final int GOOGLE_AUTH_CODE = 102938102;
    private static final int GOOGLE_GOT_AUTH_CODE = 89230589;
    private Button loginButton;
    private GoogleAuth googleAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        this.loginButton = (Button) findViewById(R.id.login_button);

        this.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleAuth.getAuthToken(new OnDoneCallback<String>() {
                    @Override
                    public void onDone(String param) {
                        LoginActivity.this.finish();
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        GoogleAuth.onActivityResult(requestCode, resultCode, data);
    }

}