package com.example.administrator.day0121.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.day0121.R;
import com.example.administrator.day0121.javaBean.Record;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Administrator on 2016/1/21.
 */
public class MyXueXiGridViewAdapter extends BaseAdapter {
    private Context context;
    private List<Record> list;

    public MyXueXiGridViewAdapter(Context context, List<Record> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    //用于更新数据
    public void setData(List<Record> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_newmain_gv, null);
            vh = new ViewHolder();
            vh.iv = (ImageView) convertView.findViewById(R.id.iv);
            vh.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            vh.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
            vh.tv_posttime = (TextView) convertView.findViewById(R.id.tv_posttime);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        Picasso.with(context).load(list.get(position).getThumbnail()).into(vh.iv);
        vh.tv_time.setText("时长：" + list.get(position).getDutation());
        vh.tv_title.setText(list.get(position).getTitle());
        vh.tv_posttime.setText("发布日期：" + list.get(position).getDate());
        return convertView;
    }

    private class ViewHolder {
        private ImageView iv;
        private TextView tv_title;
        private TextView tv_time;
        private TextView tv_posttime;
    }
}
