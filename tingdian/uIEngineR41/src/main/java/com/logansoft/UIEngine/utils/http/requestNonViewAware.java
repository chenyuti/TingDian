package com.logansoft.UIEngine.utils.http;

import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.imageaware.NonViewAware;

public class requestNonViewAware extends NonViewAware {
	private View targetView;
	private int state;
	public requestNonViewAware(String imageUri, View targetView, int state) {
		super(imageUri,getImageSize(targetView),ViewScaleType.CROP);
		this.state=state;
		this.targetView=targetView;
	}
	private static ImageSize getImageSize(View targetView){
		LayoutParams lp=targetView.getLayoutParams();
		return new ImageSize(lp.width,lp.height);
	}
	
	@Override
	public int getId() {
		return (targetView.hashCode()<<2+state);
	}

	

}
