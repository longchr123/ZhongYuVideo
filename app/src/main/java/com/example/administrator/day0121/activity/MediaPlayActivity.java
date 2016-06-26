package com.example.administrator.day0121.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.sdk.mobile.play.DWMediaPlayer;
import com.example.administrator.day0121.Application.DemoApplication;
import com.example.administrator.day0121.R;
import com.example.administrator.day0121.receiver.NetWorkReceiver;
import com.example.administrator.day0121.utils.ConfigUtil;
import com.example.administrator.day0121.utils.ParamsUtil;
import com.example.administrator.day0121.utils.SubtitleUtil;
import com.example.administrator.day0121.view.PopMenu;
import com.example.administrator.day0121.view.VerticalSeekBar;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MediaPlayActivity extends Activity implements
        DWMediaPlayer.OnBufferingUpdateListener, DWMediaPlayer.OnInfoListener, DWMediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener, SurfaceHolder.Callback, NetWorkReceiver.BRInteraction {

    private boolean networkConnected = true;
    private DWMediaPlayer player;
    private SubtitleUtil subtitle;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private ProgressBar bufferProgressBar;
    private SeekBar skbProgress;
    private ImageView playOp, backPlayList;
    private TextView videoIdText, playDuration, videoDuration;
    private Button screenSizeBtn, definitionBtn, subtitleBtn;
    private PopMenu screenSizeMenu, definitionMenu, subtitleMenu;
    private LinearLayout playerTopLayout, volumeLayout;
    private RelativeLayout playerBottomLayout;
    private AudioManager audioManager;
    private VerticalSeekBar volumeSeekBar;
    private int currentVolume;
    private int maxVolume;
    private TextView subtitleText;

    private boolean isLocalPlay;
    private boolean isPrepared;
    private Map<String, Integer> definitionMap;

    private Handler playerHandler;
    private Timer timer = new Timer();
    private TimerTask timerTask, networkInfoTimerTask;

    private int currentScreenSizeFlag = 1;
    private int currrentSubtitleSwitchFlag = 0;
    private int currentDefinition = 0;

    private boolean firstInitDefinition = true;

    String path;

    private Boolean isPlaying;
    // 当player未准备好，并且当前activity经过onPause()生命周期时，此值为true
    private boolean isFreeze = false;
    private boolean isSurfaceDestroy = false;

    int currentPosition;
    private Dialog dialog;

    private String[] definitionArray;
    private final String[] screenSizeArray = new String[]{"满屏", "100%",
            "75%", "50%"};
    private final String[] subtitleSwitchArray = new String[]{"开启", "关闭"};
    private final String subtitleExampleURL = "http://dev.bokecc.com/static/font/example.utf8.srt";

    private GestureDetector detector;
    private float scrollTotalDistance, scrollCurrentPosition;
    private NetWorkReceiver receiver;
    private DemoApplication demoApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        demoApplication = (DemoApplication)getApplication();
        // 隐藏标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //设置播放时不锁屏不休眠
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_play);

        detector = new GestureDetector(this, new MyGesture());

        initView();

        initPlayHander();

        initPlayInfo();

        initScreenSizeMenu();

        if (!isLocalPlay) {
            initNetworkTimerTask();
        }

        registerBroadCastReceiver();

    }

    private void registerBroadCastReceiver() {
        receiver = new NetWorkReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
//        intentFilter.addAction("Intent.ACTION_BATTERY_CHANGED");
        registerReceiver(receiver, intentFilter);
        receiver.setBRInteractionListener(this);
    }

    private void initNetworkTimerTask() {
        networkInfoTimerTask = new TimerTask() {
            @Override
            public void run() {
                ConnectivityManager cm = (ConnectivityManager) MediaPlayActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isAvailable()) {
                    if (!networkConnected) {
                        timerTask = new TimerTask() {
                            @Override
                            public void run() {

                                if (!isPrepared) {
                                    return;
                                }

                                playerHandler.sendEmptyMessage(0);
                            }
                        };
                        timer.schedule(timerTask, 0, 1000);
                    }
                    networkConnected = true;
                } else {
                    networkConnected = false;
                    timerTask.cancel();
                }

            }
        };

        timer.schedule(networkInfoTimerTask, 0, 600);
    }

    //布局控件
    private void initView() {
        surfaceView = (SurfaceView) findViewById(R.id.playerSurfaceView);
        playOp = (ImageView) findViewById(R.id.btnPlay);
        backPlayList = (ImageView) findViewById(R.id.backPlayList);
        bufferProgressBar = (ProgressBar) findViewById(R.id.bufferProgressBar);

        videoIdText = (TextView) findViewById(R.id.videoIdText);
        playDuration = (TextView) findViewById(R.id.playDuration);
        videoDuration = (TextView) findViewById(R.id.videoDuration);
        playDuration.setText(ParamsUtil.millsecondsToStr(0));
        videoDuration.setText(ParamsUtil.millsecondsToStr(0));

        screenSizeBtn = (Button) findViewById(R.id.playScreenSizeBtn);
        definitionBtn = (Button) findViewById(R.id.definitionBtn);
        subtitleBtn = (Button) findViewById(R.id.subtitleBtn);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        volumeSeekBar = (VerticalSeekBar) findViewById(R.id.volumeSeekBar);
        volumeSeekBar.setThumbOffset(2);

        volumeSeekBar.setMax(maxVolume);
        volumeSeekBar.setProgress(currentVolume);
        volumeSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        skbProgress = (SeekBar) findViewById(R.id.skbProgress);
        skbProgress.setOnSeekBarChangeListener(onSeekBarChangeListener);

        playerTopLayout = (LinearLayout) findViewById(R.id.playerTopLayout);
        volumeLayout = (LinearLayout) findViewById(R.id.volumeLayout);
        playerBottomLayout = (RelativeLayout) findViewById(R.id.playerBottomLayout);

        playOp.setOnClickListener(onClickListener);
        backPlayList.setOnClickListener(onClickListener);
        screenSizeBtn.setOnClickListener(onClickListener);
        definitionBtn.setOnClickListener(onClickListener);
        subtitleBtn.setOnClickListener(onClickListener);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); //2.3及以下使用，不然出现只有声音没有图像的问题
        surfaceHolder.addCallback(this);

        subtitleText = (TextView) findViewById(R.id.subtitleText);
    }

    //更新播放进度
    private void initPlayHander() {
        playerHandler = new Handler() {
            public void handleMessage(Message msg) {

                if (player == null) {
                    return;
                }

                // 刷新字幕
                subtitleText.setText(subtitle.getSubtitleByTime(player
                        .getCurrentPosition()));

                // 更新播放进度
                int position = player.getCurrentPosition();
                int duration = player.getDuration();
//在播放到五分之一的时候弹出注册界面
//                if(position*100/duration>20){
//                    new AlertDialog.Builder(MediaPlayActivity.this).setTitle("温馨提示").setMessage("想看更多视频，请注册...").setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            Intent intent = new Intent(MediaPlayActivity.this, CreateActivity.class);
//                            startActivity(intent);
//                        }
//                    }).setNegativeButton("取消", null).show();
//                }
                if (duration > 0) {
                    long pos = skbProgress.getMax() * position / duration;
                    playDuration.setText(ParamsUtil.millsecondsToStr(player
                            .getCurrentPosition()));
                    skbProgress.setProgress((int) pos);

                }
            }

            ;
        };

        // 通过定时器和Handler来更新进度
        timerTask = new TimerTask() {
            @Override
            public void run() {

                if (!isPrepared) {
                    return;
                }
                playerHandler.sendEmptyMessage(0);
            }
        };

    }

    //设置播放视频信息
    private void initPlayInfo() {
        timer.schedule(timerTask, 0, 1000);
        isPrepared = false;
        player = new DWMediaPlayer();
        player.reset();
        player.setOnVideoSizeChangedListener(this);
        player.setOnInfoListener(this);

        String videoId = getIntent().getStringExtra("videoId");
        String videoText = getIntent().getStringExtra("videoText");
        videoIdText.setText(videoText);
        isLocalPlay = getIntent().getBooleanExtra("isLocalPlay", false);

        // DRM加密播放
        player.setDRMServerPort(demoApplication.getDrmServerPort());
        try {

            if (!isLocalPlay) {// 播放线上视频

                player.setVideoPlayInfo(videoId, ConfigUtil.USERID,
                        ConfigUtil.API_KEY, this);
//                player.setDefaultDefinition(DWMediaPlayer.HIGH_DEFINITION);

            } else {// 播放本地已下载视频

                if (android.os.Environment.MEDIA_MOUNTED.equals(Environment
                        .getExternalStorageState())) {
                    path = Environment.getExternalStorageDirectory()
                            + "/".concat(ConfigUtil.DOWNLOAD_DIR).concat("/")
                            .concat(videoId).concat(ConfigUtil.FORMAT);
                    if (!new File(path).exists()) {
                        return;
                    }

//                    player.setDataSource(path);
                    player.setDRMVideoPath(path, this);
                }
            }

            player.prepareAsync();

        } catch (IllegalArgumentException e) {
            Log.e("player error", e.getMessage());
        } catch (SecurityException e) {
            Log.e("player error", e.getMessage());
        } catch (IllegalStateException e) {
            Log.e("player error", e + "");
        } catch (IOException e) {
            Log.e("player error", e.getMessage());
        }

        // 设置视频字幕
        subtitle = new SubtitleUtil(new SubtitleUtil.OnSubtitleInitedListener() {

            @Override
            public void onInited(SubtitleUtil subtitle) {
                // 初始化字幕控制菜单
                initSubtitleSwitchpMenu(subtitle);
            }
        });
        subtitle.initSubtitleResource(subtitleExampleURL);

    }

    //调整视频宽高
    private void initScreenSizeMenu() {
        screenSizeMenu = new PopMenu(this, R.mipmap.popdown_9,
                currentScreenSizeFlag);
        screenSizeMenu.addItems(screenSizeArray);
        screenSizeMenu
                .setOnItemClickListener(new PopMenu.OnItemClickListener() {

                    @Override
                    public void onItemClick(int position) {
                        // 提示已选择的屏幕尺寸
                        Toast.makeText(getApplicationContext(),
                                screenSizeArray[position], Toast.LENGTH_SHORT)
                                .show();

                        RelativeLayout.LayoutParams params = getScreenSizeParams(position);
                        params.addRule(RelativeLayout.CENTER_IN_PARENT);
                        surfaceView.setLayoutParams(params);

                    }
                });

    }

    //获取屏幕尺寸
    @SuppressWarnings("deprecation")
    private RelativeLayout.LayoutParams getScreenSizeParams(int position) {
        currentScreenSizeFlag = position;
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();

        int vWidth = player.getVideoWidth();
//        if (vWidth == 0) {
//            vWidth = 600;
//        }

        int vHeight = player.getVideoHeight();
//        if (vHeight == 0) {
//            vHeight = 400;
//        }

        if (vWidth > width || vHeight > height) {
            float wRatio = (float) vWidth / (float) width;
            float hRatio = (float) vHeight / (float) height;
            float ratio = Math.max(wRatio, hRatio);

            width = (int) Math.ceil((float) vWidth / ratio);
            height = (int) Math.ceil((float) vHeight / ratio);
        } else {
            float wRatio = (float) width / (float) vWidth;
            float hRatio = (float) height / (float) vHeight;
            float ratio = Math.min(wRatio, hRatio);

            width = (int) Math.ceil((float) vWidth * ratio);
            height = (int) Math.ceil((float) vHeight * ratio);
        }

        String screenSizeStr = screenSizeArray[position];
        if (screenSizeStr.indexOf("%") > 0) {// 按比例缩放
            int screenSize = ParamsUtil.getInt(screenSizeStr.substring(0,
                    screenSizeStr.indexOf("%")));
            width = (width * screenSize) / 100;
            height = (height * screenSize) / 100;

        } else { // 拉伸至全屏
            width = wm.getDefaultDisplay().getWidth();
            height = wm.getDefaultDisplay().getHeight();
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        return params;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setOnBufferingUpdateListener(this);
            player.setOnPreparedListener(this);
            player.setDisplay(holder);
//            if (isSurfaceDestroy) {
                if (isLocalPlay) {
//                    player.setDataSource(path);
                    player.setDRMVideoPath(path, this);
                }
                player.prepareAsync();
//            }
        } catch (Exception e) {
            Log.e("videoPlayer", "error", e);
        }
        Log.i("videoPlayer", "surface created");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        holder.setFixedSize(width, height);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (player == null) {
            return;
        }
        if (isPrepared) {
            currentPosition = player.getCurrentPosition();
        }

        isPrepared = false;
        isSurfaceDestroy = true;

        player.stop();
        player.reset();

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isPrepared = true;
        if (!isFreeze) {
            if (isPlaying == null || isPlaying.booleanValue()) {
                player.start();
                playOp.setImageResource(R.mipmap.btn_pause);
            }
        }

        if(isPlaying != null && !isPlaying.booleanValue()){
            player.pause();
        }

        if (currentPosition > 0) {
            player.seekTo(currentPosition);
        }

        definitionMap = player.getDefinitions();
        if (!isLocalPlay) {
            initDefinitionPopMenu();
        }

        bufferProgressBar.setVisibility(View.GONE);
        RelativeLayout.LayoutParams params = getScreenSizeParams(currentScreenSizeFlag);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        surfaceView.setLayoutParams(params);
        videoDuration.setText(ParamsUtil.millsecondsToStr(player.getDuration()));
    }

    private void initDefinitionPopMenu() {
        definitionBtn.setVisibility(View.GONE);

//        if (definitionMap.size() > 1 && firstInitDefinition) {
//            currentDefinition = 1;
//            firstInitDefinition = false;
//        }

        definitionMenu = new PopMenu(this, R.mipmap.popup_9, currentDefinition);
        // 设置清晰度列表
        definitionArray = new String[]{};
        definitionArray = definitionMap.keySet().toArray(definitionArray);

        definitionMenu.addItems(definitionArray);
        definitionMenu.setOnItemClickListener(new PopMenu.OnItemClickListener() {

            @Override
            public void onItemClick(int position) {
                try {

                    currentDefinition = position;
                    int definitionCode = definitionMap
                            .get(definitionArray[position]);

                    if (isPrepared) {
                        currentPosition = player.getCurrentPosition();
                        if (player.isPlaying()) {
                            isPlaying = true;
                        } else {
                            isPlaying = false;
                        }
                    }

                    setLayoutVisibility(View.GONE, false);
                    bufferProgressBar.setVisibility(View.VISIBLE);

                    player.reset();

                    player.setDefinition(getApplicationContext(),
                            definitionCode);

                } catch (IOException e) {
                    Log.e("player error", e.getMessage());
                }

            }
        });
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        skbProgress.setSecondaryProgress(percent);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnPlay:
                    if (!isPrepared) {
                        return;
                    }

                    if (isLocalPlay && !player.isPlaying()) {
                        try {
                            player.prepare();

                        } catch (IllegalArgumentException e) {
                            Log.e("player error", e.getMessage());
                        } catch (SecurityException e) {
                            Log.e("player error", e.getMessage());
                        } catch (IllegalStateException e) {
                            Log.e("player error", e + "");
                        } catch (IOException e) {
                            Log.e("player error", e + "");
                        }
                    }
                    changePlayStatus();
                    break;

                case R.id.backPlayList:
                    finish();
                    break;
                case R.id.playScreenSizeBtn:
                    screenSizeMenu.showAsDropDown(v);
                    break;
                case R.id.subtitleBtn:
                    subtitleMenu.showAsDropDown(v);
                    break;
                case R.id.definitionBtn:
                    definitionMenu.showAsDropDown(v);
                    break;
            }
        }
    };

    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        int progress = 0;

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
//            if (networkConnected || isLocalPlay) {
                player.seekTo(progress);
