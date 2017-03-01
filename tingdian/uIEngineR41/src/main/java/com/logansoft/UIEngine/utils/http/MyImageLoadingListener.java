package com.logansoft.UIEngine.utils.http;

import java.util.HashMap;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import android.graphics.Bitmap;
import android.view.View;

public class MyImageLoadingListener implements ImageLoadingListener {
	private RequestListener rl;
	private boolean netrequest;
	public MyImageLoadingListener(RequestListener RL,boolean netrequest){
		rl=RL;
		this.netrequest=netrequest;
	}
	@Override
	public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
		rl.exception(null);
	}
	@Override
	public void onLoadingCancelled(String imageUri, View view) {
		rl.DidCanceled();
	}
	@Override
	public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
		HashMap<String,Object> args=new HashMap<String,Object>();
		args.put("response", loadedImage);
		rl.DidLoad(loadedImage,args,netrequest?RequestListener.NetCallBackType:RequestListener.LocalCallBackType);
	}
	@Override
	public void onLoadingStarted(String imageUri, View view) {
		
	}
	    
	
}
