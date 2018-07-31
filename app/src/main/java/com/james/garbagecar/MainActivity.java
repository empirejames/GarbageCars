package com.james.garbagecar;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.james.garbagecar.network.OnGetDataListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements LocationListener{
    private String GARBAGE_URL = "http://data.ntpc.gov.tw/od/data/api/28AB4122-60E1-4065-98E5-ABCCB69AACA6?$format=json";
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    final private int REQUEST_CODE_ASK_ALL = 122;
    private LocationManager lms;
    Double mLongitude, mLatitude;
    private String TAG = MainActivity.class.getSimpleName();
    ArrayList<GarbageCar> garbageCars = new ArrayList<GarbageCar>();
    TextView txt_time;
    Timer timer ;
    MyReceiver receiver;
    ViewPagerAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        checkPermission();
        locationServiceInitial();
        timer = new Timer(true);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        txt_time = (TextView)findViewById(R.id.txt_time);
        FirstFragment firestFragment = new FirstFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("flag", true);
        firestFragment.setArguments(bundle);


        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        Intent intent = new Intent(this, LongRunningService.class);
        startService(intent);
        new AsyncHttpTask().execute(GARBAGE_URL);
        receiver = new MyReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction("android.intent.action.test");
        registerReceiver(receiver,filter);
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Log.e(TAG,"OnReceiver");
            new AsyncHttpTask().execute(GARBAGE_URL);
            Bundle bundle=intent.getExtras();
            String a = bundle.getString("i");
            Log.e(TAG,"Get I : " + a);
            txt_time.setText("更新時間 : " + a);
            adapter.notifyDataSetChanged();
        }
        public MyReceiver(){
            System.out.println("MyReceiver");
        }

    }
    public void checkPermission() {
        Log.e(TAG,"checkPermission...");
        lms = (LocationManager) (this.getSystemService(Context.LOCATION_SERVICE));
        if (lms.isProviderEnabled(LocationManager.GPS_PROVIDER) || lms.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //如果GPS或網路定位開啟，呼叫locationServiceInitial()更新位置
            locationServiceInitial();
        } else {
            locationServiceInitial();
            Toast.makeText(this, "請開啟定位服務", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));    //開啟設定頁面
        }
    }
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.test");
        registerReceiver(receiver, filter);
    }

    private void locationServiceInitial() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    || (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    ) {
                requestPermissions(new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                }, REQUEST_CODE_ASK_ALL);
                finish();
            } else {
                Location location = lms.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);    ;
                if (location == null) {
                    location = lms.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                getLocation(location);

            }
        }else {
            Location location  =lms.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);    ;
            if (location == null) {
                location = lms.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            getLocation(location);
        }
    }
    public ArrayList<GarbageCar> get_garbageData() {
        return garbageCars;
    }
    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... urls) {
            Integer result = 0;
            garbageCars.clear();
            String lineid, car, time, location, longitude, latitude, cityid, cityname;
            try{

                Log.e(TAG,"Get garbage Data");
                String json = Jsoup.connect(urls[0]).ignoreContentType(true).execute().body();
                //String output = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonObject = array.getJSONObject(i);
                    lineid = jsonObject.getString("lineid");
                    car = jsonObject.getString("car");
                    time = jsonObject.getString("time");
                    location = jsonObject.getString("location");
                    longitude = jsonObject.getString("longitude");
                    latitude = jsonObject.getString("latitude");
                    cityid = jsonObject.getString("cityid");
                    cityname = jsonObject.getString("cityname");
                    String distanceFin = DistanceText(Distance(Double.parseDouble(longitude),Double.parseDouble(latitude), mLongitude, mLatitude));
                    garbageCars.add(new GarbageCar(lineid,car,time,location,longitude,latitude,cityid,cityname,distanceFin));
                }
            }catch (Exception e){
                Log.e(TAG,"Exception : " + e.toString());
            }

            return result;
        }
        @Override
        protected void onPostExecute(Integer result) {
            Log.e(TAG,"Main activity : " + garbageCars.size());
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
    private void getLocation(Location location) {    //將定位資訊顯示在畫面中
        if (location != null) {
            mLongitude = location.getLongitude();    //取得經度
            mLatitude = location.getLatitude();    //取得緯度
            //Log.e(TAG, longitude + " .. " + latitude);
        } else {
            Toast.makeText(this, "無法定位座標", Toast.LENGTH_LONG).show();
        }
    }
    public double Distance(double longitude1, double latitude1, double longitude2, double latitude2) {
        //Log.e(TAG, longitude1 + "." + latitude1 + " V.S. " + longitude2 + "." + latitude2);
        double radLatitude1 = latitude1 * Math.PI / 180;
        double radLatitude2 = latitude2 * Math.PI / 180;
        double l = radLatitude1 - radLatitude2;
        double p = longitude1 * Math.PI / 180 - longitude2 * Math.PI / 180;
        double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(l / 2), 2)
                + Math.cos(radLatitude1) * Math.cos(radLatitude2)
                * Math.pow(Math.sin(p / 2), 2)));
        distance = distance * 6378137.0;
        distance = Math.round(distance * 10000) / 10000;
        DistanceText(distance);
        return distance;
    }
    private String DistanceText(double distance) {
        if (distance < 1000) {
            Log.e(TAG, String.valueOf((int) distance) + "公尺");
            return String.valueOf((int) distance) + "公尺";
        } else {
            //Log.e(TAG, new DecimalFormat("#.00").format(distance / 1000) + "公里");
            return new DecimalFormat("#.00").format(distance / 1000) + "公里";
        }
    }
    private static JSONObject getJsonObj(String src, String code) {
        InputStreamReader reader = null;
        BufferedReader in = null;
        try {
            URL url = new URL(src);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(1000);
            reader = new InputStreamReader(connection.getInputStream(), code);
            in = new BufferedReader(reader);
            String line = null;        //每行内容
            int lineFlag = 0;        //标记: 判断有没有数据
            StringBuffer content = new StringBuffer();
            while ((line = in.readLine()) != null) {
                content.append(line);
                lineFlag++;
            }
            return lineFlag == 0 ? null : new org.json.JSONObject(content.toString());
        } catch (SocketTimeoutException e) {
            System.out.println("连接超时!!!");
            return null;
        } catch (JSONException e) {
            System.out.println("网站响应不是json格式，无法转化成JSONObject!!!");
            return null;
        } catch (Exception e) {
            System.out.println("连接网址不对或读取流出现异常!!!");
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    System.out.println("关闭流出现异常!!!");
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.out.println("关闭流出现异常!!!");
                }
            }
        }
    }
    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FirstFragment(), "看垃圾車");
        adapter.addFragment(new ThirdFragment(), "看詳細位置");
        viewPager.setAdapter(adapter);
        //viewPager.setOffscreenPageLimit(2);
    }
    @Override
    public void onLocationChanged(Location location) {
        mLongitude = location.getLongitude();    //取得經度
        mLatitude = location.getLatitude();    //取得緯度
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

    @Override
    public void onStart() {
        super.onStart();

    }
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
