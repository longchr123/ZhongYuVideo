package com.example.administrator.day0121.utils;

import android.app.AlertDialog;
import android.content.Context;

/**
 * Created by Administrator on 2016/3/19.
 */
public class ShowDialog {
    public static void showDialog(Context context, String detail) {
        new AlertDialog.Builder(context).setTitle("提示").setMessage(detail).setPositiveButton("确定", null).show();
    }
}
