
package com.logansoft.UIEngine.parse.xmlview.Text;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import com.logansoft.UIEngine.Base.UIEngineColorParser;
import com.logansoft.UIEngine.Base.UIEngineDrawable;
import com.logansoft.UIEngine.Base.UIEngineGroupView.UIEngineLayoutParams;
import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.keyboard.DkkjPWDEditText;
import com.logansoft.UIEngine.keyboard.KeyboardInstance;
import com.logansoft.UIEngine.keyboard.ParserKeyboard;
import com.logansoft.UIEngine.parse.xmlview.GroupView;
import com.logansoft.UIEngine.utils.Configure;
import com.logansoft.UIEngine.utils.DisplayUtil;
import com.logansoft.UIEngine.utils.LogUtil;
import com.logansoft.UIEngine.utils.http.RequestListener;
import com.logansoft.UIEngine.utils.http.RequestManager;
import com.logansoft.UIEngine.utils.http.RequestOptions;
import com.logansoft.UIEngine.utils.ninePatch.NinePatchChunk;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.text.method.NumberKeyListener;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MTextField extends MLabel {
	public final static String ATTR_LEFTIMAGE="leftImage";
	public final static String ATTR_CLEARBUTTON="clearButton";
    public static final String ATTR_PLACEHOLDERCOLOR= "placeHolderColor";
	protected UIEngineDrawable leftImageDrawable;
	protected UIEngineDrawable rightImageDrawable;

	
	private boolean showClearButton;
    
    public MTextField(BaseFragment fragment,GroupView parentView,Element element) {
        super(fragment,parentView,element);
    }
    @Override
    protected void createMyView(){
    	 String encrypionType = attrMap.get(ATTR_ENCRYPTION_TYPE);
         if (attrMap.containsKey(ATTR_INPUTTYPE)) {
             inputType = attrMap.get(ATTR_INPUTTYPE);
         }
         if (INPUTTYP_PASSWORD.equals(encrypionType)) {
             if (!INPUTTYP_NUMBER.equals(inputType)) {
                 inputType = INPUTTYP_PASSWORD;
             }
             DkkjPWDEditText View = new DkkjPWDEditText(baseFragment.mHandler,mContext, DkkjPWDEditText.PassWord, inputType){
            	 @Override
            	 public void draw(Canvas canvas) {
            		 bdraw(canvas);
            		 super.draw(canvas);
            		 adraw(canvas);
            	 }
             };
             mView=mTextView=View;
             View.init();
         } else {
        	 mView=mTextView=new EditText(mContext){
         		@Override
         		public void draw(Canvas canvas) {
         			bdraw(canvas);
        			super.draw(canvas);
        			adraw(canvas);
         		}
         	};
             if (INPUTTYP_PASSWORD.equals(inputType)) {// 系统密码控件
            	 mTextView.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
             } else if (INPUTTYP_NUMBER.equals(inputType)) {
            	 mTextView.setRawInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
            	 mTextView.setKeyListener(new DigitsKeyListener(false, true));
             }
         }
    }
    @Override
    protected void parseView() {
    	super.parseView();
    	showClearButton=true;
		defaultMaxLines=1;
    	defaultClickable=true;
    	leftImageDrawable=new UIEngineDrawable(mContext);
    	rightImageDrawable=new UIEngineDrawable(mContext);
    	mTextView.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            	String onFcusChange = attrMap.get("onFocus");
            	if (!TextUtils.isEmpty(onFcusChange)){
            		HashMap<String, Object> map = new HashMap<String, Object>();
            		map.put("hasFocus",hasFocus);
            		executeLua(onFcusChange,map);
            	}
            	if (!hasFocus) {
            		KeyboardInstance.hideKeyboard();// 隐藏键盘
            		setClearButton(null);
            	} else {
            		if (mView instanceof DkkjPWDEditText) {
            			DkkjPWDEditText dket=(DkkjPWDEditText)mView;
            			dket.setLinksClickable(false);
            			dket.setCursorVisible(true);
            			dket.showhandle();
            		}
            		baseFragment.showKeyBoardWithView(MTextField.this);
            		if(showClearButton)
            			setClearButton(rightImageDrawable);
            	}
            }
        });
        mTextView.addTextChangedListener(new TextWatcher(){
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String textChangedLua = attrMap.get(ATTR_TEXT_CHANGE);
                if (!TextUtils.isEmpty(textChangedLua))
                	executeLua(textChangedLua,null);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            @Override
            public void afterTextChanged(Editable s){
            	if(showClearButton && length()>0)
                	setClearButton(rightImageDrawable);
            	else
                	setClearButton(null);
            }
        });
        setOnTouchEvent();
    }
    @Override
    protected void parseAttributes(){
    	super.parseAttributes();
    	setClearButtonShow(attrMap.get(ATTR_CLEARBUTTON));
    	setHint(attrMap.get(ATTR_HINT));
    	setImeOptions((EditText)mTextView);
    	setOnEditorActionListener(mTextView);
         
    	if (attrMap.containsKey("digits")) {
    		mTextView.setKeyListener(new NumberKeyListener() {
    			@Override
    			protected char[] getAcceptedChars() {
    				String digits = attrMap.get("digits");
    				char[] numberChars = digits.toCharArray();
    				return numberChars;
    			}
    			@Override
    			public int getInputType() {
    				return InputType.TYPE_CLASS_TEXT;
    			}   
    		});
    	}
    	parseImage(leftImageDrawable,UIEngineDrawable.StateNormal,attrMap.get(ATTR_LEFTIMAGE));
    	Drawable[] drawables=mTextView.getCompoundDrawables();
    	drawables[0]=leftImageDrawable;
    	if (attrMap.containsKey(ATTR_IMAGE_SIZE)) {
    		String size = attrMap.get(ATTR_IMAGE_SIZE);
    		String[] s = size.split(",");
         	int width = UIEngineLayoutParams.parseSingleSizeString(s[0],mContext,0);
         	int height = UIEngineLayoutParams.parseSingleSizeString(s[1],mContext,1);
            leftImageDrawable.setBounds(0, 0, width, height);
    	}
    	mTextView.setCompoundDrawables(drawables[0],drawables[1],drawables[2],drawables[3]);
    	//mTextView.setCompoundDrawablePadding(20);
    	//parseImage(rightImageDrawable,UIEngineDrawable.StateNormal,attrMap.get(ATTR_LEFTIMAGE));
    	//parseImage(rightImageDrawable,UIEngineDrawable.StateNormal,attrMap.get(ATTR_LEFTIMAGE));
    	rightImageDrawable.setBounds(0, 0, DisplayUtil.dip2px(mContext, 30), DisplayUtil.dip2px(mContext, 30));
    	
    	 if (INPUTTYP_PASSWORD.equals(inputType) && !(mTextView instanceof DkkjPWDEditText)) {
        	 mTextView.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
         }
     	setPlaceHolderColor(attrMap.get(ATTR_PLACEHOLDERCOLOR));
    }
    protected void parseImage(final UIEngineDrawable imageDrawable,int state,String imageString){
    	String oldImageURL=imageDrawable.getImageURLForState(state);
    	if(oldImageURL!=null && oldImageURL.equals(imageString))
    		return;
    	imageDrawable.setColorForState(state, -2);
    	if (imageString==null||imageString.length()==0) 
             return;
    	      	 
    	int color=UIEngineColorParser.getColor(imageString);
    	if(color!=-2)	
    		imageDrawable.setColorForState(state,color);
    	else{
        	  Builder builder = getBuilder(isCache);
              RequestListener rl=new RequestListener(){
              	@Override
              	public void DidLoad(Object result,Map<String,Object>responseLuaArgs,int callBackType){
	    			if(callBackType==CacheDidUpdateCallBackType)return;
              		int state =(Integer)params.get(RequestManager.IMAGEREQUESTSTATE);
                  	String RequestURL=(String) params.get(RequestOptions.REQUESTURL);
              		Bitmap loadedImage=null;
              		if(result instanceof Bitmap)
              			loadedImage=(Bitmap)result;
              		else
              			return;
              		
              		if(RequestURL.endsWith(".9.png")){
            			loadedImage.setDensity(NinePatchChunk.DEFAULT_DENSITY);
              			imageDrawable.setDrawableForState(state,NinePatchChunk.create9PatchDrawable(mContext, loadedImage, RequestURL));
              		}
              		else
              			imageDrawable.setBitmapForState(state, loadedImage);
              	}
              };
             
              imageDrawable.setImageURLForState(state, imageString);
              HashMap<String,Object> params=new HashMap<String,Object>();
              params.put(RequestManager.IMAGEREQUESTTARGETWIDTH,mLayoutParams.width);
              params.put(RequestManager.IMAGEREQUESTTARGETHEIGHT,mLayoutParams.height);            	
              params.put(RequestOptions.REQUESTURL,imageString);
              params.put(RequestManager.REQUESTCALLBACK,mView);
              params.put(RequestOptions.RESPONSETYPE,"IMAGE");
              params.put(RequestManager.REQUESTLISTENER,rl);
              params.put(RequestManager.IMAGEREQUESTSTATE,state);
              params.put(RequestManager.IMAGEREQUESTOPTIONS,builder.build());
              RequestManager.request(params);
        }
    }

    protected void setClearButton(UIEngineDrawable imageDrawable){
     	Drawable[] drawables=mTextView.getCompoundDrawables();
     	if(drawables[2]!=imageDrawable){
     		drawables[2]=imageDrawable;
     		if (drawables[2]==null) return;
     		mTextView.setCompoundDrawables(drawables[0],drawables[1],drawables[2],drawables[3]);
     	}
    }
    protected void setClearButtonShow(String clearButtonShow){
    	if(clearButtonShow==null || clearButtonShow.length()==0)
    		showClearButton=true;
    	else
    		showClearButton=BOOLEANTRUE.equals(clearButtonShow);
    }
     
    public void setOnTouchEvent(){
    	mTextView.setOnTouchListener(new OnTouchListener() {
    		@Override
            public boolean onTouch(View v, MotionEvent event) {
            	switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					if (rightImageDrawable.getDrawableResForState(UIEngineDrawable.StateNormal)!=null &&
						rightImageDrawable.getDrawableResForState(UIEngineDrawable.StateNormal)!=(Integer)(-2)
						&& showClearButton) {  
	                    int eventX=(int)event.getRawX(),eventY=(int)event.getRawY();  
	                    Rect rect = new Rect();  
	                    v.getGlobalVisibleRect(rect);  
	                    rect.left = rect.right - 100;  
	                    if(rect.contains(eventX, eventY)&& ((EditText)v).isFocused()){
	                        if (v instanceof DkkjPWDEditText) 
	                            ((DkkjPWDEditText)v).clearText();
	                        else if (v instanceof EditText) 
	                            ((EditText)v).setText("");
	                        setClearButton(null);
	                    }
	                    else
	                        setClearButton(rightImageDrawable);
	                }	
					break;
//               case MotionEvent.ACTION_DOWN:
//            		if (pressAble != null) {  
//                      	int eventX = (int) event.getRawX();  
//	                    int eventY = (int) event.getRawY();  
//	                    Rect rect = new Rect();  
//	                    v.getGlobalVisibleRect(rect);  
//	                    rect.left = rect.right - 100;  
//	                    if(rect.contains(eventX, eventY)&& ((EditText)v).isFocused()){
//	                    	  mView.post(new Runnable() {
//	                                @Override
//	                                public void run() {
//		                            	setClearButton(null);
// 
//	                                }
//	                            });
//	                    } 
//	                }	
//            	   break;
				}
                return false;  
            }
        });
    }
    
    /**
     * 清除输入内容
     */
    public void clearText(){
        setValue("");
    }

    /**
     * 获取光标位置
     * @param s
     */
    public int getSelectionStart(){return mTextView.getSelectionStart();}
    public void setSelection(int i){((EditText)mTextView).setSelection(i);}
 
    public void setHint(String hintStirng){
    	if (hintStirng==null) hintStirng="";
       //这里设置提示语的字体大小，颜色，后期要进行可配置
     	SpannableString ss = new SpannableString(hintStirng);
        ss.setSpan(new ForegroundColorSpan(0xFFCCCCCC), 0, hintStirng.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        mTextView.setHint(new SpannedString(ss));
    }
    public void setPlaceHolderColor(String placeHolderColor){
    	int color=UIEngineColorParser.getColor(placeHolderColor);
    	if(color==-2)
    		color=0xFFCCCCCC;
    	SpannableString ss = new SpannableString(mTextView.getHint());
        ss.setSpan(new ForegroundColorSpan(color), 0, mTextView.getHint().length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        mTextView.setHint(ss);
    }
    
	private void setOnEditorActionListener(TextView view) {
        if (attrMap.containsKey(ATTR_RETURN_KEYONCLICK)) {
            view.setOnEditorActionListener(new OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                	executeLua(attrMap.get(ATTR_RETURN_KEYONCLICK));
                    return true;
                }
            });
        }
    }

   
    /**
     * set the returnKeyType of keyboard enter
     * 
     * @param view
     */
    protected void setImeOptions(EditText view) {
        String keyType = attrMap.get(ATTR_RETURN_KEYTYPE);
        int action = EditorInfo.IME_ACTION_DONE;
        if (TextUtils.isEmpty(keyType)) {
            return;
        } else if ("go".equals(keyType)) {
            action = EditorInfo.IME_ACTION_GO;
        } else if ("next".equals(keyType)) {
            action = EditorInfo.IME_ACTION_NEXT;
        } else if ("search".equals(keyType)) {
            action = EditorInfo.IME_ACTION_SEARCH;
        } else if ("done".equals(keyType)) {
            action = EditorInfo.IME_ACTION_DONE;
        }
        view.setImeOptions(action);
    }

    public void hideKeyboard() {
        mTextView.clearFocus();
        if (mTextView instanceof DkkjPWDEditText)
            KeyboardInstance.hideKeyboard();
        else {
            InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mTextView.getWindowToken(), 0);
            //mTextView.setCursorVisible(false);// 失去光标
        }
    }
    
	public void showKeyboard() {
    	mTextView.post(new Runnable() {
			@Override
			public void run() {
		    	mTextView.requestFocus();
				InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		        imm.showSoftInput(mTextView, 0);
			}
		});
    }
        
    public void requestFocus() {
    	mTextView.setFocusable(true);
    	mTextView.setFocusableInTouchMode(true);
    	mTextView.requestFocus();
    }
   
    
    @Override
    public void setValue(String value) {
        String encrypionType = attrMap.get(ATTR_ENCRYPTION_TYPE);
        if (INPUTTYP_PASSWORD.equals(encrypionType)) {
        	DkkjPWDEditText pwEditText=((DkkjPWDEditText)mView);
            if(TextUtils.isEmpty(value)){
            	pwEditText.clearText();
                return;
            }
            final String password = ParserKeyboard.decodeStr(value, "AD_" + Configure.macAddress);
            if (password != null) 
            	pwEditText.setEncodeText(password);
            else if(!TextUtils.isEmpty(value)) 
            	pwEditText.setEncodeText(value);
        } else {
        	mTextView.setText(value==null?"":value);
        	if(mTextView instanceof EditText){
        		((EditText)mTextView).setSelection(value==null?0:value.length());
        	}
        }
    }
    @Override
    public String getValue() {
        if (mTextView instanceof DkkjPWDEditText) 
            return ((DkkjPWDEditText)mTextView).getEncodeText();
        else
            return mTextView.getText().toString();
    }
    
    public String getNorValue(){
    	String value="";
        if (mTextView instanceof DkkjPWDEditText){
            value = ((DkkjPWDEditText)mTextView).getNormolText();
        } else {
            value = mTextView.getText().toString();
        }
        return value;
    }
    
    public void setInputType(int type){
    	mTextView.setInputType(type);
    }
    
    public void append(CharSequence s){
    	mTextView.append(s);
    }
    
    public void deleteEvent(){
    	KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0,
				0, 0, KeyEvent.KEYCODE_ENDCALL);
		mTextView.dispatchKeyEvent(event);
    }
}
