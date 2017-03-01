/*
 * Created by Storm Zhang, Feb 11, 2014.
 */

package com.logansoft.UIEngine.utils.images.cache;

import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;

public class DrawableLruCache extends LruCache<String, Drawable>  {
	public DrawableLruCache(int maxSize) {
		super(maxSize);
	}
	
	@Override
	protected int sizeOf(String key, Drawable bitmap) {
	    return bitmap.getIntrinsicWidth() * bitmap.getIntrinsicHeight();
	}
	

	public Drawable getBitmap(String url) {
		return get(url);
	}

	public void putBitmap(String url, Drawable bitmap) {
		put(url, bitmap);
	}
}
