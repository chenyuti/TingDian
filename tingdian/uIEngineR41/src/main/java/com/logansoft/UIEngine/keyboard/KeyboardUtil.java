package com.logansoft.UIEngine.keyboard;

import com.logansoft.UIEngine.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Handler;
import android.text.Editable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.List;



public class KeyboardUtil {

    public WindowManager windowManager;
    public WindowManager.LayoutParams windowParams;
	
	private KeyboardView keyboardView;
	private Keyboard k1;// 字母键盘
	private Keyboard k2;// 数字键盘
	private Keyboard k3;// 键盘
	private Keyboard k4;// 符号键盘
	private  int keyboad_type ;// 是否数据键盘
	public boolean isupper = false;// 是否大写

	public MyEditText ed;
//	private TextView textView;
	LinearLayout layout  = null;
	private EditText edit;
	
	public boolean isencode = false;
	public boolean isShowEdit = false;
	

//    不允许屏幕截图。//
	public static final int FIRST_SYSTEM_WINDOW    = 2000;
//   内部输入法对话框，显示于当前输入法窗口之上。
    public static final int TYPE_INPUT_METHOD_DIALOG= FIRST_SYSTEM_WINDOW +12;
//    public static final int FLAG_SECURE  = 0x00002000;
    
    
    
    public static final int TYPE_NUMBER = 0x00000001;
    public static final int TYPE_WORD = 0x00000001<<1;
    public static final int TYPE_SYMBOL = 0x00000001<<2;
    
    public static final int TYPE_NUM = 0x00000001<<8;
    
    Handler handler;
    
    public void setKeyBoardType(int type)
    {
    	switch(type)
    	{
    	case TYPE_NUMBER:
    		keyboardView.setKeyboard(k2);
    		setKeyboad_type(TYPE_NUMBER);
    		break;
    	case TYPE_WORD:
    		keyboardView.setKeyboard(k1);
    		setKeyboad_type(TYPE_WORD);
    		break;
    	case TYPE_SYMBOL:
    	    keyboardView.setKeyboard(k4);
    	    setKeyboad_type(TYPE_SYMBOL);
    	    break;
    	case TYPE_NUM:
    		keyboardView.setKeyboard(k3);
    		setKeyboad_type(TYPE_NUM) ;
    		break;
    	default:
    		keyboardView.setKeyboard(k1);
    		setKeyboad_type(TYPE_WORD);
    		break;
    	}
    }
    
    public void destory() {
        try {
            ed = null;
            // textView = null;
            keyboardView = null;

            k1 = null;
            k2 = null;
            k3 = null;
            k4 = null ;
            if (layout != null) {
                layout.removeAllViews();
                layout.clearAnimation();
                layout.clearDisappearingChildren();
                if (isShow) {
                    windowManager.removeView(layout);
                    isShow = false;
                }
                layout = null;
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void setCurrentActivity(Activity activity){
    	this.activity=activity;
    }
    
    Activity activity=null;
	public KeyboardUtil(Handler hler, Activity act, Context ctx) {
//		this.act = act;
//		this.ctx = ctx;
//		this.ed = edit;
		activity=act;
		handler=hler;
		k1 = new Keyboard(ctx, R.xml.qwerty);
		k2 = new Keyboard(ctx,R.xml.symbols);
		k3 = new Keyboard(ctx, R.xml.num);
		k4 = new Keyboard(ctx, R.xml.fuhao);
		
//		keyboardView = (KeyboardView) act.findViewById(R.id.keyboard_view);
//		keyboardView = new KeyboardView(ctx,null);
		layout =(LinearLayout)activity.getLayoutInflater().inflate(R.layout.keybview, null); 
//		keyboardView = (RelativeLayout) act.getLayoutInflater().inflate(R.layout.keybview, null);  
		keyboardView =  (KeyboardView)layout.findViewById(R.id.keyboardviewtemp);
//		textView = (TextView)layout.findViewById(R.id.textid);
		edit = (EditText)layout.findViewById(R.id.edit);
//		edit.setOnClickListener(null);
//		edit.setOnFocusChangeListener(null);
		edit.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return true;
			}});
//		edit.setOnKeyListener(null);
//		edit.setOnEditorActionListener(null);
		edit.setClickable(false);
		edit.setLongClickable(false);

//		edit.setEnabled(false);
//		if(keyboad_type==TYPE_NUM)
//		{
//		    keyboardView.setKeyboard(k3);
//		}else if(keyboad_type==TYPE_NUMBER)
//		{
//			keyboardView.setKeyboard(k2);
//		}else 
//		{
//			keyboardView.setKeyboard(k1);
//		}
		keyboardView.setEnabled(true);
		keyboardView.setPreviewEnabled(false);
		keyboardView.setOnKeyboardActionListener(listener);
			}
	
	
    Runnable runhide = new Runnable() {
		@Override
		public void run() {
			hideKeyboard();		
		}
    };
    
    
    		

