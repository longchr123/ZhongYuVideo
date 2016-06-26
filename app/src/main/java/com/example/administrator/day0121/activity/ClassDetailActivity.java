package com.example.administrator.day0121.activity;
//班级详情
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.administrator.day0121.R;
import com.umeng.analytics.MobclickAgent;

import cn.jpush.android.api.JPushInterface;

public class ClassDetailActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageView iv_back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_detail);
        initView();
        initListener();
    }

    private void initListener() {
        iv_back.setOnClickListener(this);
    }

    private void initView() {
        iv_back= (ImageView) findViewById(R.id.iv_back);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                finish();
                break;
        }
    }
    private final String mPageName = "ClassDetailActivity";
    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mPageName);
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mPageName);
        MobclickAgent.onPause(this);
    }
}
