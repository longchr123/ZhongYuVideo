package com.example.administrator.day0121.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.administrator.day0121.R;
import com.example.administrator.day0121.javaBean.SubjectType;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2016/1/26.
 */
public class MySubjectListAdapter extends BaseAdapter {
    private Context context;
    private List<SubjectType> list;
    private HashMap<Integer, Integer> hashMap = new HashMap<>();

    public MySubjectListAdapter(Context context, List<SubjectType> list) {
        this.context = context;
        this.list = list;
        for (int i = 0; i < list.size(); i++) {
            if (i == 0) {
                hashMap.put(i, 1);
            } else {
                hashMap.put(i, 0);
            }
        }
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void changeTextColor(int position) {
        hashMap.clear();
        for (int i = 0; i < list.size(); i++) {
            if (i == position) {
                hashMap.put(i, 1);
            } else {
                hashMap.put(i, 0);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tv;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_subject_lv, null);
            tv = (TextView) convertView.findViewById(R.id.tv);
            convertView.setTag(tv);
        } else {
            tv = (TextView) convertView.getTag();
        }
        tv.setText(list.get(position).getContent());
        tv.setGravity(Gravity.CENTER);
        if (hashMap.get(position) == 1) {
            tv.setTextColor(Color.RED);
            tv.setBackgroundColor(Color.WHITE);
        } else {
            tv.setTextColor(Color.parseColor("#25cccd"));
            tv.setBackgroundColor(Color.parseColor("#80c0c0c0"));
        }
        return convertView;
    }
}
