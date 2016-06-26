package com.example.administrator.day0121.Application;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.example.administrator.day0121.R;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import cn.jpush.android.api.JPushInterface;

@ReportsCrashes(formUri = "http://www.zhongyuedu.com/api/error.php",
		mode = ReportingInteractionMode.DIALOG,
		resToastText = R.string.crash_toast_text, // optional, displayed as soon as the crash occurs, before collecting data which can take a few seconds
		resDialogText = R.string.crash_dialog_text,
		resDialogIcon = android.R.drawable.ic_dialog_info, //optional. default is a warning sign
		resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
		resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. When defined, adds a user text field input with this text resource as a label
		resDialogOkToast = R.string.crash_dialog_ok_toast // optional. displays a Toast message when the user accepts to send a report.
)

public class DemoApplication extends Application{
	
	private int drmServerPort;

	private static final String TAG = "JPush";

	@Override
	public void onCreate() {
		Log.d(TAG, "[ExampleApplication] onCreate");
		super.onCreate();

//		JPushInterface.setDebugMode(true); 	// 设置开启日志,发布时请关闭日志
		JPushInterface.init(this);     		// 初始化 JPush
	}

	public int getDrmServerPort() {
		return drmServerPort;
	}

	public void setDrmServerPort(int drmServerPort) {
		this.drmServerPort = drmServerPort;
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);

		// The following line triggers the initialization of ACRA
		ACRA.init(this);
	}

}
