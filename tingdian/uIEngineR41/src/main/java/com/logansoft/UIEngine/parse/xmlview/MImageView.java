
package com.logansoft.UIEngine.parse.xmlview;

import org.w3c.dom.Element;

import android.graphics.Canvas;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.logansoft.UIEngine.Base.UIEngineDrawable;
import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.utils.LogUtil;
import com.logansoft.UIEngine.view.RotateImage;
import com.squareup.picasso.Picasso;

public class MImageView extends BaseView {
    public static final String SCALE_TYPE = "scaleType";
    public static final String CENTER = "center"; // 按图片的原来size居中显示，当图片长/宽超过View的长/宽，则截取图片的居中部分显示
    public static final String CENTER_CROP = "centerCrop"; // 按比例扩大图片的size居中显示，使得图片长(宽)等于或大于View的长(宽)
    public static final String CENTER_INSIDE = "centerInside"; // 将图片的内容完整居中显示，通过按比例缩小或原来的size使得图片长/宽等于或小于View的长/宽
    public static final String FIT_CENTER = "fitCenter"; // 把图片按比例扩大/缩小到View的宽度，居中显示
    public static final String FIT_END = "fitEnd"; // 把图片按比例扩大/缩小到View的宽度，显示在View的下部分位置
    public static final String FIT_START = "fitStart"; // 把图片按比例扩大/缩小到View的宽度，显示在View的上部分位置
    public static final String FIT_XY = "fitXY"; // 把图片不按比例扩大/缩小到View的大小显示
    public static final String MATRIX = "matrix"; // 用矩阵来绘制

    protected UIEngineDrawable imageDrawable;
    protected boolean needResume;
   
    public MImageView(BaseFragment fragment,GroupView parentView,Element element) {
        super(fragment,parentView,element);
    }
    @Override
    protected void createMyView(){
        mView = new RotateImage(mContext){
    		@Override
    		public void draw(Canvas canvas) {
    			bdraw(canvas);
    			super.draw(canvas);
    			adraw(canvas);
    		}
    	};
    }
    @Override 
    protected void parseView(){
    	super.parseView();
     	imageDrawable=new UIEngineDrawable(mContext);
       	((ImageView)mView).setImageDrawable(imageDrawable);
    }
    @Override
    protected void parseAttributes(){
        super.parseAttributes();
        setScaleType((ImageView)mView);
        setImage((ImageView)mView, attrMap);
    }

    public String getImagePath(){return imageDrawable.getImageURLForState(UIEngineDrawable.StateNormal);}
  
    protected void setScaleType(ImageView imageView) {
        ScaleType st = ScaleType.FIT_XY;
        String scaleType = attrMap.get(SCALE_TYPE);
        if (CENTER.equals(scaleType)) {
            st = ScaleType.CENTER;
        } else if (CENTER_CROP.equals(scaleType)) {
            st = ScaleType.CENTER_CROP;
        } else if (CENTER_INSIDE.equals(scaleType)) {
            st = ScaleType.CENTER_INSIDE;
        } else if (FIT_CENTER.equals(scaleType)) {
            st = ScaleType.FIT_CENTER;
        } else if (FIT_END.equals(scaleType)) {
            st = ScaleType.FIT_END;
        } else if (FIT_START.equals(scaleType)) {
            st = ScaleType.FIT_START;
        } else if (MATRIX.equals(scaleType)) {
            st = ScaleType.MATRIX;
        } else if (FIT_XY.equals(scaleType)) {
            st = ScaleType.FIT_XY;
        }
        imageView.setScaleType(st);
        
    }

    @Override
    public void setValue(String value){
    	setImage(value);
    }
    @Override 
    public String getValue(){
    	return value;
    }
    public void setImage(String imageURL){
    	value=imageURL;
    	parseImage((ImageView)mView,UIEngineDrawable.StateNormal,imageURL);
    }  
    //加载网络图片
    @Override
    public void loadImage(String url) {
    	LogUtil.e("loadImage执行了,开始加载网络图片=="+url);
    	Picasso.with(mContext).load(url).into((ImageView)mView);
    }

    @Override
    public void setCornerRadiusString(String cornerRadiusString){
    	super.setCornerRadiusString(cornerRadiusString);
    	imageDrawable.setCornerRadius(cornerRadiusString, mContext);
    }

    public void onLowMemory(){
    	super.onLowMemory();
    	if(!baseFragment.isViewAppearance()){
    		needResume=true;
        	parseImage((ImageView)mView,UIEngineDrawable.StateNormal,null);
    	}
    }
    public void viewWillAppear(){
    	super.viewWillAppear();
    	if(needResume){
        	parseImage((ImageView)mView,UIEngineDrawable.StateNormal,value);
    	}	
		needResume=false;
    }
    public void viewDidDisappear(){
    	super.viewDidDisappear();
    }
    public void Destory(){
    	((ImageView)mView).setImageDrawable(null);
    	super.Destory();
    }
    
}
