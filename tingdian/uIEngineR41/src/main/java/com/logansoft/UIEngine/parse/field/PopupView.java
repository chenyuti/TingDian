
package com.logansoft.UIEngine.parse.field;

import java.util.HashMap;

import org.w3c.dom.Element;

import com.logansoft.UIEngine.Base.UIEngineGroupView;
import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.xmlview.BaseView;
import com.logansoft.UIEngine.parse.xmlview.GroupView;
import com.logansoft.UIEngine.utils.DisplayUtil;
import com.logansoft.UIEngine.utils.LogUtil;

import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;

public class PopupView extends GroupView {
	private boolean ignoreDissmiss;
    private PopupWindow backgroundMask;
    private PopupWindow window;
    
    private String onDismiss;
    
    public PopupView(BaseFragment fragment,GroupView parentView,Element element) {
        super(fragment,parentView,element);
    }
    @Override
    protected void createMyView(){
    	mView = new UIEngineGroupView(mContext){
          	public boolean onInterceptTouchEvent(MotionEvent ev) {
          		if(baseFragment.getRootView().getShowingKeyBoard()!=null){    
          			int[] l = new int[2];
          			this.getLocationOnScreen(l);
          			int x=l[0]+(int)ev.getX();
          			int y=l[1]+(int)ev.getY();
          			View targetView=findEditTextView(this,x,y);
          			
          			if(!(targetView instanceof EditText)){
          				baseFragment.getRootView().hideKeyboard();
          				ignoreDissmiss=true;
          				return true;
              		}
          		}
          		return false;
          	}
          	public View findEditTextView(View v,int x,int y){
          		View targetView=null;
          		for(View view : v.getTouchables()){
      				int[] location = new int[2];
      				view.getLocationOnScreen(location);
      				int left = location[0];
      				int top = location[1];
      				int right = left + view.getMeasuredWidth();
      				int bottom = top + view.getMeasuredHeight();
      				if (view.isClickable() && y >= top && y <= bottom && x >= left && x <= right) {
      					targetView=view;
      					break;
      				}
      			}
      			if(targetView!=null && targetView!=v){
      				View tempTargetView=findEditTextView(targetView,x,y);
      				if(tempTargetView!=null)
      					targetView=tempTargetView;
      			}
          		return targetView;
          	}
          };
    }
    @Override
    protected void parseView() {
    	super.parseView();
        window = new PopupWindow(mView,ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT,true);
        View mask=new View(mContext);
        backgroundMask=new PopupWindow(mask,ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT,true);
        mask.setBackgroundDrawable(mDrawable);
        mView.setBackgroundDrawable(null);
        onDismiss = attrMap.get("onDismiss");
    	window.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
    	window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        window.setOutsideTouchable(true);
        window.setFocusable(true);
        mView.setFocusableInTouchMode(true);  
        mView.setOnKeyListener(new OnKeyListener(){
        	public boolean onKey(View v, int keyCode, KeyEvent event){
        		if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK){
                	String autoPush=attrMap.get("autoPush");
    				if(autoPush==null || autoPush.equals(BOOLEANTRUE))
    					window.dismiss();
    				else{
    					String onBackPressed=attrMap.get("onBackPressed");
    					if(onBackPressed!=null)
    	                	executeLua(onBackPressed);
    				}
    				return true;
        		}
        		return false;
        	}
        });  
        window.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
            	backgroundMask.dismiss();
                if (!TextUtils.isEmpty(onDismiss)) {
                	executeLua(onDismiss);
                }
            }
        });
    }
    @Override
    public void setOnClick(String onClickString){
    	if(onClickString!=null && onClickString.length()!=0){
    		super.setOnClick(onClickString);
    	}
    	else{
    		mView.setOnClickListener(new OnClickListener(){
    			@Override
    			public void onClick(View v) {
    				if(ignoreDissmiss==true){
    					ignoreDissmiss=false;
    					return;
    				}
    				String autoPush=attrMap.get("autoPush");
    				if(autoPush==null || autoPush.equals(BOOLEANTRUE))
    					window.dismiss();
    			}
    		});
    	}
    }
    @Override
    public void upDateEntity(Object entityData){
    	super.upDateEntity(entityData);
    }
    
    public void popView(){
    	if( window.isShowing()){
            window.dismiss();
            ignoreDissmiss=false;
        }
        window.setBackgroundDrawable(mView.getBackground());
        backgroundMask.showAtLocation(baseFragment.getView(),0,0,0);
        window.showAtLocation(baseFragment.getView(),0,0,0);
    }
    /**
     * in the BaseFragment show PopupView
     * 
     * @param gravity 在父窗口中的位置 (left,reght,center,top ,bottom)
     * @param x 横向偏移值
     * @param y 纵向偏移值
     */
    public void showInFragment(final String gravity, final int x, final int y) {   
    	if(childBaseViews.size()>0){
    		BaseView bv=childBaseViews.get(0);
    		HashMap<String, String> sizeMap=bv.getAttributes();
    		String margin= x+","+y+",0,0";
    		sizeMap.put(ATTR_MARGIN,margin);
			bv.setMargin(margin);
    		bv.setLayoutGravity(gravity);
    	}
    	if( window.isShowing()){
    		return;
    	}
    	Runnable r=new Runnable() {
            @Override
            public void run() {
                try {
                    backgroundMask.showAtLocation(baseFragment.getView(),0,0,0);
                    window.showAtLocation(baseFragment.getRootView().getView(),0,0,0);
                } catch (Exception e) {
                    e.printStackTrace();
                }     
           }
        };
        if(Looper.myLooper()==Looper.getMainLooper())
        	r.run();
        else
        	baseFragment.mHandler.post(r);
    }
    
    
    
    /**
     * 设置动画
     * @param anima  资源R中style的id
     */
    public void setAnimationStyle(int anima){
    	window.setAnimationStyle(anima);

    }
    public void setAnimation(int anima){
    	if(childBaseViews.size()>0){
    		BaseView bv=childBaseViews.get(0);
    		bv.getView().setAnimation(AnimationUtils.loadAnimation(mContext, anima));
    	}
    }
    
    /**
     * 显示在指定控件下
     * 
     * @param anchor 指定的控件
     * @param xoff 横向偏移值
     * @param yoff 纵向偏移值
     * @author Zhang Xing
     */
    public void showAsDropDown(BaseView anchor, int xoff, int yoff) {
    	int[] location = new int[2];
    	int[] lo2=new int[2];
    	baseFragment.getRootView().getView().getLocationOnScreen(lo2);
     	View anchorV=anchor.getView();
     	anchorV.getLocationOnScreen(location);
    	location[0]=DisplayUtil.px2dip(mContext, location[0]-lo2[0])+xoff;
    	location[1]=DisplayUtil.px2dip(mContext, location[1]-lo2[1]+anchorV.getHeight())+yoff;

    	if(childBaseViews.size()>0){
    		BaseView bv=childBaseViews.get(0);
    		HashMap<String, String> sizeMap=bv.getAttributes();
    		String margin=location[0]+","+location[1]+",0,0";
    		sizeMap.put(ATTR_MARGIN,margin);
			bv.setMargin(margin);
    	}
        backgroundMask.showAtLocation(baseFragment.getView(),0,0,0);
    	window.showAtLocation(baseFragment.getRootView().getView(),0,0,0);
    	//window.showAsDropDown(anchorV,0,0);
    }
    
    public boolean isShowing(){return window.isShowing();} 
 
    public void dismiss(){window.dismiss();ignoreDissmiss=false;}
}
