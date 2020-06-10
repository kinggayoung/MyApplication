package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;


import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;
import net.daum.mf.map.gen.DaumMapLibraryAndroidMeta;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {
    private static final String TAG = "MainActivity";

    // private 꼭 입력하기!
    // ToolBar
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;

    // Google Map
    private LocationManager manager;
    private LocationListener listener;
    private GoogleMap mMap;

    double latitude;
    double longitude;

    private Geocoder geocoder;
    List<Address> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        initLayout();

        Google_Map();

        manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                onLocationChange( location );
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    ///////////////// ToolBar
    private void initLayout() {
        // toolbar를 통해 App Bar 생성
        toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );


        // App Bar의 좌츨 영역에 Drawer를 Open하기 위한 icon 추가
        // getSupportActionBar().setDisplayShowCustomEnabled( true );
        // getSupportActionBar().setDisplayHomeAsUpEnabled( true ); // 뒤로가기 버튼을 만들어 줌
        getSupportActionBar().setDisplayShowTitleEnabled( false ); // 기본 앱 타이틀을 없애줌
        getSupportActionBar().setHomeAsUpIndicator( R.mipmap.ic_launcher );

        drawerLayout = findViewById( R.id.main_drawer_root );
        navigationView = findViewById( R.id.nv_main_navigation_root );
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.draw_open,
                R.string.draw_close
        );
        drawerLayout.addDrawerListener( drawerToggle );
        navigationView.setNavigationItemSelectedListener( this );



    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.item1:
                Intent intent = new Intent( this, DaumMap.class );
                startActivity( intent );
                break;

            case R.id.item2:

                Toast.makeText( this, "item2 clicked", Toast.LENGTH_SHORT ).show();
                break;

            case R.id.item3:
                Toast.makeText( this, "item3 clicked", Toast.LENGTH_SHORT ).show();
                break;
        }
        drawerLayout.closeDrawer( GravityCompat.START );
        return false;
    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate( savedInstanceState );
        drawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected( item )) {
            return true;
        }
        return super.onOptionsItemSelected( item );
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged( newConfig );
        drawerToggle.onConfigurationChanged( newConfig );
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen( GravityCompat.START )) {
            drawerLayout.closeDrawer( GravityCompat.START );
        } else {
            super.onBackPressed();
        }

    }

    ///////////////// Google Map
    private void Google_Map() {
        Log.d( TAG, "Google_Map: " );
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById( R.id.map );
        mapFragment.getMapAsync( this );

        geocoder = new Geocoder( this );


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        long minTime = 10000;
        float minDistance = 0;

        checkPermission(); // 권한 확인

        manager.requestLocationUpdates( LocationManager.GPS_PROVIDER, minTime, minDistance, listener );
        manager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, minTime, minDistance, listener );
        Location lastLocation = manager.getLastKnownLocation( LocationManager.GPS_PROVIDER ); // 최근 좌표를 가져옴

        Log.d( TAG, "onButton1Clickaaa" );
        if (lastLocation != null) {
            Double latitude = lastLocation.getLatitude(); // 위도 y
            Double longitude = lastLocation.getLongitude();
            Log.d( TAG, "onLocationChanged: 내 위치 : " + latitude + ", " + longitude );

            mMap.setMyLocationEnabled( true ); // 내 위치 보여줌

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMap.setMyLocationEnabled( false );

        if (manager != null) {
            manager.removeUpdates( listener );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        onMapReady( mMap );

    }


    private void onLocationChange(Location location) {

        try {


            MarkerOptions markerOptions = new MarkerOptions();

            mMap.clear();

            // Double -> double
            // Double 은 레퍼런스 객체임
            // 프리미티브 타입으로 변경할 것!
//                Double latitude = location.getLatitude(); // 위도 y
//                Double longitude = location.getLongitude();
            latitude = location.getLatitude(); // 위도 y
            longitude = location.getLongitude();

            list = geocoder.getFromLocation( latitude, longitude, 1 );

            Log.d( TAG, "onLocationChanged: 내 위치 : " + latitude + ", " + longitude );

            LatLng curPoint = new LatLng( latitude, longitude );
            mMap.animateCamera( CameraUpdateFactory.newLatLngZoom( curPoint, 15 ) );
            mMap.setMapType( GoogleMap.MAP_TYPE_NORMAL ); // 지도 형태

            String[] splitStr = list.get( 0 ).toString().split( "," );
            String addr = splitStr[0].substring( splitStr[0].indexOf( "\"" ) + 1, splitStr[0].length() -2 );

            markerOptions.position( new LatLng( latitude, longitude ) );
            markerOptions.title( addr );
            markerOptions.snippet( latitude +", " + longitude );

            mMap.addMarker( markerOptions );
        } catch (Exception e) {

        }
    }

    private void checkPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission( this,
                Manifest.permission.ACCESS_FINE_LOCATION )
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale( this,
                    Manifest.permission.ACCESS_FINE_LOCATION )) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions( this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1 );

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
    }

}
