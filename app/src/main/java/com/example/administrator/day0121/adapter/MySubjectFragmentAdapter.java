package com.example.administrator.day0121.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.sdk.mobile.download.Downloader;
import com.bokecc.sdk.mobile.download.OnProcessDefinitionListener;
import com.bokecc.sdk.mobile.exception.DreamwinException;
import com.example.administrator.day0121.R;
import com.example.administrator.day0121.activity.MediaPlayActivity;
import com.example.administrator.day0121.download.DownloadService;
import com.example.administrator.day0121.javaBean.SubjectItem;
import com.example.administrator.day0121.model.DownloadInfo;
import com.example.administrator.day0121.utils.ConfigUtil;
import com.example.administrator.day0121.utils.DataSet;
import com.example.administrator.day0121.utils.MediaUtil;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2016/1/27.
 */
public class MySubjectFragmentAdapter extends BaseAdapter {
    private Context context;
    private List<SubjectItem> list;
    //定义hashmap存储downloader信息
    public static HashMap<String, Downloader> downloaderHashMap = new HashMap<String, Downloader>();
    final String GET_DEFINITION_ERROR  = "getDefinitionError";

    public MySubjectFragmentAdapter(Context context, List<SubjectItem> list) {
        this.context = context;
        this.list = list;
        initDownloaderHashMap();
    }

    private void initDownloaderHashMap(){
        DataSet.init(context);
        //初始化DownloaderHashMap
        List<DownloadInfo> downloadInfoList = DataSet.getDownloadInfos();
        for(int i = 0; i<downloadInfoList.size(); i++){
            DownloadInfo downloadInfo = downloadInfoList.get(i);
            if (downloadInfo.getStatus() == Downloader.FINISH) {
                continue;
            }

            String title = downloadInfo.getTitle();
            File file = MediaUtil.createFile(title);
            if (file == null ){
                continue;
            }

            String dataVideoId = downloadInfo.getVideoId();
            Downloader downloader = new Downloader(file, dataVideoId, ConfigUtil.USERID, ConfigUtil.API_KEY);

            int downloadInfoDefinition = downloadInfo.getDefinition();
            if (downloadInfoDefinition != -1){
                downloader.setDownloadDefinition(downloadInfoDefinition);
            }
            downloaderHashMap.put(title, downloader);
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

    private class ViewHolder {
        private TextView tv_title, tv_play, tv_load;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_subject_fragment_lv, null);
            vh = new ViewHolder();
            vh.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            vh.tv_load = (TextView) convertView.findViewById(R.id.tv_load);
            vh.tv_play = (TextView) convertView.findViewById(R.id.tv_play);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        vh.tv_title.setText(list.get(position).getTitle());
        vh.tv_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MediaPlayActivity.class);
                intent.putExtra("videoId", list.get(position).getVideoid());
                intent.putExtra("videoText", list.get(position).getTitle());
                context.startActivity(intent);
            }
        });
        vh.tv_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downLoad();
                Toast.makeText(context, "成功加入下载队列", Toast.LENGTH_SHORT).show();
            }
        });
        return convertView;
    }

    private String videoId;
    private Downloader downloader;
    HashMap<Integer, String> hm;
    final String POPUP_DIALOG_MESSAGE = "dialogMessage";
    int[] definitionMapKeys;
    private String title;
    private DownloadService.DownloadBinder binder;
    private AlertDialog definitionDialog;

    /**
     * 下载视频
     */
    private void downLoad() {
        //点击item时，downloader初始化使用的是设置清晰度方式

//        Log.e("downloaderHashMap", downloaderHashMap.toString());

//        Pair<String, Integer> pair = (Pair<String, Integer>) convertView;
//        videoId = pair.first;
        videoId = "2FD815DA18613F809C33DC5901307461";//可能有问题
        downloader = new Downloader(videoId, ConfigUtil.USERID, ConfigUtil.API_KEY);
        downloader.setOnProcessDefinitionListener(onProcessDefinitionListener);
        downloader.getDefinitionMap();
    }

    private OnProcessDefinitionListener onProcessDefinitionListener = new OnProcessDefinitionListener() {
        @Override
        public void onProcessDefinition(HashMap<Integer, String> definitionMap) {
            hm = definitionMap;
            if (hm != null) {
                Message msg = new Message();
                msg.obj = POPUP_DIALOG_MESSAGE;
                handler.sendMessage(msg);
            } else {
                Log.e("get definition error", "视频清晰度获取失败");
            }
        }

        @Override
        public void onProcessException(DreamwinException e) {

        }
    };

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String message = (String) msg.obj;
            if ( message.equals(POPUP_DIALOG_MESSAGE)) {
                String[] definitionMapValues = new String[hm.size()];
                definitionMapKeys = new int[hm.size()];
                Set<Map.Entry<Integer, String>> set = hm.entrySet();
                Iterator<Map.Entry<Integer, String>> iterator = set.iterator();
                int i = 0;
                while(iterator.hasNext()){
                    Map.Entry<Integer, String> entry = iterator.next();
                    definitionMapKeys[i] = entry.getKey();
                    definitionMapValues[i] = entry.getValue();
                    i++;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("选择下载清晰度");
                builder.setSingleChoiceItems(definitionMapValues, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        int definition = definitionMapKeys[which];

                        title = videoId + "-" + definition;
                        if (DataSet.hasDownloadInfo(title)) {
                            Toast.makeText(context, "文件已存在", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        File file = MediaUtil.createFile(title);
                        if (file == null ){
                            Toast.makeText(context, "创建文件失败", Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (binder == null || binder.isStop()) {
                            Intent service = new Intent(context, DownloadService.class);
                            service.putExtra("title", title);
                            context.startService(service);
                        } else{
                            Intent intent = new Intent(ConfigUtil.ACTION_DOWNLOADING);
                            context.sendBroadcast(intent);
                        }

                        downloader.setFile(file); //确定文件名后，把文件设置到downloader里
                        downloader.setDownloadDefinition(definition);
                        downloaderHashMap.put(title, downloader);
                        DataSet.addDownloadInfo(new DownloadInfo(videoId, title, 0, null, Downloader.WAIT, new Date(), definition));

                        definitionDialog.dismiss();
                        Toast.makeText(context, "文件已加入下载队列", Toast.LENGTH_SHORT).show();
                    }
                });
                definitionDialog = builder.create();
                definitionDialog.show();
            }

            if ( message.equals(GET_DEFINITION_ERROR)) {
                Toast.makeText(context, "网络异常，请重试", Toast.LENGTH_LONG).show();
            }
            super.handleMessage(msg);
        }
    };

}
