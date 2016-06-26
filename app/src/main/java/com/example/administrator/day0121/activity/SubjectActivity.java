package com.example.administrator.day0121.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.example.administrator.day0121.adapter.MySubjectListAdapter;
import com.example.administrator.day0121.fragment.SubjectFragment;
import com.example.administrator.day0121.javaBean.SubjectType;
import com.example.administrator.day0121.utils.ConfigUtil;
import com.example.administrator.day0121.utils.ParseJson;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubjectActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
    private ListView lv;
    private TextView tv_back, tv_down, tv_title;
    private ImageView iv_back;
    private MySubjectListAdapter adapter;
    private FragmentManager manager;
    private FragmentTransaction transaction;
    private SubjectFragment fragment;
    private String keyword, title, token, groupId, uid;
    private RequestQueue mQueue;
    private StringRequest stringRequest;
    private List<SubjectType> list;

    /**用于存储是否从本页面返回至MainActivity的信息*/
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);
        getIntentData();
        initView();
        downJson();
    }

    private void getIntentData() {
        keyword = getIntent().getStringExtra("keyword");
        token = getIntent().getStringExtra("token");
        groupId = getIntent().getStringExtra("groupId");
        title = getIntent().getStringExtra("subject");
        uid = getIntent().getStringExtra("uid");
        mSharedPreferences=this.getSharedPreferences(ConfigUtil.NAME,MODE_PRIVATE);
        mEditor=mSharedPreferences.edit();
    }

    private void initListener() {
        lv.setOnItemClickListener(this);
        tv_back.setOnClickListener(this);
        tv_down.setOnClickListener(this);
        iv_back.setOnClickListener(this);
    }

    private void initData() {
        adapter = new MySubjectListAdapter(this, list);
        lv.setAdapter(adapter);
        tv_title.setText(title);
        manager = getFragmentManager();
        transaction = manager.beginTransaction();
        fragment = new SubjectFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("subject", list.get(0).getSubjectList());
        fragment.setArguments(bundle);
        transaction.replace(R.id.lin, fragment).commit();
    }

    private void initView() {
        lv = (ListView) findViewById(R.id.lv);
        tv_back = (TextView) findViewById(R.id.tv_back);
        tv_down = (TextView) findViewById(R.id.tv_down);
        tv_title = (TextView) findViewById(R.id.tv_title);
        iv_back = (ImageView) findViewById(R.id.iv_back);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        adapter.changeTextColor(position);
        fragment = new SubjectFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("subject", list.get(position).getSubjectList());
        fragment.setArguments(bundle);
        transaction = manager.beginTransaction();
        transaction.replace(R.id.lin, fragment).commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_back:
                finish();
                break;
            case R.id.tv_down:
//                mEditor.putBoolean(ConfigUtil.FROMSUB,true).commit();
                startActivity(new Intent(this,DownLoadActivity.class));
//                finish();
                break;
            case R.id.iv_back:
                finish();
                break;
        }
    }

    //根据keyword返回list集合
    public void downJson() {
        mQueue = Volley.newRequestQueue(this);
        String httpUrl = "http://www.zhongyuedu.com/api/sp/subjectCategory.php";//公司服务器
        stringRequest = new StringRequest(Request.Method.POST, httpUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("sss", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String resultCode = jsonObject.getString("resultCode");
                    String result = jsonObject.getString("result");
                    if (resultCode.equals("0")) {
                        list = ParseJson.subjectTypeList(response);
                        if (list == null || list.size() == 0) {
                            Toast.makeText(SubjectActivity.this, "暂无数据", Toast.LENGTH_SHORT).show();
                        } else {
                            initData();
                            initListener();
                        }
                    } else {
                        Toast.makeText(SubjectActivity.this, result, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(SubjectActivity.this, "网络连接有问题", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("keyword", keyword);
                map.put("uid", uid);
                map.put("token", token);
                map.put("groupid", groupId);
                return map;
            }
        };
        mQueue.add(stringRequest);
    }

    private final String mPageName = "SubjectActivity";
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
