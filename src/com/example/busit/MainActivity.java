package com.example.busit;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.JsonReader;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String ALL_BUS_DATA_URL = "http://busit.herokuapp.com/busses";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ConnectivityManager connMgr = (ConnectivityManager) 
                getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                renderNearbyBusData();
            } else {
                // display error
            }
    }
    
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
    	URLConnection urlConnection = url.openConnection();
		InputStream in = new BufferedInputStream(urlConnection.getInputStream());
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
    	TextView view = new TextView(this);
    	view.setText(busData.toString().toCharArray(), 0, busData.toString().length());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
