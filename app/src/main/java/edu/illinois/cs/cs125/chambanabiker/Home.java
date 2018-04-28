package edu.illinois.cs.cs125.chambanabiker;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public class Home extends AppCompatActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 0;
    public boolean isDark = false;

    private  GoogleMap mMap;

    Toolbar myToolBar;

    String provider;

    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //General initialization.

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        //Setup the toolbar.

        this.myToolBar = findViewById(R.id.my_toolbar);
        setSupportActionBar(this.myToolBar);

        //Check security.

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION
                    );
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    /**
     * On click for the nearest rack button.
     *
     * @param view the current view.
     */
    public void nearestRack(View view) {
        String message = "Finding Nearest Rack...";

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }


    /**
     * On click for the set location button.
     * @param view the current view.
     */
    public void setLocation(View view) {

        String message = "Setting Location...";

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public String jsontostring() {
        InputStream is = getResources().openRawResource(R.raw.bikeracks);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);

            }
            is.close();
        } catch (IOException e) {

            return null;

        }

        return writer.toString();
    }

    /**
     *
     * @param json String version of json
     */

    public String[][] parseJson(String json) {
        JsonParser parser = new JsonParser();
        JsonObject rootObj = parser.parse(json).getAsJsonObject();
        JsonArray formObj = rootObj.getAsJsonArray("locations");
        String[] latStorage = new String[formObj.size()];
        String[] longStorage = new String[formObj.size()];
        String[] descriptionStorage = new String[formObj.size()];
        String[][] latLongStorage = new String[3][formObj.size()];


        for (int i = 0; i < formObj.size(); i++) {
            JsonElement res = formObj.get(i);
            JsonObject result = res.getAsJsonObject();
            descriptionStorage[i] = result.get("name").getAsString();
            latStorage[i] = result.get("lat").getAsString();
            longStorage[i] = result.get("lng").getAsString();
        }

        latLongStorage[0] = latStorage;

        latLongStorage[1] = longStorage;

        latLongStorage[2] = descriptionStorage;

        return latLongStorage;
    }


    /**
     * On click for the clear location button.
     * @param view the current view.
     */

    public void clearLocation(View view) {
        String message = "Clearing Location...";

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }


    /**
     * Sets up the menu.
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * When the menu item is clicked.
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about: {
                Dialog dialog = new Dialog(this);

                dialog.setContentView(R.layout.popup);

                dialog.show();

                break;
            }

            case R.id.changemode: {
               changeMapStyle();
            }
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Change map style
     */

    public void changeMapStyle() {
        if (!isDark) {
            MapStyleOptions nightStyle = MapStyleOptions.
                    loadRawResourceStyle(this, R.raw.mapstyle_night);

            mMap.setMapStyle(nightStyle);

            isDark = true;

        } else {
            MapStyleOptions lightStyle = MapStyleOptions.
                    loadRawResourceStyle(this, R.raw.mapstyle_light);

            mMap.setMapStyle(lightStyle);

            isDark = false;
        }

    }




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);

        }


        //Find user location and center map on their location with zoom.
        
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria();

        provider = locationManager.getBestProvider(criteria, true);

        Location location = locationManager.getLastKnownLocation(provider);

        double latitude = location.getLatitude();

        double longitude = location.getLongitude();

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude),
                17.0f));
    }
}
