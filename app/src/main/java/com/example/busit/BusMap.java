package com.example.busit;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BusMap {
    MapView mapView;
    GoogleMap map;
    JSONObject busData;
    JSONArray busLocations; // populated from busData (TODO: Improve this?)
    TextView overviewTextView;
    JSONObject checkInBus;

    public BusMap(Bundle savedInstanceState, MapView mapView, final TextView overviewTextView) {
        this.mapView = mapView;
        this.overviewTextView = overviewTextView;
        this.mapView.onCreate(savedInstanceState);

        this.map = this.mapView.getMap();
        if (this.map == null) {
            return;
        }

        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(37.7745952, -122.456628)));
                map.moveCamera(CameraUpdateFactory.zoomTo(18.0f));
            }
        });

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            public JSONObject getClosestBus(LatLng location) {
                double distance = Float.MAX_VALUE;
                JSONObject closestBus = null;

                try {
                    for (int i = 0; i < busLocations.length(); i++) {
                        JSONObject bus = (JSONObject) busLocations.get(i);
                        double lat = bus.getDouble("lat");
                        double lon = bus.getDouble("lon");

                        double thisDistance = Math.sqrt(Math.pow((location.latitude - lat), 2)
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
            public void onMapClick(final LatLng arg0) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject closestBus = getClosestBus(arg0);
                        try {
                            if (closestBus != null) {
                                checkInBus = closestBus;
                                overviewTextView.setText((String) closestBus.get("route"));
                            } else {
                                throw new JSONException("No Closest Bus D:");
                            }
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    }
                }).run();
            }
        });
    }

    public void setBusData(JSONObject busData) {
        this.busData = busData;
        this.renderBusData();
    }

    public JSONObject getClosestBus() {
        return this.checkInBus;
    }

    private void renderBusData() {
        overviewTextView.setText(busData.toString());

        try {
            this.busLocations = busData.getJSONArray("results");
            for (int i = 0; i < this.busLocations.length(); i++) {
                JSONObject busLocation = (JSONObject) this.busLocations.get(i);
                double lat = busLocation.getDouble("lat");
                double lon = busLocation.getDouble("lon");
                LatLng location = new LatLng(lat, lon);

                // map.addMarker((new MarkerOptions()).position());
                map.addCircle((new CircleOptions()).radius(10.0).fillColor(Color.RED).center(
                        location).strokeWidth(3f));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
