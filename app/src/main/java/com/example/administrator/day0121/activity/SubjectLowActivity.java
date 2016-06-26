package com.example.administrator.day0121.activity;
//非学员版课程分类详情

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.administrator.day0121.R;
import com.example.administrator.day0121.adapter.MySubjectLowAdapter;
import com.umeng.analytics.MobclickAgent;

public class SubjectLowActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView iv_back;
    private TextView tv_call;
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_low);
        initView();
        initData();
        initListener();
    }

    private void initListener() {
        iv_back.setOnClickListener(this);
        tv_call.setOnClickListener(this);
    }

    private void initData() {
        lv.setAdapter(new MySubjectLowAdapter(this));
    }

    private void initView() {
        iv_back = (ImageView) findViewById(R.id.iv_back);
        tv_call = (TextView) findViewById(R.id.tv_call);
        lv = (ListView) findViewById(R.id.lv);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_call:
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:4008-355-366"));
                startActivity(intent);
                break;
        }
    }

    private final String mPageName = "SubjectLowActivity";
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
