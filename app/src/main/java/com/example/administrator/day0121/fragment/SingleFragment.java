package com.example.administrator.day0121.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import com.example.administrator.day0121.activity.MainActivity;
import com.example.administrator.day0121.utils.ConfigUtil;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/1/21.
 */
public class SingleFragment extends Fragment implements View.OnClickListener {
    private MainActivity mainActivity;
    private View view;
    private TextView tv_phone, tv_title, tv_call, tv_clear, tv_check, tv_update_user;
    private SharedPreferences sp;
    private RequestQueue mQueue;
    private StringRequest stringRequest;
    private String number;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_single, null);
        initView();
        initData();
        initLinstener();
        return view;
    }

    private void initData() {
        sp = mainActivity.getSharedPreferences(ConfigUtil.spKey, Activity.MODE_PRIVATE);
        number = sp.getString("number", "15853275949");
        String grouptitle = sp.getString("grouptitle", "");
        tv_phone.setText(number.substring(0, 3) + "****" + number.substring(7, 11));
        tv_title.setText(grouptitle);
    }

    private void initLinstener() {
        tv_call.setOnClickListener(this);
        tv_clear.setOnClickListener(this);
        tv_check.setOnClickListener(this);
        tv_update_user.setOnClickListener(this);
    }

    private void initView() {
        tv_call = (TextView) view.findViewById(R.id.tv_call);
        tv_clear = (TextView) view.findViewById(R.id.tv_clear);
        tv_phone = (TextView) view.findViewById(R.id.tv_phone);
        tv_title = (TextView) view.findViewById(R.id.tv_title);
        tv_check = (TextView) view.findViewById(R.id.tv_check);
        tv_update_user = (TextView) view.findViewById(R.id.tv_update_user);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_clear:
                Toast.makeText(mainActivity, "清除缓存成功", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_call:
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:4008-355-366"));
                mainActivity.startActivity(intent);
                break;
            case R.id.tv_check:
                Toast.makeText(mainActivity, "已经是最新版本", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_update_user:
//                upData();
                break;
        }
    }
//更新groupId
    private void upData() {
        mQueue = Volley.newRequestQueue(mainActivity);
        String httpUrl = "http://www.zhongyuedu.com/api/sp/changegroupID.php";//公司服务器
        stringRequest = new StringRequest(Request.Method.POST, httpUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String resultCode = jsonObject.getString("resultCode");
                    JSONObject jsonObject1 = jsonObject.getJSONObject("result");
                    if (resultCode.equals("0")) {//修改groupId
                        String groupId = jsonObject1.getString("groupid");
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("groupId", groupId);
                        editor.commit();
                        Intent intent = new Intent(mainActivity, MainActivity.class);
                        startActivity(intent);
                        mainActivity.finish();
                    } else {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mainActivity, "网络连接有问题", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("username", number);
                return map;
            }
        };
        mQueue.add(stringRequest);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("MainActivity"); //统计页面，"MainScreen"为页面名称，可自定义
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("MainActivity");
    }
}
