package com.example.administrator.day0121.activity;
//启动页

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bokecc.sdk.mobile.download.Downloader;
import com.example.administrator.day0121.R;
import com.example.administrator.day0121.model.DownloadInfo;
import com.example.administrator.day0121.utils.ConfigUtil;
import com.example.administrator.day0121.utils.DataSet;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.jpush.android.api.JPushInterface;

public class WelcomeActivity extends AppCompatActivity {

    private SharedPreferences sp;
    private String token, uid;
    private RequestQueue mQueue;
    private StringRequest stringRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        sp = getSharedPreferences(ConfigUtil.spKey, Activity.MODE_PRIVATE);
        token = sp.getString("token", "");
        uid = sp.getString("uid", "");

        MobclickAgent.openActivityDurationTrack(false);
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);

        if (token == "") {//无token需要进行登录
            final Intent intent = new Intent(this, CreateActivity.class);
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    startActivity(intent);
                    finish();
                }
            };
            timer.schedule(task, 1000 * 1);
        } else {//有token需要进行验证是否有效
            isUseful(token, uid);
        }
        DataSet.init(this);
        setDataStatus();

    }

    /**
     * 初始化下载数据状态
     */
    private void setDataStatus() {
        List<DownloadInfo> downloadInfos = DataSet.getDownloadInfos();
        for (DownloadInfo downloadInfo : downloadInfos) {
            if (downloadInfo.getStatus() == Downloader.DOWNLOAD) {
                downloadInfo.setStatus(Downloader.PAUSE);
            }
        }
    }

    public void isUseful(final String token, final String uid) {
        mQueue = Volley.newRequestQueue(this);
        String httpUrl = "http://www.zhongyuedu.com/api/sp/checktoken.php";//公司服务器
        stringRequest = new StringRequest(Request.Method.POST, httpUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //服务器返回的内容
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String result = jsonObject.getString("result");
                    String resultCode = jsonObject.getString("resultCode");
                    if (resultCode.equals("0")) {//token有效
                        final Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                        intent.putExtra("main_activity","0");
                        Timer timer = new Timer();
                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                startActivity(intent);
                                finish();
                            }
                        };
                        timer.schedule(task, 1000 * 2); //2秒后
                    } else {//token无效,重新登录
                        Toast.makeText(WelcomeActivity.this, result, Toast.LENGTH_LONG).show();
                        final Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                        Timer timer = new Timer();
                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                startActivity(intent);
                                finish();
                            }
                        };
                        timer.schedule(task, 1000 * 2); //2秒后
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(WelcomeActivity.this, "网络连接有问题", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("token", token);
                map.put("uid", uid);
                return map;
            }
        };
        mQueue.add(stringRequest);
    }

    private final String mPageName = "WelcomeActivity";
    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mPageName);
        MobclickAgent.onResume(this);
        JPushInterface.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mPageName);
        MobclickAgent.onPause(this);
        JPushInterface.onPause(this);
    }
}
