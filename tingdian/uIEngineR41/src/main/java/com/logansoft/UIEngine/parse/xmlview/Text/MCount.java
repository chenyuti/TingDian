package com.logansoft.UIEngine.parse.xmlview.Text;

import org.w3c.dom.Element;

import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.xmlview.GroupView;

import android.os.CountDownTimer;
import android.widget.TextView;

public class MCount extends MButton{

	private CountDownTimer cTimer;
	
	public MCount(BaseFragment baseFragment,GroupView parentView,Element mElement) {
		super(baseFragment,parentView,mElement);
	}
	
	public void startCountDown(final String str,int downTime){
        if (cTimer!=null) {
           cTimer.cancel();
        }
        cTimer = new CountDownTimer(downTime*1000, 1000){
           @Override
           public void onTick(long millisUntilFinished) {
               ((TextView)mView).setText(str.replaceAll("%f", millisUntilFinished/1000+""));
               refreshBackground(mView);
               mView.setEnabled(false);
           }
           
           @Override
           public void onFinish() {
               ((TextView)mView).setText(attrMap.get("label"));
               refreshBackground(mView);
               mView.setEnabled(true);
           }
       };
       cTimer.start();
   } 
}
