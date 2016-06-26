package com.example.administrator.day0121.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.sdk.mobile.download.Downloader;
import com.bokecc.sdk.mobile.exception.ErrorCode;
import com.example.administrator.day0121.R;
import com.example.administrator.day0121.activity.DownLoadActivity;
import com.example.administrator.day0121.activity.MediaPlayActivity;
import com.example.administrator.day0121.adapter.MyPagerAdapter;
import com.example.administrator.day0121.download.DownloadService;
import com.example.administrator.day0121.download.DownloadView;
import com.example.administrator.day0121.download.DownloadViewAdapter;
import com.example.administrator.day0121.download.VideoListViewAdapter;
import com.example.administrator.day0121.model.DownloadInfo;
import com.example.administrator.day0121.utils.ConfigUtil;
import com.example.administrator.day0121.utils.DataSet;
import com.example.administrator.day0121.utils.ParamsUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2016/1/21.
 */
public class LoadManagerFragment extends Fragment implements View.OnClickListener,ViewPager.OnPageChangeListener{
    private DownLoadActivity mainActivity;
    private View view;
    private ViewPager mViewPager;
    private ImageView img_cursor;
    private TextView tv_one;
    private TextView tv_two;
    private Context context;

    private ArrayList<View> listViews;
    private int offset = 0;//移动条图片的偏移量
    private int currIndex = 0;//当前页面的编号
    private int bmpWidth;// 移动条图片的长度
    private int one = 0; //移动条滑动一页的距离

    private ListView mDownLoadingList;//下载中列表
    private Timer timter = new Timer();
    private boolean isBind;
    private Intent service;
    private DownloadedReceiver receiver;
    private String currentDownloadTitle;
    private ServiceConnection serviceConnection;
    private DownloadService.DownloadBinder binder;
    private List<DownloadInfo> downloadingInfos;
    private DownloadViewAdapter downloadingAdapter;

    private ListView mDownLoadedList;//已下载列表
    private List<Pair<String, Integer>> pairs;
    private VideoListViewAdapter videoListViewAdapter;
    private boolean isFirst;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (DownLoadActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = LayoutInflater.from(mainActivity).inflate(R.layout.fragment_loadmanager, null);
        context = mainActivity.getApplicationContext();
        currentDownloadTitle = mainActivity.getIntent().getStringExtra("title");
        initView();
        initData();
        initListener();
        timter.schedule(timerTask, 0, 1000);
        receiver = new DownloadedReceiver();
        mainActivity.registerReceiver(receiver, new IntentFilter(ConfigUtil.ACTION_DOWNLOADING));
        bindServer();
        return view;
    }


    private void initData() {
        downloadingInfos = new ArrayList<DownloadInfo>();
        List<DownloadInfo> downloadInfos = DataSet.getDownloadInfos();
        for (DownloadInfo downloadInfo : downloadInfos) {
            if (downloadInfo.getStatus() == Downloader.FINISH) {
                continue;
            }
            downloadingInfos.add(downloadInfo);
        }
        downloadingAdapter = new DownloadViewAdapter(context, downloadingInfos);
        mDownLoadingList.setAdapter(downloadingAdapter);
        mDownLoadingList.invalidate();

        // 生成动态数组，加入数据
        pairs = new ArrayList<Pair<String,Integer>>();
        for (DownloadInfo downloadInfo : downloadInfos) {

            if (downloadInfo.getStatus() != Downloader.FINISH) {
                continue;
            }
            Pair<String, Integer> pair = new Pair<String, Integer>(downloadInfo.getTitle(), R.mipmap.play);
            pairs.add(pair);
        }

        videoListViewAdapter = new VideoListViewAdapter(context, pairs);
        mDownLoadedList.setAdapter(videoListViewAdapter);
    }

    private void initListener() {}

