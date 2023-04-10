package kr.ac.duksung.parkingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,Overlay.OnClickListener {
    private static final String TAG = "MainActivity";

    private static double[][] location = {{37.6506005, 127.0158205}, {37.650972, 127.015213}, {37.654109, 127.014669}, {37.6502939, 127.0193974}, {37.656725, 127.011576}};
    private static String[] placeName = {"영근터 주차장", "영근터 소형 주차장", "사유 주차장", "하나누리관 주차장", "우이동 공영 주차장"};
    private static String[] address = {"서울특별시 삼양로144길 33", "서울 도봉구 쌍문1동 420-13", "서울특별시 도봉구 삼양로144가길" , "서울특별시 도봉구 삼양로144길 33" , "서울특별시 강북구 우이동 105-2"};
    private static int[] leftover={0,2,5,1,0};

    private static Map< String, Integer > leftoverarr = new HashMap<>();

    // 위치 권한을 받아오기 위함
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] PERMSSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private FusedLocationSource mLocationSource;
    private NaverMap mNaverMap;

    private InfoWindow mInfoWindow;

    private long time = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NaverMapSdk.getInstance(this).setClient(
                new NaverMapSdk.NaverCloudPlatformClient("s7xoj8yasp"));

        // 하단 네비게이션 바
        BottomNavigationView bottomNaView = findViewById(R.id.bottom_navigation_view);
        bottomNaView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.logout) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("로그아웃");
                    builder.setMessage("로그아웃 하시겠습니까?");
                    builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intentLogOut = new Intent(getApplicationContext(),LoginActivity.class);
                            startActivity(intentLogOut);
                        }
                    });
                    builder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            return;
                        }
                    });
                    builder.show();
                    return true;


                }
                return true;
            }
        });

        //지도 객체 생성
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment)fm.findFragmentById(R.id.map);
        if(mapFragment==null){
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }
        //getMapAsync를 호출하여 비동기로 onMapReady 콜백 메서드 호출
        //onMapReady에서 NaverMap 객체를 받음
        mapFragment.getMapAsync(this);
        //위치를 반환하는 구현체인 FusedLocationSource 생성
        mLocationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);
    }
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        Log.d( TAG, "onMapReady");

        // 지도 상에 마커 표시하기
        Marker [] markerList = new Marker[5];
        // 리스트 별로 마커 차례대로 표시함
        for (int i=0; i<5; i++) {
            markerList[i] = new Marker();
            markerList[i].setTag(placeName[i]);
            markerList[i].setSubCaptionText(address[i]);
            markerList[i].setPosition(new LatLng(location[i][0], location[i][1]));
            markerList[i].setMap(naverMap);
            markerList[i].setOnClickListener(this);
            markerList[i].setWidth(200);
            markerList[i].setHeight(200);
            // marker2 = 노란색 marker3 = 보라색
            markerList[i].setIcon(OverlayImage.fromResource(R.drawable.marker3));
            leftoverarr.put(placeName[i], leftover[i]);
        }

        //NaverMAP 객체 받아서 NaverMap 객체에 위치 소스 지정
        mNaverMap = naverMap;
        mNaverMap.setLocationSource(mLocationSource);
        //권한확인. 결과는 onRequesetPermissionsResult 콜백 매서드 호출
        ActivityCompat.requestPermissions(this, PERMSSIONS, PERMISSION_REQUEST_CODE);

        //InfoWindow 객체 생성
        mInfoWindow = new InfoWindow();
        mInfoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(this){
            @NonNull
            @Override
            public CharSequence getText(@NonNull InfoWindow infoWindow){
                return (CharSequence)infoWindow.getMarker().getTag();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //request code와 권한획득 여부 확인
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        }
    }

    public void showAlertDialog(Marker marker) {

        // 클릭 시 다이얼로그 생성
        AlertDialog.Builder d = new AlertDialog.Builder(MainActivity.this);
        View view = LayoutInflater.from(MainActivity.this).inflate(
                R.layout.layout_dialog,
                (LinearLayout)findViewById(R.id.layoutDialog)
        );
        d.setView(view);
        //제목
        ((TextView)view.findViewById(R.id.textTitle)).setText((CharSequence) marker.getTag());
        //상세 내용
        ((TextView)view.findViewById(R.id.textContent)).setText((CharSequence) marker.getSubCaptionText()+"\n주차 남는자리\n잔여: "+leftoverarr.get((CharSequence)marker.getTag())+"\n");
        // 버튼 생성
        if (leftoverarr.get((CharSequence)marker.getTag())>0) {
            d.setPositiveButton("예약하기", new DialogInterface.OnClickListener() {
                // 버튼 누를 때 act
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(getApplicationContext(), "예약버튼 누름", Toast.LENGTH_LONG).show();
                    Intent intentBook = new Intent(getApplicationContext(), BookActivity.class);
                    startActivity(intentBook);
                }
            });
        }
        d.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Toast.makeText(getApplicationContext(), "취소 누름", Toast.LENGTH_LONG).show();
            }
        });
        d.show();

    }

    // Back Button 클릭 시 종료

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - time >= 2000) {
            time = System.currentTimeMillis();
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("종료");
            builder.setMessage("시스템을 종료하시겠습니까?");
            builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        // finish 후 다른 Activity 뜨지 않도록 함
                        moveTaskToBack(true);
                        // 현재 액티비티 종료
                        finish();
                        // 모든 루트 액티비티 종료
                        finishAffinity();
                        // 인텐트 애니 종료
                        overridePendingTransition(0, 0);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            });
            builder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    return;
                }
            });
            builder.show();
        }
    }

    @Override
    public boolean onClick(@NonNull Overlay overlay) {
        if(overlay instanceof Marker) {
            Marker marker = (Marker) overlay;
            if (marker.getInfoWindow() != null){
                mInfoWindow.close();
            }
            else{
                mInfoWindow.open(marker);
                // mInfoWindow 클릭 시 상세정보 띄우기
                mInfoWindow.setOnClickListener(new Overlay.OnClickListener() {
                    @Override
                    public boolean onClick(@NonNull Overlay overlay) {
                        showAlertDialog(marker);
                        return true;
                    }
                });
            }
            return true;
        }
        return false;
    }
}