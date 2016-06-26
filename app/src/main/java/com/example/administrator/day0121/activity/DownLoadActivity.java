package com.example.administrator.day0121.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.day0121.R;
import com.example.administrator.day0121.fragment.LoadManagerFragment;
import com.umeng.analytics.MobclickAgent;

public class DownLoadActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView iv_back;
    private TextView tv_test;
    private FragmentManager manager;
    private LoadManagerFragment loadManagerFragment;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down_load);
        initView();
        initListener();
    }

    private void initListener() {
        iv_back.setOnClickListener(this);
    }

    private void initView() {
        manager = getFragmentManager();
        iv_back = (ImageView) findViewById(R.id.iv_back);
//        tv_test = (TextView) findViewById(R.id.tv_test);
        loadManagerFragment = new LoadManagerFragment();
        transaction = manager.beginTransaction();
        transaction.add(R.id.layout_load, loadManagerFragment);
        transaction.commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
        }
    }

    private final String mPageName = "DownLoadActivity";
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
