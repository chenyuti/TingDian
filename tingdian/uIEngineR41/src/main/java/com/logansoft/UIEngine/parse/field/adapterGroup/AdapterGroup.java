package com.logansoft.UIEngine.parse.field.adapterGroup;

import java.util.ArrayList;

import org.w3c.dom.Element;

import com.logansoft.UIEngine.Base.Entity.UIEngineEntity;
import com.logansoft.UIEngine.Base.Interface.AdapterInterface;
import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.xmlview.BaseView;
import com.logansoft.UIEngine.parse.xmlview.GroupView;
import com.logansoft.UIEngine.view.pullToRefresh.PullToRefreshBase;

import android.view.ViewGroup;

public abstract class AdapterGroup extends GroupView {
	protected AdapterInterface adapter;
	public AdapterGroup(BaseFragment fragment, GroupView parentView, Element element) {
		super(fragment, parentView, element);
		setAdapter(adapter);
	}
	@Override 
	protected void parseView(){
		super.parseView();
		adapter=createAdapter();
		entity=new UIEngineEntity();
	}
	public abstract AdapterInterface createAdapter();
	public abstract void setAdapter(AdapterInterface adapter);

    @Override
    protected void parseSubView(){
    }
	@Override
    public void addView(BaseView view) {
    }
	@Override
    public ArrayList<BaseView> getChildViews(){return null;}
	@Override
    public BaseView getChildAtIndex(int index) {return null;}
	@Override
	public void removeView(BaseView view){}
	@Override
	public void removeIdView(BaseView baseView){}
	@Override
    public void removeView(int index){}
	@Override
	public void removeView(String id){}
	@Override
	public void removeAllViews(){}
	@Override
    public void upDateEntity(Object entityData){
    	if(entityData!=entity && entityData instanceof UIEngineEntity)
    		clearTemplateData();
    	super.upDateEntity(entityData);
    }
	@Override
    public void addTemplateData(Object entities){
		reloadTemplateData();
    }
	@Override
    public void clearTemplateData() {
		reloadTemplateData();
    }

    public void onRefreshComplete() {
    	mView.post(new Runnable() {
            @Override
            public void run() {
            	if(mView instanceof PullToRefreshBase){
            		((PullToRefreshBase)mView).onRefreshComplete();
            		if(mLayoutParams.height==ViewGroup.LayoutParams.WRAP_CONTENT)
            			mView.setLayoutParams(mView.getLayoutParams());
            	}
            }
        });
    }
	@Override
	public void reloadTemplateData(){
		if(adapter!=null)
			adapter.notifyDataSetChanged();
		if(mView instanceof PullToRefreshBase)
			onRefreshComplete();
	}
	@Override
	public void Destory(){
		adapter=null;
		super.Destory();
	}
	    
}
