package com.example.administrator.day0121.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

/**
 * Created by Administrator on 2016/3/23.
 */
public class NetWorkReceiver extends BroadcastReceiver {
    private BRInteraction brInteraction;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        //网络变化的时候会发送通知
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            brInteraction.setDialog();
        }
    }

    public interface BRInteraction {
        public void setDialog();
    }

    public void setBRInteractionListener(BRInteraction brInteraction) {
        this.brInteraction = brInteraction;
    }
}
