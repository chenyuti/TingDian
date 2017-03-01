package com.logansoft.UIEngine.parse.field.adapterGroup;

import java.lang.ref.WeakReference;

import org.w3c.dom.Element;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.logansoft.UIEngine.Base.UIEngineColorParser;
import com.logansoft.UIEngine.Base.UIEngineGroupView;
import com.logansoft.UIEngine.Base.UIEngineGroupView.UIEngineLayoutParams;
import com.logansoft.UIEngine.Base.Entity.UIEngineEntity;
import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.xmlview.BaseView;
import com.logansoft.UIEngine.parse.xmlview.BaseViewFactory;
import com.logansoft.UIEngine.parse.xmlview.GroupView;
import com.logansoft.UIEngine.utils.Configure;
import com.logansoft.UIEngine.utils.GlobalConstants;

public class SectionIndexView extends GroupView {
	protected String sectionIndexId;
	protected WeakReference<SectionListTable> sectionTable;
	public SectionIndexView(BaseFragment fragment,GroupView parentView,Element element) {
        super(fragment,parentView, element);
	}
	@Override
	protected void parseView(){
		super.parseView();
    	sectionIndexId=attrMap.get("sectionIndexId");
	}
	@Override
	public void addTemplateData(Object entities){
		super.addTemplateData(entities);
		((UIEngineGroupView)mView).removeAllViews();
		if(entity.getTemplateData().size()>0 && sectionIndexId!=null){
			Element templates=null;
			if(Templates!=null && Templates.size()==1){
				templates=(Element)Templates.values().iterator().next();
			}
			int i=0;
			String textColor=attrMap.get("fontColor");
			int tc=Color.BLACK;
			if(textColor!=null)
				tc=UIEngineColorParser.getColor(textColor);
			String itemHeight=attrMap.get("itemHeight");
			int th=(int)(Configure.screenDensity*30);
			if(itemHeight!=null)
				th=(int)(Configure.screenDensity*Float.parseFloat(itemHeight));
			for(UIEngineEntity tempItemEntity:entity.getTemplateData()){
				String value=tempItemEntity.getValueForKey(sectionIndexId);
				if(templates==null){
					TextView text=new TextView(mContext);
					text.setText(value);
					text.setOnClickListener(new sectionIndexOnClickListener(i));
					text.setTextColor(tc);
					text.setGravity(Gravity.CENTER);
					text.setLayoutParams(new UIEngineLayoutParams(LayoutParams.MATCH_PARENT,th));
					((UIEngineGroupView)mView).addView(text);
				}else{
					BaseView bv=BaseViewFactory.newInstance(baseFragment,this,templates);
					bv.upDateEntity(tempItemEntity);
					bv.getView().setOnClickListener(new sectionIndexOnClickListener(i));
					addView(bv);					
				}
				i++;
			}
		}
	}
	
	@Override
    public void setOrientation(String orientation) {
		super.setOrientation(GlobalConstants.ATTR_VERTICAL);
	}
	public void setSectionTable(SectionListTable sectionTable){
		this.sectionTable=new WeakReference(sectionTable);
	}
	public class sectionIndexOnClickListener implements OnClickListener{
		protected int index;
		
		public sectionIndexOnClickListener(int index){
			this.index=index;
		}
		
		@Override
		public void onClick(View v) {
			SectionListTable sec=sectionTable.get();
			if(sec!=null){
				sec.scollToGroupPosition(index);
			}
		}
		
	}
    public void setLayoutGravity(String gravity){
    	super.setLayoutGravity(gravity);
    	mLayoutParams.setGravity(UIEngineGroupView.GravityRight);
    }

}
