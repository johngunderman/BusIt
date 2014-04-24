package com.tdooner.munificent.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.tdooner.munificent.api.BusItConnection.OnDoneCallback;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

public class GoogleAuth {
    public static final int RESULT_GOOGLE_GOT_PERMISSIONS = 89230589;
    private static final String DEBUG_TAG = "BusIt";
    private static final String AUTH_SETTINGS_FILE = "access_prefs";
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String EMAIL_KEY = "access_email";
    private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";

    private Context context;
    private SharedPreferences prefs;

    public GoogleAuth(Context applicationContext) {
        this.context = applicationContext;
        this.prefs = this.context.getSharedPreferences(AUTH_SETTINGS_FILE, 0);
    }

    public boolean needsToSignIn() {
        return !(this.prefs.contains(EMAIL_KEY) && this.prefs.contains(ACCESS_TOKEN_KEY));
    }

    public String getSavedAuthToken() {
        return this.prefs.getString(ACCESS_TOKEN_KEY, "");
    }

    public String getSavedEmail() {
        return this.prefs.getString(EMAIL_KEY, "");
    }

    public void setPreferredEmail(String accountName) {
        this.prefs.edit().putString(EMAIL_KEY, accountName).commit();
    }

    public Account[] getAccounts() {
        AccountManager mAccountManager = AccountManager.get(context);
        return mAccountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
    }

    // TODO: Instead of passing the accountName (i.e. email) everywhere we should probably just pass
    // around backend accounts
    public void getAuthToken(Activity activity, String accountName, OnDoneCallback<String> callback) {
        Log.d(DEBUG_TAG, "checking logins for " + accountName);

        if (accountName.equals("")) {
            Log.d(DEBUG_TAG, "ERROR: Tried to get auth token for empty account");
            return;
        }

        // Conveniently, we have a cached access token already! Hurrah!
        if (this.getSavedEmail().equals(accountName) && this.getSavedAuthToken().length() > 0) {
            // TODO: Verify that the access token is still valid?
            callback.onDone(this.getSavedAuthToken());
            return;
        }

        new GetTokenTask(activity, callback).execute(accountName);
    }

    private class GetTokenTask extends AsyncTask<String, Void, String> {
        OnDoneCallback<String> callback;
        Activity activity; // must forward onActivityResult with code RESULT_GOOGLE_GOT_PERMISSIONS

        public GetTokenTask(Activity activity, OnDoneCallback<String> callback) {
            this.activity = activity;
            this.callback = callback;
        }

        @Override
        protected String doInBackground(String... arg0) {
            try {
                return GoogleAuthUtil.getToken(GoogleAuth.this.context, arg0[0], SCOPE);
            } catch (UserRecoverableAuthException e) {
                // TODO: This is still a bit saddening, but it might make sense to make any Activity
                // that wants an access token have to forward the onActivityResult.
                this.activity.startActivityForResult(e.getIntent(), RESULT_GOOGLE_GOT_PERMISSIONS);
                return "";
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }

        @Override
        protected void onPostExecute(String accessToken) {
            if (!accessToken.equals("")) {
                Log.d(DEBUG_TAG, "got an auth token: " + accessToken);
                GoogleAuth.this.prefs.edit().putString(ACCESS_TOKEN_KEY, accessToken).commit();
                this.callback.onDone(accessToken);
            }
        }
    }
}
