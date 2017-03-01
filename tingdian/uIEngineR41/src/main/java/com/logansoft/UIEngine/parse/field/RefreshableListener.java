package com.logansoft.UIEngine.parse.field;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import com.logansoft.UIEngine.Base.Entity.UIEngineEntity;
import com.logansoft.UIEngine.parse.xmlview.BaseView;
import com.logansoft.UIEngine.parse.xmlview.GroupView;
import com.logansoft.UIEngine.utils.GlobalConstants;
import com.logansoft.UIEngine.utils.LogUtil;
import com.logansoft.UIEngine.view.pullToRefresh.PullToRefreshBase;
import com.logansoft.UIEngine.view.pullToRefresh.PullToRefreshBase.Mode;
import com.logansoft.UIEngine.view.pullToRefresh.PullToRefreshBase.OnRefreshListener2;

import android.text.TextUtils;
import android.view.View;

public class RefreshableListener<T extends PullToRefreshBase<?>> implements OnRefreshListener2<T> {
	private WeakReference<BaseView> viewReference;
    String flush;
    String update;
    
    public RefreshableListener(GroupView refreshableGroup) {
    	viewReference=new WeakReference<BaseView>(refreshableGroup);
        if(refreshableGroup!=null){
        	View refreshableView=refreshableGroup.getView();
        	if(refreshableView instanceof PullToRefreshBase){
        		PullToRefreshBase RFV=(PullToRefreshBase<T>) refreshableView;
        		RFV.getLoadingLayoutProxy().setPullLabel("加载更多");
        		RFV.getLoadingLayoutProxy().setRefreshingLabel("正在加载中..");
        		update = refreshableGroup.getAttribute(GlobalConstants.ATTR_ON_UPDATE);
                flush = refreshableGroup.getAttribute(GlobalConstants.ATTR_ON_FLUSH);
                String interceptTouchEvent = refreshableGroup.getAttribute("interceptTouchEvent");
                boolean intercept = false;
                if (!TextUtils.isEmpty(interceptTouchEvent)) {
                    intercept =Boolean.parseBoolean(interceptTouchEvent);
                }
                RFV.interceptTouchEvent(intercept);
                boolean isUpdate=false;
                boolean isFlush=false;
                if (!TextUtils.isEmpty(update)) 
                    isUpdate = true;
                if (!TextUtils.isEmpty(flush)) 
                    isFlush = true;
                Mode mode;
                if(isUpdate && isFlush)
                	mode=Mode.BOTH;
                else if (isUpdate)
                	mode=Mode.PULL_FROM_START;
                else if (isFlush)
                	mode=Mode.PULL_FROM_END;
                else 
                	mode=Mode.DISABLED;
                RFV.setMode(mode);
                if(isUpdate || isFlush)
                	RFV.setOnRefreshListener(this);
        	}
        }
    }
    public BaseView getView(){
    	return viewReference==null?null:viewReference.get();
    }
    @Override
    public void onPullDownToRefresh(PullToRefreshBase<T> refreshView){
    	BaseView bv=getView();
		if(bv!=null){
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("loadPageNumber",1);
			bv.executeLua(update, map);
		}
    }
    //warring not best!
    @Override
    public void onPullUpToRefresh(PullToRefreshBase<T> refreshView){
    	final BaseView bv=getView();
		if(bv!=null){
			bv.getView().post(new Runnable(){
				@Override
			    public void run(){
					HashMap<String, Object> map = new HashMap<String, Object>();
					UIEngineEntity entity=bv.getEntity();
					if(entity!=null){
						if(entity.getPageIndex()==entity.getMaxPage()){
							if(bv instanceof refreshable)
								((refreshable)bv).onRefreshComplete();
							return;
						}
						map.put("loadPageNumber", entity.getPageIndex()+1);
					}
					bv.executeLua(flush, map);
				}
			});
		}
    }
    public interface refreshable{
        public void onRefreshComplete();
    }
}