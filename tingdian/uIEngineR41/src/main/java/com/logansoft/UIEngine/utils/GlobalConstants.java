package com.logansoft.UIEngine.utils;

import android.os.Environment;

public class GlobalConstants {
	
	public static final String BOOLEANTRUE="true";
	public static final String BOOLEANFALSE="false";
  /**********************XML parser 节点*******************/ 
	public static final String XML_CONTENT = "content";
	public static final String XML_DATASOURCE = "datasource";
	public static final String XML_RESULT = "result";
	public static final String XML_DATA = "data";
	public static final String XML_PAGE="page";
	public static final String XML_GROUP = "group";
	public static final String XML_ITEM = "item";
	public static final String XML_LUA = "lua";
	public static final String XML_LIST = "list";
	public static final String XML_HIDDEN = "hidden";
	public static final String XML_RESPONSE = "response";
	public static final String XML_RESULT_CODE = "resultCode";
	public static final String XML_RESULT_MSG = "resultMsg";
    public static final String XML_TEMPLATES= "Templates";
    public static final String XML_DIALOG= "dialog";
    public static final String XML_POPUPVIEW= "PopupView";
    public static final String XML_DROPLIST= "droplist";

 
	/**********************XML parser 属性*******************/ 
    public static final String ATTR_CLASS="class";
    public static final String ATTR_FIELD="field";
    public static final String ATTR_ORIENTATION = "orientation";
    public static final String ATTR_HORIZONTAL = "horizontal";
    public static final String ATTR_VERTICAL = "vertical";
    public static final String ATTR_ABSOLUTE = "absolute";

    public static final String ATTR_LEFT="left";
    public static final String ATTR_RIGHT="right";
    public static final String ATTR_TOP="top";
    public static final String ATTR_BOTTOM="bottom";
    public static final String ATTR_CENTER="center";
    public static final String ATTR_CENTER_HORIZONTAL="center_horizontal";
    public static final String ATTR_CENTER_VERTICAL="center_vertical";

    
	public static final String ATTR_LIGHT_COLOR = "lightColor";
	public static final String ATTR_CLICK_COLOR = "clicklightedColor";
    public static final String ATTR_FLUSH = "onFlush";
    public static final String ATTR_DROPFLUSH = "dropFlush";
    public static final String ATTR_IMAGE_GRAVITY="imageGravity";
    public static final String ATTR_STYLE = "style";
    public static final String ATTR_STYLE_DEFAULT = "Default";
    
    public static final String ATTR_CACHE="cache";
    public static final String ATTR_MAX="maxPage";
    public static final String ATTR_INDEX="pageIndex";

    public static final String ATTR_DELETE ="onDelete";
    public static final String ATTR_ACTION="action";
    
    public static final String ACTION_SUBMIT="submit";//提交
    public static final String ACTION_NEXTPAGE="nextPage";//下一页
    
    public static final String ATTR_ON_FLUSH = "onFlush";
    public static final String ATTR_ON_UPDATE = "onUpdate";
    
    
    

    /**********************XML parser field 属性*******************/ 
    public static final String FIELD_HORIZONTAL_SCROOLL_VIEWS="HorizontalScroll";
    public static final String FIELD_GRAPH="weekIncomeChart";
    
    /**********************XML parser class 属性*******************/ 
    public static final String CLASS_LABEL="Label";
    public static final String CLASS_BUTTON="Button";
    public static final String CLASS_IMAGEBUTTON="ImageButton";
    public static final String CLASS_LINE="Line";
    public static final String CLASS_SEARCH="SearchField";
	public static final String CLASS_IMAGE = "Image";
	public static final String CLASS_TEXT_FIELD = "TextField";
	public static final String CLASS_PROGRESS="Progress";
	public static final String CLASS_WEBVIEW = "WebView";
	public static final String CLASS_VIDEOVIEW = "VideoView";
	public static final String CLASS_MARQUEE_LABEL = "MarqueeLabel";
    public static final String CLASS_TEXTVIEW = "TextView";
    public static final String CLASS_SWITCH = "Switch";
    public static final String CLASS_COUNT = "Count";
    public static final String CLASS_DATETIMEPICKER = "TimePickerList";
    
    public static final String FIELD_NAVIGATIONBAR = "NavigationBar";
    public static final String FIELD_LISTTABLE = "ListTable";
    public static final String FIELD_DROPLIST = "DropList";
    public static final String FIELD_SECTIONLISTTABLE = "SectionListTable";
    public static final String FIELD_FOLDERVIEW = "FolderView";
    public static final String FIELD_GRIDTABLE = "GridTable";
    public static final String FIELD_IMAGESLIDER = "ImageSlider";
    public static final String FIELD_SCROLLVIEW = "ScrollView";
    public static final String FIELD_SLIDERVIEW = "SliderView";
    public static final String FIELD_TABBAR = "TabBar";
    public static final String FIELD_TOOLBAR = "ToolBar";
    public static final String FIELD_TOOLBARITEM = "ToolBarItem";
    public static final String FIELD_EXPANDABLELISTTABLE = "ExpandableListTable";
    public static final String FIELD_CATELGORYLABEL = "CatelgoryLabel";

    
    /********************* 路径的干活 *******************/ 
   
	public static String ROOT_PATH = Environment.getExternalStorageDirectory().getPath()+"/";
	public static  String IMAGE_PATH ="";
	public static  String TEMP_PATH = "";
	
	public static  String ROOT_URL = "";
    public static  String UPDATA_URL ="";
    public static  String CONFIG_URL ="";
    public static  String TEMPLATE_URL="";
    public static  String MULTI_TEMPLATES_URL="";
    public static  String TEMPLATES_ENCRYPT="";
    public static final String ATTR_URL="url";
    public static final String URI_SCHEME_RES = "res";
    public static final String URI_SCHEME_LOCAL = "local";
    public static final String URI_SCHEME_HTTP = "http";
    public static final String URI_SCHEME_HTTPS = "https";

    
    public static final String EXTRA_BARCODE = "barcode" ;
    
    /*************************handler**************************************/
    public static final int HANDLER_COMPARE_SUCCEED= 0xf00001;
    public static final int HANDLER_COMPARE_FAILED= 0xf00002;
    
   
}
