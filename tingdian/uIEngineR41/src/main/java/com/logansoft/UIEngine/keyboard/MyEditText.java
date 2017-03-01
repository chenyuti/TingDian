package com.logansoft.UIEngine.keyboard;


import com.logansoft.UIEngine.utils.Configure;
import com.logansoft.UIEngine.utils.LogUtil;

import android.app.Activity;
import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.EditText;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public abstract class MyEditText extends EditText{
	
	
	

	public MyEditText(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		setLongClickable(false);
		boolean a=false,b=false;
		if (android.os.Build.VERSION.SDK_INT <= 10) {
            setInputType(InputType.TYPE_NULL);
        } else {
            try {
                Class<EditText> cls = EditText.class;
                Method setShowSoftInputOnFocus;
                setShowSoftInputOnFocus = cls.getMethod("setSoftInputShownOnFocus",
                        boolean.class);
                setShowSoftInputOnFocus.setAccessible(true);
                setShowSoftInputOnFocus.invoke(this, false);
            } catch (Exception e) {
//                e.printStackTrace();
                a = true;
            } 
            //setShowSoftInputOnFocus
			if (!a) {
				try {
					Class<EditText> cls = EditText.class;
					Method setShowSoftInputOnFocus;
					setShowSoftInputOnFocus = cls.getMethod(
							"setShowSoftInputOnFocus", boolean.class);
					setShowSoftInputOnFocus.setAccessible(true);
					setShowSoftInputOnFocus.invoke(this, false);
				} catch (Exception e) {
//					e.printStackTrace();
					b = true;
				}
			}
            if(a&&b) {
            	setInputType(InputType.TYPE_NULL);
            }
            
        }
  
		this.setFocusable(true);
		this.setCursorVisible(true);
	}
	
    public MyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub

    }

	public  void updateedit(Context context)
	{
		KeyboardInstance.GetInterface((Activity)context, context).updatekeyboard(this);
	}
	
	public abstract List<Integer> getArrayValue();
	
	
//	public abstract void editedEvent();
   /**
    * fale: 执行隐藏键盘  true:不隐藏键盘
    * @return
    */
	public abstract boolean onEndEdited();
	
	
	
	
	
	public  String getEncodeText()
	{
			if(getArrayValue().size()==0)
			{
				return "";
			}
			if(ParserKeyboard.getInstance().formobj!=null)
			{
				long size = ParserKeyboard.getInstance().size ;
				if(size <= 0)
				{
					byte[] temp =  ParserKeyboard.getInstance().openfilestream(ParserKeyboard.localPopupTabPath,getContext());
					ParserKeyboard.getInstance().parseDataSourcesXML(temp);
					 size = ParserKeyboard.getInstance().size;
					 if(size <= 0 )
					 {
						 return "";
					 }
				}
				size = System.currentTimeMillis()%size;
				String Ramdom =Long.toString(size+1);
				String password = ParserKeyboard.GetPassWord(Ramdom, getArrayValue());
				password = ParserKeyboard.encodeStr(password, "AD_"+Configure.macAddress);
				return password;
			}
			return "";

	}
	
	public void setEncodeText(String password){
	    long size = ParserKeyboard.getInstance().size ;
        if(size <= 0)
        {
            byte[] temp =  ParserKeyboard.getInstance().openfilestream(ParserKeyboard.localPopupTabPath,getContext());
            ParserKeyboard.getInstance().parseDataSourcesXML(temp);
             size = ParserKeyboard.getInstance().size;
             if(size <= 0 )
             {
                 return ;
             }
        }
        
        String decodeStr = ParserKeyboard.getDecodeStr(password);
        String a="";
        for (int i = 0; i < decodeStr.length(); i++) {
            a+="●";
        }
        setText(a);
        if (getArrayValue().isEmpty()) {
            try {
                byte[] unicode = decodeStr.getBytes("unicode");
                unicode[0]=0;
                unicode[1]=0;
                for (int i = 0; i < unicode.length; i++) {
                    if (unicode[i]!=0) {
                        getArrayValue().add((int)unicode[i]);
                    }
                } 
//     getArrayValue().add(1);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
	}
	
	public  String getNormolText()
	{
		if(getArrayValue().size()==0)
		{
			return "";
		}
		StringBuilder builder = new StringBuilder();
		for(Integer item:getArrayValue())
		{ 	
		    String temp =Character.toString(((char)item.intValue()));
			 builder.append(temp);
		}
		return builder.toString();
	}
	
	public abstract void clearText();

	public abstract boolean getEditEndState();
	
	
	public abstract  void showhandle();
}
