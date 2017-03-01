
package com.logansoft.UIEngine.parse.field.adapterGroup.adapter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import com.logansoft.UIEngine.Base.Entity.UIEngineEntity;
import com.logansoft.UIEngine.Base.Interface.AdapterInterface;
import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.field.adapterGroup.ExpandableListTable;
import com.logansoft.UIEngine.parse.xmlview.BaseView;
import com.logansoft.UIEngine.parse.xmlview.BaseViewFactory;
import com.logansoft.UIEngine.parse.xmlview.GroupView;
import com.logansoft.UIEngine.utils.DisplayUtil;
import com.logansoft.UIEngine.utils.GlobalConstants;
import com.logansoft.UIEngine.utils.StringUtil;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;

public class XExpandableAdapter extends BaseExpandableListAdapter implements AdapterInterface{
    Context mContext;
    BaseFragment fragment;
    GroupView groupView;
    public boolean childLeftPadding;
        
    public XExpandableAdapter(BaseFragment baseFragment,GroupView groupView) {
        mContext = baseFragment.getActivity();
        fragment=baseFragment;
        this.groupView=groupView;
    }
    
    @Override
    public int getGroupCount() {
       	UIEngineEntity entity=groupView.getEntity();
    	if(entity!=null)
    		return entity.getTemplateData().size();
        return 0;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
    	UIEngineEntity groupEntity=getGroup(groupPosition);
    	if(groupEntity!=null){
    		return groupEntity.getTemplateData().size();
    	}
    	return 0;
    }

    @Override
    public UIEngineEntity getGroup(int groupPosition) {
    	UIEngineEntity entity=groupView.getEntity();
    	if(entity!=null){
    		List<UIEngineEntity> templateData=entity.getTemplateData();
    		if(groupPosition<templateData.size()){
    			return templateData.get(groupPosition);
    		}
    	}
    	return null;
    }