    private void initView() {
        mViewPager= (ViewPager) view.findViewById(R.id.viewPager);
        tv_one = (TextView) view.findViewById(R.id.tv_one);
        tv_two = (TextView) view.findViewById(R.id.tv_two);
        img_cursor = (ImageView) view.findViewById(R.id.img_cursor);

        //下划线动画的相关设置：
        bmpWidth = BitmapFactory.decodeResource(getResources(), R.mipmap.line).getWidth();// 获取图片宽度
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;// 获取分辨率宽度
        offset = (screenW / 2 - bmpWidth) / 2;// 计算偏移量
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        img_cursor.setImageMatrix(matrix);// 设置动画初始位置
        //移动的距离
        one = offset * 2 + bmpWidth;// 移动一页的偏移量,比如1->2,或者2->3

        //往ViewPager填充View，同时设置点击事件与页面切换事件
        listViews = new ArrayList<View>();
        LayoutInflater mInflater = getActivity().getLayoutInflater();
        listViews.add(mInflater.inflate(R.layout.list_downloading, null, false));
        listViews.add(mInflater.inflate(R.layout.list_downloaded, null, false));
        mViewPager.setAdapter(new MyPagerAdapter(listViews));
        mViewPager.setCurrentItem(0);          //设置ViewPager当前页，从0开始算

        tv_one.setOnClickListener(this);
        tv_two.setOnClickListener(this);
        mViewPager.addOnPageChangeListener(this);

        mDownLoadingList= (ListView) listViews.get(0).findViewById(R.id.list_downloading);
        mDownLoadingList.setOnItemClickListener(onDownLoadingItemClickListener);
        mDownLoadingList.setOnCreateContextMenuListener(onDownLoadingCreateContextMenuListener);

        mDownLoadedList= (ListView) listViews.get(1).findViewById(R.id.list_downloaded);
        mDownLoadedList.setOnItemClickListener(onDownLoadedItemClickListener);
        mDownLoadedList.setOnCreateContextMenuListener(onDownLoadedCreateContextMenuListener);
    }

