package com.logansoft.scss;

import com.logansoft.UIEngine.R;
import com.logansoft.UIEngine.utils.DisplayUtil;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SCSSProgressDialogUtil {
	public static MyProgressDialog pd;
    private static OnDismissListener myDismissListener;
    private static boolean can;
	
    static{ 
    	can=false;
    }
    public static void setDefaultCancelable(Boolean can){
    	SCSSProgressDialogUtil.can=can;
    	if (pd!=null) {
    		pd.setCancelable(can);
	    } 
    }
    public static void shouPD(Context context,String msg){
		shouPD(context,msg,can);
	}
    public static void shouPD(Context context,String msg,boolean cancelable){
		if(pd==null){
			pd=new MyProgressDialog(context,msg);
			pd.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (myDismissListener!=null) {
                        myDismissListener.onDismiss(dialog);
                    }
                    pd.cancel();
                    pd=null;
                }
            });
			pd.show();
		}
		pd.setCancelable(cancelable);
	}
	
	
	public static void setOnDismissListener(OnDismissListener mDismissListener){
	    myDismissListener=mDismissListener;
	}
	
	public static void dismissPD(){
		if(pd!=null&&pd.isShowing()){
			pd.dismiss();
		}
	}
	
	public static boolean isShowing(){
	    if (pd!=null) {
	        return pd.isShowing();
        }
	    return false;
	}
	
	public static void setMessage(String msg){
	    if (pd!=null) {
	        pd.setMessage(msg);
	    } 
	}
	
	private static class MyProgressDialog extends ProgressDialog{
		
		String msg;
		private ImageView image;
		private TextView tv;
		private static final int DIALOG_SET_MESSAGE=0xe00001;
		private Handler mHandler=new Handler(){
		    @Override
		    public void handleMessage(Message msg) {
		        switch (msg.what) {
                    case DIALOG_SET_MESSAGE:
                        if (tv!=null) {
                            tv.setText((String)msg.obj);
                        }
                        break;
                }
		    }
		};
		
		public MyProgressDialog(Context context,String msg) {
			super(context);
			this.msg=msg;
		}
		
		public MyProgressDialog(Context context ,int i) {
			super(context, i);
		}
		
		public void setMessage(final String msg){
		    if (tv!=null) {
		        Message message = mHandler.obtainMessage();
		        message.what = DIALOG_SET_MESSAGE;
		        message.obj = msg ;
                mHandler.sendMessage(message);
            }
		}
		
		@Override  
	    public void onCreate(Bundle savedInstanceState) {  
	        super.onCreate(savedInstanceState);
	        LinearLayout linearLayout=new LinearLayout(getContext());
	        linearLayout.setOrientation(LinearLayout.VERTICAL);
            int p = DisplayUtil.dip2px(getContext(), 5);
            linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
	        linearLayout.setPadding(p, p, p, p);
	        LayoutParams params=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            linearLayout.setLayoutParams(params);
            image =new ImageView(getContext());
            LinearLayout.LayoutParams imageparams=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            imageparams.leftMargin=4*p;
            imageparams.rightMargin=4*p;
            
            image.setLayoutParams(imageparams);
            tv=new TextView(getContext());
            LinearLayout.LayoutParams tvparams=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            tv.setLayoutParams(tvparams);
            linearLayout.addView(image);
            linearLayout.addView(tv);
            linearLayout.setBackgroundColor(0x77000000);
                    
	        setContentView(linearLayout);
	        image.setImageResource(R.drawable.loading_img);//待改
	        
	        tv.setGravity(Gravity.CENTER_HORIZONTAL);
	        tv.setPadding(p, p, p, p);
	        tv.setMinWidth(DisplayUtil.dip2px(getContext(), 100));
	        tv.setText("加载中...");
	        tv.setTextColor(0xffffffff);
	        setScreenBrightness();  
//	        image = (ImageView) findViewById(R.id.loading_img);  
//	        tv=(TextView)findViewById(R.id.loading_tv); 
	        tv.setText(msg);
	        this.setOnShowListener(new OnShowListener(){  
                    @Override  
	                public void onShow(DialogInterface dialog) {  
	                    Animation anim = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF, 0.5f);  
	                    anim.setRepeatCount(Animation.INFINITE); // 设置INFINITE，对应值-1，代表重复次数为无穷次  
	                    anim.setDuration(1000);                  // 设置该动画的持续时间，毫秒单位  
	                    anim.setInterpolator(new LinearInterpolator()); // 设置一个插入器，或叫补间器，用于完成从动画的一个起始到结束中间的补间部分  
	                    image.startAnimation(anim); 
	                }  
	            });  
	    } 
	    private void setScreenBrightness() {  
	        Window window = getWindow();  
	        WindowManager.LayoutParams lp = window.getAttributes();  
	        /** 
	        *  此处设置亮度值。dimAmount代表黑暗数量，也就是昏暗的多少，设置为0则代表完全明亮。 
	        *  范围是0.0到1.0 
	        */  
	        lp.dimAmount = 0.3f;  
	        window.setAttributes(lp);  
	    }  
	}
}
