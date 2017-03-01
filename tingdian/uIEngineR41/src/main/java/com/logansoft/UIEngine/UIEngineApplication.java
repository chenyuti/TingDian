package com.logansoft.UIEngine;

import java.util.HashMap;

import android.app.Application;

public class UIEngineApplication extends Application {
	public HashMap<String,Object> tempDataMap;
	public UIEngineApplication() {
		super();
		tempDataMap=new HashMap<String,Object>();
	}
	
	public void exit(){
		System.exit(0);
	}
}
