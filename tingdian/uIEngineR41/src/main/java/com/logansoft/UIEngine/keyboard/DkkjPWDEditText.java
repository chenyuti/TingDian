package com.logansoft.UIEngine.keyboard;

import com.logansoft.UIEngine.utils.StringUtil;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


public class DkkjPWDEditText extends MyEditText implements  BaseCtrl{
	public String nameId = "";

	public String value = null;

	public int fontsize = 14;

	public String fontcolor = null;

	public String fontcolorPress = "#e60000";

	public String size = null;

	public String margin = null;

	public String padding = null;


	int maxLength = 100;

	String hint = null;
	String inputtype = null;
	
	public static final int TextField=1;
	public static final int PassWord=2;
	int editType=TextField;
	
	public List<Integer> arrayvalue;
	
	Context context1=null;
	boolean encode = true;
    boolean isShowEdit = false;
    Handler handler;
	public DkkjPWDEditText(Handler handler,Context context,int editType,String inputtype){
		super(context);
		this.handler=handler;
		this.context1=context;
		this.editType=editType;
		this.inputtype = inputtype;
	}
	


	public void init() {
		load();
		if(arrayvalue==null)
		arrayvalue = new ArrayList<Integer>();
	}
	
	public void setCurrentContext(Context context){
		this.context1=context;
	}
	int right_icon_width = 0;
	int right_icon_height = 0;
	public void load() {
		if (!StringUtil.isEmpty(this.value)) {
			this.setText(this.value);
		}
		
		this.setFocusable(true);
		this.setClickable(true);
		cssLayout();
//		setOnFocusChangeListener(new OnFocusChangeListener() {
//			@Override
//			public void onFocusChange(View v, boolean hasFocus) {
//				
//				
//			}
//		}
//
//		);
//
//		this.setOnKeyListener(new View.OnKeyListener() {
//
//			@Override
//			public boolean onKey(View v, int keyCode, KeyEvent event) {
//				// TODO Auto-generated method stub
//				if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
//				}
//
//				if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
//				} else {
//
//				}
//
//				return false;
//			}
//		});

//		addTextChangedListener(new TextWatcher() {
//
//			@Override
//			public void afterTextChanged(Editable arg0) { }
//
//			@Override
//			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }
//
//			@Override
//			public void onTextChanged(CharSequence s, int start, int before, int count) {
//					
//			}
//
//		});

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
        boolean sign = super.onTouchEvent(event);

		switch (event.getAction()) {

		case MotionEvent.ACTION_DOWN:
//		    setInputType(InputType.TYPE_NULL);
			break;

		case MotionEvent.ACTION_MOVE:
		    
		    
			break;

		case MotionEvent.ACTION_CANCEL: {
			// Toast.makeText(getContext(), "cancel",
			// Toast.LENGTH_SHORT).show();
			break;
		}

		case MotionEvent.ACTION_UP: {
		    showhandle();
		}

		}

		return sign;
	}
	
	
	int keyBoardType= KeyboardUtil.TYPE_NUM;
	public void showhandle() {
				KeyboardInstance.GetInterface( (Activity)context1,context1).eventhandle(handler,this, encode, keyBoardType,isShowEdit);
	}

	@SuppressWarnings("deprecation")
    public void cssLayout() {

		try {
			setBackgroundDrawable(null);
			if (!StringUtil.isEmpty(hint)) {
				this.setHint(hint);
			}

			this.setSingleLine(true);

			if (editType==PassWord) {// textfield instanceof PassWord
			    encode =true ;
				if (!StringUtil.isEmpty(inputtype)
						&& inputtype.equalsIgnoreCase("system-number")) {
				} else if (!StringUtil.isEmpty(inputtype)
						&& inputtype.equalsIgnoreCase("number")) {
					keyBoardType= KeyboardUtil.TYPE_NUM;
				} else {
					keyBoardType= KeyboardUtil.TYPE_WORD;
				}
				setTransformationMethod(PasswordTransformationMethod
						.getInstance());

			} else if (editType==TextField) {// textfield instanceof TextField
			    encode =false ;
				if (!StringUtil.isEmpty(inputtype)
						&& inputtype.equalsIgnoreCase("system-number")) {
//					setInputType(InputType.TYPE_CLASS_TEXT);
				} else if (!StringUtil.isEmpty(inputtype)
						&& inputtype.equalsIgnoreCase("number")) {
				    keyBoardType= KeyboardUtil.TYPE_NUM;
//				    setInputType(InputType.TYPE_CLASS_NUMBER);
				} else {
				    keyBoardType= KeyboardUtil.TYPE_WORD;
				}
//				setTransformationMethod(PasswordTransformationMethod
//                        .getInstance());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public List<Integer> getArrayValue() {
		return arrayvalue;
	}

	@Override
	public boolean onEndEdited() {
		return false;
	}

	@Override
	public void destroy() {
		if(arrayvalue!=null)
		arrayvalue.clear();
	}

	@Override
	public void clearText() {
		if(arrayvalue!=null)
		{
			arrayvalue.clear();
			this.setText("");
			updateedit(getContext());
		}
	}

	@Override
	public boolean getEditEndState() {
		return true;
	}




}
