package com.example.administrator.day0121.fragment;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
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
import com.example.administrator.day0121.activity.WebActivity;
import com.example.administrator.day0121.adapter.MyListViewAdapter;
import com.example.administrator.day0121.javaBean.NewsItem;
import com.example.administrator.day0121.utils.ParseJson;
import com.example.administrator.day0121.utils.TimeUtil;
import com.example.administrator.day0121.view.XListView;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/1/25.
 */
public class NewsFragment extends Fragment implements XListView.IXListViewListener {
    private MainActivity mainActivity;
    private View view;
    private XListView lv;
    private MyListViewAdapter myListViewAdapter;
    private StringRequest stringRequest;
    private RequestQueue mQueue;
    private int page = 1;
    private List<NewsItem> list = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        mQueue = Volley.newRequestQueue(mainActivity);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_news, null);
        initView();
        downJson(page);
        initData();
        initListener();
        return view;
    }

    private void initData() {
        myListViewAdapter = new MyListViewAdapter(mainActivity, list);
        lv.setAdapter(myListViewAdapter);
    }

    //使用XListView的时候会发生position的错位，需要给position加一或者减一
    private void initListener() {
        lv.setPullLoadEnable(true);
        lv.setXListViewListener(this);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mainActivity, WebActivity.class);
                intent.putExtra("webUrl", list.get(position - 1).getUrl());
                startActivity(intent);
            }
        });
    }


    private void initView() {
        lv = (XListView) view.findViewById(R.id.lv);
    }

    //下载新闻json字符串
    public void downJson(final int pages) {
        String httpUrl = "http://www.zhongyuedu.com/api/newslist.php";//公司服务器
        stringRequest = new StringRequest(Request.Method.POST, httpUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //服务器返回的内容
                list.addAll(ParseJson.parseJsonNews(response));
                myListViewAdapter.setData(list);
                onLoad();
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
                map.put("groupId", 1 + "");
                map.put("type", 1 + "");
                map.put("page", pages + "");
                return map;
            }
        };
        mQueue.add(stringRequest);
    }

    //加载刷新完成
    private void onLoad() {
        lv.stopRefresh();
        lv.stopLoadMore();
        String time = TimeUtil.getFormated2DateTime(new Date().getTime());
        lv.setRefreshTime(time);
    }

    //刷新
    @Override
    public void onRefresh() {
        list.clear();
        page = 1;
        downJson(page);
    }

    //加载
    @Override
    public void onLoadMore() {
        downJson(++page);
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
