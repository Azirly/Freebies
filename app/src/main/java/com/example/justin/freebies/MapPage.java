package com.example.justin.freebies;

import java.util.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.os.Bundle;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MapPage extends AppCompatActivity implements OnMapReadyCallback, OnMarkerClickListener, OnMapClickListener, GoogleMap.OnInfoWindowClickListener {


    private static final String TAG = "MapPage";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private Marker mSelectedMarker;
    private List<Marker> eventMarkers = new ArrayList<Marker>();
    private List<Marker> blogMarkers = new ArrayList<Marker>();
    private List<InfoWindowData> blogInfoData = new ArrayList<InfoWindowData>();
    private List<InfoWindowData> eventInfoData = new ArrayList<InfoWindowData>();

    //widget
    private ImageView mGps;
    private Switch simpleSwitch;
    private TextView switchText;

    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch(item.getItemId()) {
                case R.id.navigation_home:
                    Intent intent = new Intent(MapPage.this, MainPage.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                    break;

                case R.id.navigation_events:
                    Intent intent2 = new Intent(MapPage.this, EventsBlogPage.class);
                    intent2.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent2);
                    overridePendingTransition(0,0);
                    break;

                case R.id.navigation_map:
                    break;

                case R.id.navigation_account:
                    Intent intent3 = new Intent(MapPage.this, AccountPage.class);
                    intent3.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent3);
                    overridePendingTransition(0,0);
                    break;
            }
            return false;
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            init();



        }
    }

    private void init(){


        //m.showInfoWindow();

        //showBlogs();
        //fillBlogs();
        //fillEvents();

        simpleSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (simpleSwitch.isChecked()){
                    switchText.setText("Events");
                    hideBlogs();
                    showEvents();
                }
                else{
                    switchText.setText("Blogs");
                    hideEvents();
                    showBlogs();
                }
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked gps icon");
                getDeviceLocation();
            }
        });


        //moveCamera(sydney,DEFAULT_ZOOM);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_page);

        mGps = (ImageView) findViewById(R.id.ic_gps);
        simpleSwitch = (Switch) findViewById(R.id.simpleSwitch);
        switchText = (TextView) findViewById(R.id.switchText);
        switchText.setText("Blogs");
        fireBaseSetup();
        fireBaseSetupEvent();
        getLocationPermission();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);
    }

    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapPage.this);
    }


    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);

                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapPage.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    private void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }


    @Override
    public void onMapClick(LatLng latLng) {
        mSelectedMarker = null;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (mSelectedMarker != null && marker.equals(mSelectedMarker)) {
            mSelectedMarker = null;
            marker.hideInfoWindow();
            return true;
        }
        else{
            mSelectedMarker = marker;
            return false;
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        return;
    }

    public void basicSearch(String find){
        if (simpleSwitch.isChecked()){
            for(Marker marker:eventMarkers){
                if(marker.getTitle().toLowerCase().contains(find.toLowerCase())){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(),DEFAULT_ZOOM));
                    marker.showInfoWindow();
                }
            }
        }
        else{
            for(Marker marker: blogMarkers){
                if(marker.getTitle().toLowerCase().contains(find.toLowerCase())){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(),DEFAULT_ZOOM));
                    marker.showInfoWindow();
                }
            }
        }
    }

    public void hideEvents(){
        for (Marker marker : eventMarkers){
            marker.setVisible(false);
        }
    }

    public void hideBlogs(){
        for (Marker marker : blogMarkers){
            marker.setVisible(false);
        }
    }
    public void fillEvents() {
        eventMarkers.clear();
        for (InfoWindowData winData: eventInfoData){
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(winData.getLatLng()).title(winData.getTitle()).snippet(winData.getDescription());

            InfoWindowGMap customWindow = new InfoWindowGMap(this);
            mMap.setInfoWindowAdapter(customWindow);
            Marker m = mMap.addMarker(markerOptions);
            m.setTag(winData);
            m.setVisible(true);
            eventMarkers.add(m);
        }
    }

    public void fillBlogs() {
       blogMarkers.clear();
        for (InfoWindowData winData: blogInfoData){
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(winData.getLatLng()).title(winData.getTitle()).snippet(winData.getDescription());

            InfoWindowGMap customWindow = new InfoWindowGMap(this);
            mMap.setInfoWindowAdapter(customWindow);
            Marker m = mMap.addMarker(markerOptions);
            m.setTag(winData);
            m.setVisible(true);
            blogMarkers.add(m);
        }
    }

    public void showEvents() {
        if(eventMarkers.size() == 0){
            fillEvents();
        }
        for(Marker m: eventMarkers){
            m.setVisible(true);
        }
    }

    public void showBlogs() {
        if(blogMarkers.size() == 0){
            fillBlogs();
        }
        for(Marker m: blogMarkers){
            m.setVisible(true);
        }
    }

    public void fireBaseSetup(){
        blogInfoData.clear();
        eventInfoData.clear();
        FirebaseDatabase.getInstance().getReference().child("Blog").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    InfoWindowData info = new InfoWindowData();

                    info.setTitle(snapshot.child("Title").getValue().toString());
                    info.setDescription(snapshot.child("Description").getValue().toString());
                    info.setLatLng(new LatLng(Double.parseDouble(snapshot.child("Location").getValue().toString().split(",")[0]), Double.parseDouble(snapshot.child("Location").getValue().toString().split(",")[1])));
                    info.setDate(snapshot.child("Time").getValue().toString());
                    info.setLocation(snapshot.child("Location").getValue().toString());
                    info.setImage(snapshot.child("Image").getValue().toString());

                    blogInfoData.add(info);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void fireBaseSetupEvent(){
        eventInfoData.clear();

        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (int i = 0; i < 25; i++) {

                        InfoWindowData info = new InfoWindowData();

                        if(snapshot.hasChild(Integer.toString(i))){
                            if(snapshot.child(Integer.toString(i)).child("place").hasChild("location")){
                                if(snapshot.child(Integer.toString(i)).child("place").child("location").hasChild("latitude")){
                                    if(snapshot.child(Integer.toString(i)).child("place").child("location").hasChild("longitude")) {
                                        info.setTitle(snapshot.child(Integer.toString(i)).child("name").getValue().toString());
                                        info.setDescription(snapshot.child(Integer.toString(i)).child("description").getValue().toString());
                                        info.setLatLng(new LatLng(Double.parseDouble(snapshot.child(Integer.toString(i)).child("place").child("location").child("latitude").getValue().toString()),
                                                Double.parseDouble(snapshot.child(Integer.toString(i)).child("place").child("location").child("longitude").getValue().toString())));
                                        if(snapshot.child(Integer.toString(i)).child("place").child("location").hasChild("street") &&
                                                snapshot.child(Integer.toString(i)).child("place").child("location").hasChild("city") &&
                                                snapshot.child(Integer.toString(i)).child("place").child("location").hasChild("state") &&
                                                snapshot.child(Integer.toString(i)).child("place").child("location").hasChild("zip")){
                                            info.setLocation(snapshot.child(Integer.toString(i)).child("place").child("location").child("street").getValue().toString() + " " +
                                                    snapshot.child(Integer.toString(i)).child("place").child("location").child("city").getValue().toString() + " " +
                                                    snapshot.child(Integer.toString(i)).child("place").child("location").child("state").getValue().toString() + " " +
                                                    snapshot.child(Integer.toString(i)).child("place").child("location").child("zip").getValue().toString());
                                        }
                                        eventInfoData.add(info);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

    }


}





