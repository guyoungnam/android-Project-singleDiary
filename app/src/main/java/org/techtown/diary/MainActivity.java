package org.techtown.diary;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;
import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;

import org.techtown.diary.data.WeatherItem;
import org.techtown.diary.data.WeatherResult;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements OnTabItemSelectedListener, OnRequestListener, AutoPermissionsListener, MyApplication.OnResponseListener{

   private static final String TAG = "MainActivity";

    Fragment1 fragment1;
    Fragment2 fragment2;
    Fragment3 fragment3; //선언

    BottomNavigationView bottomNavigation;

    Location currentLocation;
    GPSListener gpsListener;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
    SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH시");
    SimpleDateFormat dateFormat3 = new SimpleDateFormat("MM월 dd일");
    SimpleDateFormat dateFormat4 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");



    int locationCount = 0;
    //위치를 한 번 확인한 후에는 위치 요청을 취소할 수 있도록 위치 젖ㅇ보를 확인하는 횟수

    String currentWeather;
    String currentAddress;
    String currentDateString;
    Date currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragment1 = new Fragment1();
        fragment2 = new Fragment2();
        fragment3 = new Fragment3(); //객체 변수 할당

        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment1).commit();

        bottomNavigation = findViewById(R.id.bottom_navigation);

        bottomNavigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                        switch (item.getItemId()){
                            case R.id.tab1:
                                Toast.makeText(getApplicationContext(), "첫 번째 탭 선택됨", Toast.LENGTH_LONG).show();
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment1).commit();

                                return true;

                            case R.id.tab2:
                                Toast.makeText(getApplicationContext(), "두 번째 탭 선택됨", Toast.LENGTH_LONG).show();
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment2).commit();

                                return true;

                            case R.id.tab3:
                                Toast.makeText(getApplicationContext(), "세 번째 탭 선택됨", Toast.LENGTH_LONG).show();
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment3).commit();

                                return true;
                        }

                        return false;
                    }
                });

        AutoPermissions.Companion.loadAllPermissions(this, 101);

     //   setPicturePath();




    }

   // private void setPicturePath() {

   //     String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
   //     AppConstants.FOLDER_PHOTO = sdcardPath + File.separator + "photo"; //에러 발생
   // }


    @Override
    public void onTableSelected(int position) {
        if (position == 0) {
            bottomNavigation.setSelectedItemId(R.id.tab1);
        } else if (position == 1) {
            bottomNavigation.setSelectedItemId(R.id.tab2);
        } else if (position == 2) {
            bottomNavigation.setSelectedItemId(R.id.tab3);
        }

    }





    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this); }

    @Override
    public void onDenied(int requestCode, @NonNull String[] permissions) {
        Toast.makeText(this, "permission denied: " + permissions.length, Toast.LENGTH_LONG).show();

    }

    @Override
    public void onGranted(int requestCode, @NonNull  String[] permissions) {

        Toast.makeText(this, "permission granted: " + permissions.length, Toast.LENGTH_LONG).show();

    }


  //두 번째 프래그먼트에서 호출
    public void onRequest(String command){
        if(command != null){
            if(command.equals("getCurrentLocation")){
                getCurrentLocation();
            }
        }
    }

    public void getCurrentLocation() {

        //set current time
        currentDate = new Date();
        currentDateString = dateFormat3.format(currentDate);
        if(fragment2 !=null){
            fragment2.setDateString(currentDateString);
        }

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try{
            currentLocation =manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(currentLocation !=null){
                double latitude = currentLocation.getLatitude();
                double longitude = currentLocation.getLongitude();
                String message = "LAST Location->Latitude :" + latitude + "Longitude:" + longitude;
                println(message);

                getCurrentWeather();
                getCurrentAddress();

            }

            gpsListener = new GPSListener();
            long minTime = 10000;
            float minDistance = 0;

            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,minTime, minDistance, gpsListener);
            println("Current location requested");

        }catch (SecurityException e){
            e.printStackTrace();
        }

    }

    public void stopLocationService(){
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try{
            manager.removeUpdates(gpsListener);
            println("Current location requested");
        }catch (SecurityException e){
            e.printStackTrace();
        }
    }


    //요청된 위치를 수신하기위해

    class GPSListener implements LocationListener {
        public void onLocationChanged(Location location){
            currentLocation = location;

            locationCount++;

            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            String message = "Current Location -> Latitude:"+latitude + "Longitude "+longitude;
            println(message);

            getCurrentWeather();
            getCurrentAddress();
        }
        public void onProviderDisabled(String provider){}

        public void onProviderEnabled(String provider){}

        public void onStatusChanged(String provider, int status , Bundle extras) {}
    }




    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


