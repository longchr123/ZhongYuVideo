package com.example.administrator.day0121.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.sdk.mobile.download.Downloader;
import com.bokecc.sdk.mobile.download.OnProcessDefinitionListener;
import com.bokecc.sdk.mobile.exception.DreamwinException;
import com.bokecc.sdk.mobile.exception.ErrorCode;
import com.example.administrator.day0121.R;
import com.example.administrator.day0121.activity.MediaPlayActivity;
import com.example.administrator.day0121.activity.SubjectActivity;
import com.example.administrator.day0121.activity.WebActivity;
import com.example.administrator.day0121.download.DownloadService;
import com.example.administrator.day0121.javaBean.SubjectItem;
import com.example.administrator.day0121.javaBean.SubjectList;
import com.example.administrator.day0121.model.DownloadInfo;
import com.example.administrator.day0121.utils.ConfigUtil;
import com.example.administrator.day0121.utils.DataSet;
import com.example.administrator.day0121.utils.MediaUtil;
import com.example.administrator.day0121.utils.ParamsUtil;
import com.umeng.analytics.MobclickAgent;

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
public class SubjectFragment extends Fragment implements AdapterView.OnItemClickListener {
    private SubjectActivity mainActivity;
    private ListView listView;
    private View view;
    private MySubjectFragmentAdapter adapter;
    private SubjectList subjectList;
    private List<SubjectItem> list;

    private Context context;
    private DownloadedReceiver receiver;
    private DownloadService.DownloadBinder binder;
    private Intent service;
    public static HashMap<String, Downloader> downloaderHashMap = new HashMap<String, Downloader>();
    private String videoId;
    private String videoText;
    private Downloader downloader;
    HashMap<Integer, String> hm;
    final String POPUP_DIALOG_MESSAGE = "dialogMessage";
    int[] definitionMapKeys;
    private String title;
    final String GET_DEFINITION_ERROR = "getDefinitionError";

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("service disconnected", name + "");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (DownloadService.DownloadBinder) service;
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (SubjectActivity) getActivity();
        context = mainActivity.getApplicationContext();
        Bundle bundle = getArguments();
        subjectList = (SubjectList) bundle.getSerializable("subject");
        list = subjectList.getList();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_subject, null);
        initView();
        initData();
        initListener();
        receiver = new DownloadedReceiver();
        mainActivity.registerReceiver(receiver, new IntentFilter(ConfigUtil.ACTION_DOWNLOADING));
        service = new Intent(context, DownloadService.class);
        mainActivity.bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
        initDownloaderHashMap();
        return view;
    }

    private void initData() {
        adapter = new MySubjectFragmentAdapter(mainActivity, list);
        listView.setAdapter(adapter);
    }

    private void initListener() {
        listView.setOnItemClickListener(this);
    }

    private void initView() {
        listView = (ListView) view.findViewById(R.id.lv);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(mainActivity, WebActivity.class);
        intent.putExtra("webUrl", list.get(position).getReleatedurl());
        startActivity(intent);
    }

    private void initDownloaderHashMap() {
        //初始化DownloaderHashMap
        List<DownloadInfo> downloadInfoList = DataSet.getDownloadInfos();
        for (int i = 0; i < downloadInfoList.size(); i++) {
            DownloadInfo downloadInfo = downloadInfoList.get(i);
            if (downloadInfo.getStatus() == Downloader.FINISH) {
                continue;
            }

            String title = downloadInfo.getTitle();
            File file = MediaUtil.createFile(title);
            if (file == null) {
                continue;
            }

            String dataVideoId = downloadInfo.getVideoId();
            Downloader downloader = new Downloader(file, dataVideoId, ConfigUtil.USERID, ConfigUtil.API_KEY);

            int downloadInfoDefinition = downloadInfo.getDefinition();
            if (downloadInfoDefinition != -1) {
                downloader.setDownloadDefinition(downloadInfoDefinition);
            }
            downloaderHashMap.put(title, downloader);
        }
    }

    /**
     * 下载广播
     */
    private class DownloadedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 若下载出现异常，提示用户处理
            int errorCode = intent.getIntExtra("errorCode", ParamsUtil.INVALID);
            if (errorCode == ErrorCode.NETWORK_ERROR.Value()) {
                Toast.makeText(context, "网络异常，请检查", Toast.LENGTH_SHORT).show();
            } else if (errorCode == ErrorCode.PROCESS_FAIL.Value()) {
                Toast.makeText(context, "下载失败，请重试", Toast.LENGTH_SHORT).show();
            } else if (errorCode == ErrorCode.INVALID_REQUEST.Value()) {
                Toast.makeText(context, "下载失败，请检查帐户信息", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class MySubjectFragmentAdapter extends BaseAdapter {
        private Context context;
        private List<SubjectItem> list;
        //定义hashmap存储downloader信息

        public MySubjectFragmentAdapter(Context context, List<SubjectItem> list) {
            this.context = context;
            this.list = list;
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
        public View getView(final int position, View convertView, final ViewGroup parent) {
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
                    downLoad(position);
                }
            });
            return convertView;
        }

        /**
         * 下载视频
         */
        private void downLoad(int position) {
            videoId = list.get(position).getVideoid();//可能有问题
            videoText=list.get(position).getTitle();
            downloader = new Downloader(videoId, ConfigUtil.USERID, ConfigUtil.API_KEY);
            downloader.setOnProcessDefinitionListener( onProcessDefinitionListener);
            downloader.getDefinitionMap();
        }
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
            if (message.equals(POPUP_DIALOG_MESSAGE)) {
                String[] definitionMapValues = new String[hm.size()];
                definitionMapKeys = new int[hm.size()];
                Set<Map.Entry<Integer, String>> set = hm.entrySet();
                Iterator<Map.Entry<Integer, String>> iterator = set.iterator();
                int i = 0;
                while (iterator.hasNext()) {
                    Map.Entry<Integer, String> entry = iterator.next();
                    definitionMapKeys[i] = entry.getKey();
                    definitionMapValues[i] = entry.getValue();
                    i++;
                }

//              int definition = definitionMapKeys[which];
                int definition = 1;

                title = videoText;
                if (DataSet.hasDownloadInfo(title)) {
                    Toast.makeText(context, "文件已存在", Toast.LENGTH_SHORT).show();
                    return;
                }

                File file = MediaUtil.createFile(title);
                if (file == null) {
                    Toast.makeText(context, "创建文件失败", Toast.LENGTH_LONG).show();
                    return;
                }

                if (binder == null || binder.isStop()) {
                    Intent service = new Intent(context, DownloadService.class);
                    service.putExtra("title", title);
                    context.startService(service);
                } else {
                    Intent intent = new Intent(ConfigUtil.ACTION_DOWNLOADING);
                    context.sendBroadcast(intent);
                }

                downloader.setFile(file); //确定文件名后，把文件设置到downloader里
                downloader.setDownloadDefinition(definition);
                downloaderHashMap.put(title, downloader);
                DataSet.addDownloadInfo(new DownloadInfo(videoId, title, 0, null, Downloader.WAIT, new Date(), definition));
                Toast.makeText(context, "文件已加入下载队列", Toast.LENGTH_SHORT).show();
            }

            if (message.equals(GET_DEFINITION_ERROR)) {
                Toast.makeText(context, "网络异常，请重试", Toast.LENGTH_LONG).show();
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainActivity.unbindService(serviceConnection);
        mainActivity.unregisterReceiver(receiver);
        mainActivity.stopService(service);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("SubjectActivity"); //统计页面，"MainScreen"为页面名称，可自定义
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("SubjectActivity");
    }
}
