package com.example.busit.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.example.busit.api.BusItConnection.OnDoneCallback;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;

public class GoogleAuth {
    private static final String DEBUG_TAG = "BusIt";
    private static final int GOOGLE_GOT_AUTH_CODE = 89230589;
    private static final int GOOGLE_ACCOUNT_PICKER = 102938102;
    private static final String AUTH_SETTINGS_FILE = "access_prefs";
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String EMAIL_KEY = "access_email";
    private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";
    public static Activity context;
    private static OnDoneCallback<String> callback; // XXX Store this callback so it can be called onActivityResult

    private static SharedPreferences prefs() {
        return context.getSharedPreferences(AUTH_SETTINGS_FILE, 0);
    }

    public static boolean needsToSignIn() {
        return !prefs().contains(ACCESS_TOKEN_KEY);
    }

    public static String getSavedAuthToken() {
        return prefs().getString(ACCESS_TOKEN_KEY, "");
    }

    public static String getSavedEmail() {
        return prefs().getString(EMAIL_KEY, "");
    }

    public static void getAuthToken(OnDoneCallback<String> callback) {
        Log.d(DEBUG_TAG, "checking logins...");

        if (GoogleAuth.getSavedEmail().length() > 0) {
            if (getSavedAuthToken().length() > 0) {
                Log.d(DEBUG_TAG, "there's an auth token already!");
                callback.onDone(getSavedAuthToken()); // todo: verify it is still valid?
                return;
            }
        }

        AccountManager mAccountManager = AccountManager.get(context);
        Account[] accounts = mAccountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);

        if (accounts.length == 0) {
            // I believe the user will be prompted to connect their account at this point?
            // new AlertDialog.Builder(this.context).setMessage("No Google Account Found :(").show();
        } else if (accounts.length == 1) {
            new GetTokenTask(callback).execute(accounts[0].name);
        } else if (accounts.length > 1) {
            Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                    new String[]{"com.google"}, false, null, null, null, null);
            GoogleAuth.callback = callback;
            context.startActivityForResult(intent, GOOGLE_ACCOUNT_PICKER);
        }
    }

    // TODO: This might be a weird way of approaching this.
    // I'm leaving this comment here so that 3-year-future-me will look back
    // with consternation.
    public static void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == GOOGLE_ACCOUNT_PICKER) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            Log.d(DEBUG_TAG, "Logging in as: " + accountName);
            new GetTokenTask(GoogleAuth.callback).execute(accountName);
            GoogleAuth.callback = null;
        }
        if (requestCode == GOOGLE_GOT_AUTH_CODE) {
            Log.d(DEBUG_TAG, "GOT AUTH TOKEN WOOHOOO: " + data.toString());
        }
    }

    private static class GetTokenTask extends AsyncTask<String, Void, String> {
        OnDoneCallback<String> callback;

        public GetTokenTask(OnDoneCallback<String> callback) {
            this.callback = callback;
        }

        @Override
        protected String doInBackground(String... arg0) {
            try {
                prefs().edit().putString(EMAIL_KEY, arg0[0]).commit();
                return GoogleAuthUtil.getToken(context, arg0[0], SCOPE);
            } catch (UserRecoverableAuthException e) {
                context.startActivityForResult(e.getIntent(), GOOGLE_GOT_AUTH_CODE);
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String r) {
            Log.d(DEBUG_TAG, "got an auth token: " + r);
            prefs().edit().putString(ACCESS_TOKEN_KEY, r).commit();
            this.callback.onDone(r);
        }
    }
}
