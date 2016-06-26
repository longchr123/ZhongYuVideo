package com.example.administrator.day0121.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.day0121.R;

/**
 * Created by Administrator on 2016/4/11.
 */
public class MySubjectLowGridViewAdapter extends BaseAdapter {
    private Context context;
    public MySubjectLowGridViewAdapter(Context context){
        this.context=context;
    }
    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder{
        private TextView tv,tv_listen;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if(convertView==null){
            convertView= LayoutInflater.from(context).inflate(R.layout.item_subject_low_gv,null);
            vh=new ViewHolder();
            vh.tv= (TextView) convertView.findViewById(R.id.tv);
            vh.tv_listen= (TextView) convertView.findViewById(R.id.tv_listen);
            convertView.setTag(vh);
        }else {
            vh= (ViewHolder) convertView.getTag();
        }
        vh.tv_listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"播放试听"+position,Toast.LENGTH_SHORT).show();
            }
        });
        return convertView;
    }
}
