
package com.logansoft.UIEngine.parse.field.adapterGroup;

import org.w3c.dom.Element;

import com.logansoft.UIEngine.Base.Interface.AdapterInterface;
import com.logansoft.UIEngine.Base.Interface.verticalScrollableInterface;
import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.field.RefreshableListener;
import com.logansoft.UIEngine.parse.field.adapterGroup.adapter.XBaseAdapter;
import com.logansoft.UIEngine.parse.field.adapterGroup.adapter.adapterItemView;
import com.logansoft.UIEngine.parse.xmlview.GroupView;
import com.logansoft.UIEngine.utils.DisplayUtil;
import com.logansoft.UIEngine.utils.LogUtil;
import com.logansoft.UIEngine.view.pullToRefresh.PullToRefreshGridView;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.GridView;

public class GridTable extends AdapterGroup implements RefreshableListener.refreshable,verticalScrollableInterface{
    public static final String ATTR_NUMCOLUMNS = "numColumns";
    public static final String ATTR_SPACING = "spacing";
    private GridView gridView;

    public GridTable(BaseFragment fragment,GroupView parentView,Element element) {
        super(fragment,parentView,element);
    }
	@Override
    protected void createMyView(){
		mView=new PullToRefreshGridView(mContext){
    		@Override
    		public void draw(Canvas canvas) {
    			bdraw(canvas);
    			super.draw(canvas);
    			adraw(canvas);
    		}
    	};
		gridView=((PullToRefreshGridView)mView).getRefreshableView();
    }
    @Override
    protected void parseAttributes() {
        super.parseAttributes();
        String num = attrMap.get(ATTR_NUMCOLUMNS);
        if (!TextUtils.isEmpty(num) && num.matches("\\d+")) {
            gridView.setNumColumns(Integer.parseInt(num));
        }
        String Spacing=attrMap.get(ATTR_SPACING);
        int horizontal= 0;
        int vertical =0;
        if (!TextUtils.isEmpty(Spacing)) {
            LogUtil.i("Spacing:"+Spacing);
            String[] spacing =Spacing.split(",");
            horizontal= Integer.parseInt(spacing[0]);
            vertical =Integer.parseInt(spacing[1]);
            horizontal= DisplayUtil.dip2px(mContext, horizontal);
            vertical = DisplayUtil.dip2px(mContext, vertical);
            gridView.setVerticalSpacing(vertical);   
            gridView.setHorizontalSpacing(horizontal);   
        }
        new RefreshableListener(this);
    }
    @Override
	public AdapterInterface createAdapter() {
		return new XBaseAdapter(baseFragment,this);
	}
    @Override
	public void setAdapter(AdapterInterface adapter){
    	gridView.setAdapter((XBaseAdapter)adapter);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gridView.setDescendantFocusability(GridView.FOCUS_AFTER_DESCENDANTS);
    }
    
    @Override
    public void setOnClick(final String luaStr) {
    }

	@Override
	public void scrollToTop(){
		gridView.smoothScrollToPosition(0);
	}
	@Override
	public void scrollToBottom() {
		gridView.smoothScrollToPosition(((XBaseAdapter)adapter).getCount()-1);
	}
	 @Override
	    public void onLowMemory(){
	    	super.onLowMemory();
	    	for(int i=0;i<gridView.getChildCount();i++){
	    		View v=gridView.getChildAt(i);
	    		if(v instanceof adapterItemView){
	    			((adapterItemView) v).onLowMemory();
	    		}
	    	}
	    }
	    @Override
	    public void viewWillAppear(){
	    	super.viewWillAppear();
	    	for(int i=0;i<gridView.getChildCount();i++){
	    		View v=gridView.getChildAt(i);
	    		if(v instanceof adapterItemView){
	    			((adapterItemView) v).viewWillAppear();
	    		}
	    	}
	    }
	    @Override
	    public void viewDidDisappear(){
	    	super.viewDidDisappear();
	    	for(int i=0;i<gridView.getChildCount();i++){
	    		View v=gridView.getChildAt(i);
	    		if(v instanceof adapterItemView){
	    			((adapterItemView) v).viewDidDisappear();
	    		}
	    	}
	    }
	@Override
	public void Destory(){
		for(int i=0;i<gridView.getChildCount();i++){
    		View v=gridView.getChildAt(i);
    		if(v instanceof adapterItemView){
    			((adapterItemView) v).Destory();
    		}
    	}
		gridView=null;
		super.Destory();
	}
}
