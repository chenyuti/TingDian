
package com.logansoft.UIEngine.utils.WeiXin;

import com.jakewharton.disklrucache.Util;
import com.logansoft.UIEngine.utils.images.BitmapUtil;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage.IMediaObject;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

public class WeiXinUtil {
    private static WeiXinUtil weixinUtil;

    private Activity mContext;

    private Bitmap shareBitmap;// 需要分享的图片

    private String shareString;// 需要分享的文字

    private String urlString;
    
    private static final int THUMB_SIZE = 150;

    private boolean isShareUrl =false;

    private boolean isShareBitmap = false;

    private boolean isShareString = false;

    public static int SCENE_TIMELINE = SendMessageToWX.Req.WXSceneTimeline;// 发送的目标场景，表示发送到朋友圈

    public static int SCENE_SESSION = SendMessageToWX.Req.WXSceneSession;// 发送的目标场景，表示发送到会话

    public static int SCENE_FAVORITE = SendMessageToWX.Req.WXSceneFavorite;// 发送的目标场景，表示发送到微信收藏

    // * SendMessageToWX.Req.WXSceneSession 发送的目标场景，表示发送到会话
    // * SendMessageToWX.Req.WXSceneFavorite 发送的目标场景，表示发送到微信收藏

    // IWXAPI 是第三方app和微信通信的openapi接口
    private IWXAPI mWxApi;

    private int mScene;

    private String description;

    private String title;

    private Bitmap thumb;
    
    
    public static WeiXinUtil instanc(Activity context) {
        if (weixinUtil == null) {
            weixinUtil = new WeiXinUtil(context);
        }
        return weixinUtil;
    }

    public WeiXinUtil(Activity context) {
        mContext = context;
        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        mWxApi = WXAPIFactory.createWXAPI(context, Constants.APP_ID, false);// appId
//        handleIntent(mContext.getIntent(), this);
    }
    
    
    public  void  handleIntent(Intent intent,IWXAPIEventHandler handler){
        mWxApi.handleIntent(intent, handler);
    }
    
    public void setThumbData(Bitmap thumb){
        this.thumb=thumb;
    }
    public void setTitle(String title){
        this.title =title ;
    }
    public void setDescription(String description){
        this.description = description;
    }
    
    public void setShareBitmap(Bitmap shareBitmap) {
        this.shareBitmap = shareBitmap;
        if (shareBitmap != null) {
            isShareBitmap = true;
        } else {
            isShareBitmap = false;
        }
    }
    
    

    public void setShareString(String shareString) {
        this.shareString = shareString;
        if (!TextUtils.isEmpty(this.shareString)) {
            isShareString = true;
        } else {
            isShareString = false;
        }
    }
    
    public void setShareWeb(String urlString) {
        this.urlString = urlString;
        if (!TextUtils.isEmpty(this.urlString)) {
            isShareUrl = true;
        } else {
            isShareUrl = false;
        }
    }
    

    /**
     * scene的值 SendMessageToWX.Req.WXSceneTimeline 发送的目标场景，表示发送到朋友圈
     * SendMessageToWX.Req.WXSceneSession 发送的目标场景，表示发送到会话
     * SendMessageToWX.Req.WXSceneFavorite 发送的目标场景，表示发送到微信收藏
     * 
     * @param scene
     */
    public void sandMessageToWX(int scene) {
        mWxApi.registerApp(Constants.APP_ID);

        // 用WXTextObject对象初始化一个WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage();
        
        if(isShareUrl){
            WXWebpageObject  webpageObject =new WXWebpageObject();
            webpageObject.webpageUrl =urlString;
            msg.mediaObject = webpageObject;
        } else  if (isShareBitmap ) {// 发送图片加文字
            WXImageObject imgObj = new WXImageObject(shareBitmap);
            msg.mediaObject = imgObj;
        } else if(isShareString){ // 发送文字
            // 初始化一个WXTextObject对象
            WXTextObject textObj = new WXTextObject(shareString);
            msg.mediaObject = textObj;
            // 发送文本类型的消息时，title字段不起作用
        }
        
        if (thumb!=null) {
            Bitmap thumbBmp = Bitmap.createScaledBitmap(thumb, THUMB_SIZE, THUMB_SIZE, true);
            thumb.recycle();
            msg.thumbData = BitmapUtil.bmpToByteArray(thumbBmp, true); // 设置缩略图
        } 
        msg.title =title;  //分享的标题
        msg.description= description; //分享的描述
        
        // 构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction(isShareBitmap ? "image" :isShareString? "text":"webpage"); // transaction字段用于唯一标识一个请求
        req.message = msg;
        req.scene =scene;
        mScene  = scene;
        // 调用api接口发送数据到微信
        mWxApi.sendReq(req);
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type
                + System.currentTimeMillis();
    }

    public String getShareWeb() {
        return urlString;
    }

    public Bitmap getShareBitmap() {
        return shareBitmap;
    }

    public String getShareString() {
        return shareString;
    }
    public int getScene() {
        return mScene;
    }

    public IWXAPI getmWxApi() {
        return mWxApi;
    }
}
