package com.bokecc.sdk.mobile.demo.drm;

import android.app.Application;

public class DemoApplication extends Application{
	
	private int drmServerPort;

	public int getDrmServerPort() {
		return drmServerPort;
	}

	public void setDrmServerPort(int drmServerPort) {
		this.drmServerPort = drmServerPort;
	}

}
