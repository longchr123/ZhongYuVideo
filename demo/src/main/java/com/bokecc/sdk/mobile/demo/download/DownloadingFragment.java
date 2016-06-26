package com.bokecc.sdk.mobile.demo.download;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.bokecc.sdk.mobile.demo.R;
import com.bokecc.sdk.mobile.demo.adapter.DownloadViewAdapter;
import com.bokecc.sdk.mobile.demo.download.DownloadService.DownloadBinder;
import com.bokecc.sdk.mobile.demo.model.DownloadInfo;
import com.bokecc.sdk.mobile.demo.util.ConfigUtil;
import com.bokecc.sdk.mobile.demo.util.DataSet;
import com.bokecc.sdk.mobile.demo.util.ParamsUtil;
import com.bokecc.sdk.mobile.demo.view.DownloadView;
import com.bokecc.sdk.mobile.download.Downloader;
import com.bokecc.sdk.mobile.exception.ErrorCode;

/**
 * 下载中标签页
 * 
 * @author CC视频
 *
 */
public class DownloadingFragment extends Fragment {

	private Context context;
	private FragmentActivity activity;

	private DownloadBinder binder;
	private ServiceConnection serviceConnection;
	private ListView downloadingListView;
	private List<DownloadInfo> downloadingInfos;
	private DownloadViewAdapter downloadAdapter;

	private Timer timter = new Timer();
	private boolean isBind;
	private Intent service;
	private DownloadedReceiver receiver;
	private String currentDownloadTitle;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		activity = getActivity();
		context = activity.getApplicationContext();
		currentDownloadTitle = activity.getIntent().getStringExtra("title");
		timter.schedule(timerTask, 0, 1000);
		receiver = new DownloadedReceiver();
		activity.registerReceiver(receiver, new IntentFilter(ConfigUtil.ACTION_DOWNLOADING));

		bindServer();

		RelativeLayout view = new RelativeLayout(context);
		initView(view);
		
		initData();
		return view;
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
				binder = (DownloadBinder) service;
			}
		};
		activity.bindService(service, serviceConnection,
				Context.BIND_AUTO_CREATE);
	}
	
	private void initView(RelativeLayout view ){
		view.setBackgroundColor(Color.WHITE);
		LayoutParams downloadingLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		downloadingListView = new ListView(context);
		downloadingListView.setPadding(10, 10, 10, 10);
		downloadingListView.setDivider(getResources().getDrawable(R.drawable.line));
		view.addView(downloadingListView, downloadingLayoutParams);
		
		downloadingListView.setOnItemClickListener(onItemClickListener);
		downloadingListView.setOnCreateContextMenuListener(onCreateContextMenuListener);
		
	}

	private void initData() {
		downloadingInfos = new ArrayList<DownloadInfo>();
		List<DownloadInfo> downloadInfos = DataSet.getDownloadInfos();
		for (DownloadInfo downloadInfo : downloadInfos) {

			if (downloadInfo.getStatus() == Downloader.FINISH) {
				continue;
			}

			if ((downloadInfo.getStatus() == Downloader.DOWNLOAD) && (binder == null || binder.isStop())) {
				Intent service = new Intent(context, DownloadService.class);
				service.putExtra("title", downloadInfo.getTitle());
				activity.startService(service);
			}
			downloadingInfos.add(downloadInfo);
		}

		downloadAdapter = new DownloadViewAdapter(context, downloadingInfos);
		downloadingListView.setAdapter(downloadAdapter);
	}

	OnCreateContextMenuListener onCreateContextMenuListener = new OnCreateContextMenuListener() {
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			menu.setHeaderTitle("操作");
			menu.add(ConfigUtil.DOWNLOADING_MENU_GROUP_ID, 0, 0, "删除");
		}
	};

	OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			DownloadView downloadView = (DownloadView) view;
			
			//若下载任务已停止，则下载新数据
			if (binder.isStop()) {
				Intent service = new Intent(context, DownloadService.class);
				service.putExtra("title", downloadView.getTitle());
				activity.startService(service);
				currentDownloadTitle = downloadView.getTitle();
				
			} else if (downloadView.getTitle().equals(currentDownloadTitle)) {

				switch (binder.getDownloadStatus()) {
				case Downloader.PAUSE:
					binder.download();
					break;
				case Downloader.DOWNLOAD:
					binder.pause();
					break;
				}
			} 
		}
	};
	
	
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getGroupId() != ConfigUtil.DOWNLOADING_MENU_GROUP_ID) {
			return false;
		}
		
		int selectedPosition = ((AdapterContextMenuInfo) item.getMenuInfo()).position;// 获取点击了第几行
		DownloadInfo downloadInfo = (DownloadInfo) downloadAdapter.getItem(selectedPosition);
		String title = downloadInfo.getTitle();

		// 删除数据库记录
		DataSet.removeDownloadInfo(title);

		File file = new File(Environment.getExternalStorageDirectory()+"/CCDownload", title+".mp4");
		if(file.exists()){
			file.delete();
		}
		
		// 通知service取消下载
		if (!binder.isStop() && title.equals(currentDownloadTitle)) {
			binder.cancel();
			startWaitStatusDownload();
		}
		
		initData();
		downloadAdapter.notifyDataSetChanged();
		downloadingListView.invalidate();

		if (getUserVisibleHint()) {
			return true;
		}
		
		return false;
	}

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
			
			int progress = binder.getProgress();
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
				downloadAdapter.notifyDataSetChanged();
				downloadingListView.invalidate();

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

	@Override
	public void onDestroy() {
		activity.unbindService(serviceConnection);
		activity.unregisterReceiver(receiver);
		timerTask.cancel();
		isBind = false;
		super.onDestroy();
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
			downloadAdapter.notifyDataSetChanged();
			downloadingListView.invalidate();
			
			// 若当前状态为下载完成，且下载队列不为空，则启动service下载其他视频
			if (downloadStatus == Downloader.FINISH) {
				if (!downloadingInfos.isEmpty()) {
					startWaitStatusDownload();
				}
			}
			
			// 若下载出现异常，提示用户处理
			int errorCode = intent.getIntExtra("errorCode", ParamsUtil.INVALID);
			if (errorCode == ErrorCode.NETWORK_ERROR.Value()) {
				Toast.makeText(context, "网络异常，请检查", Toast.LENGTH_SHORT).show();
			} else if (errorCode == ErrorCode.PROCESS_FAIL.Value()) {
				Toast.makeText(context, "下载失败，请重试", Toast.LENGTH_SHORT).show();
			} else if (errorCode == ErrorCode.INVALID_REQUEST.Value()) {
				Toast.makeText(context, "下载失败，请检查帐户信息", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}
	
	private void startWaitStatusDownload() {
		for (DownloadInfo downloadInfo: downloadingInfos) {
			if (downloadInfo.getStatus() == Downloader.WAIT) {
				currentDownloadTitle = downloadInfo.getTitle();
				Intent service = new Intent(context, DownloadService.class);
				service.putExtra("title", currentDownloadTitle);
				activity.startService(service);
				break;
			}
		}
	}

}
