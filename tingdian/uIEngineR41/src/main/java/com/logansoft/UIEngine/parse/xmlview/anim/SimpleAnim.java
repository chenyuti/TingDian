package com.logansoft.UIEngine.parse.xmlview.anim;

import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.logansoft.UIEngine.parse.xmlview.BaseView;

public class SimpleAnim implements AnimInterface {
	AlphaAnimation alphaAnimation;

	public SimpleAnim() {
		alphaAnimation = new AlphaAnimation(0.5f, 1.0f);
		alphaAnimation.setDuration(300);
		alphaAnimation.setRepeatCount(Animation.INFINITE);
		alphaAnimation.setRepeatMode(Animation.REVERSE);
	}

	@Override
	public void startAni(View view) {
		view.setAnimation(alphaAnimation);
		alphaAnimation.start();
	}

	@Override
	public void startAni(BaseView view) {
		view.mView.setAnimation(alphaAnimation);
		alphaAnimation.start();
	}

	@Override
	public void stopAni(View view) {
		if (alphaAnimation == null) {
			Log.d("cyt", "anim is null ");
			return;
		}
		alphaAnimation.cancel();
	}

	@Override
	public void stopAni(BaseView view) {
		if (alphaAnimation == null) {
			Log.d("cyt", "anim is null ");
			return;
		}
		alphaAnimation.cancel();
	}

}
