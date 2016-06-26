package com.bokecc.sdk.mobile.demo.drm.play;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.sdk.mobile.demo.drm.DemoApplication;
import com.bokecc.sdk.mobile.demo.drm.R;
import com.bokecc.sdk.mobile.demo.drm.play.Subtitle.OnSubtitleInitedListener;
import com.bokecc.sdk.mobile.demo.drm.util.ConfigUtil;
import com.bokecc.sdk.mobile.demo.drm.util.MediaUtil;
import com.bokecc.sdk.mobile.demo.drm.util.ParamsUtil;
import com.bokecc.sdk.mobile.demo.drm.view.PopMenu;
import com.bokecc.sdk.mobile.demo.drm.view.PopMenu.OnItemClickListener;
import com.bokecc.sdk.mobile.demo.drm.view.VerticalSeekBar;
import com.bokecc.sdk.mobile.play.DWMediaPlayer;

/**
 * 视频播放界面
 * 
 * @author CC视频
 *
 */
public class MediaPlayActivity extends Activity implements
		OnBufferingUpdateListener, DWMediaPlayer.OnInfoListener, DWMediaPlayer.OnPreparedListener, SurfaceHolder.Callback, DWMediaPlayer.OnVideoSizeChangedListener {

	private DWMediaPlayer player;
	private Subtitle subtitle;
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
	private TimerTask timerTask;
	
	
	private int currentScreenSizeFlag = 1;
	private int currrentSubtitleSwitchFlag = 0;
	private int currentDefinition = 0;
	
	//标志经过onPause生命周期时，视频当前的播放状态
	private Boolean isPlaying;
	//当player未准备好，并且当前activity经过onPause()生命周期时，此值为true
	private boolean isFreeze = false;
	private boolean isSurfaceDestroy = false;
	int currentPosition;
	String path;
	
	private String[] definitionArray;
	private final String[] screenSizeArray = new String[] { "满屏", "100%", "75%", "50%" };
	private final String[] subtitleSwitchArray = new String[] { "开启", "关闭" };
	private final String subtitleExampleURL = "http://dev.bokecc.com/static/font/example.utf8.srt";

	private DemoApplication demoApplication;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		demoApplication = (DemoApplication)getApplication();
		// 隐藏标题
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 设置全屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.media_play);
		
		initView();
		
		initPlayHander();
		
		initPlayInfo();
		
		initScreenSizeMenu();
		
		super.onCreate(savedInstanceState);
	}

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
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		surfaceHolder.addCallback(this);
		surfaceView.setOnTouchListener(touchListener);
	
		subtitleText = (TextView) findViewById(R.id.subtitleText);
	}

	private void initPlayHander() {
		playerHandler = new Handler() {
			public void handleMessage(Message msg) {
				
				if (player == null) {
					return;
				}
				
				//刷新字幕
				subtitleText.setText(subtitle.getSubtitleByTime(player.getCurrentPosition()));
				
				if (!isPrepared) {
					return;
				}
				
				// 更新播放进度
				int position = player.getCurrentPosition();
				int duration = player.getDuration();

				if (duration > 0) {
					long pos = skbProgress.getMax() * position / duration;
					playDuration.setText(ParamsUtil.millsecondsToStr(player.getCurrentPosition()));
					skbProgress.setProgress((int) pos);

				}
			};
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
	
	private void initPlayInfo(){
		timer.schedule(timerTask, 0, 1000);
		isPrepared = false;
		player = new DWMediaPlayer();
		player.reset();
	
		Intent intent = getIntent();
		String videoId = intent.getStringExtra("videoId");
		videoIdText.setText(videoId);
		
		isLocalPlay = intent.getBooleanExtra("isLocalPlay", false);
		
		// DRM加密播放
		player.setDRMServerPort(demoApplication.getDrmServerPort());
		try {
			
			if (!isLocalPlay) {// 播放线上视频
				
				player.setVideoPlayInfo(videoId, ConfigUtil.USERID, ConfigUtil.API_KEY, this);
		
			} else {// 播放本地已下载视频
				
				if (android.os.Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
				
					path = Environment.getExternalStorageDirectory() + "/".
							concat(ConfigUtil.DOWNLOAD_DIR).concat("/").concat(videoId).concat(MediaUtil.PCM_FILE_SUFFIX);
					
					if (!new File(path).exists()) {
						finish();
					}
					
					player.setDRMVideoPath(path, this);
				}
			}
			
		} catch (IllegalArgumentException e) {
			Log.e("player error", e.getMessage());
		} catch (SecurityException e) {
			Log.e("player error", e.getMessage());
		} catch (IllegalStateException e) {
			Log.e("player error", "illegal", e);
		} catch (IOException e) {
			Log.e("player error", e.getMessage());
		}
		
		//设置视频字幕
		subtitle = new Subtitle(new OnSubtitleInitedListener() {
			
			@Override
			public void onInited(Subtitle subtitle) {
				//初始化字幕控制菜单
				initSubtitleSwitchpMenu(subtitle);
			}
		});
		subtitle.initSubtitleResource(subtitleExampleURL);
	
	}

	private void initScreenSizeMenu() {
		screenSizeMenu = new PopMenu(this, R.drawable.popdown, currentScreenSizeFlag);
		screenSizeMenu.addItems(screenSizeArray);
		screenSizeMenu.setOnItemClickListener(new PopMenu.OnItemClickListener() {

			@Override
			public void onItemClick(int position) {
				// 提示已选择的屏幕尺寸
				Toast.makeText(getApplicationContext(), screenSizeArray[position], Toast.LENGTH_SHORT).show();
				
				LayoutParams params = getScreenSizeParams(position);
				params.addRule(RelativeLayout.CENTER_IN_PARENT);
				surfaceView.setLayoutParams(params);
			}
		});

	}
	
	@SuppressWarnings("deprecation")
	private LayoutParams getScreenSizeParams(int position){
		currentScreenSizeFlag = position;
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		int height = wm.getDefaultDisplay().getHeight();
		
		int vWidth = player.getVideoWidth();
		int vHeight = player.getVideoHeight();
		
		if (vWidth > width || vHeight > height) {
			float wRatio = (float)vWidth / (float)width; 
			float hRatio = (float)vHeight / (float)height;
			float ratio = Math.max(wRatio, hRatio);
			
			width = (int)Math.ceil((float)vWidth / ratio);
			height = (int)Math.ceil((float)vHeight / ratio); 
		} else {
			float wRatio = (float)width / (float)vWidth; 
			float hRatio = (float)height / (float)vHeight;
			float ratio = Math.min(wRatio, hRatio);
			
			width = (int)Math.ceil((float)vWidth * ratio);
			height = (int)Math.ceil((float)vHeight * ratio); 
		}
		
		String screenSizeStr = screenSizeArray[position];
		if (screenSizeStr.indexOf("%") > 0) {// 按比例缩放
			int screenSize = ParamsUtil.getInt(screenSizeStr.substring(0, screenSizeStr.indexOf("%")));
			width = (width * screenSize) / 100;
			height = (height * screenSize) / 100;
			
		} else { // 拉伸至全屏
			width = wm.getDefaultDisplay().getWidth();
			height = wm.getDefaultDisplay().getHeight();
		}

		LayoutParams params = new LayoutParams(width , height);
		return params;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.setOnBufferingUpdateListener(this);
			player.setOnPreparedListener(this);
			player.setOnInfoListener(this);
			player.setDisplay(holder);
			if(isLocalPlay){
				player.setDRMVideoPath(path, this);
			}
			player.prepareAsync();
			
		} catch (Exception e) {
			Log.e("videoPlayer", "error", e);
		}
		Log.i("videoPlayer", "surface created");
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		holder.setFixedSize(width, height);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (player == null) {
			return;
		}
		if(isPrepared){
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
		
		if(!isFreeze){
			player.start();
		}
		
		if(isPlaying != null && !isPlaying.booleanValue()){
			player.pause();
		}
		
		if(currentPosition > 0){
			player.seekTo(currentPosition);
		}
		
		definitionMap = player.getDefinitions();
		if (!isLocalPlay) {
			initDefinitionPopMenu();
		}
		
		bufferProgressBar.setVisibility(View.GONE);
		LayoutParams params = getScreenSizeParams(currentScreenSizeFlag);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		surfaceView.setLayoutParams(params);
		videoDuration.setText(ParamsUtil.millsecondsToStr(player.getDuration()));
	}

	private void initDefinitionPopMenu() {
		definitionBtn.setVisibility(View.VISIBLE);
		definitionMenu = new PopMenu(this, R.drawable.popup, currentDefinition);
		// 设置清晰度列表
		definitionArray = new String[]{};
		definitionArray = definitionMap.keySet().toArray(definitionArray);
		
		definitionMenu.addItems(definitionArray);
		definitionMenu.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(int position) {
				try {
					
					setLayoutVisibility(View.GONE, false);
					bufferProgressBar.setVisibility(View.VISIBLE);
					
					currentDefinition = position;
					int definitionCode = definitionMap.get(definitionArray[position]);
					
					if(isPrepared){
						currentPosition = player.getCurrentPosition();
						if(player.isPlaying()){
							isPlaying = true;
						} else {
							isPlaying = false;
						}
					}
					
					player.reset();
					player.setDefinition(getApplicationContext(), definitionCode);

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

	OnClickListener onClickListener = new OnClickListener() {

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

				if (player.isPlaying()) {
					player.pause();
					playOp.setImageResource(R.drawable.btn_play);

				} else {
					player.start();
					playOp.setImageResource(R.drawable.btn_pause);
				}

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

	OnSeekBarChangeListener onSeekBarChangeListener = new OnSeekBarChangeListener() {
		int progress = 0;

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			player.seekTo(progress);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			this.progress = progress * player.getDuration() / skbProgress.getMax();
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
		public void onProgressChanged(VerticalSeekBar verticalseekbar, int i, boolean flag) {
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
			currentVolume = i;
			volumeSeekBar.setProgress(i);
		}
	};

	//控制播放器面板显示
	private boolean isDisplay = false;
	private OnTouchListener touchListener = new OnTouchListener() {

		public boolean onTouch(View v, MotionEvent event) {
			if (!isPrepared) {
				return false;
			}
			
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (isDisplay) {
					setLayoutVisibility(View.GONE, false);
				} else {
					setLayoutVisibility(View.VISIBLE, true);
				}
			}
			return false;
		}
	};

	private void initSubtitleSwitchpMenu(Subtitle subtitle) {
		this.subtitle = subtitle;
		subtitleBtn.setVisibility(View.VISIBLE);
		subtitleMenu = new PopMenu(this, R.drawable.popup, currrentSubtitleSwitchFlag);
		subtitleMenu.addItems(subtitleSwitchArray);
		subtitleMenu.setOnItemClickListener(new OnItemClickListener() {

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
			
			
			int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
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
	
	private void setLayoutVisibility(int visibility, boolean isDisplay){
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

	@Override
	public void onResume(){
		if(isFreeze){
			isFreeze = false;
			if(isPrepared){
				player.start();
			}
		} else {
			if(isPlaying != null && isPlaying.booleanValue() && isPrepared){
				player.start();
			}
		}
		super.onResume();
	}
	
	@Override
	public void onPause(){
		if(isPrepared){
			//如果播放器prepare完成，则对播放器进行暂停操作，并记录状态
			if(player.isPlaying()){
				isPlaying = true;
			} else {
				isPlaying = false;
		    }
			player.pause();
		} else {
			//如果播放器没有prepare完成，则设置isFreeze为true
			isFreeze = true;
		}
		
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
		
		super.onDestroy();
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		surfaceHolder.setFixedSize(width, height);
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		switch(what) {
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
