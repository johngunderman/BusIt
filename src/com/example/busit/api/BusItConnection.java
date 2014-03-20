package com.example.busit.api;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.AsyncTask;
import android.util.Log;

public class BusItConnection {

    private static final String DEBUG_TAG = "LoginActivity";
    private static final String API_ROOT = "http://busit.herokuapp.com";
    private static final String ALL_BUS_DATA_URL = API_ROOT + "/buses";
    private static final String CHECK_IN_URL = API_ROOT + "/check_ins";

    public void getBusData(final OnDoneCallback<JSONObject> callback) {
        new AsyncTask<Void, Void, JSONObject>() {

            private JSONObject getNearbyBusData() throws IOException, JSONException {
                URL url = new URL(ALL_BUS_DATA_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(DEBUG_TAG, "The response is: " + response);
                InputStream in = new BufferedInputStream(conn.getInputStream());
                Scanner scanner = new Scanner(in);
                String input = "";
                try {
                    input = scanner.next();
                } finally {
                    in.close();
                    scanner.close();
                }
                return new JSONObject(new JSONTokener(input));
            }

            @Override
            protected JSONObject doInBackground(Void... params) {
                JSONObject res = null;
                try {
                    res = getNearbyBusData();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return res;
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                callback.onDone(result);
            }
        }.execute();
    }

    public void checkIn(int busNum, OnDoneCallback<JSONObject> callback) {
    }

    public interface OnDoneCallback<T> {
        public void onDone(T param);
    }
}
