package kr.ac.duksung.parkingapp;

import static kr.ac.duksung.parkingapp.R.drawable.no_car_button;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.naver.maps.map.overlay.Marker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import kotlin.collections.ArraysKt;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ParkingLotFragment extends Fragment {

    private Button button1, button2, button3, button4, button5;
    private TextView carnum;
    //private int[] YN={0,0,1,1};
    private String carnum_result;
    ArrayList<Integer> YN = new ArrayList<>();
    private Button[] buttons = new Button[4];
    @SuppressLint("ResourceAsColor")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //int[] YN = new int[4];
        // 서버 연결 시작
        Log.d("PLOT","plot시작");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.ip))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitAPI result = retrofit.create(RetrofitAPI.class);
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("plotid", 1);
        Log.d("PLOT: ","ongoing");
        View v = inflater.inflate(R.layout.fragment_parking_lot, container, false);
        View v2 = inflater.inflate(R.layout.layout_dialog_book, container, false);

        button1 = (Button) v.findViewById(R.id.button1);
        button2 = (Button) v.findViewById(R.id.button2);
        button3 = (Button) v.findViewById(R.id.button3);
        button4 = (Button) v.findViewById(R.id.button4);
        buttons[0] = button1;
        buttons[1] = button2;
        buttons[2] = button3;
        buttons[3] = button4;
        result.postSlotData(param).enqueue(new Callback<List<slotResult>>() {
            @Override
            public void onResponse(Call<List<slotResult>> call, Response<List<slotResult>> response) {
                List<slotResult> data = response.body();
                Log.d("POST: ", "SUCCESS!");
                for(int i = 0; i < data.size(); i++) {
                    int index = i;
                    Log.d("POST: ", Integer.toString(i));
                    Log.d("POST: ", data.get(i).getSlotId());
                    Log.d("POST: ", data.get(i).getAvailable());
                    if(data.get(i).getAvailable().equals("y")) {
                        Log.d("POST", "YY!");
                        YN.add(i,0);
                        //Log.d("POST: ", Arrays.toString(YN));
                        buttons[i].setBackgroundResource(R.drawable.yes_car_button);
                        buttons[i].setTextColor(Color.WHITE);
                        buttons[i].setText("A-"+(i+1)+"  예약가능");
                        // 버튼 클릭 시 메서드
                        buttons[i].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("예약");
                                builder.setMessage("예약 하시겠습니까?");
                                builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        AlertDialog.Builder book = new AlertDialog.Builder(getActivity());
                                        View view = inflater.inflate(R.layout.layout_dialog_book, container, false);
                                        book.setView(view);
                                        Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
                                        ArrayAdapter timeAdapter = ArrayAdapter.createFromResource(view.getContext(), R.array.time, android.R.layout.simple_spinner_item);
                                        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        spinner.setAdapter(timeAdapter);
                                        ((TextView) view.findViewById(R.id.textTitle)).setText("A-"+(index+1));
                                        carnum = (TextView) view.findViewById(R.id.carnumber);
                                        book.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            // 버튼 누를 때 act
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                String str = spinner.getSelectedItem().toString();
                                                str = str.substring(0,1);
                                                carnum_result = carnum.getText().toString();
                                                //booktime.set(i, Integer.parseInt(str));
                                                Toast.makeText(view.getContext(), str+"/"+carnum_result, Toast.LENGTH_SHORT).show();
                                                param.put("plotid","1");
                                                param.put("slotid","1_A"+(index+1));
                                                param.put("userid","2");
                                                param.put("carnum",carnum_result);
                                                param.put("usagetime",str);
                                                result.postBookData(param).enqueue(new Callback<BookResult>() {
                                                    @Override
                                                    public void onResponse(Call<BookResult> call, Response<BookResult> response) {
                                                        if(response.isSuccessful()) {
                                                            BookResult data = response.body();
                                                            Log.d("POST: ", data.getParking_lot_location());
                                                            Log.d("POST: ", data.getParking_lot_name());
                                                            Log.d("POST: ", data.getSlotid());
                                                            Log.d("POST: ", String.valueOf(data.getUsagetime()));
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<BookResult> call, Throwable t) {
                                                        Log.d("POST: ", "Failed!!!!");
                                                        Log.d("TEST: ", "Failed!!!!");
                                                        t.printStackTrace();
                                                    }
                                                });
                                            }
                                        });
                                        book.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        });
                                        book.show();
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
                        });
                    }
                    else{
                        Log.d("POST", "NN!");
                        YN.add(i,1);
                        //Log.d("POST: ", Arrays.toString(YN));
                        buttons[i].setBackgroundResource(R.drawable.no_car_button);
                        buttons[i].setTextColor(Color.BLACK);
                        buttons[i].setText("A-"+(i+1)+"  예약불가");
                    }
                }
            }

            @Override
            public void onFailure(Call<List<slotResult>> call, Throwable t) {
                Log.d("POST", "POST 실패");
                t.printStackTrace();
            }
        });

        //super.onCreateView(inflater, container, savedInstanceState);
        //Log.d("RESULT: ", Arrays.toString(YN));
        return v;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

}