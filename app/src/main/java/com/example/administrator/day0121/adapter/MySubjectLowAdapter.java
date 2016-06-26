package com.example.administrator.day0121.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.day0121.R;
import com.example.administrator.day0121.activity.ClassDetailActivity;
import com.example.administrator.day0121.view.MyGridView;

/**
 * Created by Administrator on 2016/4/11.
 */
public class MySubjectLowAdapter extends BaseAdapter {
    private Context context;

    public MySubjectLowAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        private TextView tv;
        private MyGridView gv;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_subject_low_lv, null);
            vh = new ViewHolder();
            vh.tv = (TextView) convertView.findViewById(R.id.tv);
            vh.gv = (MyGridView) convertView.findViewById(R.id.gv);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        vh.gv.setAdapter(new MySubjectLowGridViewAdapter(context));
        vh.gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(context, ClassDetailActivity.class);
                context.startActivity(intent);
            }
        });
        return convertView;
    }
}
