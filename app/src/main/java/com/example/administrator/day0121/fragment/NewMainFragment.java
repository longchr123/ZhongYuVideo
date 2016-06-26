package com.example.administrator.day0121.fragment;
//使用picasso加载图片

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.administrator.day0121.activity.LoginActivity;
import com.example.administrator.day0121.activity.MainActivity;
import com.example.administrator.day0121.R;
import com.example.administrator.day0121.activity.MediaPlayActivity;
import com.example.administrator.day0121.activity.SubjectActivity;
import com.example.administrator.day0121.activity.SubjectLowActivity;
import com.example.administrator.day0121.activity.WebActivity;
import com.example.administrator.day0121.adapter.MyKeChengGridViewAdapter;
import com.example.administrator.day0121.adapter.MyXueXiGridViewAdapter;
import com.example.administrator.day0121.adapter.MyViewPagerAdapter;
import com.example.administrator.day0121.javaBean.Banners;
import com.example.administrator.day0121.javaBean.Record;
import com.example.administrator.day0121.javaBean.Subject;
import com.example.administrator.day0121.utils.ConfigUtil;
import com.example.administrator.day0121.utils.ParseJson;
import com.squareup.picasso.Picasso;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2016/1/21.
 */
public class NewMainFragment extends Fragment implements ViewPager.OnPageChangeListener {
    private MainActivity mainActivity;
    private View view;
    private GridView gv_kecheng, gv_xuexi;
    private ViewPager vp;
    private MyXueXiGridViewAdapter adapter_xuexi;
    private MyKeChengGridViewAdapter adapter_kecheng;
    private MyViewPagerAdapter myViewPagerAdapter;
    private TextView tv_top;
    private int currentItem = 0;
    private int j;
    private ScheduledExecutorService scheduledExecutorService;
    private List<Banners> bannersList;
    private List<Subject> subjectList;
    private List<Record> recordList;
    private List<ImageView> imageViews = new ArrayList<>();
    private RequestQueue mQueue;
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            vp.setCurrentItem(currentItem);
        }
    };
    private LinearLayout lin;
    private SharedPreferences sp;
    private String groupId, uid, token;
    private Dialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        mQueue = Volley.newRequestQueue(mainActivity);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_newmain, null);
        initView();
        getData();
        downJson();
        return view;
    }

    private void getData() {
        sp = mainActivity.getSharedPreferences(ConfigUtil.spKey, Activity.MODE_PRIVATE);
        groupId = sp.getString("groupId", "");
        uid = sp.getString("uid", "");
        token = sp.getString("token", "");
        dialog = new AlertDialog.Builder(mainActivity).setTitle("加载中...").
                setView(new ProgressBar(mainActivity)).setCancelable(false).create();
    }

    //bunner的切换
    private void startPlay() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        // 当Activity显示出来后，每五秒执行一次ScrollTask
        scheduledExecutorService.scheduleAtFixedRate(new ScrollTask(), 5, 3,
                TimeUnit.SECONDS);
    }

    private class ScrollTask implements Runnable {

        @Override
        public void run() {
            synchronized (vp) {
                currentItem = (currentItem + 1) % imageViews.size();
                handler.obtainMessage().sendToTarget();
            }
        }
    }

    private void initListener() {
        gv_kecheng.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.show();
                isUsefulToKeCheng(position);
            }
        });
        gv_xuexi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.show();
                isUsefulToMedia(position);
            }
        });
        vp.setOnPageChangeListener(this);
    }

    private void initData() {
        adapter_kecheng = new MyKeChengGridViewAdapter(mainActivity, subjectList);
        gv_kecheng.setAdapter(adapter_kecheng);

        adapter_xuexi = new MyXueXiGridViewAdapter(mainActivity, recordList);
        gv_xuexi.setAdapter(adapter_xuexi);

        myViewPagerAdapter = new MyViewPagerAdapter(imageViews);
        vp.setAdapter(myViewPagerAdapter);
        //给bunner添加图片地址
        for (int i = 0; i < bannersList.size(); i++) {
            ImageView iv = new ImageView(mainActivity);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            j = i;
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mainActivity, WebActivity.class);
                    intent.putExtra("webUrl", bannersList.get(j).getContentURL());
                    Log.i("webUrl", bannersList.get(j).getContentURL());
                    mainActivity.startActivity(intent);
                }
            });
            Picasso.with(mainActivity).load(bannersList.get(i).getImageURL()).into(iv);
            imageViews.add(iv);
        }
        myViewPagerAdapter.setData(imageViews);
        tv_top.setText(bannersList.get(0).getTitle());
    }

    private void initView() {
        vp = (ViewPager) view.findViewById(R.id.vp);
        gv_xuexi = (GridView) view.findViewById(R.id.gv_xuexi);
        gv_kecheng = (GridView) view.findViewById(R.id.gv_kecheng);
        tv_top = (TextView) view.findViewById(R.id.tv_top);
        lin = (LinearLayout) view.findViewById(R.id.lin);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        currentItem = position;
        tv_top.setText(bannersList.get(position).getTitle());
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    //下载主页json字符串
    private void downJson() {
        String url = "http://www.zhongyuedu.com/api/sp/studenthomepage.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("hhh", response);
                        bannersList = ParseJson.bannersList(response);
                        subjectList = ParseJson.subjectList(response);
                        recordList = ParseJson.recordList(response);
                        initData();
                        startPlay();
                        initListener();
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
                map.put("groupid", groupId);
                return map;
            }
        };
        mQueue.add(stringRequest);
    }

    //监测token是否有效播放视频
    public void isUsefulToMedia(final int location) {
        String httpUrl = "http://www.zhongyuedu.com/api/sp/checktoken.php";//公司服务器
        StringRequest stringRequest = new StringRequest(Request.Method.POST, httpUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //服务器返回的内容
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String resultCode = jsonObject.getString("resultCode");
                    String result = jsonObject.getString("result");
                    if (resultCode.equals("0")) {//token有效
                        Intent intent = new Intent(mainActivity, MediaPlayActivity.class);
                        intent.putExtra("videoId", recordList.get(location).getVideoId());
                        intent.putExtra("videoText", recordList.get(location).getTitle());
                        startActivity(intent);
                    } else {//token无效,重新登录
//                        Toast.makeText(mainActivity, result, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(mainActivity, LoginActivity.class);
                        startActivity(intent);
                        mainActivity.finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                Toast.makeText(mainActivity, "网络连接有问题", Toast.LENGTH_SHORT).show();
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

    //监测token是否有效进入课程分类
    public void isUsefulToKeCheng(final int location) {
        String httpUrl = "http://www.zhongyuedu.com/api/sp/checktoken.php";//公司服务器
        StringRequest stringRequest = new StringRequest(Request.Method.POST, httpUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //服务器返回的内容
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String resultCode = jsonObject.getString("resultCode");
                    String result = jsonObject.getString("result");
                    //token有效
                    if (resultCode.equals("0")) {
                        //非正式学员
                        if (groupId.equals("0") || groupId.equals("8")) {
                            Intent intent = new Intent(mainActivity, SubjectLowActivity.class);
                            intent.putExtra("keyword", "");
                            startActivity(intent);
                        } else {//正式学员
                            Intent intent = new Intent(mainActivity, SubjectActivity.class);
                            intent.putExtra("keyword", subjectList.get(location).getKeyword());
                            intent.putExtra("subject", subjectList.get(location).getTitle());
                            intent.putExtra("groupId", groupId);
                            intent.putExtra("token", token);
                            intent.putExtra("uid", uid);
                            startActivity(intent);
                        }
                    } else {//token无效,重新登录
//                        Toast.makeText(mainActivity, result, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(mainActivity, LoginActivity.class);
                        startActivity(intent);
                        mainActivity.finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                Toast.makeText(mainActivity, "网络连接有问题", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onStop() {
        super.onStop();
        dialog.dismiss();
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
