package com.example.busit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
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
    private GoogleMap map;
    private JSONArray busLocations;
    private JSONObject checkInBus;
    private BusItConnection busItConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.busItConnection = new BusItConnection();
        this.textView = (TextView) findViewById(R.id.default_text);
        this.mapView = (MapView) findViewById(R.id.map_view);
        this.checkInButton = (Button) findViewById(R.id.check_in_button);
        this.mapView.onCreate(savedInstanceState);
        this.map = this.mapView.getMap();
        MapsInitializer.initialize(getApplicationContext());
        this.map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(
                37.7745952, -122.456628)));
        this.map.moveCamera(CameraUpdateFactory.zoomTo(18.0f));
        this.map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            public JSONObject getClosestBus(LatLng location) {
                double distance = Float.MAX_VALUE;
                JSONObject closestBus = null;

                try {
                    for (int i = 0; i < busLocations.length(); i++) {
                        JSONObject bus = (JSONObject) busLocations.get(i);
                        double lat = bus.getDouble("lat");
                        double lon = bus.getDouble("lon");

                        double thisDistance = Math.sqrt(Math.pow(
                                (location.latitude - lat), 2)
                                + Math.pow((location.longitude - lon), 2));

                        if (thisDistance < distance) {
                            distance = thisDistance;
                            closestBus = bus;
                        }
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }

                return closestBus;
            }

            @Override
            public void onMapClick(LatLng arg0) {
                JSONObject closestBus = getClosestBus(arg0);

                try {
                    if (closestBus != null) {
                        checkInBus = closestBus;
                        textView.setText((String) checkInBus.get("route"));
                    } else {
                        throw new JSONException("No Closest Bus D:");
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        });

        this.checkInBus = null;
        this.checkInButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // todo API request here!
            }
        });

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            busItConnection.getBusData(new OnDoneCallback<JSONObject>() {
				@Override
				public void onDone(JSONObject param) {
			    	MainActivity.this.renderBusData(param);
				}
			});
        } else {
            Log.d(DEBUG_TAG, "Couldn't connect to the network!");
        }
    }

    private void renderBusData(JSONObject busData) {
        textView.setText(busData.toString());

        try {
            busLocations = busData.getJSONArray("results");
            for (int i = 0; i < busLocations.length(); i++) {
                JSONObject busLocation = (JSONObject) busLocations.get(i);
                double lat = busLocation.getDouble("lat");
                double lon = busLocation.getDouble("lon");
                LatLng location = new LatLng(lat, lon);

                // map.addMarker((new MarkerOptions()).position());
                map.addCircle((new CircleOptions()).radius(10.0)
                        .fillColor(Color.RED).center(location)
                        .strokeWidth(3f));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

}