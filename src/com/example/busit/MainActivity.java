package com.example.busit;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends Activity {

    private static final String ALL_BUS_DATA_URL = "http://busit.herokuapp.com/buses";
    private static final String DEBUG_TAG = "MainActivity";
    private TextView textView;
    private MapView mapView;
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.textView = (TextView) findViewById(R.id.default_text);
        this.mapView = (MapView) findViewById(R.id.map_view);
        this.mapView.onCreate(savedInstanceState);

        this.map = this.mapView.getMap();
        MapsInitializer.initialize(getApplicationContext());

        this.map.moveCamera(CameraUpdateFactory.newLatLng(
                new LatLng(37.7745952,-122.456628)));

        this.map.moveCamera(CameraUpdateFactory.zoomTo(18.0f));

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new RenderBusListTask().execute();
        } else {
            Log.d(DEBUG_TAG, "Couldn't connect to the network!");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        this.mapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.mapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mapView.onDestroy();
        this.mapView = null;
    }

    @Override
    public final void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.mapView.onSaveInstanceState(outState);
    }

    @Override
    public final void onLowMemory() {
        super.onLowMemory();
        this.mapView.onLowMemory();
    }

    private class RenderBusListTask extends AsyncTask<Void, Void, JSONObject> {

        private void renderNearbyBusData() {
            JSONObject busData;
            try {
                busData = getNearbyBusData();
                renderBusData(busData);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

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

        private void renderBusData(JSONObject busData) {
            textView.setText(busData.toString());
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            try {
                return getNearbyBusData();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            renderBusData(result);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}