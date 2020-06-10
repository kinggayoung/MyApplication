package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.security.MessageDigest;

public class DaumMap extends AppCompatActivity {
    private static final String TAG = "DaumMap";

    MapView mapView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_daum_map );


        // 키해시 구하기
        try {
            PackageInfo info = getPackageManager().getPackageInfo( getPackageName(), PackageManager.GET_SIGNATURES );
            for (Signature signature : info.signatures){
                MessageDigest md = MessageDigest.getInstance( "SHA" );
                md.update( signature.toByteArray() );
                Log.d( TAG, "KeyHash : " + Base64.encodeToString( md.digest(), Base64.DEFAULT ) );
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        
        checkPermission( );

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,locationListener);
        
        mapView = new MapView(this);

        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
       // mapView.zoomIn( true );
       // mapView.zoomOut( true );
        mapViewContainer.addView(mapView);
    }
    
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d( TAG, "onLocationChanged: " + location );

            MapPOIItem marker = new MapPOIItem();
            mapView.removeAllPOIItems();
            mapView.setMapCenterPoint( MapPoint.mapPointWithGeoCoord( location.getLatitude(), location.getLongitude() ), true );

            marker.setItemName( "Title" );
            marker.setTag( 0 );
            marker.setMapPoint( MapPoint.mapPointWithGeoCoord( location.getLatitude(), location.getLongitude() ) );
            marker.setMarkerType( MapPOIItem.MarkerType.RedPin );
            mapView.addPOIItem( marker );
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

    public void onClick(View view) {
        finish();
        // finishAffinity() : 앱 종료
    }


}
