package kr.ac.duksung.parkingapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BookActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener  {
    SwipeRefreshLayout swipeRefreshLayout;

    private int plotid;
    private String slotid;
    private String userid;
    private String carnum;
    private int usagetime;
    private String parking_lot_name;
    private String parking_lot_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);


        ViewPager vp =findViewById(R.id.viewpager);
        VPAdapter adapter = new VPAdapter(getSupportFragmentManager());
        vp.setAdapter(adapter);

        TabLayout tab = findViewById(R.id.tab);
        tab.setupWithViewPager(vp);


        // 위로 끌어올리면 새로고침되도록 구현하였음
        swipeRefreshLayout = findViewById(R.id.bookLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        //새로 고침 코드
        updateLayoutView();
        //새로 고침 완
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.darker_gray);
    }

    // 당겨서 새로고침 했을 때 뷰 변경 메서드
    public void updateLayoutView(){

    }
}