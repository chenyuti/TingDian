package com.logansoft.UIEngine.parse.field;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.xmlview.BaseView;
import com.logansoft.UIEngine.parse.xmlview.GroupView;
import com.logansoft.UIEngine.utils.DisplayUtil;
import com.logansoft.UIEngine.utils.GlobalConstants;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public class SliderView extends GroupView{
	SlidingMenu menu ;
    private BaseView mContentView;
    private BaseView mMenuView;
    private BaseView mSecondaryMenu;

    public static  final String CONTENT ="center";
    public static  final String LEFT="left";
    public static  final String RIGHT="right";
    public static  final String SITE="site";
    
    public SliderView(BaseFragment fragment,GroupView parentView,Element element) {
        super(fragment,parentView,element);
    }
    @Override 
    protected void createMyView(){
        mView =menu=new SlidingMenu(mContext){
    		@Override
    		public void draw(Canvas canvas) {
    			bdraw(canvas);
    			super.draw(canvas);
    			adraw(canvas);
    		}
    	};
    }
    @Override
    protected void parseView() {
    	super.parseView();
//        menu.setContent(R.layout.activity_text);  //设置中间的view
//        menu.setMenu(R.layout.activity_text2);    //设置右边的view
        setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN); 
//        setShadowDrawable(null);   //设置阴影位图
        setShadowWidth(DisplayUtil.dip2px(mContext, 10));//设置阴影宽度
        setBehindOffset(DisplayUtil.dip2px(mContext, 100)); //设置contentView的可见款宽度
        setFadeDegree(0.25f);     //淡出时的阴影
        setMode(SlidingMenu.RIGHT);  //设置阴影显示位置
    }
    
    @Override
    protected void parseSubView(){
    	/* 创建 List 用来保存 子元素 */
        NodeList groupNodes = mElement.getChildNodes();
        boolean existLeft=false;
        for (int a = 0; a < groupNodes.getLength(); a++) {
            Node groupNode = groupNodes.item(a);
            /* 如果这个节点是一个可用的元素,添加进集合 */
            if (groupNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element)groupNode;
                if(GlobalConstants.XML_GROUP.equals(groupNode.getNodeName())){
                    BaseView baseview = new GroupView(baseFragment,this,childElement);
                    if(baseview!=null){
                         String site = childElement.getAttribute(SITE);
                         if (CONTENT.equals(site)) {
                             setContentView(baseview);
                         }else if (LEFT.equals(site)) {
                             existLeft=true ;
                             setMenuView(baseview);
                             setMode(SlidingMenu.LEFT);
                         }else if (RIGHT.equals(site)){
                             if (existLeft) {
                                 setSecondaryMenu(baseview);
                                 setMode(SlidingMenu.LEFT_RIGHT);
                             }else {
                                setMenuView(baseview);
                                setMode(SlidingMenu.RIGHT);
                             }
                         }else {
                            menu.addView(baseview.getView());
                         }
                    }
                }
            }
        }
    }
   
    
    
    public void ShowRightSlideView(){
        if (menu.isMenuShowing()) {
            menu.showContent();
        }else {
            menu.showMenu();
        }
    }
    public void ShowCenterView(){
            menu.showContent();
    }
    
    public void  setShadowWidth(int width){
        menu.setShadowWidth(width);//设置阴影宽度
    }
    
    public void setTouchModeAbove(int slidingMenuTouchMode){
        menu.setTouchModeAbove(slidingMenuTouchMode); 
    }
    
    public void setShadowDrawable(Drawable shadow){
        menu.setShadowDrawable(shadow);   //设置阴影位图
    }
    
    public void setShadowDrawable(int  resShadow){
        menu.setShadowDrawable(resShadow);   //设置阴影位图
    }
    
    /**
     * 从资源文件中获取宽度
     * @param offset
     */
    public void setBehindOffsetRes(int offset){
        menu.setBehindOffsetRes(offset); //设置contentView的可见款宽度
    }
    
    /**
     * 设置content可见宽度
     * @param offset
     */
    public void setBehindOffset(int offset){
        menu.setBehindOffset(offset); //设置contentView的可见款宽度
    }
    
    /**
     * 设置contentView
     */
    public void setContentView(BaseView contentView){
        mContentView=contentView;
        menu.setContent(contentView.getView());
    }
    /**
     * 设置MenuView
     */
    public void setMenuView(BaseView contentView){
        mMenuView=contentView;
        menu.setMenu(contentView.getView());
    }
    /**
     * 设置 SecondaryMenu
     */
    public void setSecondaryMenu(BaseView contentView){
        mSecondaryMenu=contentView;
        menu.setSecondaryMenu(contentView.getView());
    }
    
    /**
     * 获取contentView
     */
    public BaseView getContentView(){
        return mContentView;
    }
    /**
     * 获取MenuView
     */
    public BaseView getMenuView(){
       return mMenuView;
    }
    /**
     * 获取 SecondaryMenu
     */
    public BaseView getSecondaryMenu(){
        return mSecondaryMenu;
    }
    
    
    /**
     * 设置第二个Menu的阴影位图
     */
    public void setSecondaryMenu(Drawable shadow){
        menu.setSecondaryShadowDrawable(shadow);
    }
    
    /**
     * 设置第二个Menu的阴影位图
     * @param resShadow
     */
    public void setSecondaryShadowDrawable(int  resShadow){
        menu.setSecondaryShadowDrawable(resShadow);   //设置阴影位图
    }
    /**
     * 设置 menuView的格式->左 or 右
     * @param SlidingMenuMode
     */
    public void setMode(int SlidingMenuMode){
        menu.setMode(SlidingMenuMode);   //设置Menu在右边
    }
    
    public void setFadeDegree(float alpha){
        menu.setFadeDegree(alpha);     //淡出时的阴影
    }
}
