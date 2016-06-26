package com.example.administrator.day0121.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bokecc.sdk.mobile.drm.DRMServer;
import com.example.administrator.day0121.Application.DemoApplication;
import com.example.administrator.day0121.R;
import com.example.administrator.day0121.fragment.LoadManagerFragment;
import com.example.administrator.day0121.fragment.NewMainFragment;
import com.example.administrator.day0121.fragment.NewsFragment;
import com.example.administrator.day0121.fragment.SingleFragment;
import com.example.administrator.day0121.utils.ConfigUtil;
import com.example.administrator.day0121.utils.DataSet;
import com.example.administrator.day0121.utils.ExampleUtil;
import com.example.administrator.day0121.utils.LogcatHelper;
import com.umeng.analytics.MobclickAgent;

public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener {
    private RadioGroup rg;
    private RadioButton mDownButton;
    private RadioButton mShouyeButton;
    private FragmentManager manager;
    private NewMainFragment newMainFragment;
    private LoadManagerFragment loadManagerFragment;
    private SingleFragment singleFragment;
    private Fragment mCurrentFragment;
    private NewsFragment newsFragment;
    private FragmentTransaction transaction;
    private SharedPreferences sp;
    private boolean isDownLoad=true;//用于标记是否从SubjectActivity返回到MainActivity
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    private DRMServer drmServer;
    private DemoApplication app;

    public static boolean isForeground = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkNetworkAvailable(this);
        initView();
        initDate();
        initListener();
        registerMessageReceiver();  // used for receive msg

        // 启动DRMServer
        drmServer = new DRMServer();
        drmServer.start();
        app = (DemoApplication)getApplication();
        app.setDrmServerPort(drmServer.getPort());
    }

    private void initDate() {
        manager = getFragmentManager();
        newMainFragment = new NewMainFragment();
        loadManagerFragment = new LoadManagerFragment();
        singleFragment = new SingleFragment();
        newsFragment = new NewsFragment();
        transaction = manager.beginTransaction();
        transaction.add(R.id.lin, newMainFragment).commit();
        mCurrentFragment = newMainFragment;
        mSharedPreferences=this.getSharedPreferences(ConfigUtil.NAME,MODE_PRIVATE);
        mEditor=mSharedPreferences.edit();
    }

    private void initListener() {
        rg.setOnCheckedChangeListener(this);
    }

    private void initView() {
        rg = (RadioGroup) findViewById(R.id.rg);
        mDownButton= (RadioButton) findViewById(R.id.rb_down);
        mShouyeButton= (RadioButton) findViewById(R.id.rb_shouye);
    }

    //要先hide再show
    public void changeFragment(Fragment fragment) {
        if (fragment == null) {
            return;
        }
        transaction = manager.beginTransaction();
        if (mCurrentFragment != null) {
            transaction.hide(mCurrentFragment);
        }
//		fragmentTransaction.remove(fragment);
        if (fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            transaction.add(R.id.lin, fragment);
        }
        transaction.commit();
        mCurrentFragment = fragment;
    }

    public static boolean checkNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            Toast.makeText(context, "没有联网", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        NetworkInfo netWorkInfo = info[i];
                        if (netWorkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                            Toast.makeText(context, "已连接wifi", Toast.LENGTH_SHORT).show();
                            return true;
                        } else if (netWorkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                            Toast.makeText(context, "你正在使用2g/3g/4g网络", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    }
                }
            }
        }
        Toast.makeText(context, "没有联网", Toast.LENGTH_SHORT).show();
        return false;
    }

    private long mExitTime;

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        transaction = manager.beginTransaction();
        switch (checkedId) {
            case R.id.rb_shouye:
                changeFragment(newMainFragment);
                break;
            case R.id.rb_zixun:
                changeFragment(newsFragment);
                break;
            case R.id.rb_down:
//                changeFragment(loadManagerFragment);
                startActivity(new Intent(this,DownLoadActivity.class));
                mShouyeButton.setChecked(true);
                break;
            case R.id.rb_geren:
                changeFragment(singleFragment);
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //从分类课程中进入“下载管理”
        if (mSharedPreferences.getBoolean(ConfigUtil.FROMSUB,false)){
            mDownButton.setChecked(true);
            mEditor.putBoolean("IsComeFromSubject",false).commit();
            mDownButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStop() {
        DataSet.saveData();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        DataSet.saveData();
        super.onDestroy();
        if (drmServer != null) {
            drmServer.stop();
        }
        LogcatHelper.getInstance(this).stop();
        unregisterReceiver(mMessageReceiver);

    }

    private MessageReceiver mMessageReceiver;
    public static final String MESSAGE_RECEIVED_ACTION = "com.example.jpushdemo.MESSAGE_RECEIVED_ACTION";
    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_EXTRAS = "extras";

    public void registerMessageReceiver() {
        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(MESSAGE_RECEIVED_ACTION);
        registerReceiver(mMessageReceiver, filter);
    }

    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
                String messge = intent.getStringExtra(KEY_MESSAGE);
                String extras = intent.getStringExtra(KEY_EXTRAS);
                StringBuilder showMsg = new StringBuilder();
                showMsg.append(KEY_MESSAGE + " : " + messge + "\n");
                if (!ExampleUtil.isEmpty(extras)) {
                    showMsg.append(KEY_EXTRAS + " : " + extras + "\n");
                }
            }
        }
    }

    private final String mPageName = "MainActivity";
    @Override
    protected void onResume() {
        isForeground = true;
        super.onResume();
        MobclickAgent.onPageStart(mPageName);
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        isForeground=false;
        super.onPause();
        MobclickAgent.onPageEnd(mPageName);
        MobclickAgent.onPause(this);
    }
}