	private OnKeyboardActionListener listener = new OnKeyboardActionListener() {
		@Override
		public void swipeUp() {
		}

		@Override
		public void swipeRight() {
		}

		@Override
		public void swipeLeft() {
		}

		@Override
		public void swipeDown() {
		}

		@Override
		public void onText(CharSequence text) {
		}

		@Override
		public void onRelease(int primaryCode) {
		}

		@Override
		public void onPress(int primaryCode) {
		}

		@Override
		public synchronized void onKey(int primaryCode, int[] keyCodes) {
//			if (!isShowEdit) {
//				int visibility = textView.getVisibility();
//				if (visibility == View.VISIBLE) {
//					textView.setVisibility(View.GONE);
//				}
//			}
			Editable editable = ed.getText();
			int start = ed.getSelectionStart();
			if (primaryCode == Keyboard.KEYCODE_CANCEL) {// 完成
//				keyboardView.setPressed(false);
//				Handler handle = new Handler();

				//if(!ed.onEndEdited())
				//{
				    if(ed.getEditEndState())
					{
				    	ed.postDelayed(runhide, 300);
					}
					ed.onEndEdited();
					handler.sendEmptyMessage(9);
//					try {
//						Thread.sleep(100);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					
//				}
						
			} else if (primaryCode == Keyboard.KEYCODE_DELETE) {// 回退
				if (editable != null && editable.length() > 0) {
					if (start > 0) {
						int index = start-1;
						if(index<ed.getArrayValue().size())
						{
							ed.getArrayValue().remove(index);
							editable.delete(index, start);
//							PlaySoundPool.vibrate(50);
						}
					}
				}
				if(isShowEdit)
				{
					updateedit(ed);
				}
			} else if (primaryCode == Keyboard.KEYCODE_SHIFT) {// 大小写切换
				changeKey();
				setKeyBoardType(TYPE_WORD);

			} else if (primaryCode == -7) {// 切换到数字键盘
			    setKeyBoardType(TYPE_NUMBER);
			} else if (primaryCode == -8) {// 切换到word键盘
			    setKeyBoardType(TYPE_WORD);
			} else if (primaryCode == -9) {// 切换到符号键盘
			    setKeyBoardType(TYPE_SYMBOL);
			} else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE) {// 数字键盘切换
				setKeyBoardType(getKeyboad_type()>>1);
			} else if (primaryCode == 57419) { // go left
				if (start > 0) {
					ed.setSelection(start - 1);
//					PlaySoundPool.vibrate(50);
				}
				if(isShowEdit)
				{
					updateedit(ed);
				}
			} else if (primaryCode == 57421) { // go right
				if (start < ed.length()) {
					ed.setSelection(start + 1);
//					PlaySoundPool.vibrate(50);
				}
				if(isShowEdit)
				{
					updateedit(ed);
				}
			} else {
//				if(keyboad_type==TYPE_NUM)
//				{
//					if(primaryCode<48||primaryCode>57)
//						return;
//				}
				if(start<0)
				{
					start = 0;
				}
				if(isencode)
				{
					editable.insert(start,"●");
					
				}else
				{
					editable.insert(start,Character.toString((char)primaryCode));
				}
				int leng = ed.getText().length();
				if (leng==start) {
					if (isencode) {
						editable.insert(start, "●");

					} else {
						editable.insert(start,
								Character.toString((char) primaryCode));
					}
				}
				if(leng>ed.getArrayValue().size())
				{
					ed.getArrayValue().add(start, primaryCode);
					
				}
//				DLog.i("array", ""+ed.getText().length());
				if(isShowEdit)
				{
					updateedit(ed);
//				edit.setText(ed.getText());
//				edit.setSelection(ed.getSelectionStart());
				}
				
			}
		}
	};
	
	/**
	 * 键盘大小写切换
	 */
	private void changeKey() {
		List<Key> keylist = k1.getKeys();
		if (isupper) {//大写切换小写
			isupper = false;
			for(Key key:keylist){
				if (key.label!=null && isword(key.label.toString())) {
					key.label = key.label.toString().toLowerCase();
					key.codes[0] = key.codes[0]+32;
				}
			}
		} else {//小写切换大写
			isupper = true;
			for(Key key:keylist){
				if (key.label!=null && isword(key.label.toString())) {
					key.label = key.label.toString().toUpperCase();
					key.codes[0] = key.codes[0]-32;
				}
			}
		}
	}
	public static boolean isNeedToNew=false;
    public synchronized void showKeyboard(boolean isshowedit) {

//        int visibility = layout.getVisibility();
//        if (visibility == View.GONE || visibility == View.INVISIBLE) {
////        	layout.setVisibility(View.VISIBLE);
//        	textView.setVisibility(View.VISIBLE);
//        	setKeyBoardType(keyboad_type);
//        	windowManager.addView(layout, windowParams);
//        }
    	try{
    	isShowEdit = isshowedit;
    	if(!isShow)
    	{
    		isShow = true;
    		if(windowManager==null&&windowParams==null)
    			isNeedToNew = true;
    		if(isNeedToNew){
        		windowManager = (WindowManager) this.activity.getSystemService(
        				Context.WINDOW_SERVICE);// "window"
        		windowParams = new WindowManager.LayoutParams();
        		windowParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
        		windowParams.x = 0;
        		windowParams.y = 0;
        		windowParams.format = PixelFormat.RGBA_8888;
        		windowParams.alpha = 1f;
        		windowParams.height = LayoutParams.WRAP_CONTENT;
        		windowParams.width = LayoutParams.FILL_PARENT;
        		windowParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
        				| LayoutParams.FLAG_NOT_FOCUSABLE;
//        				| LayoutParams.FLAG_NOT_FOCUSABLE |FLAG_SECURE ;   //部分手机会有问题
    			isNeedToNew = false;
    		}
	
    		
    		setKeyBoardType(getKeyboad_type());
    		
			if(!isShowEdit)
			{
				if(edit.getVisibility()!=View.GONE)
				edit.setVisibility(View.GONE);
			}else
			{
				updateedit(ed);
				if(edit.getVisibility()!=View.VISIBLE)
				edit.setVisibility(View.VISIBLE);
			}
    		windowManager.addView(layout, windowParams);
//    		layout.requestFocus();
    		keyboardView.setEnabled(true);
    		keyboardView.requestFocus();
    		
    	}
    	
    	}catch(Exception e)
    	{
    		try{
    		windowManager.removeViewImmediate(layout);
    		}catch(Exception e1)
    		{
    			e1.printStackTrace();
    		}
    		isShow = false;
    		e.printStackTrace();
    	}
    	
    	
    }
    
    public void updateedit(MyEditText edview)
    {
		edit.setText(edview.getText());
		edit.setHint(edview.getHint());
		edit.setSelection(edview.getSelectionStart());
    }
    
    public boolean isShow = false;
    
    public synchronized void hideKeyboard() {
//        int visibility = layout.getVisibility();
//        if (visibility == View.VISIBLE) {
//        	layout.setVisibility(View.INVISIBLE);
//        }
    	try{
    	if(isShow)
    	{
//    		textView.setVisibility(View.VISIBLE);
    		windowManager.removeViewImmediate(layout);
    		isShow = false;
    	}
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    }
    
    private boolean isword(String str){
    	String wordstr = "abcdefghijklmnopqrstuvwxyz";
    	if (wordstr.indexOf(str.toLowerCase())>-1) {
			return true;
		}
    	return false;
    }

	public synchronized int getKeyboad_type() {
		return keyboad_type;
	}

	public synchronized void setKeyboad_type(int keyboad_type) {
		this.keyboad_type = keyboad_type;
	}
    
//    private boolean isNum(String str){
//    	String wordstr = "1234567890";
//    	if (wordstr.indexOf(str.toLowerCase())>-1) {
//			return true;
//		}
//    	return false;
//    }

}
