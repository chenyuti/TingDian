package com.logansoft.UIEngine.parse.field.adapterGroup;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.logansoft.UIEngine.Base.UIEngineGroupView;
import com.logansoft.UIEngine.Base.Entity.UIEngineEntity;
import com.logansoft.UIEngine.Base.Interface.AdapterInterface;
import com.logansoft.UIEngine.Base.UIEngineGroupView.UIEngineLayoutParams;
import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.field.adapterGroup.adapter.XExpandableAdapter;
import com.logansoft.UIEngine.parse.xmlview.BaseView;
import com.logansoft.UIEngine.parse.xmlview.GroupView;
import com.logansoft.UIEngine.utils.GlobalConstants;
import com.logansoft.UIEngine.utils.LogUtil;
import com.logansoft.UIEngine.view.pullToRefresh.PullToRefreshExpandableListView;

import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.FrameLayout.LayoutParams;

public class SectionListTable extends ExpandableListTable {
	protected SectionIndexView sectionIndexView;

    public SectionListTable(BaseFragment fragment,GroupView parentView,Element element) {
        super(fragment,parentView, element);
    }
    @Override
    protected void createMyView(){
    	mView=new UIEngineGroupView(mContext);
    	RefreshView =new PullToRefreshExpandableListView(mContext);
        expandableListView =RefreshView.getRefreshableView();
        ((UIEngineGroupView)mView).addView(RefreshView);
    }
    @Override
    protected void parseView() {
    	super.parseView();
    	NodeList sectionIndexNodes=mElement.getElementsByTagName("sectionIndex");
    	if(sectionIndexNodes.getLength()==1){
    		Node sectionIndexNode=sectionIndexNodes.item(0);
    		if(sectionIndexNode.getNodeType()==Node.ELEMENT_NODE){
    			sectionIndexView=new SectionIndexView(baseFragment,this,(Element)sectionIndexNode);
    	        ((UIEngineGroupView)mView).addView(sectionIndexView.getView());
    			sectionIndexView.setSectionTable(this);
    		}
    	}
    }
    @Override
   	public AdapterInterface createAdapter() {
       	return new XExpandableAdapter(baseFragment,this){
       		@Override
       		public String getGroupStyle(int groupPosition,boolean isExpanded){
       	    	UIEngineEntity entity = getGroup(groupPosition);
       	    	String style=null;
       	    	if(entity!=null){
       	    		if(entity.getOriginalData() instanceof Element){
       	        		Element oe=(Element) entity.getOriginalData();
       	        		String nodeName =oe.getNodeName();
       	        		if (nodeName.equals(GlobalConstants.XML_ITEM)) {
       	        			style=(String)entity.getSubEntityValue("itemStyle");
       	        		}else {
       	        			style=(String)entity.getSubEntityValue("listStyle");
       	        		}
       	        	}
       	        	else
       	        		style=entity.getStyle();
       	    		if(style==null)
       	        		style="header";
       	    	}
       	    	return style;
       	    }
       	};	
   	}
    @Override
    public void setSize(String sizeString){
    	super.setSize(sizeString);
    	UIEngineLayoutParams lp=new UIEngineLayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
    	if(mLayoutParams.width==LayoutParams.WRAP_CONTENT)lp.width=LayoutParams.WRAP_CONTENT;
    	if(mLayoutParams.height==LayoutParams.WRAP_CONTENT)lp.height=LayoutParams.WRAP_CONTENT;
    	RefreshView.setLayoutParams(lp);
    }
    
    @Override
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
                return true;
            }
        });
    }
    @Override
	public void scrollToBottom() {
		XExpandableAdapter Adapter=(XExpandableAdapter)adapter;
		int position=Adapter.getGroupCount();
		for(int i=0;i<position;i++){
			position+=Adapter.getChildrenCount(i);
		}
    	expandableListView.smoothScrollToPosition(position-1);
    }
    @Override
   	public void reloadTemplateData(){
   		super.reloadTemplateData();
   		List<UIEngineEntity> templateData=entity.getTemplateData();
   		for(int i=0;i<templateData.size();i++){
   			expandableListView.expandGroup(i);
   		}
   	  if(sectionIndexView!=null)
      	sectionIndexView.upDateEntity(new UIEngineEntity(entity));
   	}
    @Override
    public void Destory(){
    	super.Destory();
    	if(sectionIndexView!=null)
    		sectionIndexView.Destory();
    	sectionIndexView=null;
    		
    }
    
}
