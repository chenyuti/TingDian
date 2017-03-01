
package com.logansoft.UIEngine.parse.xmlview;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.logansoft.UIEngine.Base.UIEngineGroupView;
import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.field.PopupView;
import com.logansoft.UIEngine.parse.xmlview.Text.MTextField;
import com.logansoft.UIEngine.utils.Configure;
import com.logansoft.UIEngine.utils.StringUtil;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public  class PageView extends GroupView {
	private int statusBarHeight;
    private Object showingKeyBoardView;
    private View statusMask;
    private HashMap<String,BaseView> popupViews;

    public PageView(BaseFragment fragment,GroupView parentView,Element root) {
        super(fragment,parentView,root);
    }
    @Override
    protected void createMyView(){
    	mView = new UIEngineGroupView(mContext){
//    		@Override
//    		public void draw(Canvas canvas) {
//    			bdraw(canvas);
//    			super.draw(canvas);
//    			adraw(canvas);
//    		}
          	public boolean onInterceptTouchEvent(MotionEvent ev) {
          		if(showingKeyBoardView!=null){    
          			int[] l = new int[2];
          			this.getLocationOnScreen(l);
          			int x=l[0]+(int)ev.getX();
          			int y=l[1]+(int)ev.getY();
          			View targetView=findEditTextView(this,x,y);
          			
          			if(!(targetView instanceof EditText)){
                  		hideKeyboard();
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
    protected void parseView(){
    	super.parseView();
    	popupViews=new HashMap<String,BaseView>();
        ((UIEngineGroupView)mView).defaultOrientation=UIEngineGroupView.VERTICAL;
        mView.setFocusableInTouchMode(true);
        mDrawable.defaultBackgroundColor=Color.WHITE;
        
    	if(Configure.translucent_Status()) {//mElement.getTagName().equals(GlobalConstants.XML_PAGE) &&
    		Rect frame = new Rect();
    		((Activity)mContext).getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
    		statusBarHeight = frame.top;
    		statusMask=new View(mContext);
    		statusMask.setBackgroundColor(Color.BLACK);
    		UIEngineGroupView.UIEngineLayoutParams lp=new UIEngineGroupView.UIEngineLayoutParams(LayoutParams.MATCH_PARENT,statusBarHeight);
    		statusMask.setLayoutParams(lp);
    		((ViewGroup)mView).addView(statusMask);
    	}
    	else
    		statusBarHeight=0;
    }
    @Override
    protected void parseSubView() {
    	super.parseSubView();
        NodeList pops=mElement.getElementsByTagName("PopupView");
        for(int i=0;i<pops.getLength();i++){
        	Node pop=pops.item(i);
        	if(pop.getNodeType()==Node.ELEMENT_NODE){
        		BaseView bv = new PopupView(baseFragment,this,(Element)pop);
        		if(bv.mId!=null)
        			popupViews.put(bv.mId,bv);
        	}
        }
    }
   
    @Override
    public void setViewId(String tag){
        if (!StringUtil.isEmpty(tag)){
            baseFragment.setmId(tag);
        }
    }
//    @Override
//    public void upDateEntity(Object entity){
//    	
//    }
    
    public Object getShowingKeyBoard(){
    	return showingKeyBoardView;
    }
    public void showKeyBoardWithView(Object view){
    	showingKeyBoardView=view;
    }
    public void hideKeyboard(){
    	if(showingKeyBoardView==null)
    		return;
    	
    	if(showingKeyBoardView instanceof MTextField){
    		((MTextField)showingKeyBoardView).hideKeyboard();
    	}
    	else if(showingKeyBoardView instanceof EditText){
    		((View)showingKeyBoardView).clearFocus();
    	     InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    	     imm.hideSoftInputFromWindow(((View)showingKeyBoardView).getWindowToken(), 0);
    	}
    	showingKeyBoardView=null;
    }
}
