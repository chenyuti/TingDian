package com.logansoft.UIEngine.keyboard;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.preference.PreferenceActivity.Header;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

/**
 * 自定义密码键盘
 * @author bincom
 *
 */
public class KeyboardInstance {
	
	public Activity act=null;
	public Context ctx=null;
	/**
	 * 键盘------
	 */
	KeyboardUtil keyboard=null;
	Handler inputhandler = new Handler();
	
	private static KeyboardInstance instance=null;
	
	/**
	 * 获取密码键盘实例，必须提供要调用的activity
	 * @param act
	 * @param ctx
	 * @return
	 */
	public static KeyboardInstance GetInterface(Activity act,Context ctx)
	{
		if(instance == null)
		{
			instance = new KeyboardInstance();
		}
		if(instance.act==null||instance.act!=act)
		{
//			Log.e("test","instance.act==null||instance.act!=act");
			KeyboardUtil.isNeedToNew = true;
			if(act!=null){
				instance.act = act;
				if(instance.keyboard!=null){
					instance.keyboard.setCurrentActivity(act);
				}
			}
		}else{
			KeyboardUtil.isNeedToNew=false;
		}
		instance.act = act;
		instance.ctx = ctx;
		return instance;
	}
	
	public static boolean getState()
	{
		if(instance!=null && instance.keyboard!=null&&instance.keyboard.isShow == true)
		{
			return true;	
		}
		return false;
	}
	
	
	public static void clear()
	{
		if(instance!=null)
		{
		 if(instance.keyboard!=null)
		 {
			 instance.keyboard.destory();
			 instance.keyboard=null;

		 }
		 if(instance.inputhandler!=null)
		 {		 
			 instance.inputhandler.removeCallbacks(instance.rhidekeyb);
			 instance.inputhandler = null;
			 instance.rhidekeyb = null;
		 }
		 instance.act=null;
		 instance.ctx=null;
		 instance = null;
		}
	}
	
    int count = 1;
	/**
	 * 	初始化密码键盘实例，要显示键盘时调用
	 * @param skeyb 
	 * @param encode
	 * @param inputtype
	 */
   
	public void eventhandle(Handler handler,MyEditText skeyb,boolean encode,int inputtype)
	{
		if(keyboard==null)
		{
			keyboard = new KeyboardUtil(handler,act, ctx);
		}
		keyboard.setCurrentActivity(act);
		keyboard.ed = skeyb;
		keyboard.setKeyboad_type(inputtype);// KeyboardUtil.TYPE_NUM;
		keyboard.showKeyboard(true);
		keyboard.isencode = encode;
		count=1;
		inputhandler.postDelayed(rhidekeyb, 0);
		count--;
	}
	
	/**
	 * 	初始化密码键盘实例，要显示键盘时调用
	 * @param skeyb     要调用键盘的输入框
	 * @param encode    是否加密
	 * @param inputtype   键盘类型
	 * @param isShowEdit  显示键盘中得输入框
	 */
	public void eventhandle(Handler handler,MyEditText skeyb,boolean encode,int inputtype,final boolean isShowEdit)
	{
		if(keyboard==null)
		{
			keyboard = new KeyboardUtil(handler,act, ctx);
		}
		count=4;
		inputhandler.postDelayed(rhidekeyb, 0);
		count--;
		
		keyboard.setCurrentActivity(act);
		keyboard.ed = skeyb;
		keyboard.setKeyboad_type(inputtype);// KeyboardUtil.TYPE_NUM;
		inputhandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				keyboard.showKeyboard(isShowEdit);			
			}
		},300);
	
		keyboard.isencode = encode;
//		skeyb.post(rhidekeyb);

	}
	
	
	public void updatekeyboard(MyEditText edview)
	{
		if(keyboard!=null)
		{
			keyboard.updateedit(edview);
		}
	}
	
	
	public static void hideKeyboard()
	{
//		if(instance!=null)
//		{
//		/**
//		 * 键盘事件处理-------------
//		 */
//		Runnable hider = new Runnable() {
//
//			@Override
//			public void run() {
		
				if (instance!=null&&instance.keyboard != null) {
					instance.keyboard.hideKeyboard();
				}
//			}
//		};
//		instance.inputhandler.removeCallbacks(instance.rhidekeyb);
//		instance.inputhandler.post(hider);
//		
//		}
	}
	
	/**
	 * 键盘事件处理-------------
	 */
	Runnable rhidekeyb =new Runnable()
	{

		@Override
		public synchronized void run() {
//			// TODO Auto-generated method stub
//			InputMethodManager imm = (InputMethodManager)act.getSystemService(Context.INPUT_METHOD_SERVICE);  
//			//得到InputMethodManager的实例  
//			if (imm.isActive()) { 
//				//如果开启
//				imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS); 
//				//关闭软键盘，开启方法相同，这个方法是切换开启与关闭状态的 
//				}
//			else
//				{
//					handler.postDelayed(r, 400);
//				}
			
			  InputMethodManager imm = (InputMethodManager)act.getSystemService(Context.INPUT_METHOD_SERVICE);
			  imm.hideSoftInputFromWindow(keyboard.ed.getWindowToken(), 0);
		
			 if(count>0)
			 {
			  inputhandler.postDelayed(rhidekeyb, 200);
			  count--;
			 }
		}
		
	};
}
