package com.umeng.example;

import com.umeng.example.analytics.AnalyticsHome;
import com.umeng.example.game.GameAnalyticsHome;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View v) {
        int id = v.getId();
        Intent in = null;
        if (id == R.id.normal) {
            in = new Intent(this, AnalyticsHome.class);
        } else if (id == R.id.game) {
            in = new Intent(this, GameAnalyticsHome.class);
        }

        startActivity(in);
    }

}
