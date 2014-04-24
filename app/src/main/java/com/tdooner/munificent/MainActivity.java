package com.tdooner.munificent;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tdooner.munificent.R;
import com.tdooner.munificent.api.BusItConnection;
import com.tdooner.munificent.api.BusItConnection.OnDoneCallback;
import com.tdooner.munificent.auth.GoogleAuth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;

public class MainActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
    private static final String DEBUG_TAG = "MainActivity";
    private static final int RESULT_GET_AUTH_TOKEN = 23890299;
    private TextView textView;
    private MapView mapView;
    private BusMap busMap;
    private BusItConnection busItConnection;
    private LocationClient locationClient;
    private GoogleAuth auth;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = new GoogleAuth(this.getApplicationContext());

        this.textView = (TextView) findViewById(R.id.default_text);
        this.locationClient = new LocationClient(this, this, this);
        this.locationClient.connect();

        if (auth.needsToSignIn()) {
            Log.d(DEBUG_TAG, "email " + auth.getSavedEmail() + " | access " + auth.getSavedAuthToken());
            this.startActivityForResult(new Intent(this, LoginActivity.class), RESULT_GET_AUTH_TOKEN);
        } else {
            Log.d(DEBUG_TAG, "no need to login for " + auth.getSavedEmail() + ":" + auth.getSavedAuthToken());
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.initializeMap(savedInstanceState);
                MainActivity.this.busItConnection = new BusItConnection(MainActivity.this.getApplicationContext());
                MainActivity.this.busItConnection.getBusData(new OnDoneCallback<JSONObject>() {
                    @Override
                    public void onDone(JSONObject param) {
                        MainActivity.this.busMap.setBusData(param);
                    }
                });
            }
        }).run();

        Button checkInButton = (Button) findViewById(R.id.check_in_button);
        checkInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(DEBUG_TAG, "Checking into the bus...");

                        busItConnection.checkIn(MainActivity.this.busMap.getClosestBus(), auth.getSavedAuthToken());
                    }
                }).run();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        this.locationClient.disconnect();
        super.onStop();
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

    @Override
    public void onConnected(Bundle bundle) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Location l = MainActivity.this.locationClient.getLastLocation();
                MainActivity.this.locationClient.requestLocationUpdates(LocationRequest.create(), new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (location != null) {
                            MainActivity.this.busMap.setUserLocation(location);
                        }
                    }
                });
            }
        }).run();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_GET_AUTH_TOKEN:
                String accessToken = data.getStringExtra("accessToken");
                Log.d(DEBUG_TAG, "MainActivity got auth token back");
                Log.d(DEBUG_TAG, accessToken);
                busItConnection.registerWithBackend(getApplicationContext(), accessToken);
                break;
            default:
                Log.d(DEBUG_TAG, "WTF?");
        }
    }

    @Override
    public void onDisconnected() {
        // this is here for google play services needed for location, maybe find a way to move to BusMap?
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // this is here for google play services needed for location, maybe find a way to move to BusMap?
    }
}
