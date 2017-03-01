
package com.logansoft.UIEngine.parse.field;

import org.w3c.dom.Element;

import com.logansoft.UIEngine.Base.UIEngineGroupView;
import com.logansoft.UIEngine.Base.UIEngineGroupView.UIEngineLayoutParams;
import com.logansoft.UIEngine.Base.Interface.verticalScrollableInterface;
import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.xmlview.GroupView;
import com.logansoft.UIEngine.utils.GlobalConstants;
import com.logansoft.UIEngine.view.pullToRefresh.PullToRefreshBase;
import com.logansoft.UIEngine.view.pullToRefresh.PullToRefreshScrollView;

import android.content.Context;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.HorizontalScrollView;

public class ScrollView extends GroupView implements RefreshableListener.refreshable,verticalScrollableInterface{
    private UIEngineGroupView mRootLayout;

    public ScrollView(BaseFragment fragment,GroupView parentView,Element element) {
        super(fragment,parentView,element);
    }
    @Override
    protected void createMyView(){
    	String orientation = attrMap.get(GlobalConstants.ATTR_ORIENTATION);
    	if ("horizontal".equalsIgnoreCase(orientation)  || "center_horizontal".equalsIgnoreCase(orientation)) {
    		attrMap.put(GlobalConstants.ATTR_ORIENTATION, "horizontal");
    		MyHorizontalScrollView horizontalScrollView =new MyHorizontalScrollView(mContext){
        		@Override
        		public void draw(Canvas canvas) {
        			bdraw(canvas);
        			super.draw(canvas);
        			adraw(canvas);
        		}
        	};
    		mView =horizontalScrollView;
    		horizontalScrollView.setVerticalScrollBarEnabled(false);
    		horizontalScrollView.setHorizontalScrollBarEnabled(false);
    	} else {
    		PullToRefreshBase<android.widget.ScrollView> mVPullToRefreshBase = new PullToRefreshScrollView(mContext){
        		@Override
        		public void draw(Canvas canvas) {
        			bdraw(canvas);
        			super.draw(canvas);
        			adraw(canvas);
        		}
        	};
    		mView =mVPullToRefreshBase;
    		setPullToRefresh2(mVPullToRefreshBase);

    	}
    }
    @Override
    protected void parseView(){
    	super.parseView();
    	groupView=mRootLayout=new UIEngineGroupView(mContext){
    	    @Override
    		protected void setChildMeasure(View childView,int elementwidth,int elementheight,int availableWidth,int availableHeight,int[] childMargin){
    	    	super.setChildMeasure(childView, elementwidth, elementheight, 0, 0, childMargin);
    	    }
    	};
        UIEngineLayoutParams rootParams = new UIEngineLayoutParams(mLayoutParams);
    	mRootLayout.setOrientation(attrMap.get(GlobalConstants.ATTR_ORIENTATION));
    	if(mRootLayout.Orientation==UIEngineGroupView.HORIZONTAL)
    		rootParams.width=LayoutParams.WRAP_CONTENT;
    	else if(mRootLayout.Orientation==UIEngineGroupView.VERTICAL)
    		rootParams.height=LayoutParams.WRAP_CONTENT;
    	mRootLayout.setLayoutParams(rootParams);
    	mRootLayout.setBackgroundColor(0x00000000);
    	((ViewGroup)mView).addView(mRootLayout);
    }
   
    public void setOnBorderListener(final String left,final String right ,final String scroll){
        if(mView instanceof MyHorizontalScrollView){
            if (TextUtils.isEmpty(left)&&TextUtils.isEmpty(right)&&TextUtils.isEmpty(scroll)) {
                return ;
            }
            MyHorizontalScrollView  horizontalScrollView = (MyHorizontalScrollView)mView;
            horizontalScrollView.setOnBorderListener(new OnBorderListener() {
                @Override
                public void onLeft() {
                    if(!TextUtils.isEmpty(left))
                    	executeLua(left);
                }
                
                @Override
                public void onRight() {
                    if(!TextUtils.isEmpty(right))
                    	executeLua(right);
                }
    
                @Override
                public void onScroll() {
                	if(!TextUtils.isEmpty(scroll))
                		executeLua(scroll);
                }
            });
        }
    }
    
    /**
     * 按方向滑动
     * @param direction   horizontal "left" "right" 
     *                                "up" "down"
     */
    @SuppressWarnings("unchecked")
    public void scroll(String direction){
        String orientation = attrMap.get(GlobalConstants.ATTR_ORIENTATION);
        if ("horizontal".equalsIgnoreCase(orientation)  || "center_horizontal".equalsIgnoreCase(orientation)) {
            if (direction.equals("left")) {
                ((HorizontalScrollView)mView).arrowScroll(View.FOCUS_LEFT);
            }else  if (direction.equals("right")) {
                ((HorizontalScrollView)mView).arrowScroll(View.FOCUS_RIGHT);
            }
        }else {
            if (direction.equals("up")) {
                ((PullToRefreshBase<android.widget.ScrollView>)mView).getRefreshableView().arrowScroll(View.FOCUS_UP);
            }else  if (direction.equals("down")) {
                ((PullToRefreshBase<android.widget.ScrollView>)mView).getRefreshableView().arrowScroll(View.FOCUS_DOWN);
            }
        }
    }
    
