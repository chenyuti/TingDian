//ZetaCY

package com.logansoft.UIEngine.Base;

import java.util.ArrayList;

import com.logansoft.UIEngine.parse.xmlview.BaseView;
import com.logansoft.UIEngine.utils.Configure;
import com.logansoft.UIEngine.utils.DisplayUtil;
import com.logansoft.UIEngine.utils.GlobalConstants;
import com.logansoft.UIEngine.utils.StringUtil;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
public class UIEngineGroupView extends ViewGroup {
    public String TAG = "UIEngineGroupView";
    public static final int HORIZONTAL=1;
    public static final int VERTICAL=2;
    public static final int ABSOLUTE=3;
    
    public static final int GravityNone=0;
    public static final int GravityLeft=1<<0;
    public static final int GravityTop=1<<1;
    public static final int GravityRight=1<<2;
    public static final int GravityBottom=1<<3;
    public static final int GravityCenter_vertical=1<<4;
    public static final int GravityCenter_horizontal=1<<5;
    public static final int GravityCenter=GravityCenter_vertical|GravityCenter_horizontal;
    
    public int defaultOrientation;
    public int Orientation;
    
    //only for Navigation,fxck
    public int fixHeight;

    public UIEngineGroupView(Context context) {
        super(context);
        fixHeight=0;
    }
    
    private static final int specWrap;
    static{
    	if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR2){
    		specWrap=MeasureSpec.AT_MOST;
    	}
    	else
    		specWrap=MeasureSpec.EXACTLY;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int paddingLeft=getPaddingLeft(),paddingRight=getPaddingRight(),paddingTop=getPaddingTop(),paddingBottom=getPaddingBottom();
        int x = paddingLeft;
        int y = paddingTop;
        int childCount = getChildCount();

        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if(childView.getVisibility()!=View.VISIBLE)
            	continue;
            UIEngineLayoutParams childParams = (UIEngineLayoutParams)childView.getLayoutParams();
            int[] margin=childParams.getMargin();
            int gravity=childParams.getGravity();
            int measuredHeight=childView.getMeasuredHeight();
            int measuredWidth=childView.getMeasuredWidth();

            int childx=x+margin[0];
            int childy=y+margin[1];
      
            
            if ((gravity&GravityCenter_vertical)==GravityCenter_vertical)
                childy=(bottom-top-measuredHeight-margin[1]-margin[3]-paddingTop-paddingBottom)/2+margin[1]+paddingTop;
            if ((gravity&GravityCenter_horizontal)==GravityCenter_horizontal)
                childx=(right-left-measuredWidth-margin[0]-margin[2]-paddingLeft-paddingRight)/2+margin[0]+paddingLeft;
            
            if((gravity&GravityLeft)==GravityLeft)
                childx=paddingLeft+margin[0];
            else if((gravity&GravityRight)==GravityRight)
                childx=right-left-paddingRight-measuredWidth-margin[2];       
            if ((gravity&GravityTop)==GravityTop)
                childy=paddingTop+margin[1];
            else if ((gravity&GravityBottom)==GravityBottom)
                childy=bottom-top-paddingBottom-measuredHeight-margin[3];
        