    private void bindServer() {
        service = new Intent(context, DownloadService.class);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.i("service disconnected", name + "");
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder = (DownloadService.DownloadBinder) service;
            }
        };
        mainActivity.bindService(service, serviceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_one:
                mViewPager.setCurrentItem(0);
                break;
            case R.id.tv_two:
                mViewPager.setCurrentItem(1);
                break;
        }
    }

    @Override
    public void onPageSelected(int index) {
        Animation animation = null;
        switch (index) {
            case 0:
                animation = new TranslateAnimation(one, 0, 0, 0);
                break;
            case 1:
                animation = new TranslateAnimation(offset, one, 0, 0);
                break;
        }
        currIndex = index;
        animation.setFillAfter(true);// true表示图片停在动画结束位置
        animation.setDuration(300); //设置动画时间为300毫秒
        img_cursor.startAnimation(animation);//开始动画
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    // 通过定时器和Handler来更新进度条
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {

            if (binder == null || binder.isStop()) {
                return;
            }

            // 判断是否存在正在下载的视频
            if (currentDownloadTitle == null) {
                currentDownloadTitle = binder.getTitle();
            }

            if (currentDownloadTitle == null || downloadingInfos.isEmpty()) {
                return;
            }

            Message msg = new Message();
            msg.obj = currentDownloadTitle;
            handler.sendMessage(msg);
        }
    };

    private Handler handler = new Handler() {

        private int currentPosition = ParamsUtil.INVALID;
        private int currentProgress = 0;

        @Override
        public void handleMessage(Message msg) {

            String title = (String) msg.obj;

            if (title == null || downloadingInfos.isEmpty()) {
                return;
            }

            resetHandlingTitle(title);
            int progress=0;
            if (binder.getTitle().equals(title)||binder.getProgress()>0){
                progress = binder.getProgress();
            }

            if (progress > 0 && currentPosition != ParamsUtil.INVALID) {

                if (currentProgress == progress) {
                    return;
                }
                currentProgress = progress;
                DownloadInfo downloadInfo = downloadingInfos.remove(currentPosition);

                downloadInfo.setProgress(binder.getProgress());
                downloadInfo.setProgressText(binder.getProgressText());
                DataSet.updateDownloadInfo(downloadInfo);

                downloadingInfos.add(currentPosition, downloadInfo);
                downloadingAdapter.notifyDataSetChanged();
                mDownLoadingList.invalidate();
            }
            super.handleMessage(msg);
        }

        private void resetHandlingTitle(String title){
            for(DownloadInfo d : downloadingInfos){
                if (d.getTitle().equals(title)) {
                    currentPosition = downloadingInfos.indexOf(d);
                    break;
                }
            }
        }

    };

    AdapterView.OnItemClickListener onDownLoadingItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            DownloadView downloadView = (DownloadView) view;
            Intent service = new Intent(context, DownloadService.class);
            //若下载任务已停止，则下载新数据
            if (binder == null ||binder.isStop()) {
                service.putExtra("title", downloadView.getTitle());
                mainActivity.startService(service);
                currentDownloadTitle = downloadView.getTitle();
            } else{
                switch (binder.getDownloadStatus()) {
                    case Downloader.PAUSE:
                        //下载其他任务
//                        if (!currentDownloadTitle.equals(downloadView.getTitle())) {
//                            DataSet.reloadData();
//                            initData();
//                            binder.cancel();
//                            mainActivity.stopService(service);
//                            service.putExtra("title", downloadView.getTitle());
//                            mainActivity.startService(service);
//                            currentDownloadTitle = downloadView.getTitle();
//                        }
                        binder.download();
                        break;
                    case Downloader.DOWNLOAD:
                        binder.pause();
                        DataSet.saveData();
                        break;
                }
            }
        }
    };

    View.OnCreateContextMenuListener onDownLoadingCreateContextMenuListener = new View.OnCreateContextMenuListener() {
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
//            menu.setHeaderTitle("操作");
            menu.add(ConfigUtil.DOWNLOADING_MENU_GROUP_ID, 0, 0, "删除");
        }
    };

    AdapterView.OnItemClickListener onDownLoadedItemClickListener = new AdapterView.OnItemClickListener() {
        @SuppressWarnings("unchecked")
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Pair<String, Integer> pair = (Pair<String, Integer>) parent.getItemAtPosition(position);
            Intent intent = new Intent(context, MediaPlayActivity.class);
            intent.putExtra("videoId", pair.first);
            intent.putExtra("isLocalPlay", true);
            startActivity(intent);
        }
    };

    View.OnCreateContextMenuListener onDownLoadedCreateContextMenuListener = new View.OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(ConfigUtil.DOWNLOADED_MENU_GROUP_ID, 0, 0, "删除");
        }
    };

    public boolean onContextItemSelected(MenuItem item) {
        String title=null;
        int selectedPosition = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;// 获取点击了第几行
        DownloadInfo downloadInfo=null;
        if (item.getGroupId() == ConfigUtil.DOWNLOADING_MENU_GROUP_ID){
            downloadInfo = (DownloadInfo) downloadingAdapter.getItem(selectedPosition);
            title = downloadInfo.getTitle();
            // 通知service取消下载
            if (!binder.isStop() && title.equals(currentDownloadTitle)) {
                binder.cancel();
            }
        }
        if (item.getGroupId() == ConfigUtil.DOWNLOADED_MENU_GROUP_ID){
            Pair pair = (Pair)videoListViewAdapter.getItem(selectedPosition);
            title = (String)pair.first;
        }
        if (title==null){
            return false;
        }
        // 删除数据库记录
        DataSet.removeDownloadInfo(title);
//        if (downloadingInfos.contains(downloadInfo)){
//            downloadingInfos.remove(downloadInfo);
//        }else{
//            pairs.remove(title);
//        }

        initData();
        downloadingAdapter.notifyDataSetChanged();
        mDownLoadingList.invalidate();
        videoListViewAdapter.notifyDataSetChanged();
        mDownLoadedList.invalidate();

        if (getUserVisibleHint()) {
            return true;
        }
        return false;
    }

    private class DownloadedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (isBind) {
                bindServer();
            }

            if (intent.getStringExtra("title") != null) {
                currentDownloadTitle = intent.getStringExtra("title");
            }

            int downloadStatus = intent.getIntExtra("status", ParamsUtil.INVALID);
            // 若当前状态为下载中，则重置view的标记位置
            if (downloadStatus == Downloader.DOWNLOAD) {
                currentDownloadTitle = null;
            }

            initData();
            downloadingAdapter.notifyDataSetChanged();
            mDownLoadingList.invalidate();
            videoListViewAdapter.notifyDataSetChanged();
            mDownLoadedList.invalidate();

            // 若当前状态为下载完成，且下载队列不为空，则启动service下载其他视频
            if (downloadStatus == Downloader.FINISH) {
                if (!downloadingInfos.isEmpty()) {
                    currentDownloadTitle = downloadingInfos.get(0).getTitle();
                    Intent service = new Intent(context, DownloadService.class);
                    service.putExtra("title", currentDownloadTitle);
                    mainActivity.startService(service);
                }
            }

            // 若下载出现异常，提示用户处理
            int errorCode = intent.getIntExtra("errorCode", ParamsUtil.INVALID);
            if (errorCode == ErrorCode.NETWORK_ERROR.Value()) {
                Toast.makeText(context, "网络异常，请检查", Toast.LENGTH_SHORT).show();
            } else if (errorCode == ErrorCode.PROCESS_FAIL.Value()) {
                Toast.makeText(context, "下载失败，请删除重试", Toast.LENGTH_SHORT).show();
            } else if (errorCode == ErrorCode.INVALID_REQUEST.Value()) {
                Toast.makeText(context, "下载失败，请检查帐户信息", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
//        binder.cancel();
//        mainActivity.unbindService(serviceConnection);
//        mainActivity.unregisterReceiver(receiver);
//        mainActivity.stopService(service);
        timerTask.cancel();
        isBind = false;
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("DownLoadActivity"); //统计页面，"MainScreen"为页面名称，可自定义
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("DownLoadActivity");
    }
}
