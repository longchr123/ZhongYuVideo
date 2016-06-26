package com.example.administrator.day0121.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
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

public class UseInviteCodeActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText et_yaoqing;
    private TextView tv_ok, tv_call;
    private RequestQueue mQueue;
    private StringRequest stringRequest;
    private SharedPreferences sp;
    private String szImei;
    private String phone;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_use_invite_code);
        initView();
        getIntentData();
        initData();
        initListener();
    }

    private void getIntentData() {
        phone = getIntent().getStringExtra("phone");
    }

    private void initData() {
        TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        szImei = TelephonyMgr.getDeviceId();
    }

    private void initListener() {
        tv_ok.setOnClickListener(this);
        tv_call.setOnClickListener(this);
    }

    private void initView() {
        et_yaoqing = (EditText) findViewById(R.id.et_yaoqing);
        tv_ok = (TextView) findViewById(R.id.tv_ok);
        tv_call = (TextView) findViewById(R.id.tv_call);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_ok:
                dialog = new AlertDialog.Builder(this).setTitle("激活中...")
                        .setCancelable(false).show();
                useInviteCode();
                break;
            case R.id.tv_call:
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:4008-355-366"));
                startActivity(intent);
                break;
        }
    }

    //检验邀请码
    private void useInviteCode() {
        mQueue = Volley.newRequestQueue(this);
        String httpUrl = "http://www.zhongyuedu.com/api/sp/useinvitecode.php";//公司服务器
        stringRequest = new StringRequest(Request.Method.POST, httpUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
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
                        sp = getSharedPreferences(ConfigUtil.spKey, Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("number", phone);
                        editor.putString("groupId", groupId);
                        editor.putString("uid", uid);
                        editor.putString("token", token);
                        editor.putString("grouptitle", grouptitle);
                        editor.commit();
                        Toast.makeText(UseInviteCodeActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UseInviteCodeActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {//登录失败
                        dialog.dismiss();
                        Toast.makeText(UseInviteCodeActivity.this, result, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                Toast.makeText(UseInviteCodeActivity.this, "网络连接有问题", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("username", phone);
                map.put("inviteCode", et_yaoqing.getText().toString().trim());
                map.put("udid", szImei);
                return map;
            }
        };
        mQueue.add(stringRequest);

    }

    private final String mPageName = "UseInviteCodeActivity";
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
