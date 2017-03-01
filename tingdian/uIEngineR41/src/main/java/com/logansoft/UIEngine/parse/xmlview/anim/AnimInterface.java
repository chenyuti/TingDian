package com.logansoft.UIEngine.parse.xmlview.anim;

import com.logansoft.UIEngine.parse.xmlview.BaseView;

import android.view.View;

public interface AnimInterface {

	void startAni(View view);
	void stopAni(View view);

	
	void startAni(BaseView view);
	void stopAni(BaseView view);

}
