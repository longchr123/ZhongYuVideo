package com.example.administrator.day0121.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.jpush.android.api.JPushInterface;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.utils.SMSLog;

public class CreateActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText phone, et_mima, et_mima2, cord, et_yaoqing;
    private TextView now, getCord, saveCord, tv_content, tv_login;
    private String iPhone, iCord;
    private int time = 60;
    private StringRequest stringRequest1, stringRequest2;
    private RequestQueue mQueue;
    private String szImei;//设备唯一标识
    private SharedPreferences sp;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        showMessage();
        initView();
        initData();
        initListener();
        SMSSDK.initSDK(this, "1017746d6ae86", "689d17a739b8d33423f4f152c355b0d5");
        EventHandler eh = new EventHandler() {

            @Override
            public void afterEvent(int event, int result, Object data) {

                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                handler.sendMessage(msg);
            }

        };
        SMSSDK.registerEventHandler(eh);

    }

    private void showMessage() {
        new AlertDialog.Builder(this).setTitle("提示").setMessage("如果你在中域题库中有账号，请直接登录").setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(CreateActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }).setNegativeButton("取消", null).show();
    }

    private void initData() {
        TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        szImei = TelephonyMgr.getDeviceId();
        mQueue = Volley.newRequestQueue(this);
    }

    private void initListener() {
        getCord.setOnClickListener(this);
        saveCord.setOnClickListener(this);
        tv_content.setOnClickListener(this);
        tv_login.setOnClickListener(this);
    }

    private void initView() {
        phone = (EditText) findViewById(R.id.phone);
        cord = (EditText) findViewById(R.id.cord);
        now = (TextView) findViewById(R.id.now);
        getCord = (TextView) findViewById(R.id.getcord);
        saveCord = (TextView) findViewById(R.id.savecord);
        et_mima = (EditText) findViewById(R.id.et_mima);
        et_mima2 = (EditText) findViewById(R.id.et_mima2);
        tv_content = (TextView) findViewById(R.id.tv_content);
        et_yaoqing = (EditText) findViewById(R.id.et_yaoqing);
        tv_login = (TextView) findViewById(R.id.tv_login);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.getcord://获取验证码
                if (!TextUtils.isEmpty(phone.getText().toString().trim())) {
                    if (phone.getText().toString().trim().length() == 11) {
                        iPhone = phone.getText().toString().trim();
                        SMSSDK.getVerificationCode("86", iPhone);
                        cord.requestFocus();
                        getCord.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(this, "请输入完整电话号码", Toast.LENGTH_LONG).show();
                        phone.requestFocus();
                    }
                } else {
                    Toast.makeText(this, "请输入您的电话号码", Toast.LENGTH_LONG).show();
                    phone.requestFocus();
                }
                break;

            case R.id.savecord://学员注册
                if (et_yaoqing.getText().toString().length() < 8) {
                    Toast.makeText(this, "请输入邀请码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (et_mima.getText().toString().length() < 6) {
                    Toast.makeText(this, "请输入至少六位的密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!et_mima.getText().toString().equals(et_mima2.getText().toString())) {
                    Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!TextUtils.isEmpty(cord.getText().toString().trim())) {
                    //验证验证码
                    if (cord.getText().toString().trim().length() == 4) {
                        iCord = cord.getText().toString().trim();
                        SMSSDK.submitVerificationCode("86", iPhone, iCord);
                        //通过post请求把号码和密码传到服务器进行注册
                    } else {
                        Toast.makeText(this, "请输入完整验证码", Toast.LENGTH_LONG).show();
                        cord.requestFocus();
                    }
                } else {
                    Toast.makeText(this, "请输入验证码", Toast.LENGTH_LONG).show();
                    cord.requestFocus();
                }
                break;
            case R.id.tv_content://联系客服
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:4008-355-366"));
                startActivity(intent);
                break;
            case R.id.tv_login:
                Intent intent1 = new Intent(this, LoginActivity.class);
                startActivity(intent1);
                finish();
                break;
        }
    }

    //验证码送成功后提示文字
    private void reminderText() {
        now.setVisibility(View.VISIBLE);
        handlerText.sendEmptyMessageDelayed(1, 1000);
    }

    Handler handlerText = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (time > 0) {
                    now.setText("验证码已发送" + time + "秒");
                    time--;
                    handlerText.sendEmptyMessageDelayed(1, 1000);
                } else {
                    now.setText("提示信息");
                    time = 60;
                    now.setVisibility(View.GONE);
                    getCord.setVisibility(View.VISIBLE);
                }
            } else {
                cord.setText("");
                now.setText("提示信息");
                time = 60;
                now.setVisibility(View.GONE);
                getCord.setVisibility(View.VISIBLE);
            }
        }

        ;
    };

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {

            // TODO Auto-generated method stub
            super.handleMessage(msg);
            int event = msg.arg1;
            int result = msg.arg2;
            Object data = msg.obj;
            Log.e("event", "event=" + event);
            System.out.println("--------result---0" + event + "--------*" + result + "--------" + data);
            if (result == SMSSDK.RESULT_COMPLETE) {
                System.out.println("--------result---1" + event + "--------*" + result + "--------" + data);
                //短信注册成功后，返回MainActivity,然后提示新好友
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {//提交验证码成功
                    create();
                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                    //已经验证
                    Toast.makeText(getApplicationContext(), "验证码已经发送", Toast.LENGTH_SHORT).show();
                    reminderText();
                }
            } else {
                int status = 0;
                try {
                    ((Throwable) data).printStackTrace();
                    Throwable throwable = (Throwable) data;

                    JSONObject object = new JSONObject(throwable.getMessage());
                    String des = object.optString("detail");
                    status = object.optInt("status");
                    if (!TextUtils.isEmpty(des)) {
                        Toast.makeText(CreateActivity.this, des, Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (Exception e) {
                    SMSLog.getInstance().w(e);
                }
            }
        }
    };

    private final String mPageName = "CreateActivity";
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
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterAllEventHandler();
    }

    //注册逻辑
    public void create() {
        dialog = new AlertDialog.Builder(this).setTitle("注册...").setCancelable(false).show();
        String httpUrl = "http://www.zhongyuedu.com/api/sp/register.php";//公司服务器
        stringRequest1 = new StringRequest(Request.Method.POST, httpUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //服务器返回的内容
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String resultCode = jsonObject.getString("resultCode");
                    String result = jsonObject.getString("result");
                    if (resultCode.equals("0")) {
                        JSONObject jsonObject1 = jsonObject.getJSONObject("result");
                        String token = jsonObject1.getString("token");
                        String groupId = jsonObject1.getString("groupid");
                        String grouptitle = jsonObject1.getString("grouptitle");
                        String uid = jsonObject1.getString("uid");
                        sp = getSharedPreferences(ConfigUtil.spKey, Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("number", phone.getText().toString());
                        editor.putString("password", et_mima.getText().toString());
                        editor.putString("groupId", groupId);
                        editor.putString("uid", uid);
                        editor.putString("token", token);
                        editor.putString("grouptitle", grouptitle);
                        editor.commit();
                        Toast.makeText(CreateActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(CreateActivity.this, MainActivity.class);
                        startActivity(intent);
                        dialog.dismiss();
                        finish();
                    } else if (resultCode.equals("1")) {
                        Toast.makeText(CreateActivity.this, "账号已存在，检验邀请码...", Toast.LENGTH_SHORT).show();
                        useInviteCode();
                    } else {
                        Toast.makeText(CreateActivity.this, result, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                Toast.makeText(CreateActivity.this, "网络连接有问题", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("username", phone.getText().toString().trim());
                map.put("password", et_mima.getText().toString().trim());
                map.put("inviteCode", et_yaoqing.getText().toString().trim());
                map.put("udid", szImei);
                return map;
            }
        };
        mQueue.add(stringRequest1);
    }

    //判断邀请码
    private void useInviteCode() {
        String httpUrl = "http://www.zhongyuedu.com/api/sp/useinvitecode.php";//公司服务器
        stringRequest2 = new StringRequest(Request.Method.POST, httpUrl, new Response.Listener<String>() {
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
                        editor.putString("number", phone.getText().toString().trim());
                        editor.putString("groupId", groupId);
                        editor.putString("uid", uid);
                        editor.putString("token", token);
                        editor.putString("grouptitle", grouptitle);
                        editor.commit();
                        Toast.makeText(CreateActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(CreateActivity.this, MainActivity.class);
                        startActivity(intent);
                        dialog.dismiss();
                        finish();
                    } else {//登录失败
                        dialog.dismiss();
                        Toast.makeText(CreateActivity.this, result, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                Toast.makeText(CreateActivity.this, "网络连接有问题", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("username", phone.getText().toString().trim());
                map.put("inviteCode", et_yaoqing.getText().toString().trim());
                map.put("udid", szImei);
                return map;
            }
        };
        mQueue.add(stringRequest2);
    }
}
