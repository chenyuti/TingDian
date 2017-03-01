
package com.logansoft.UIEngine.parse.field.adapterGroup;

import java.util.List;

import org.w3c.dom.Element;

import com.logansoft.UIEngine.Base.Entity.UIEngineEntity;
import com.logansoft.UIEngine.Base.Interface.AdapterInterface;
import com.logansoft.UIEngine.Base.Interface.verticalScrollableInterface;
import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.field.RefreshableListener;
import com.logansoft.UIEngine.parse.field.adapterGroup.adapter.XExpandableAdapter;
import com.logansoft.UIEngine.parse.xmlview.BaseView;
import com.logansoft.UIEngine.parse.xmlview.GroupView;
import com.logansoft.UIEngine.view.pullToRefresh.PullToRefreshExpandableListView;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

public class ExpandableListTable extends AdapterGroup implements RefreshableListener.refreshable,verticalScrollableInterface{
    protected ExpandableListView expandableListView;
    protected PullToRefreshExpandableListView RefreshView;

    private String currGroupPostion = null;// 点击之后当前父位置
    private String currChildPostion = null;// 点击之后当前子位置

    public static final String ATTR_LIST_CLICK = "onListClick";
    public static final String ATTR_ITEM_CLICK = "onItemClick";
 
    public ExpandableListTable(BaseFragment fragment,GroupView parentView,Element element) {
        super(fragment, parentView, element);
    }
    @Override
    protected void createMyView(){
    	mView=RefreshView =new PullToRefreshExpandableListView(mContext){
    		@Override
    		public void draw(Canvas canvas) {
    			bdraw(canvas);
    			super.draw(canvas);
    			adraw(canvas);
    		}
    	};
        expandableListView=RefreshView.getRefreshableView();
    }
    @Override
    protected void parseView() {
    	super.parseView();
        expandableListView.setGroupIndicator(null);
        expandableListView.setDivider(new ColorDrawable(0xffcccccc));
        expandableListView.setChildDivider(new ColorDrawable(0xffcccccc));
        expandableListView.setDividerHeight(1);
        expandableListView.setCacheColorHint(0x00000000);
        if (attrMap.containsKey("divisionColor")) {
            String divisionColor = attrMap.get("divisionColor");
            int c = Color.parseColor(divisionColor);
            expandableListView.setDivider(new ColorDrawable(c));
            expandableListView.setChildDivider(new ColorDrawable(c));
            expandableListView.setDividerHeight(1);
        }
    	expandableListView.setSelector(new ColorDrawable(Color.TRANSPARENT));  
        new RefreshableListener(this);
    }
    @Override
	public AdapterInterface createAdapter() {
    	XExpandableAdapter ad=new XExpandableAdapter(baseFragment,this);
    	ad.childLeftPadding=true;
    	return ad;
	}
    @Override
   	public void setAdapter(AdapterInterface adapter){
    	expandableListView.setAdapter((XExpandableAdapter)adapter);
    }
    
    public String getCurrGroupPostion() {
        return currGroupPostion;
    }

    public void setCurrGroupPostion(String currGroupPostion) {
        this.currGroupPostion = currGroupPostion;
    }

    public String getCurrChildPostion() {
        return currChildPostion;
    }

    public void setCurrChildPostion(String currChildPostion) {
        this.currChildPostion = currChildPostion;
    }
    @Override
    public void setOnClick(String onClickString){
		setGroupClickListener();
		setChildClickListener();
    }

    public void setGroupClickListener() {
        expandableListView.setOnGroupClickListener(new OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                setCurrGroupPostion(String.valueOf(groupPosition));
                setCurrChildPostion("-1");
                Object tag=v.getTag();
                if(tag instanceof BaseView){
                	((BaseView) tag).getView().performClick();
                }
                return false;
            }
        });
    }

    public void setChildClickListener(){
        expandableListView.setOnChildClickListener(new OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                    int childPosition, long id) {
            	
                setCurrChildPostion(String.valueOf(childPosition));
                setCurrGroupPostion(String.valueOf(groupPosition));
                Object tag=v.getTag();
                if(tag instanceof BaseView){
                	((BaseView) tag).getView().performClick();
                }
                return false;
            }
        });
    }



    /**
     * 提供点击一个group其他group收起的功能
     */
    public void onClickGroupCollapseOther(String postion) {
    	if(entity!=null){
    		for (int i = 0; i < entity.getTemplateData().size(); i++) {
        		expandableListView.collapseGroup(i);
        	}
        	Integer grouppostion = Integer.valueOf(postion);
        	expandableListView.expandGroup(grouppostion);
    	}
       
    }

    /**
     * 提供点击一个group展开
     */
    public void onClickGroupExpanded(String postion) {
        Integer grouppostion = Integer.valueOf(postion);
        expandableListView.expandGroup(grouppostion);
    }

    /**
     * 提供点击一个group收起
     */
    public void onClickGroupCollapse(String postion) {
        Integer grouppostion = Integer.valueOf(postion);
        expandableListView.collapseGroup(grouppostion);
    }
    public boolean isGroupExpanded(int groupPosition){
    	return expandableListView.isGroupExpanded(groupPosition);
    }
    
    public void scollToGroupPosition(int groupPosition){
    	expandableListView.setSelectedGroup(groupPosition);
    }
    
    @Override
	public void scrollToTop(){
    	expandableListView.smoothScrollToPosition(0);
	}
	@Override
	public void scrollToBottom() {
		XExpandableAdapter Adapter=(XExpandableAdapter)adapter;
		int position=Adapter.getGroupCount();
		for(int i=0;i<position;i++){
			if(expandableListView.isGroupExpanded(i)){
				position+=Adapter.getChildrenCount(i);
			}
		}
    	expandableListView.smoothScrollToPosition(position-1);
    }
    
    @Override
	public void reloadTemplateData(){
		super.reloadTemplateData();
		String subListSelectMode=attrMap.get("subListSelectMode");
		if(subListSelectMode==null ||subListSelectMode.length()==0)
			return;
		List<UIEngineEntity> templateData=entity.getTemplateData();
		for(UIEngineEntity subEntity:templateData){
			if(subEntity.getSelectedProvider()==null){
				subEntity.createSelectionProvider(subListSelectMode);
			}
		}
	}

    @Override
    public void Destory(){
    	expandableListView=null;
    	super.Destory();
    }
	
}
