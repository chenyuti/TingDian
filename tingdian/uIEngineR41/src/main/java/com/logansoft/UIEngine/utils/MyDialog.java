package com.logansoft.UIEngine.utils;

import com.logansoft.UIEngine.R;
import com.logansoft.UIEngine.parse.xmlview.BaseView;

import android.R.integer;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class MyDialog extends Dialog {
    
    private Window window = null;
//    int animStyle = R.style.pop;
    int animStyle = R.style.myDialog;
    
    View.OnClickListener cancelOnClickListener;
    View.OnClickListener confirmOnClickListener;
    String message ;
    private BaseView contentBaseView;
    
    protected MyDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public MyDialog(Context context) {
        super(context);
    }

    public MyDialog(Context context,int animId) {
        super(context,animId);
        animStyle = animId ;
    }

    public void showDialog(int layoutResID, int x, int y){
        setContentView(layoutResID);
        windowDeploy(x, y);
      //设置触摸对话框意外的地方取消对话框
        setCanceledOnTouchOutside(true);
        show();
    }
    

    
    public void setContentView(BaseView view){
        contentBaseView = view;
        if(contentBaseView!=null)
            setContentView(contentBaseView.getView());
    }
    
    public void showDialog(int x, int y){
        windowDeploy(x, y);
        show();
    }
    
    public BaseView getBaseView(){
        return contentBaseView;
    }
    
    
    public MyDialog setCancleListener(View.OnClickListener onClick){
        cancelOnClickListener =onClick ;
        return this;
    }
    
    public MyDialog setConfirmOnClickListener(View.OnClickListener confirmOnClickListener) {
        this.confirmOnClickListener = confirmOnClickListener;
        return this;
    }
    
    /**
     * 设置消息内容
     * @param message
     */
    public MyDialog setMessage(String message) {
        this.message = message;
        return this;
    }
    
    /**
     * 显示默认样式dialog
     */
    public void showDialog(){
        setContentView(R.layout.back_dialog);
         View cancle = findViewById(R.id.content_dialog_btn_c);
         View confirm = findViewById(R.id.content_dialog_btn_s);
         if (!TextUtils.isEmpty(message)) {
             TextView content = (TextView)findViewById(R.id.content_dialog_tv);
             content.setText(message);
        }
         
         cancle.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                dismiss();
                if(cancelOnClickListener!=null){
                    cancelOnClickListener.onClick(v);
                }
            }
        });
         confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();                
                if (confirmOnClickListener!=null) {
                    confirmOnClickListener.onClick(v);
                }
            }
        });
        windowDeploy(0, 0);
      //设置触摸对话框意外的地方取消对话框
        setCanceledOnTouchOutside(true);
        show();
    }
    
    
    //设置窗口显示
    @SuppressWarnings("deprecation")
    public void windowDeploy(int x, int y){
        window = getWindow(); //得到对话框
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN |
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
//        window.setWindowAnimations(animStyle); //设置窗口弹出动画
        window.setBackgroundDrawable(new BitmapDrawable()); //设置对话框背景为透明
        WindowManager.LayoutParams wl = window.getAttributes();
        //根据x，y坐标设置窗口需要显示的位置
        wl.x = DisplayUtil.dip2px(getContext(), x); //x小于0左移，大于0右移
        wl.y = DisplayUtil.dip2px(getContext(), y); //y小于0上移，大于0下移  
//        wl.alpha = 0.6f; //设置透明度
//        wl.gravity = Gravity.BOTTOM; //设置重力
        window.setAttributes(wl);
    }
    
    

}
