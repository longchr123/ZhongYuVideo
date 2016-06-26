package com.example.administrator.day0121.activity;
//做测试接口用的界面

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class TestActivity extends AppCompatActivity {
    private RequestQueue mQueue;
    private StringRequest stringRequest;
    private String szImei;
    private Button bt, bt_video, bt_ky_create, bt_ky_login;
    private SharedPreferences sp;
    private EditText et_phone, et_mima, et_invote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);
        mQueue = Volley.newRequestQueue(this);
        TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        szImei = TelephonyMgr.getDeviceId();
        initView();
        initListener();
    }

    private void initListener() {
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTK();
            }
        });
        bt_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createSP();
            }
        });
        bt_ky_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createKY();
            }
        });
        bt_ky_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginKY();
            }
        });
    }

    private void initView() {
        et_phone = (EditText) findViewById(R.id.et_phone);
        et_mima = (EditText) findViewById(R.id.et_mima);
        et_invote = (EditText) findViewById(R.id.et_invote);
        bt = (Button) findViewById(R.id.bt);
        bt_video = (Button) findViewById(R.id.bt_video);
        bt_ky_create = (Button) findViewById(R.id.bt_ky_create);
        bt_ky_login = (Button) findViewById(R.id.bt_ky_login);
    }

    private void playVideo() {
        Intent intent = new Intent(this, MediaPlayActivity.class);
        intent.putExtra("videoId", "86B61D5E5484C4BF9C33DC5901307461");
        startActivity(intent);
    }

    //视频登录
    private void loginSP() {
        String httpUrl = "http://www.zhongyuedu.com/api/sp/register.php";//公司服务器
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
                        if (groupId.equals("8")) {
                            Toast.makeText(TestActivity.this, "普通会员需要输入邀请码", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(TestActivity.this, UseInviteCodeActivity.class);
                            intent.putExtra("phone", et_phone.getText().toString());
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
                            Toast.makeText(TestActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(TestActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {//登录失败
                        Toast.makeText(TestActivity.this, result, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(TestActivity.this, "网络连接有问题", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("username", et_phone.getText().toString());
                map.put("password", et_mima.getText().toString());
                map.put("udid", szImei);
                return map;
            }
        };
        mQueue.add(stringRequest);
    }

    //视频注册
    public void createSP() {
        String httpUrl = "http://www.zhongyuedu.com/api/sp/register.php";//公司服务器
        stringRequest = new StringRequest(Request.Method.POST, httpUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //服务器返回的内容
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String resultCode = jsonObject.getString("resultCode");
                    String result = jsonObject.getString("result");
                    if (resultCode.equals("0")) {
                        Toast.makeText(TestActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TestActivity.this, result, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(TestActivity.this, "网络连接有问题", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("username", et_phone.getText().toString());
                map.put("password", et_mima.getText().toString());
                map.put("inviteCode", et_invote.getText().toString());
                map.put("udid", szImei);
                return map;
            }
        };
        mQueue.add(stringRequest);
    }

    //题库注册
    public void createTK() {
        String httpUrl = "http://www.zhongyuedu.com/api/register.php";//公司服务器
        stringRequest = new StringRequest(Request.Method.POST, httpUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //服务器返回的内容
                try {
                    Log.i("xxx", response);
                    JSONObject jsonObject = new JSONObject(response);
                    String resultCode = jsonObject.getString("resultCode");
                    String result = jsonObject.getString("result");
                    if (resultCode.equals("0")) {
                        Toast.makeText(TestActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TestActivity.this, result, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(TestActivity.this, "网络连接有问题", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("username", et_phone.getText().toString());
                map.put("password", et_mima.getText().toString());
                map.put("tikuid", "11");
                return map;
            }
        };
        mQueue.add(stringRequest);
    }

    //题库登录
    public void loginTK() {
        String httpUrl = "http://www.zhongyuedu.com/api/login.php";//公司服务器
        stringRequest = new StringRequest(Request.Method.POST, httpUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("jjj", response);
                //服务器返回的内容
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String resultCode = jsonObject.getString("resultCode");
                    String result = jsonObject.getString("result");
                    if (resultCode.equals("0")) {
                        Toast.makeText(TestActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TestActivity.this, result, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(TestActivity.this, "网络连接有问题", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("username", et_phone.getText().toString());
                map.put("password", et_mima.getText().toString());
                return map;
            }
        };
        mQueue.add(stringRequest);
    }

    private void createKY() {
        String httpUrl = "http://www.zhongyuedu.com/api/tk_ky_register.php";//公司服务器
        stringRequest = new StringRequest(Request.Method.POST, httpUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String resultCode = jsonObject.getString("resultCode");
                    String result = jsonObject.getString("result");
                    if (resultCode.equals("0")) {
                        Toast.makeText(TestActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TestActivity.this, result, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(TestActivity.this, "网络连接有问题", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("username", et_phone.getText().toString().trim());
                map.put("password", et_mima.getText().toString().trim());
                return map;
            }
        };
        mQueue.add(stringRequest);
    }

    private void loginKY() {
        String httpUrl = "http://www.zhongyuedu.com/api/login.php";//公司服务器
        stringRequest = new StringRequest(Request.Method.POST, httpUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String resultCode = jsonObject.getString("resultCode");
                    String result = jsonObject.getString("result");
                    if (resultCode.equals("0")) {

                        Toast.makeText(TestActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TestActivity.this, result, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(TestActivity.this, "网络连接有问题", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("username", et_phone.getText().toString().trim());
                map.put("password", et_mima.getText().toString().trim());
                return map;
            }
        };
        mQueue.add(stringRequest);
    }

    private final String mPageName = "TestActivity";
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
