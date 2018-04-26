package edu.illinois.cs.cs125.chambanabiker;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Home extends AppCompatActivity implements OnMapReadyCallback {

    public boolean isDark = false;

    private  GoogleMap mMap;

    Toolbar myToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //General initialization.

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        //Setup the toolbar.

        this.myToolBar = findViewById(R.id.my_toolbar);
        setSupportActionBar(this.myToolBar);

        //Check security.



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Button listeners.




    }

    /**
     * On click for the nearest rack button.
     *
     * @param view the current view.
     */
    public void nearestRack(View view) {
        String message = "Clicked Nearest Rack!";

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }


    /**
     * On click for the set location button.
     * @param view the current view.
     */
    public void setLocation(View view) {

        String message = "Clicked Set Location!";

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }


    /**
     * On click for the clear location button.
     * @param view the current view.
     */

    public void clearLocation(View view) {
        String message = "Clicked Clear Location!";

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
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        JsonObject rootObj = parser.parse(R.raw.bikeracks).getAsJsonObject();
        JsonArray formObj = rootObj.getAsJsonArray("tags");

        mMap = googleMap;

        // Add a marker in Chambana and move the camera
        LatLng chambana = new LatLng(40.106309, -88.227198);
        mMap.addMarker(new MarkerOptions().position(chambana).title("Campustown Center"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(chambana));

        //Zoom and autofit.

        mMap.moveCamera(CameraUpdateFactory.
                newLatLngZoom(new LatLng(40.106309, -88.227198), 14.0f));
    }

}
