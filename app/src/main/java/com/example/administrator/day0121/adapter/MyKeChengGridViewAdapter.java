package com.example.administrator.day0121.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.day0121.R;
import com.example.administrator.day0121.javaBean.Subject;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Administrator on 2016/3/7.
 */
public class MyKeChengGridViewAdapter extends BaseAdapter {
    private Context context;
    private List<Subject> list;

    public MyKeChengGridViewAdapter(Context context, List<Subject> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class ViewHolder {
        ImageView iv;
        TextView tv;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_kecheng_gv, null);
            vh = new ViewHolder();
            vh.iv = (ImageView) convertView.findViewById(R.id.iv);
            vh.tv = (TextView) convertView.findViewById(R.id.tv);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        Picasso.with(context).load(list.get(position).getImagesURL()).into(vh.iv);
        vh.tv.setText(list.get(position).getTitle());
        return convertView;
    }
}