            childView.layout(childx,childy,childx+measuredWidth,childy+measuredHeight);
            if (Orientation == VERTICAL)
                y+=(measuredHeight+margin[1]+margin[3]);
            else if(Orientation==HORIZONTAL)
                x+=(measuredWidth+margin[0]+margin[2]);

        }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	UIEngineLayoutParams Myparams=null;
    	LayoutParams p=getLayoutParams();
    	if(!(p instanceof UIEngineLayoutParams))
        	Myparams=new UIEngineLayoutParams(p);
    	else
        	Myparams=(UIEngineLayoutParams)getLayoutParams();
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        ViewParent superViewParent=this.getParent();
        View superView=null;if(superViewParent instanceof View)superView=(View) superViewParent;
        if(Myparams.width<0 && widthSize==0)
        	widthSize=superView.getMeasuredWidth();
        if(Myparams.height<0 && heightSize==0)
        	heightSize=superView.getMeasuredHeight();        
        	
        int verticalpadding = this.getPaddingTop() + this.getPaddingBottom();
        int horizontalpadding = this.getPaddingLeft() + this.getPaddingRight();
        if(widthSize>0)widthSize=fixSizeLimit(widthSize,Myparams.minWidth,Myparams.maxWidth);
        if(heightSize>0)heightSize=fixSizeLimit(heightSize,Myparams.minHeight,Myparams.maxHeight);
        int availableWidth=widthSize-horizontalpadding;
        int availableHeight=heightSize-verticalpadding;

        // only use when wrap
        int myWidth = 0;
        int myHeight = 0;        	

        // Step2ElementArray为：
        // 垂直布局中，group的width为wrap时，需要计算第一遍获取子控件最大width修正自身width后，再计算根据group
        // width计算width的子控件的数组。
        // 水平布局同理
        ArrayList<View> Step2ElementArray = new ArrayList<View>();

        ArrayList<View> fillelement = new ArrayList<View>();
        int childCount = getChildCount();

        for (int i = 0; i < childCount; i++) {
            View childView=getChildAt(i);
            if(childView.getVisibility()!=View.VISIBLE)
            	continue;
            UIEngineLayoutParams childparams = (UIEngineLayoutParams)childView.getLayoutParams();
            
            int elementwidth = childparams.width;
            int elementheight = childparams.height;
            
            if ((Myparams.height==LayoutParams.WRAP_CONTENT && elementheight==LayoutParams.MATCH_PARENT)
                    || (Myparams.width==LayoutParams.WRAP_CONTENT && elementwidth==LayoutParams.MATCH_PARENT)) {
                Step2ElementArray.add(childView);
                continue;
            }
            if (Orientation!=ABSOLUTE &&
            	(Orientation==VERTICAL && elementheight==LayoutParams.MATCH_PARENT && childparams.getHeightMode()!=UIEngineLayoutParams.PRECENT)
            	|| (Orientation==HORIZONTAL && elementwidth==LayoutParams.MATCH_PARENT && childparams.getWidthMode()!=UIEngineLayoutParams.PRECENT)) {
                fillelement.add(childView);
                continue;
            }            	
            childparams.measureMargin(widthSize, heightSize);
            elementwidth=childparams.getMeasureWidth(widthSize,availableWidth);
            elementheight=childparams.getMeasureHeight(heightSize,availableHeight);
            int[] childMargin=childparams.getMargin();
            setChildMeasure(childView,elementwidth,elementheight,availableWidth,availableHeight,childMargin);
            int childOccupationWidth = childView.getMeasuredWidth()+childMargin[0]+childMargin[2];
            int childOccupationHeight = childView.getMeasuredHeight()+childMargin[1]+childMargin[3];
            
            switch(Orientation){
            	case VERTICAL:
            		availableHeight-=childOccupationHeight;
            		if(Myparams.width==LayoutParams.WRAP_CONTENT){
            			if(myWidth<childOccupationWidth)
            				myWidth=childOccupationWidth;
            		}
            		if(Myparams.height==LayoutParams.WRAP_CONTENT)
            			myHeight+=childOccupationHeight;
            		break;
            	case HORIZONTAL:
            		availableWidth-=childOccupationWidth;
            		if(Myparams.height==LayoutParams.WRAP_CONTENT){
            			if (myHeight<childOccupationHeight) 
            				myHeight=childOccupationHeight;
            		}
            		if (Myparams.width==LayoutParams.WRAP_CONTENT) 
            			myWidth+=childOccupationWidth;
            		break;
            	case ABSOLUTE:
            		if(Myparams.height==LayoutParams.WRAP_CONTENT){
            			if (myHeight<childOccupationHeight) 
            				myHeight=childOccupationHeight;
            		}
            		if(Myparams.width==LayoutParams.WRAP_CONTENT){
            			if(myWidth<childOccupationWidth)
            				myWidth=childOccupationWidth;
            		}
            		break;
            }
        }

        for (View childView : Step2ElementArray) {
            UIEngineLayoutParams childparams = (UIEngineLayoutParams)childView.getLayoutParams();
            childparams.measureMargin(myWidth!=0?myWidth:widthSize,myHeight!=0?myHeight:heightSize);
            int[] childMargin=childparams.getMargin();
            int elementwidth=childparams.getMeasureWidth(myWidth!=0?myWidth:widthSize,availableWidth);
            int elementheight=childparams.getMeasureHeight(myHeight!=0?myHeight:heightSize,availableHeight);
            setChildMeasure(childView,elementwidth,elementheight,availableWidth,availableHeight,childMargin);
            int childOccupationWidth=childView.getMeasuredWidth()+childMargin[0]+childMargin[2];
            int childOccupationHeight=childView.getMeasuredHeight()+childMargin[1]+childMargin[3];
            switch(Orientation){
            case VERTICAL:
                availableHeight-=childOccupationHeight;
            	break;
            case HORIZONTAL:
                availableWidth-=childOccupationWidth;
            	break;
            case ABSOLUTE:
            	break;
            }
        }
    
        if (fillelement.size() != 0) {
            if(Orientation==VERTICAL) 
                availableHeight/=fillelement.size();
            else if(Orientation==HORIZONTAL) 
                availableWidth/=fillelement.size();
            
            for (View childView:fillelement) {
                UIEngineLayoutParams childparams = (UIEngineLayoutParams)childView.getLayoutParams();
                childparams.measureMargin(myWidth!=0?myWidth:widthSize,myHeight!=0?myHeight:heightSize);
                int[] childMargin=childparams.getMargin();
                int elementwidth=childparams.getMeasureWidth(myWidth!=0?myWidth:widthSize,availableWidth);
                int elementheight=childparams.getMeasureHeight(myHeight!=0?myHeight:heightSize,availableHeight);
                if (Orientation==VERTICAL && (childparams.width != LayoutParams.MATCH_PARENT))
                	elementwidth=childparams.width;
                else if(Orientation==HORIZONTAL && (childparams.height != LayoutParams.MATCH_PARENT))
                	elementheight=childparams.height;                
                setChildMeasure(childView,elementwidth,elementheight,availableWidth,availableHeight,childMargin);
                int childOccupationWidth=childView.getMeasuredWidth()+childMargin[0]+childMargin[2];
                int childOccupationHeight=childView.getMeasuredHeight()+childMargin[1]+childMargin[3];
                if (Orientation == VERTICAL && Myparams.width==LayoutParams.WRAP_CONTENT) {
                    if (myWidth<childOccupationWidth) 
                        myWidth=childOccupationWidth;
                } else if (Orientation == HORIZONTAL && Myparams.height==LayoutParams.WRAP_CONTENT) {
                    if (myHeight<childOccupationHeight) 
                        myHeight=childOccupationHeight;
                }
            }
        }
        if (Myparams.width == LayoutParams.WRAP_CONTENT)
            widthSize = myWidth + horizontalpadding;
        if (Myparams.height == LayoutParams.WRAP_CONTENT)
            heightSize = myHeight + verticalpadding;

        setMeasuredDimension(widthSize,heightSize);
    }
    protected void setChildMeasure(View childView,int elementwidth,int elementheight,int availableWidth,int availableHeight,int[] childMargin){
         int availableWidthMeasureSpec,availableHeightMeasureSpec;
         UIEngineLayoutParams childparams = (UIEngineLayoutParams)childView.getLayoutParams();
         
         if(elementwidth==LayoutParams.WRAP_CONTENT)
        	 availableWidthMeasureSpec=MeasureSpec.makeMeasureSpec(elementwidth,specWrap);
         else{
        	 elementwidth=fixSizeLimit(elementwidth,childparams.minWidth,childparams.maxWidth);
        	 availableWidthMeasureSpec=MeasureSpec.makeMeasureSpec(elementwidth,MeasureSpec.EXACTLY);
         }
         if(elementheight==LayoutParams.WRAP_CONTENT)	
        	 availableHeightMeasureSpec = MeasureSpec.makeMeasureSpec(elementheight,specWrap);
         else{
        	 elementheight=fixSizeLimit(elementheight,childparams.minHeight,childparams.maxHeight);
        	 availableHeightMeasureSpec = MeasureSpec.makeMeasureSpec(elementheight,MeasureSpec.EXACTLY);
         }
         childView.measure(availableWidthMeasureSpec, availableHeightMeasureSpec);         
         int childOccupationWidth=childView.getMeasuredWidth()+childMargin[0]+childMargin[2];
         int childOccupationHeight=childView.getMeasuredHeight()+childMargin[1]+childMargin[3];
         boolean needRetry=false;
         if(elementwidth==LayoutParams.WRAP_CONTENT && availableWidth>0 && childOccupationWidth>availableWidth){
         	elementwidth=availableWidth-childMargin[0]-childMargin[2];
         	needRetry=true;
         	if(elementwidth<0)elementwidth=0;
         }
         int tempWidth=fixSizeLimit(elementwidth,childparams.minWidth,childparams.maxWidth);
         if(tempWidth!=elementwidth){
        	 elementwidth=tempWidth;
        	 needRetry=true;
         }
         if(elementheight==LayoutParams.WRAP_CONTENT && availableHeight>0 && childOccupationHeight>availableHeight){
         	elementheight = availableHeight-childMargin[1]-childMargin[3];
         	needRetry=true;
         	if(elementheight<0)elementheight=0;
         }
         int tempHeight=fixSizeLimit(elementheight,childparams.minHeight,childparams.maxHeight);
         if(tempHeight!=elementheight){
        	 elementheight=tempHeight;
        	 needRetry=true;
         }
         if(needRetry){
             setChildMeasure(childView,elementwidth,elementheight,availableWidth,availableHeight,childMargin);
             childOccupationWidth = childView.getMeasuredWidth()+childMargin[0]+childMargin[2];
             childOccupationHeight = childView.getMeasuredHeight()+childMargin[1]+childMargin[3];
         }
    }
   
    private int fixSizeLimit(int size,int minSize,int maxSize){
    	if(minSize>0 && size<minSize)return minSize;
    	if(maxSize>0 && size>maxSize)return maxSize;
    	return size;
    }
    public void setOrientation(String orientationString) {
    	Orientation=defaultOrientation;
    	if(orientationString==null || orientationString.length()==0)
    		return;
    	if (GlobalConstants.ATTR_HORIZONTAL.equalsIgnoreCase(orientationString)) 
    		Orientation=HORIZONTAL;
    	else if(GlobalConstants.ATTR_VERTICAL.equalsIgnoreCase(orientationString))
    		Orientation=VERTICAL;
    	else if(GlobalConstants.ATTR_ABSOLUTE.equalsIgnoreCase(orientationString))
    		Orientation=ABSOLUTE;
    	
    }
    public static class UIEngineLayoutParams extends ViewGroup.LayoutParams{
    	protected static final int MODE_SHIFT=2;
        protected static final int NUMBER=1;
        protected static final int PRECENT=2;
        protected static final int MODE_MASK=3;
        protected static final int defsize=makeSpec(0,NUMBER);
        
        //only for Navigation,fxck
        public int fixHeight;

        //32byte
    	private int marginLeft,marginTop,marginRight,marginBottom;
    	private int measureMarginLeft,measureMarginTop,measureMarginRight,measureMarginBottom;
        
    	//52byte
        private int Gravity;
        public int defaultWidth,defaultHeight;
        public int minWidth,maxWidth,minHeight,maxHeight;
        public int measureMinWidth,measureMaxWidth,measureMinHeight,measureMaxHeight;
        public int widthPrecent,heightPrecent;
      
        private void init(){
        	minWidth=0;maxWidth=0;minHeight=0;maxHeight=0;
			widthPrecent=defsize;heightPrecent=defsize;
			marginLeft=defsize;marginTop=defsize;marginRight=defsize;marginBottom=defsize;
			measureMarginLeft=measureMarginTop=measureMarginRight=measureMarginBottom=0;
			measureMinWidth=measureMaxWidth=measureMinHeight=measureMaxHeight=0;
			Gravity=GravityNone;
        }
		public UIEngineLayoutParams(LayoutParams params){
			super(params);
			init();
			if(params instanceof ViewGroup.MarginLayoutParams){
				MarginLayoutParams mp=(MarginLayoutParams) params;
				marginLeft=makeSpec(mp.leftMargin,NUMBER);marginTop=makeSpec(mp.topMargin,NUMBER);
				marginRight=makeSpec(mp.rightMargin,NUMBER);marginBottom=makeSpec(mp.bottomMargin,NUMBER);
			}
			else if(params instanceof UIEngineLayoutParams){
				UIEngineLayoutParams src=(UIEngineLayoutParams) params;
				marginLeft=src.marginLeft;marginTop=src.marginTop;
				marginRight=src.marginRight;marginBottom=src.marginBottom;
				measureMarginLeft=src.measureMarginLeft;measureMarginTop=src.measureMarginTop;
				measureMarginRight=src.measureMarginRight;measureMarginBottom=src.marginBottom;
				Gravity=src.Gravity;
				defaultWidth=src.defaultWidth;defaultHeight=src.defaultHeight;
				minWidth=src.minWidth;maxWidth=src.maxWidth;minHeight=src.minHeight;maxHeight=src.maxHeight;
				widthPrecent=src.widthPrecent;heightPrecent=src.heightPrecent;	
				measureMinWidth=src.measureMinWidth;measureMaxWidth=src.measureMaxWidth;
				measureMinHeight=src.measureMinHeight;measureMaxHeight=src.measureMaxHeight;

			}
			//if(params instanceof FrameLayout.LayoutParams){
				//FrameLayout.LayoutParams fp=(FrameLayout.LayoutParams) params;
				//fp.gravity;
			//}
			
		}
        public UIEngineLayoutParams(int width, int height) {
			super(width, height);
			init();
		}
		
		public void setMargin(int[] margin){
			if(margin.length==4){
				marginLeft=makeSpec(margin[0],NUMBER);marginTop=makeSpec(margin[1],NUMBER);
				marginRight=makeSpec(margin[2],NUMBER);marginBottom=makeSpec(margin[3],NUMBER);
			}
			else{
				marginLeft=defsize;marginTop=defsize;marginRight=defsize;marginBottom=defsize;
			}
    		measureMargin(Configure.screenWidth,Configure.screenHeight);
    	}
		public float getHorizontalMargin(){return measureMarginLeft+measureMarginRight;}
		public float getVerticalMargin(){return measureMarginTop+measureMarginBottom;}
    	public int[] getMargin(){
    		return new int[]{measureMarginLeft,measureMarginTop,measureMarginRight,measureMarginBottom};
    	}
    	
    	public void setMarginString(String marginString,Context context){
			marginLeft=defsize;marginTop=defsize;marginRight=defsize;marginBottom=defsize;
			measureMarginLeft=measureMarginTop=measureMarginRight=measureMarginBottom=0;
	        if(marginString==null || marginString.length()==0)
    			return;
    		String[] margins = marginString.split(",");
    		if(margins==null || margins.length!=4)
    			return;
    		marginLeft=parseLayoutParamsSingleSizeString(margins[0],context);
    		marginTop=parseLayoutParamsSingleSizeString(margins[1],context);
    		marginRight=parseLayoutParamsSingleSizeString(margins[2],context);
    		marginBottom=parseLayoutParamsSingleSizeString(margins[3],context);
    		measureMargin(Configure.screenWidth,Configure.screenHeight);
    	}
    	public void setSizeString(String sizeString,Context context){
    		widthPrecent=defsize;heightPrecent=defsize;
			width=defaultWidth;height=defaultHeight;
    		if(sizeString==null || sizeString.length()==0)
    			return;
    		String[] s = sizeString.split(",");
    		if(s==null || s.length!=2)
    			return;
    		int w=parseLayoutParamsSingleSizeString(s[0],context);
    		int h=parseLayoutParamsSingleSizeString(s[1],context);
			widthPrecent=w;
			heightPrecent=h;
    		if(getSpecMode(w)==NUMBER) width=getSpecSize(w);
    		else width=LayoutParams.MATCH_PARENT;
    		if(getSpecMode(h)==NUMBER) height=getSpecSize(h);
    		else height=LayoutParams.MATCH_PARENT;
    	}
    	public void setLimitSizeString(Context mContext,String minWidthString,String maxWidthString,String minHeightString,String maxHeightString){
	        minWidth=0;maxWidth=0;minHeight=0;maxHeight=0;
			measureMinWidth=measureMaxWidth=measureMinHeight=measureMaxHeight=0;
    		if(!StringUtil.isEmpty(minWidthString))minWidth=parseSingleMarginOrPaddingString(minWidthString,mContext,0);
    		if(!StringUtil.isEmpty(maxWidthString))maxWidth=parseSingleMarginOrPaddingString(maxWidthString,mContext,1);
    		if(!StringUtil.isEmpty(minHeightString))minHeight=parseSingleMarginOrPaddingString(minHeightString,mContext,2);
    		if(!StringUtil.isEmpty(maxHeightString))maxHeight=parseSingleMarginOrPaddingString(maxHeightString,mContext,3);
    	}
    	public int getMeasureWidth(int fatherWidth,int availableWidth){
    		int result=width;
    		if (width==LayoutParams.MATCH_PARENT){
    			result=(getSpecMode(widthPrecent)==NUMBER)?(availableWidth-measureMarginLeft-measureMarginRight)
             			:(int)(fatherWidth*((float)getSpecSize(widthPrecent)/10000));
    		}
    		return result;
    	}
    	public int getMeasureHeight(int fatherHeight,int availableHeight){
    		int result=height;
    		if (height==LayoutParams.MATCH_PARENT){
    			result=(getSpecMode(heightPrecent)==NUMBER)?(availableHeight-measureMarginTop-measureMarginBottom)
             			:(int)(fatherHeight*((float)getSpecSize(heightPrecent)/10000));
            }
    		return result+fixHeight;
    	}
    	public void measureMargin(int fatherWidth,int fatherHeight){
    		measureMarginLeft=_measureSize(marginLeft,fatherWidth);
    		measureMarginTop=_measureSize(marginTop,fatherHeight)-fixHeight;
    		measureMarginRight=_measureSize(marginRight,fatherWidth);
    		measureMarginBottom=_measureSize(marginBottom,fatherHeight);
    	}
    	private int _measureSize(int size,int father){
    		if(getSpecMode(size)==NUMBER)
    			return getSpecSize(size);
    		else
    			return (int)(((float)father*getSpecSize(size))/10000);
    	}
    	public void measureLimitSize(int fatherWidth,int fatherHeight){
    		measureMinWidth=_measureSize(minWidth,fatherWidth);
    		measureMaxWidth=_measureSize(maxWidth,fatherWidth);
    		measureMinHeight=_measureSize(minHeight,fatherHeight);
    		measureMaxHeight=_measureSize(maxHeight,fatherHeight);
    	}
		public void setGravityString(String gravityString) {
    		Gravity=GravityNone;
    		if (!StringUtil.isEmpty(gravityString)) {
    			String[] gravities = gravityString.split("\\|");
    			for (int i = 0; i < gravities.length; i++){
    				if (GlobalConstants.ATTR_LEFT.equals(gravities[i])) 
    					Gravity|=UIEngineGroupView.GravityLeft;
    				else if(GlobalConstants.ATTR_CENTER_HORIZONTAL.equals(gravities[i])) 
    					Gravity|=UIEngineGroupView.GravityCenter_horizontal;
    				else if(GlobalConstants.ATTR_RIGHT.equals(gravities[i]))
    					Gravity|=UIEngineGroupView.GravityRight;
    				else if(GlobalConstants.ATTR_TOP.equals(gravities[i])) 
    					Gravity|=UIEngineGroupView.GravityTop;
    				else if(GlobalConstants.ATTR_BOTTOM.equals(gravities[i]))
    					Gravity|=UIEngineGroupView.GravityBottom;
    				else if(GlobalConstants.ATTR_CENTER.equals(gravities[i])) 
    					Gravity|=UIEngineGroupView.GravityCenter;
    				else if(GlobalConstants.ATTR_CENTER_VERTICAL.equals(gravities[i])) 
    					Gravity|=UIEngineGroupView.GravityCenter_vertical;
    			}
    		}
    	}
    	public void setGravity(int gravity){Gravity|=gravity;}
    	public void removeGravity(int gravity){Gravity&=(~gravity);}
    	public int getGravity(){return Gravity;}
    	public int getWidthMode(){return getSpecMode(widthPrecent);}
    	public int getHeightMode(){return getSpecMode(heightPrecent);}
    	
    	public static int parseSingleMarginOrPaddingString(String sizeString,Context context,int i){
    		int result;
    		if (sizeString.startsWith("pageWidth")){
				String a = sizeString.replace("pageWidth*","");
				String e = a.replaceAll("%", "");
				float c = Float.parseFloat(e)/100;
				result = (int)(c * Configure.screenWidth);
			} else if (sizeString.startsWith("pageHeight")){
				String a = sizeString.replace("pageHeight*", "");
				String e = a.replaceAll("%", "");
				float c = Float.parseFloat(e)/100;
				result = (int)(c * Configure.screenHeight);
			} else if (sizeString.endsWith("%")) {
				String a = sizeString.replaceAll("%", "");
				float c = Float.parseFloat(a)/100;
				result = (int)(c * (i % 2 == 0 ? Configure.screenWidth:Configure.screenHeight));
			} else {
				result = DisplayUtil.dip2px(context, Float.parseFloat(sizeString));
			}
    		return result;
    	}
    	public static int parseSingleSizeString(String sizeString,Context context,int i){
    		int result;    			
    		if (BaseView.WRAP.equals(sizeString)) 
    			result = LayoutParams.WRAP_CONTENT;
    		else if (BaseView.FILL.equals(sizeString)) 
    			result = LayoutParams.MATCH_PARENT;	
    		else
    			result=parseSingleMarginOrPaddingString(sizeString,context,i);
    		return result;
    	}
    	private int parseLayoutParamsSingleMarginSizeStirng(String sizeString,Context context){
    		if (sizeString.startsWith("pageWidth")){
    			String a = sizeString.replace("pageWidth*","");
    			String e = a.replaceAll("%", "");
    			float c = Float.parseFloat(e)/100;
    			return makeSpec((int)(c*Configure.screenWidth),NUMBER);
    		} else if (sizeString.startsWith("pageHeight")){
    			String a = sizeString.replace("pageHeight*", "");
    			String e = a.replaceAll("%", "");
    			float c = Float.parseFloat(e)/100;
    			return makeSpec((int)(c*Configure.screenHeight),NUMBER);
    		} else if (sizeString.endsWith("%")){
    			String a = sizeString.replaceAll("%", ""); 
    			return makeSpec((int)(Float.parseFloat(a)*100),PRECENT);
       		} else 
    			return makeSpec(DisplayUtil.dip2px(context, Float.parseFloat(sizeString)),NUMBER);
    	}
    	private int parseLayoutParamsSingleSizeString(String sizeString,Context context){
    		if (BaseView.WRAP.equals(sizeString))
    			return makeSpec(LayoutParams.WRAP_CONTENT,NUMBER);
    		else if (BaseView.FILL.equals(sizeString))
    			return makeSpec(LayoutParams.MATCH_PARENT,NUMBER);
    		else 
    			return parseLayoutParamsSingleMarginSizeStirng(sizeString,context);
    	}
    	static private int makeSpec(int size,int mode){
    		return (size<<MODE_SHIFT)|(mode&MODE_MASK);
    	}
    	static private int getSpecSize(int Spec){
    		return Spec>>MODE_SHIFT;
    	}
    	static private int getSpecMode(int Spec){
    		return (Spec&MODE_MASK);
    	}
    }
    
}
