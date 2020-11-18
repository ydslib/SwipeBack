package com.crystallake.swipebackdemo;


import android.os.Bundle;

import com.crystallake.swipeback.SwipeBackActivity;

public class MainActivity extends SwipeBackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}