    @Override
    public UIEngineEntity getChild(int groupPosition, int childPosition) {
    	UIEngineEntity groupEntity=getGroup(groupPosition);
    	if(groupEntity!=null){
    		List<UIEngineEntity> templateData=groupEntity.getTemplateData();
    		if(childPosition<templateData.size())
    			return templateData.get(childPosition);
    	}
    	return null;
    }
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
    @Override
    public boolean hasStableIds() {
        return true;
    }
    public View getView(UIEngineEntity entity,Element templateXML,View convertView,boolean isChild){
         if(templateXML==null){
         	if(convertView==null){
         		convertView=new View(mContext);
         	}
             return convertView;
         }
         BaseView contentViewController=null;
         if (convertView == null) {
             contentViewController = BaseViewFactory.newInstance(fragment,groupView,templateXML);
             contentViewController.setOnClick(templateXML.getAttribute("onItemClick"));
             View bvv=contentViewController.getView();
             bvv.setClickable(false);
             LinearLayout linearLayout = new LinearLayout(mContext);
             linearLayout.addView(contentViewController.getView());
             linearLayout.setTag(contentViewController);
             convertView = linearLayout;
             if(isChild && childLeftPadding){
            	 bvv.setPadding(DisplayUtil.dip2px(mContext, 30)+bvv.getPaddingLeft(),
            			 bvv.getPaddingTop(), bvv.getPaddingRight(),
            			 bvv.getPaddingBottom());
            	 
             }
         } else {
         	contentViewController=(BaseView)convertView.getTag();
         }
         contentViewController.upDateEntity(entity);
         contentViewController.setSelected(entity.isSelected());
         int[] margin=contentViewController.getmMargin();
         if(margin.length==4)
         	convertView.setPadding(margin[0],margin[1],margin[2],margin[3]);
         else
         	convertView.setPadding(0,0,0,0);
         ViewGroup.LayoutParams clp=contentViewController.getView().getLayoutParams();
         ViewGroup.LayoutParams alp=convertView.getLayoutParams();
         if(alp==null){
         	alp=new AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
             convertView.setLayoutParams(alp);
         }
         if(clp.width==LayoutParams.MATCH_PARENT)
         	alp.width=LayoutParams.MATCH_PARENT;
         if(clp.height==LayoutParams.MATCH_PARENT)
         	alp.height=groupView.getView().getHeight();
        
         return convertView;
    }
    @Override
    public View getGroupView(int groupPosition,boolean isExpanded,View convertView,ViewGroup parent) {
    	UIEngineEntity entity = getGroup(groupPosition);
    	String style=getGroupStyle(groupPosition,isExpanded);
    	Map<String,Element> templates = groupView.getTemplates();
        Element templateXML=templates.get(style);
    	View v=getView(entity,templateXML,convertView,false);
    	v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	if(v instanceof adapterItemView){
						adapterItemView aiv=(adapterItemView)v;
						BaseView bv=aiv.getBaseView();
						if(bv==null)return;
						HashMap<String,String> att=bv.getAttributes();
						String onListClick = att.get("onListClick");
						if(onListClick==null)return;
						int position =Integer.parseInt(att.get("groupPosition"));
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("groupPosition", position);
	                    bv.executeLua(onListClick, map);	
            	}
            }
        });
    	if(v instanceof adapterItemView){
    		BaseView bv=(BaseView)convertView.getTag();
    		bv.getAttributes().put("groupPosition",""+groupPosition);
    	}
        return v;
    }

    @Override
    public View getChildView(int groupPosition,int childPosition, boolean isLastChild,View convertView, ViewGroup parent) {
        UIEngineEntity entity=getChild(groupPosition, childPosition);
        String style=getChildStyle(groupPosition,childPosition);
    	Map<String,Element> templates = groupView.getTemplates();
        Element templateXML=templates.get(style);
        View v=getView(entity,templateXML,convertView,true);
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	if(v instanceof adapterItemView){
						adapterItemView aiv=(adapterItemView)v;
						BaseView bv=aiv.getBaseView();
						if(bv==null)return;
						HashMap<String,String> att=bv.getAttributes();
						String onItemClick = att.get("onItemClick");
						if(onItemClick==null)return;
						int groupPosition =Integer.parseInt(att.get("groupPosition"));
						int childPosition =Integer.parseInt(att.get("childPosition"));
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("groupPosition", groupPosition);
						map.put("childPosition", childPosition);
	                    bv.executeLua(onItemClick,map);	
            	}
            }
        });
        if(v instanceof adapterItemView){
    		BaseView bv=(BaseView)convertView.getTag();
    		bv.getAttributes().put("groupPosition",""+groupPosition);
    		bv.getAttributes().put("childPosition",""+childPosition);
    	}
        return v;
    }


    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    };
    
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
        		style=GlobalConstants.ATTR_STYLE_DEFAULT;
    		Map<String,Element> templates = groupView.getTemplates();
            Element templateXML=templates.get(style);
        	if(isExpanded){
        		Element temp=templates.get(style+"-expanded");
        		if(temp!=null)
        			style=style+"-expanded";
        	}
    	}
    	return style;
    }
    public String getChildStyle(int groupPosition,int childPosition){
    	 UIEngineEntity entity=getChild(groupPosition, childPosition);
         String style=null;
     	if(entity.getOriginalData() instanceof Element){
     		Element oe=(Element) entity.getOriginalData();
     		String nodeName =oe.getNodeName();
     		if (nodeName.equals(GlobalConstants.XML_ITEM))
     			style=(String)entity.getSubEntityValue("itemStyle");
     		else 
     			style=(String)entity.getSubEntityValue("listStyle");
     		if(style==null){
                 UIEngineEntity groupEntity=getGroup(groupPosition);
             	if (nodeName.equals(GlobalConstants.XML_ITEM))
         			style=(String)groupEntity.getSubEntityValue("itemStyle");
         		else 
         			style=(String)groupEntity.getSubEntityValue("listStyle");
         	}
     	}
     	else
     		style=entity.getStyle();
     	if(style==null)
     		style=GlobalConstants.ATTR_STYLE_DEFAULT;
    	return style;
    }
    public int getTemplateIndex(String style){
    	if (!StringUtil.isEmpty(style)) {
    		Iterator<Map.Entry<String, Element>> it = groupView.Templates.entrySet().iterator();
      		int index=0;
      		while (it.hasNext()) {
      			Map.Entry<String, Element> entry = it.next();
      			String key=entry.getKey();
      			if(style.equals(key)){
      				return index;
      			}
      			index++;
      		}
        }
    	return -1;
    }
   
    public int getTemplatesTypeCount(){
    	if(groupView.Templates != null && groupView.Templates.size()!=0) 
    		return groupView.Templates.size();
    	return 1;
    }
    @Override
    public int getGroupType(int groupPosition) {
    	if (groupView.Templates != null) {
    		boolean isExpanded=false;
    		if(groupView instanceof ExpandableListTable)
        		isExpanded=((ExpandableListTable)groupView).isGroupExpanded(groupPosition);
        	String style =getGroupStyle(groupPosition,isExpanded);
        	int index=getTemplateIndex(style);
        	if(index!=-1)
        		return index;
        } 
    	return super.getGroupType(groupPosition);
    }

    @Override
    public int getGroupTypeCount() {
    	return getTemplatesTypeCount();
    }
    @Override
    public int getChildType(int groupPosition, int childPosition) {
    	if (groupView.Templates != null) {
        	String style =getChildStyle(groupPosition,childPosition);
        	int index=getTemplateIndex(style);
        	if(index!=-1)
        		return index;
        } 
    	return super.getChildType(groupPosition,childPosition);    
    }
    @Override
    public int getChildTypeCount() {
    	return getTemplatesTypeCount();
    }

}
