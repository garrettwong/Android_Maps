package com.example.gwm.maps;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnPoiClickListener,
        LocationListener {

    private boolean initialLocationSet = false;
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;

    private LatLng prev;
    private float totalDistance = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //Initializing googleApiClient
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();


        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(getApplicationContext());
        writeDatabaseData(mDbHelper);
        readDatabaseData(mDbHelper);

        // instantiate location listener
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);
    }

    private void writeDatabaseData(FeedReaderDbHelper mDbHelper) {
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

// Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, "My Title");
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE, "Subtitle2");

// Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);
    }

    private void readDatabaseData(FeedReaderDbHelper mDbHelper) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                FeedReaderContract.FeedEntry._ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE,
                FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE
        };

// Filter results WHERE "title" = 'My Title'
        String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE + " = ?";
        String[] selectionArgs = { "My Title" };

// How you want the results sorted in the resulting Cursor
        String sortOrder =
                FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE + " DESC";

        Cursor cursor = db.query(
                FeedReaderContract.FeedEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        List itemIds = new ArrayList<>();
        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry._ID));
            itemIds.add(itemId);
        }
        cursor.close();

    }

    @Override
    public void onLocationChanged(Location location) {
        if (!initialLocationSet) {
            Toast.makeText(getApplicationContext(), "Latitude:" + location.getLatitude() +
                                                    ", Longitude:" + location.getLongitude(),
                                                    Toast.LENGTH_SHORT).show();

            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
            mMap.addMarker(new MarkerOptions().position(currentLatLng).title("It's Me!"));

            prev = currentLatLng;

            initialLocationSet = true;
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

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
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));

        LatLng napa = new LatLng(38.2975, -122.2869);
        mMap.addMarker(new MarkerOptions().position(napa).title("Marker in Napa, CA"));

        LatLng irvine = new LatLng(33.6846, -117.8265);
        prev = irvine;
        mMap.addMarker(new MarkerOptions().position(irvine).title("Marker in Irvine, CA"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(irvine));

        mMap.setOnPoiClickListener(this);
    }

//    private void setCurrentLocation() {
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(getApplicationContext(), "Invalid Permissions: ACCESS_FINE_LOCATION", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
//
//        if (currentLocation != null) {
//            LatLng currentLatLgn = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//
//            mMap.addMarker(new MarkerOptions().position(currentLatLgn).title("It's Me!"));
//
//            prev = currentLatLgn;
//        }
//    }

    private void moveMap(LatLng moveToLatLgn) {
        /**
         * Creating the latlng object to store lat, long coordinates
         * adding marker to map
         * move the camera with animation
         */
        LatLng latLng = moveToLatLgn;
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title("Marker in India"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap.getUiSettings().setZoomControlsEnabled(true);


    }

    @Override
    public void onPoiClick(PointOfInterest poi) {
        LatLng clickedLocation = new LatLng(poi.latLng.latitude, poi.latLng.longitude);

        Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(prev, clickedLocation)
                .width(5)
                .color(Color.RED));

        totalDistance += calculationByDistance(prev, clickedLocation);

        // update prev
        prev = clickedLocation;

        Toast.makeText(getApplicationContext(), "Current distance: " + totalDistance + " km.\n" +
                        "Clicked: " +
                        poi.name + "\nPlace ID:" + poi.placeId +
                        "\nLatitude:" + poi.latLng.latitude +
                        " Longitude:" + poi.latLng.longitude,
                Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MapsDirectionActivity.class);
        startActivity(intent);
    }

    public double calculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;//radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec + " Meter   " + meterInDec);

        return Radius * c;
    }

}