    public void setPullToRefresh2(PullToRefreshBase<android.widget.ScrollView>  mPullToRefreshBase){
         mPullToRefreshBase.getRefreshableView().setVerticalScrollBarEnabled(false);
         mPullToRefreshBase.getRefreshableView().setHorizontalScrollBarEnabled(false);
         new RefreshableListener(this);
    }

    // public void load(String url) {
    // new ViewProvider(baseFragment, mHandler,
    // 0,mFragmentManager).parsePageView(url, mContext);
    // }
   
    @Override
    public void onRefreshComplete(){
        baseFragment.mHandler.post(new Runnable() {
            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                if (mView instanceof  PullToRefreshBase) {
                    ((PullToRefreshBase<android.widget.ScrollView>)mView).onRefreshComplete();
                }
            }
        });
    }
	@Override
	public void reloadTemplateData(){
		super.reloadTemplateData();
		onRefreshComplete();
	}
	@Override
	public void addTemplateData(Object entities){
		super.addTemplateData(entities);
		onRefreshComplete();
	}
    public void scrollToRight(){
    	if(mView instanceof MyHorizontalScrollView ){
    		MyHorizontalScrollView horizontalScrollView=(MyHorizontalScrollView) mView;
    		horizontalScrollView.scrollToRight();
    	}
    }
    public void scrollToLeft(){
    	if(mView instanceof MyHorizontalScrollView ){
    		MyHorizontalScrollView horizontalScrollView=(MyHorizontalScrollView) mView;
    		horizontalScrollView.scrollToLeft();
    	}
    }
    @Override
    public void scrollToTop(){
    	if(mView instanceof PullToRefreshBase ){
    		mView.post(new Runnable(){
				@Override
				public void run() {
		    		PullToRefreshBase<android.widget.ScrollView> ScrollView=(PullToRefreshBase<android.widget.ScrollView>) mView;
		    		ScrollView.getRefreshableView().fullScroll(ScrollView.FOCUS_UP);
				}
    		});
    	}
    }
    @Override
    public void scrollToBottom(){
    	if(mView instanceof PullToRefreshBase ){
    		mView.post(new Runnable(){
				@Override
				public void run() {
		    		PullToRefreshBase<android.widget.ScrollView> ScrollView=(PullToRefreshBase<android.widget.ScrollView>) mView;
		    		ScrollView.getRefreshableView().fullScroll(ScrollView.FOCUS_DOWN);
				}
    		});
    	}
    }
    
    
    public class MyHorizontalScrollView extends HorizontalScrollView{

        private OnBorderListener onBorderListener;
        View contentView ;
        boolean isTop =false ;
        boolean isBottom =false;
        boolean isScroll=true;
        public MyHorizontalScrollView(Context context) {
            super(context);
        }
        
        public void scrollToRight(){
        	scrollTo(computeHorizontalScrollRange(),computeVerticalScrollOffset());	        	
        }
        public void scrollToLeft(){
        	scrollTo(0,computeVerticalScrollOffset());	        	
        }
        
        public void setOnBorderListener(OnBorderListener listener){
            onBorderListener=listener;
            if (onBorderListener == null) {
                contentView = null;
                return;
            }
            if (contentView == null) {
                contentView = getChildAt(0);
            }
        }
        
        @Override
        protected void onScrollChanged(int l, int t, int oldl, int oldt) {
            super.onScrollChanged(l, t, oldl, oldt);
            doOnBorderListener();
        }
        
        private void doOnBorderListener() {
            if (contentView != null && contentView.getMeasuredWidth() <= getScrollX() + getWidth()) {
                if (onBorderListener != null&&!isBottom) {
                    isBottom =true;
                    isTop=false;
                    isScroll = false;
                    onBorderListener.onRight();
                }
            } else if (getScrollX() == 0) {
                if (onBorderListener != null&&!isTop) {
                    isTop =true;
                    isBottom =false;
                    isScroll = false;
                    onBorderListener.onLeft();
                }
            }else {
                if (onBorderListener != null&&!isScroll){
                    isTop =false;
                    isBottom =false;
                    isScroll = true;
                    onBorderListener.onScroll();
                }
            }
        }
    }
    /**
     * OnBorderListener, Called when scroll to top or bottom
     * 
     * @author Trinea 2013-5-22
     */
    public interface OnBorderListener {
        
        /**
         * Called when scroll to bottom
         */
        public void onRight();
        
        /**
         * Called when scroll to top
         */
        public void onLeft();
    
        public void onScroll();
    }
    
    @Override
    public void Destory(){
    	mRootLayout=null;
    	super.Destory();
    }
    
}
