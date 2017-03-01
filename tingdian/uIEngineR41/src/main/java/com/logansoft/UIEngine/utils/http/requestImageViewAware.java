package com.logansoft.UIEngine.utils.http;

import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.logansoft.UIEngine.utils.LogUtil;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

public class requestImageViewAware extends ImageViewAware {
	int state;
	public requestImageViewAware(ImageView imageView,int state) {
		super(imageView);
		this.state=state;
	}

	@Override
	public int getId() {
		View view = viewRef.get();
		return (view.hashCode()<<2 +state);
	}
	
	
//	@Override
//	public boolean setImageDrawable(Drawable drawable) { // Do nothing
//		return true;
//	}
//
//	@Override
//	public boolean setImageBitmap(Bitmap bitmap) { // Do nothing
//		return true;
//	}
}