//위치 확인
    public void getCurrentAddress() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude(),
                    1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            currentAddress = address.getLocality() + " " + address.getSubLocality();
            String adminArea = address.getAdminArea();
            String country = address.getCountryName();
            println("Address : " + country + " " + adminArea + " " + currentAddress);

            if (fragment2 != null) {
                fragment2.setAddress(currentAddress);
            }
        }
    }

    //날씨 확인
    //GridUtil 객체의 getGrid ( ) 메서드 이용 격자 번호 확인

    public void getCurrentWeather() {

        //격자 번호를 확인
        Map<String, Double> gridMap = GridUtil.getGrid(currentLocation.getLatitude(), currentLocation.getLongitude());

        double gridX = gridMap.get("x");
        double gridY = gridMap.get("y");
        println("x -> " + gridX + ", y -> " + gridY);

        sendLocalWeatherReq(gridX, gridY);

    }

    //기상청 날씨 서버로 요청을 전송

    private void sendLocalWeatherReq(double gridX, double gridY) {

        String url = "http://www.kma.go.kr/wid/queryDFS.jsp";
        url += "?gridx=" + Math.round(gridX);
        url += "&gridy=" + Math.round(gridY);

        Map<String,String> params = new HashMap<String,String>();

        MyApplication.send(AppConstants.REQ_WEATHER_BY_GRID, Request.Method.GET, url, params, this);
    }


    //응답을 받으면
    // xml 응답 데이터를 자바 객체로 변경
    public void processResponse(int requestCode, int responseCode, String response) {
        if (responseCode == 200) {
            if (requestCode == AppConstants.REQ_WEATHER_BY_GRID) {
                // Grid 좌표를 이용한 날씨 정보 처리 응답
                //println("response -> " + response);

                XmlParserCreator parserCreator = new XmlParserCreator() {
                    @Override
                    public XmlPullParser createParser() {
                        try {
                            return XmlPullParserFactory.newInstance().newPullParser();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                };

                GsonXml gsonXml = new GsonXmlBuilder()
                        .setXmlParserCreator(parserCreator)
                        .setSameNameLists(true)
                        .create();

                WeatherResult weather = gsonXml.fromXml(response, WeatherResult.class);

                // 현재 기준 시간
                try {
                    Date tmDate = dateFormat.parse(weather.header.tm);
                    String tmDateText = dateFormat2.format(tmDate);
                    println("기준 시간 : " + tmDateText);

                    for (int i = 0; i < weather.body.datas.size(); i++) {
                        WeatherItem item = weather.body.datas.get(i);
                        println("#" + i + " 시간 : " + item.hour + "시, " + item.day + "일째");
                        println("  날씨 : " + item.wfKor);
                        println("  기온 : " + item.temp + " C");
                        println("  강수확률 : " + item.pop + "%");

                        println("debug 1 : " + (int)Math.round(item.ws * 10));
                        float ws = Float.valueOf(String.valueOf((int)Math.round(item.ws * 10))) / 10.0f;
                        println("  풍속 : " + ws + " m/s");
                    }

                    // set current weather
                    WeatherItem item = weather.body.datas.get(0);
                    currentWeather = item.wfKor;
                    if (fragment2 != null) {
                        fragment2.setWeather(item.wfKor);
                    }

                    // stop request location service after 2 times
                    if (locationCount > 0) {
                        stopLocationService();
                    }

                } catch(Exception e) {
                    e.printStackTrace();
                }


            } else {
                // Unknown request code
                println("Unknown request code : " + requestCode);

            }

        } else {
            println("Failure response code : " + responseCode);

        }

    }


    private void println(String data) {
        Log.d(TAG, data);
    }

}
