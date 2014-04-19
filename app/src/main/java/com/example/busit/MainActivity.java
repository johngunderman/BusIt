package com.example.busit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.busit.api.BusItConnection;
import com.example.busit.api.BusItConnection.OnDoneCallback;
import com.example.busit.auth.GoogleAuth;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends Activity {
    private static final String DEBUG_TAG = "MainActivity";
    private TextView textView;
    private MapView mapView;
    private Button checkInButton;
    private BusMap busMap;
    private JSONObject checkInBus;
    private BusItConnection busItConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if ((new GoogleAuth(this)).needsToSignIn()) {
            this.startActivity(new Intent(this, LoginActivity.class));
        }

        this.busItConnection = new BusItConnection();
        this.textView = (TextView) findViewById(R.id.default_text);
        this.checkInButton = (Button) findViewById(R.id.check_in_button);

        this.initializeMap(savedInstanceState);

        this.checkInBus = null;
        this.checkInButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(DEBUG_TAG, "Checking into the bus...");
                String accessToken = (new GoogleAuth(MainActivity.this)).getSavedAuthToken();
                busItConnection.checkIn(checkInBus, accessToken);
            }
        });

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            busItConnection.getBusData(new OnDoneCallback<JSONObject>() {
                @Override
                public void onDone(JSONObject param) {
                   MainActivity.this.busMap.setBusData(param);
                }
            });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    protected void initializeMap(Bundle savedInstanceState) {
        MapsInitializer.initialize(getApplicationContext());

        this.mapView = (MapView) findViewById(R.id.map_view);
        this.busMap = new BusMap(savedInstanceState, this.mapView, this.textView);
    }
}
