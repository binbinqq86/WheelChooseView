package com.example.tb.wheelchooseview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private WheelChooseView wcv;
    private List<String> data=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        for (int i = 0; i < 20; i++) {
            data.add(""+i);
        }
        wcv= (WheelChooseView) findViewById(R.id.wcv);
        wcv.setDataList(data);
    }
}