//            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
//            if (networkConnected || isLocalPlay) {
                this.progress = progress * player.getDuration() / seekBar.getMax();
//            }
        }
    };

    VerticalSeekBar.OnSeekBarChangeListener seekBarChangeListener = new VerticalSeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(VerticalSeekBar verticalseekbar) {

        }

        @Override
        public void onStartTrackingTouch(VerticalSeekBar verticalseekbar) {

        }

        @Override
        public void onProgressChanged(VerticalSeekBar verticalseekbar, int i,
                                      boolean flag) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
            currentVolume = i;
            volumeSeekBar.setProgress(i);
        }
    };

    // 控制播放器面板显示
    private boolean isDisplay = false;

    private void initSubtitleSwitchpMenu(SubtitleUtil subtitle) {
        this.subtitle = subtitle;
        subtitleBtn.setVisibility(View.GONE);
        subtitleMenu = new PopMenu(this, R.mipmap.popup_9,
                currrentSubtitleSwitchFlag);
        subtitleMenu.addItems(subtitleSwitchArray);
        subtitleMenu.setOnItemClickListener(new PopMenu.OnItemClickListener() {

            @Override
            public void onItemClick(int position) {
                switch (position) {
                    case 0:// 开启字幕
                        currentScreenSizeFlag = 0;
                        subtitleText.setVisibility(View.VISIBLE);
                        break;
                    case 1:// 关闭字幕
                        currentScreenSizeFlag = 1;
                        subtitleText.setVisibility(View.GONE);
                        break;
                }
            }
        });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // 监测音量变化
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN
                || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {

            int volume = audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC);
            if (currentVolume != volume) {
                currentVolume = volume;
                volumeSeekBar.setProgress(currentVolume);
            }

            if (isPrepared) {
                setLayoutVisibility(View.VISIBLE, true);
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void setLayoutVisibility(int visibility, boolean isDisplay) {
        if (player == null) {
            return;
        }

        if (player.getDuration() <= 0) {
            return;
        }

        this.isDisplay = isDisplay;
        playerTopLayout.setVisibility(visibility);
        playerBottomLayout.setVisibility(visibility);
        volumeLayout.setVisibility(visibility);
    }

    private final String mPageName = "MediaPlayActivity";
    @Override
    public void onResume() {
        if (isFreeze) {
            isFreeze = false;
            if (isPrepared) {
                player.start();
            }
        } else {
            if (isPlaying != null && isPlaying.booleanValue() && isPrepared) {
                player.start();
            }
        }
        MobclickAgent.onPageStart(mPageName);
        MobclickAgent.onResume(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        if (isPrepared) {
            // 如果播放器prepare完成，则对播放器进行暂停操作，并记录状态
            if (player.isPlaying()) {
                isPlaying = true;
            } else {
                isPlaying = false;
            }
            player.pause();
        } else {
            // 如果播放器没有prepare完成，则设置isFreeze为true
            isFreeze = true;
        }

        MobclickAgent.onPageEnd(mPageName);
        MobclickAgent.onPause(this);
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        timerTask.cancel();

        if (player != null) {
            player.reset();
            player.release();
            player = null;
        }
        if (dialog != null) {
            dialog.dismiss();
        }
        if (!isLocalPlay) {
            networkInfoTimerTask.cancel();
        }
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
//        RelativeLayout.LayoutParams params = getScreenSizeParams(currentScreenSizeFlag);
//        params.addRule(RelativeLayout.CENTER_IN_PARENT);
//        surfaceView.setLayoutParams(params);
        surfaceHolder.setFixedSize(width,height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!isPrepared) {
            return true;
        }
        // 事件监听交给手势类来处理
        return detector.onTouchEvent(event);
    }

    //网络状态发生变化的时候调用
    @Override
    public void setDialog() {
        ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            new AlertDialog.Builder(this).setTitle("提示").setMessage("网络连接有问题，请检查网络").show();
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        NetworkInfo netWorkInfo = info[i];
                        if (netWorkInfo.getType() == ConnectivityManager.TYPE_WIFI) {

                        } else if (netWorkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                            player.pause();
                            playOp.setImageResource(R.mipmap.btn_play);
                            new AlertDialog.Builder(this).setTitle("温馨提示").setMessage("监测到你在使用数据流量，请在wifi状态下观看").show();
                        }
                    }
                }
            }
        }
    }

    // 手势监听器类
    private class MyGesture extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return super.onSingleTapUp(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            if (!isDisplay) {
                setLayoutVisibility(View.VISIBLE, true);
            }
            scrollTotalDistance += distanceX;

            float duration = (float) player.getDuration();

            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

            float width = wm.getDefaultDisplay().getWidth() * 0.75f; // 设定总长度是多少，此处根据实际调整

            float currentPosition = scrollCurrentPosition - (float) duration
                    * scrollTotalDistance / width;

            if (currentPosition < 0) {
                currentPosition = 0;
            } else if (currentPosition > duration) {
                currentPosition = duration;
            }

            player.seekTo((int) currentPosition);

            playDuration.setText(ParamsUtil
                    .millsecondsToStr((int) currentPosition));
            int pos = (int) (skbProgress.getMax() * currentPosition / duration);
            skbProgress.setProgress(pos);

            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public void onShowPress(MotionEvent e) {
            super.onShowPress(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {

            scrollTotalDistance = 0f;

            scrollCurrentPosition = (float) player.getCurrentPosition();

            return super.onDown(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (!isDisplay) {
                setLayoutVisibility(View.VISIBLE, true);
            }
            changePlayStatus();
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return super.onDoubleTapEvent(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (isDisplay) {
                setLayoutVisibility(View.GONE, false);
            } else {
                setLayoutVisibility(View.VISIBLE, true);
            }
            return super.onSingleTapConfirmed(e);
        }
    }

    private void changePlayStatus() {
        if (player.isPlaying()) {
            player.pause();
            playOp.setImageResource(R.mipmap.btn_play);

        } else {
            player.start();
            playOp.setImageResource(R.mipmap.btn_pause);
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case DWMediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (player.isPlaying()) {
                    bufferProgressBar.setVisibility(View.VISIBLE);
                }
                break;
            case DWMediaPlayer.MEDIA_INFO_BUFFERING_END:
                bufferProgressBar.setVisibility(View.GONE);
                break;
        }
        return false;
    }
}
