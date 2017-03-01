
package com.logansoft.UIEngine.parse.xmlview;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import org.w3c.dom.Element;

import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.field.BarItemView;
import com.logansoft.UIEngine.parse.field.DropList;
import com.logansoft.UIEngine.parse.field.FolderView;
import com.logansoft.UIEngine.parse.field.MNavigationBar;
import com.logansoft.UIEngine.parse.field.ScrollView;
import com.logansoft.UIEngine.parse.field.SliderView;
import com.logansoft.UIEngine.parse.field.TabBar;
import com.logansoft.UIEngine.parse.field.ToolBar;
import com.logansoft.UIEngine.parse.field.adapterGroup.ExpandableListTable;
import com.logansoft.UIEngine.parse.field.adapterGroup.GridTable;
import com.logansoft.UIEngine.parse.field.adapterGroup.ListTable;
import com.logansoft.UIEngine.parse.field.adapterGroup.SectionListTable;
import com.logansoft.UIEngine.parse.field.adapterGroup.ImageSlider.ImageSlider;
import com.logansoft.UIEngine.parse.xmlview.Text.MButton;
import com.logansoft.UIEngine.parse.xmlview.Text.MCount;
import com.logansoft.UIEngine.parse.xmlview.Text.MLabel;
import com.logansoft.UIEngine.parse.xmlview.Text.MSearchField;
import com.logansoft.UIEngine.parse.xmlview.Text.MTextField;
import com.logansoft.UIEngine.parse.xmlview.Text.MTextView;
import com.logansoft.UIEngine.parse.xmlview.Text.MarqueeLabel;
import com.logansoft.UIEngine.utils.Configure;
import com.logansoft.UIEngine.utils.GlobalConstants;
import com.logansoft.UIEngine.utils.LogUtil;
import com.logansoft.UIEngine.utils.Statistics;

import android.text.TextUtils;

/**
 * Item生成工厂
 * 
 * @author Prosper.Z
 */
public class BaseViewFactory {
	static HashMap<String,Constructor<BaseView>> constrMap;
	static{
		constrMap=new HashMap<String,Constructor<BaseView>>();
		try {
			setClassConstructor(GlobalConstants.CLASS_LABEL,MLabel.class);
			setClassConstructor(GlobalConstants.CLASS_BUTTON,MButton.class);
			setClassConstructor(GlobalConstants.CLASS_IMAGEBUTTON,MImageButton.class);
			setClassConstructor(GlobalConstants.CLASS_LINE,MLine.class);
			setClassConstructor(GlobalConstants.CLASS_IMAGE,MImageView.class);
			setClassConstructor(GlobalConstants.CLASS_TEXT_FIELD,MTextField.class);
			setClassConstructor(GlobalConstants.CLASS_SEARCH,MSearchField.class);
			setClassConstructor(GlobalConstants.CLASS_PROGRESS,MProgress.class);
			setClassConstructor(GlobalConstants.CLASS_WEBVIEW,MWebView.class);
			setClassConstructor(GlobalConstants.CLASS_VIDEOVIEW,MVideoView.class);
			setClassConstructor(GlobalConstants.CLASS_MARQUEE_LABEL,MarqueeLabel.class);
			setClassConstructor(GlobalConstants.CLASS_TEXTVIEW,MTextView.class);
			setClassConstructor(GlobalConstants.CLASS_SWITCH,MSwitch.class);
			setClassConstructor(GlobalConstants.CLASS_COUNT,MCount.class);
			setClassConstructor(GlobalConstants.CLASS_DATETIMEPICKER,MDateTimePicker.class);
			setClassConstructor(GlobalConstants.FIELD_DROPLIST,DropList.class);
			
			setClassConstructor(GlobalConstants.FIELD_NAVIGATIONBAR,MNavigationBar.class);
			setClassConstructor(GlobalConstants.FIELD_LISTTABLE,ListTable.class);
			setClassConstructor(GlobalConstants.FIELD_SECTIONLISTTABLE,SectionListTable.class);
			setClassConstructor(GlobalConstants.FIELD_FOLDERVIEW,FolderView.class);
			setClassConstructor(GlobalConstants.FIELD_GRIDTABLE,GridTable.class);
			setClassConstructor(GlobalConstants.FIELD_IMAGESLIDER,ImageSlider.class);
			setClassConstructor(GlobalConstants.FIELD_SCROLLVIEW,ScrollView.class);
			setClassConstructor(GlobalConstants.FIELD_SLIDERVIEW,SliderView.class);
			setClassConstructor(GlobalConstants.FIELD_TABBAR,TabBar.class);
			setClassConstructor(GlobalConstants.FIELD_TOOLBAR,ToolBar.class);
			setClassConstructor(GlobalConstants.FIELD_TOOLBARITEM,BarItemView.class);
			setClassConstructor(GlobalConstants.FIELD_EXPANDABLELISTTABLE,ExpandableListTable.class);
			setClassConstructor(GlobalConstants.FIELD_SECTIONLISTTABLE,SectionListTable.class);

		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
	static public void setClassConstructor(String className,Class Class) throws NoSuchMethodException{
		Constructor<BaseView> constr = Class.getConstructor(BaseFragment.class,GroupView.class,Element.class);
		constrMap.put(className, constr);
	}
	
    public static BaseView newInstance(BaseFragment baseFragment,GroupView parentView,Element mElement) {
        BaseView view = null;
    	long start=System.nanoTime();
        String name = mElement.getNodeName();
        String className=mElement.getAttribute(GlobalConstants.ATTR_CLASS);
        if(TextUtils.isEmpty(className))
        	className=mElement.getAttribute(GlobalConstants.ATTR_FIELD);
        try {
            if(!TextUtils.isEmpty(className)){
            	Constructor<BaseView> constr =constrMap.get(className);
            	if(constr==null){
            		if(className.contains(":"))
            			className=className.replace(":", ".");
            		Class<BaseView> fieldClass = (Class<BaseView>)Class.forName(Configure.spacePackName + ".view."
                            + className);
            		/* 通过反射,获得实例 */
            		constr = fieldClass.getConstructor(BaseFragment.class,GroupView.class,Element.class);
            	}
            	if(constr!=null)
            		view=constr.newInstance(baseFragment,parentView,mElement);
            	else{
    			
            	}
            }else if (GlobalConstants.XML_GROUP.equals(name)) {
            	view=new GroupView(baseFragment, parentView, mElement);
            } else if (GlobalConstants.XML_ITEM.equals(name)) {
            	view=new BaseView(baseFragment, parentView, mElement);
            }
        	long end=System.nanoTime();
        	Statistics.addItemNameandTimeNano(className, end-start);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("instance BaseView "+className+" fail");
        }
        return view;
    }
    
}
