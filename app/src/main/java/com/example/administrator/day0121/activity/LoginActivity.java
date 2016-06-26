package com.example.administrator.day0121.activity;
//返回token手机唯一标识,groupId为考试类型的标识,grouptitle考试类型名称,uid用户名称

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.administrator.day0121.R;
import com.example.administrator.day0121.utils.ConfigUtil;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.smssdk.SMSSDK;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_login, tv_create;
    private EditText et_phone, et_mima;
    private StringRequest stringRequest;
    private RequestQueue mQueue;
    private String szImei;
    private SharedPreferences sp;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        initData();
        initListener();
    }

    private void initData() {
        TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        szImei = TelephonyMgr.getDeviceId();
        if(szImei==null||szImei.length()<=0) {
            WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            szImei = wm.getConnectionInfo().getMacAddress();
        }
    }

    private void initListener() {
        tv_login.setOnClickListener(this);
        tv_create.setOnClickListener(this);
    }

    private void initView() {
        tv_login = (TextView) findViewById(R.id.tv_login);
        tv_create = (TextView) findViewById(R.id.tv_create);
        et_phone = (EditText) findViewById(R.id.et_phone);
        et_mima = (EditText) findViewById(R.id.et_mima);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_login:
                if (!TextUtils.isEmpty(et_phone.getText().toString().trim())) {
                    if (et_phone.getText().toString().trim().length() == 11) {
                        dialog = new AlertDialog.Builder(this).setTitle("登录中...")
                                .setCancelable(false).show();
                        login();
                    } else {
                        Toast.makeText(this, "请输入完整电话号码", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "请输入您的电话号码", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.tv_create:
                Intent intent = new Intent(this, CreateActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    //登录逻辑
    private void login() {
        mQueue = Volley.newRequestQueue(this);
        Log.e("LoginActivity", szImei);
        String httpUrl = "http://www.zhongyuedu.com/api/sp/login.php";//公司服务器
//        String httpUrl = "http://www.zhongyuedu.com/api/login.php";//公司服务器
        stringRequest = new StringRequest(Request.Method.POST, httpUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("ddd",response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String resultCode = jsonObject.getString("resultCode");
                    String result = jsonObject.getString("result");
                    if (resultCode.equals("0")) {//登录成功
                        JSONObject jsonObject1 = jsonObject.getJSONObject("result");
                        String token = jsonObject1.getString("token");
                        String groupId = jsonObject1.getString("groupid");
                        String grouptitle = jsonObject1.getString("grouptitle");
                        String uid = jsonObject1.getString("uid");
                        if (groupId.equals("8")) {
                            Toast.makeText(LoginActivity.this, "普通会员需要输入邀请码才能观看视频", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, UseInviteCodeActivity.class);
                            intent.putExtra("phone", et_phone.getText().toString().trim());
                            startActivity(intent);
                            finish();
                        } else {
                            sp = getSharedPreferences(ConfigUtil.spKey, Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("number", et_phone.getText().toString());
                            editor.putString("password", et_mima.getText().toString());
                            editor.putString("groupId", groupId);
                            editor.putString("uid", uid);
                            editor.putString("token", token);
                            editor.putString("grouptitle", grouptitle);
                            editor.commit();
                            Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("main_activity","0");
                            startActivity(intent);
                            finish();
                        }
                    } else {//登录失败
                        dialog.dismiss();
                        Toast.makeText(LoginActivity.this, result, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                Toast.makeText(LoginActivity.this, "网络连接有问题", Toast.LENGTH_SHORT).show();
                Log.e("LoginActivity", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("username", et_phone.getText().toString().trim());
                map.put("password", et_mima.getText().toString().trim());
                map.put("udid", szImei);
                return map;
            }
        };
        mQueue.add(stringRequest);
    }

    private final String mPageName = "LoginActivity";
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

    @Override
    protected void onStop() {
        super.onStop();
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
