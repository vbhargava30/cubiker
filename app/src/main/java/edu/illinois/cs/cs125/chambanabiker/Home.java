package edu.illinois.cs.cs125.chambanabiker;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.ion.Ion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.ExecutionException;

public class Home extends AppCompatActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 0;

    private boolean isDark = false;

    private boolean isLocationSet = false;

    private  GoogleMap mMap;

    Toolbar myToolBar;

    String provider;

    LocationManager locationManager;

    Marker bikeMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //General initialization.

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        //isLocationSet initialization.

        if (getPreferences(MODE_PRIVATE).contains("isLocationSet")) {

            isLocationSet = getPreferences(MODE_PRIVATE).getBoolean("isLocationSet",
                    false);

        }


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

        if (getPreferences(MODE_PRIVATE).contains("isLocationSet")
                && getPreferences(MODE_PRIVATE).getBoolean("isLocationSet",
                false)) {
            String message = "Already set a location!";

            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

            return;
        }

        String message = "Setting Location...";

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

        double[] latAndLong = findUserLocation();

        float latitude = (float) latAndLong[0];

        float longitude = (float) latAndLong[1];


        getPreferences(MODE_PRIVATE).edit().putFloat("latitude", latitude).apply();

        getPreferences(MODE_PRIVATE).edit().putFloat("longitude", longitude).apply();

        bikeMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title("Your bike's location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        getPreferences(MODE_PRIVATE).edit().putBoolean("isLocationSet", true).apply();
    }

    public String jsonToString() {
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
        String[] numberRacks = new String[formObj.size()];
        String[][] latLongStorage = new String[4][formObj.size()];


        for (int i = 0; i < formObj.size(); i++) {
            JsonElement res = formObj.get(i);
            JsonObject result = res.getAsJsonObject();
            descriptionStorage[i] = result.get("name").getAsString();
            latStorage[i] = result.get("lat").getAsString();
            longStorage[i] = result.get("lng").getAsString();
            numberRacks[i] = result.get("number").getAsString();
        }

        latLongStorage[0] = latStorage;

        latLongStorage[1] = longStorage;

        latLongStorage[2] = descriptionStorage;

        latLongStorage[3] = numberRacks;

        return latLongStorage;
    }


    /**
     * On click for the clear location button.
     * @param view the current view.
     */

    public void clearLocation(View view) {
        String message = "Clearing Location...";

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();


        if (getPreferences(MODE_PRIVATE).contains("isLocationSet")
                && getPreferences(MODE_PRIVATE).getBoolean("isLocationSet",
                false)) {
            getPreferences(MODE_PRIVATE).edit().putBoolean("isLocationSet", false).apply();
        }



        bikeMarker.remove();

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

    public double[] findUserLocation() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            Criteria criteria = new Criteria();

            provider = locationManager.getBestProvider(criteria, true);

            Location location = locationManager.getLastKnownLocation(provider);

            double latitude = location.getLatitude();

            double longitude = location.getLongitude();

            return new double[] {latitude, longitude};
        }

        return null;
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

        //Draw map.

        mMap = googleMap;

        //Security and add blue location marker.

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);

        }


        //Find user location and center map on their location with zoom.

        double[] latLong = findUserLocation();

        if (findUserLocation() != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLong[0], latLong[1]),
                    17.0f));

        }

        //Setup bike marker if available.

        float latitudeBike = getPreferences(MODE_PRIVATE).getFloat("latitude", 0);

        float longitudeBike = getPreferences(MODE_PRIVATE).getFloat("longitude", 0);

        if (latitudeBike != 0 && longitudeBike != 0
                && isLocationSet) {
            bikeMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitudeBike, longitudeBike))
                    .title("Your bike's location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        }

        //Setup JSON markers.

        String[][] latLongAndDescriptions = parseJson(jsonToString());

        for (int i = 0; i < latLongAndDescriptions[0].length; i++) {

            double latitudeTemp = Double.valueOf(latLongAndDescriptions[0][i]);

            double longitudeTemp = Double.valueOf(latLongAndDescriptions[1][i]);

            String description = latLongAndDescriptions[2][i];

            int numberRacks = Integer.valueOf(latLongAndDescriptions[3][i]);

            String url = "https://raw.githubusercontent.com/Concept211/Google-Maps-Markers/master/images/marker_red"
                    + Integer.toString(numberRacks) + ".png";

            try {
             Bitmap iconImg = Ion.with(this).
                     load(url)
                     .asBitmap().get();

             iconImg = Bitmap.createScaledBitmap(iconImg, 80, 135, true);

             mMap.addMarker(new MarkerOptions()
                     .position(new LatLng(latitudeTemp, longitudeTemp))
                     .title(description)
                     .icon(BitmapDescriptorFactory.fromBitmap(iconImg)));
            } catch (InterruptedException e){
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }


    }
}
