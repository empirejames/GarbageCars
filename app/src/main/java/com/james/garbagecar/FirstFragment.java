package com.james.garbagecar;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.kml.KmlLayer;

import java.util.ArrayList;

import static android.content.Context.LOCATION_SERVICE;


public class FirstFragment extends Fragment implements OnMapReadyCallback {

    GoogleMap mGooglemap;
    MapView mMapView;
    View mView;
    String TAG = FirstFragment.class.getSimpleName();
    ArrayList<GarbageCar> garbageCars = new ArrayList<GarbageCar>();

    private LocationManager mLocationManager;
    public static final int LOCATION_UPDATE_MIN_DISTANCE = 10;
    public static final int LOCATION_UPDATE_MIN_TIME = 5000;
    public FirstFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG,"Fragment onCreate");
        String[] perms = {android.Manifest.permission.ACCESS_FINE_LOCATION};
        mLocationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(TAG,"onCreateView");
       mView =  inflater.inflate(R.layout.first_fragment, container, false);
        mMapView = (MapView) mView.findViewById(R.id.map);
        if(mMapView!=null){
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
            Log.e(TAG,"mMapView is success");
        }else{
            Log.e(TAG,"mMapView null");
        }



        return mView;
    }

    @Override
    public void onViewCreated(View view , Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        mMapView = (MapView) mView.findViewById(R.id.map);
        if(mMapView!=null){
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        getCurrentLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        MapsInitializer.initialize(getContext());
        mGooglemap = googleMap;
//        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//        googleMap.addMarker(new MarkerOptions().position(new LatLng(40.689247,-74.044502)).title("Statue of Lib").snippet("I hope go there"));
//        CameraPosition Liberty = CameraPosition.builder().target(new LatLng(40.689247,-74.044502)).zoom(16).bearing(0).tilt(45).build();
//        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(Liberty));

    }
    private void getCurrentLocation() {
        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Location location = null;
        if (!(isGPSEnabled || isNetworkEnabled)){
           // Toast.makeText(getActivity().getApplicationContext(), "這是一個Toast......", Toast.LENGTH_LONG).show();
        }
        //Snackbar.make(R.layout.activity_maps, "error_location_provider", Snackbar.LENGTH_LONG).show();
        else {
            if (isNetworkEnabled) {

                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
                location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (isGPSEnabled) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
                location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
        if (location != null){
            drawMarker(location);
        }
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                //Log.e(TAG, "onLocationChanged : " + String.format("%f, %f", location.getLatitude(), location.getLongitude()));
                drawMarker(location);
                drawCars();
                mLocationManager.removeUpdates(mLocationListener);
            } else {
                Log.e(TAG,"Location is null");
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
    };

    private void drawCars(){
        garbageCars = ((MainActivity)getActivity()).get_garbageData();

        LatLng gps;
        for(int i=0; i<garbageCars.size();i++){
            gps = new LatLng(Double.valueOf(garbageCars.get(i).getLatitude()),Double.valueOf(garbageCars.get(i).getLongitude()));
            mGooglemap.addMarker(new MarkerOptions()
                    .position(gps)
                    .snippet("更新時間 : " + garbageCars.get(i).getTime().split(" ")[1]) //15:30:00
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.cars))
                    .title("車牌 : " + garbageCars.get(i).getCar() + " (" + garbageCars.get(i).getDistance()+ ")"));
        }


    }
    private void drawMarker(Location location) {
        Log.e(TAG, " Start DrawMaker ...");
        if (mGooglemap != null) {
            //mGooglemap.clear();
            try{
                LatLng gps = new LatLng(location.getLatitude(), location.getLongitude());
                mGooglemap.addMarker(new MarkerOptions()
                        .position(gps)
                        .title("我在這"));
                mGooglemap.moveCamera(CameraUpdateFactory.newLatLng(gps));
                mGooglemap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 16));
                mGooglemap.getUiSettings().setZoomControlsEnabled(true);  // 右下角的放大縮小功能
                mGooglemap.getUiSettings().setCompassEnabled(true);       // 左上角的指南針，要兩指旋轉才會出現
                mGooglemap.getUiSettings().setMapToolbarEnabled(true);    // 右下角的導覽及開啟 Google Map功能
            }catch (Exception e){

            }
        }
    }

}