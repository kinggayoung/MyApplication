package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.navigation.NavigationView;

import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapView;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.List;

public class DaumMap extends AppCompatActivity {
    private static final String TAG = "DaumMap";

    MapView mapView;

    Geocoder geocoder;
    List<Address> list;

    // Thread
    private int mMainValue = 0;
    private int mSubValue = 0;
    private Handler mHandler;

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

        // Thread
        mHandler = new Handler( ){
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == 0){
                    // View 이용한 작업 구현
                }
            }
        };

        Thread thread = new Thread( new Runnable() { // 익명클래스
            @Override
            public void run() {

                while(true){
                    // thread 할 작업 구현
                    Message msg = Message.obtain();
                    msg.what = 0;
                    msg.arg1 = 1;

                    mHandler.sendEmptyMessage( 0 ); // 데이터 전달, 값은 message의 what에 넣어짐
                    mHandler.sendMessage( msg );
                    try {
                        Thread.sleep( 1000 ); // 1초마다 실행
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        } );
        thread.setDaemon( true );
        thread.start();

        /*
          setDaemon(true)
           - mainThread와 종료 동기화
           - 안 해주면 어플 종료 후에도 작업스레드는 계속 동작함
         */

    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d( TAG, "onLocationChanged: " + location );

            MapPOIItem marker = new MapPOIItem();
            MapPolyline polyline = new MapPolyline( );
            mapView.removeAllPOIItems();


            try {
                geocoder = new Geocoder( getApplicationContext() );
                list = geocoder.getFromLocation( location.getLatitude(), location.getLongitude(), 1 );


            String[] splitStr = list.get( 0 ).toString().split( "," );
            String addr = splitStr[0].substring( splitStr[0].indexOf( "\"" ) + 1, splitStr[0].length() -2 );


            mapView.setMapCenterPoint( MapPoint.mapPointWithGeoCoord( location.getLatitude(), location.getLongitude() ), true );

            marker.setItemName( addr );
            marker.setTag( 0 );
            marker.setMapPoint( MapPoint.mapPointWithGeoCoord( location.getLatitude(), location.getLongitude() ) );
            marker.setMarkerType( MapPOIItem.MarkerType.RedPin );
            mapView.addPOIItem( marker );


            // PolyLine
            polyline.setTag( 1000 );
            polyline.setLineColor( Color.argb(128, 255, 51, 0) );

            polyline.addPoint( MapPoint.mapPointWithGeoCoord( location.getLatitude(), location.getLongitude() ) );
            polyline.addPoint( MapPoint.mapPointWithGeoCoord( location.getLatitude() + 0.01, location.getLongitude()) );
            polyline.addPoint( MapPoint.mapPointWithGeoCoord( location.getLatitude() + 0.01, location.getLongitude()+0.01) );

            mapView.addPolyline( polyline );

            MapPointBounds mapPointBounds = new MapPointBounds( polyline.getMapPoints() );
            int padding = 100;
            mapView.moveCamera( CameraUpdateFactory.newMapPointBounds( mapPointBounds, padding ) );
            } catch (IOException e) {
                e.printStackTrace();
            }
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

/* Thread & Handler
    - Thread : 프로그램 안에서 실행을 담당하는 하나의 흐름
    - MessageQueue : Message를 담는 자료구조
    - Message : Parcelable 형태의 객체
        ex) public final class Message implements Parcelable{
                public static final Creator<Message> CREATOR = null;
                public int arg;
                public Object obj;
                public Messenger replyTo;
            }
     - Looper : MessageQueue에서 Message를 꺼내 Handler로 전달하는 작업
        ㅁ Thread 당 1개, Looper별 MessageQueue를 가짐
        ㅁ 메인스레드에서는 이미 가지나, 작업스레드에서는 직접 작성 및 실행해줘야 함
        ㅁ Looper 생성
            class LooperThread extends Thread{
                public Handler mHandler;

                @Override
                public void run() {
                    Looper.prepare(); // 작업스레드를 위한 Looper 준비함
                    mHandler = new Handler( ){
                        @Override
                        public void handleMessage(@NonNull Message msg) {

                        }
                    };
                    Looper.loop(); // 큐에서 메시지를 꺼내 핸들러로 전달함
                }
            }
      - Handler : 메인스레드에 접근해 UI 수정
        ㅁ MessageQueue에 보낼 데이터 넣고 Looper를 통해 처리할 데이터를 받고 보내는 중간 역할
        ㅁ 기본 생성자를 통해 Handler 생성 시, 해당 Handler를 호출한 Thread의 MessageQueue와 Looper에 자동 연결
        ㅁ 실행 방법 https://humble.tistory.com/14    https://itmining.tistory.com/5
            o 메시지 처리
            o Runnable객체 실행
*/
