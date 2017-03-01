//
//package com.logansoft.UIEngine.utils.weibo;
//
//import com.logansoft.UIEngine.utils.LogUtil;
//import com.sina.weibo.sdk.ApiUtils;
//import com.sina.weibo.sdk.api.BaseMediaObject;
//import com.sina.weibo.sdk.api.ImageObject;
//import com.sina.weibo.sdk.api.MusicObject;
//import com.sina.weibo.sdk.api.TextObject;
//import com.sina.weibo.sdk.api.VideoObject;
//import com.sina.weibo.sdk.api.VoiceObject;
//import com.sina.weibo.sdk.api.WebpageObject;
//import com.sina.weibo.sdk.api.WeiboMessage;
//import com.sina.weibo.sdk.api.WeiboMultiMessage;
//import com.sina.weibo.sdk.api.share.BaseResponse;
//import com.sina.weibo.sdk.api.share.IWeiboHandler;
//import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
//import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
//import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
//import com.sina.weibo.sdk.api.share.WeiboShareSDK;
//import com.sina.weibo.sdk.auth.AuthInfo;
//import com.sina.weibo.sdk.auth.Oauth2AccessToken;
//import com.sina.weibo.sdk.auth.WeiboAuthListener;
//import com.sina.weibo.sdk.auth.sso.SsoHandler;
//import com.sina.weibo.sdk.constant.WBConstants;
//import com.sina.weibo.sdk.exception.WeiboException;
//import com.sina.weibo.sdk.net.RequestListener;
//import com.sina.weibo.sdk.net.openapi.ShareWeiboApi;
//import com.sina.weibo.sdk.utils.Utility;
//
//import android.app.Activity;
//import android.graphics.Bitmap;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.widget.Toast;
//
//public class WeiboUtil {
//    private IWeiboShareAPI mWeiboShareAPI;
//
//    private Activity mContext;
//
//    public Oauth2AccessToken mAccessToken;
//
//    private Bitmap shareBitmap;//需要分享的图片
//    private String shareString;//需要分享的文字
//    
//    private boolean isShareBitmap=false;
//    private boolean isShareString=false;
//
//    private String urlString;
//
//    private boolean isShareUrl;
//
//    public SsoHandler mSso;
//
//    private static WeiboUtil mWeiboUtil;
//
//    public static WeiboUtil instance(Activity context) {
//        if (mWeiboUtil == null) {
//            mWeiboUtil = new WeiboUtil(context);
//        }
//        return mWeiboUtil;
//    }
//    
//    public void setShareBitmap(Bitmap shareBitmap) {
//        this.shareBitmap = shareBitmap;
//        if (shareBitmap!=null) {
//            isShareBitmap =true;
//        }else {
//            isShareBitmap =false;
//        }
//    }
//    
//    
//    public void setShareString(String shareString) {
//        this.shareString = shareString;
//        if (!TextUtils.isEmpty(this.shareString)) {
//            isShareString =true;
//        }else {
//            isShareString =false;
//        }
//    }
//    
//    public void setShareWeb(String urlString) {
//        this.urlString = urlString;
//        if (!TextUtils.isEmpty(this.urlString)) {
//            isShareUrl = true;
//        } else {
//            isShareUrl = false;
//        }
//    }
//    
//    private WeiboUtil(Activity context) {
//        if (mWeiboShareAPI == null) {
//            mContext = context;
//            mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(context, Constants.APP_KEY);
//            handleWeiboResponse();
//            mWeiboShareAPI.registerApp();// 注册第三方应用 到微博客户端中，注册成功后该应用将显示在微博的应用列表中。
//            
//        }
//    }
//    
//    
//   public void shareWeibo(){
//       //判断是否安装微博
//       if (mWeiboShareAPI.isWeiboAppInstalled()) {
//           shareSinaWeiboByClient();
//       }else {
//           shareSinaWeb();
//       }
//       
//   }
//   
//    /**
//     *  使用客户端分享
//     * @param shareStr
//     */
//    public void shareSinaWeiboByClient() {
//        LogUtil.i("--------------------------"+Thread.currentThread());
//        int supportApi = mWeiboShareAPI.getWeiboAppSupportAPI();
//        if (supportApi >= ApiUtils.BUILD_INT_VER_2_2 /*ApiUtils.BUILD_INT_VER_2_2*/) {
//            sendMultiMessage(isShareString, isShareBitmap,isShareUrl);
//        } else {
//            sendSingleMessage(isShareString, isShareBitmap,isShareUrl);
//        }
//    }
//
//
//    /**
//     * 无客户端分享
//     */
//    public void shareSinaWeb() {
//        if (mAccessToken==null) {
//            mAccessToken=AccessTokenKeeper.readAccessToken(mContext);
//        }
//        
//        if (!mAccessToken.isSessionValid()) {
////            //授权登录
////            WeiboAuth weiboAuth=new WeiboAuth(mContext, Constants.APP_KEY, Constants.REDIRECT_URL, "");
////            weiboAuth.anthorize(new myWeiboAuthListener());
//            
//            AuthInfo weiboAuthInfo=new AuthInfo(mContext, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
//              mSso = new SsoHandler(mContext, weiboAuthInfo);
//             mSso.authorize(new myWeiboAuthListener());
//        }else {
//            openApiShare();
//        }
//    
//        
////        mWeiboAuth = new WeiboAuthListener(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
////        Intent i = new Intent();
////        String data = "http://service.weibo.com/share/share.php?url=http%3A%2F%2F1600.com%2F&title="
////                + shareStr;
////        Uri uri = Uri.parse(data);
////        i.setAction(Intent.ACTION_VIEW);
////        i.setData(uri);
////        mContext.startActivity(i);
//    }
//    
//    /**
//     * 通过openApi分享
//     */
//    private void openApiShare(){
//        //创建openApi
//        ShareWeiboApi shareWeiboApi =ShareWeiboApi.create(mContext, Constants.APP_KEY,mAccessToken.getToken());
//        if (isShareBitmap) {
//            //发送图片分享
//            shareWeiboApi.upload(shareString, shareBitmap, "0.0","0.0", new myRequestListener());
//        }else{
//            //发送文本  
//            shareWeiboApi.update(shareString, "0.0","0.0", new myRequestListener());
//        }
//        
//    }
//    
//    
//    
//    class myWeiboAuthListener implements WeiboAuthListener{
//
//        @Override
//        public void onCancel() {
//            //TODO 取消登录
//            LogUtil.e("用户取消登录");
//        }
//
//        @Override
//        public void onComplete(Bundle values) {
//            // TODO 登录成功
//            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
//            if (mAccessToken.isSessionValid()) {
//                // 保存 Token 到 SharedPreferences
//                AccessTokenKeeper.keepAccessToken(mContext, mAccessToken);
//                openApiShare();
//            } else {
//                // 当您注册的应用程序签名不正确时，就会收到 Code，请确保签名正确
//                String code = values.getString("code");
//                if (!TextUtils.isEmpty(code)) {
//                    LogUtil.e("新浪微博登录异常 - 签名程序不符合->"+code);
//                }
//            }
//        }
//
//        @Override
//        public void onWeiboException(WeiboException arg0) {
//            // TODO 登录异常
//            LogUtil.e("异常:"+arg0.getMessage());
//        }
//        
//    }
//    
//    class myRequestListener implements RequestListener{
//        @Override
//        public void onComplete(String arg0) {
//            //成功分享
//            LogUtil.d("成功分享:"+arg0);
//        }
//
//        @Override
//        public void onWeiboException(WeiboException arg0) {
//            //分享异常
//            LogUtil.e("分享异常:"+arg0.getMessage());
//        }
//        
//    }
//    
//    /**
//     * 从当前应用唤起微博并进行分享后，返回到当前应用时，需要在此处调用该函数 来接收微博客户端返回的数据；执行成功，返回 true，并调用
//     * {@link IWeiboHandler.Response#onResponse}； 失败返回 false，不调用上述回调
//     */
//    public void handleWeiboResponse() {
//        mWeiboShareAPI.handleWeiboResponse(mContext.getIntent(), new MWeiboResponse());
//    }
//
//    class MWeiboResponse implements IWeiboHandler.Response {
//        /**
//         * 接收微客户端博请求的数据。 当微博客户端唤起当前应用并进行分享时，该方法被调用。
//         * 
//         * @param baseRequest 微博请求数据对象
//         * @see {@link IWeiboShareAPI#handleWeiboRequest}
//         */
//        @Override
//        public void onResponse(BaseResponse baseResp) {
//            switch (baseResp.errCode) {
//                case WBConstants.ErrorCode.ERR_OK: // 发送成功
//                    Toast.makeText(mContext, "发送成功", Toast.LENGTH_LONG).show();
//                    break;
//                case WBConstants.ErrorCode.ERR_CANCEL: // 发送取消
//                    Toast.makeText(mContext, "发送取消", Toast.LENGTH_LONG).show();
//                    break;
//                case WBConstants.ErrorCode.ERR_FAIL: // 发送失败
//                    Toast.makeText(mContext, "发送失败  Error Message: " + baseResp.errMsg,
//                            Toast.LENGTH_LONG).show();
//                    break;
//            }
//        }
//    }
//    
//    /**
//     * 第三方应用发送请求消息到微博，唤起微博分享界面。
//     * 注意：当 {@link IWeiboShareAPI#getWeiboAppSupportAPI()} >= 10351 时，支持同时分享多条消息，
//     * 同时可以分享文本、图片以及其它媒体资源（网页、音乐、视频、声音中的一种）。
//     * 
//     * @param hasText    分享的内容是否有文本
//     * @param hasImage   分享的内容是否有图片
//     * @param hasWebpage 分享的内容是否有网页
//     * @param hasMusic   分享的内容是否有音乐
//     * @param hasVideo   分享的内容是否有视频
//     * @param hasVoice   分享的内容是否有声音
//     */
//    private void sendMultiMessage(boolean hasText, boolean hasImage, boolean hasWebpage/*,
//            boolean hasMusic, boolean hasVideo, boolean hasVoice*/) {
//        
//        // 1. 初始化微博的分享消息
//        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
//        if (hasText) {
//            weiboMessage.textObject = getTextObj();
//        }
//        
//        if (hasImage) {
//            weiboMessage.imageObject = getImageObj();
//        }
//        if (hasWebpage) {
//            weiboMessage.mediaObject = getWebpageObj();
//        }
//        
//        
//        // 用户可以分享其它媒体资源（网页、音乐、视频、声音中的一种）
////        if (hasWebpage) {
////            weiboMessage.mediaObject = getWebpageObj();
////        }
////        if (hasMusic) {
////            weiboMessage.mediaObject = getMusicObj();
////        }
////        if (hasVideo) {
////            weiboMessage.mediaObject = getVideoObj();
////        }
////        if (hasVoice) {
////            weiboMessage.mediaObject = getVoiceObj();
////        }
//        
//        // 2. 初始化从第三方到微博的消息请求
//        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
//        // 用transaction唯一标识一个请求
//        request.transaction = String.valueOf(System.currentTimeMillis());
//        request.multiMessage = weiboMessage;
//        // 3. 发送请求消息到微博，唤起微博分享界面
//        mWeiboShareAPI.sendRequest(mContext, request);
//        LogUtil.i("发送消息到微博");
//    }
//    
//    
//    /**
//     * 第三方应用发送请求消息到微博，唤起微博分享界面。
//     * 当{@link IWeiboShareAPI#getWeiboAppSupportAPI()} < 10351 时，只支持分享单条消息，即
//     * 文本、图片、网页、音乐、视频中的一种，不支持Voice消息。
//     * 
//     * @param hasText    分享的内容是否有文本
//     * @param hasImage   分享的内容是否有图片
//     * @param hasWebpage 分享的内容是否有网页
//     * @param hasMusic   分享的内容是否有音乐
//     * @param hasVideo   分享的内容是否有视频
//     */
//    private void sendSingleMessage(boolean hasText, boolean hasImage, boolean hasWebpage/*,
//            boolean hasMusic, boolean hasVideo, boolean hasVoice*/) {
//        
//        // 1. 初始化微博的分享消息
//        // 用户可以分享文本、图片、网页、音乐、视频中的一种
//        WeiboMessage weiboMessage = new WeiboMessage();
//        if (hasText) {
//            weiboMessage.mediaObject = getTextObj();
//        }
//        if (hasImage) {
//            weiboMessage.mediaObject = getImageObj();
//        }
//        if (hasWebpage) {
//            weiboMessage.mediaObject = getWebpageObj();
//        }
////        if (hasMusic) {
////            weiboMessage.mediaObject = getMusicObj();
////        }
////        if (hasVideo) {
////            weiboMessage.mediaObject = getVideoObj();
////        }
//        /*if (hasVoice) {
//            weiboMessage.mediaObject = getVoiceObj();
//        }*/
//        
//        // 2. 初始化从第三方到微博的消息请求
//        SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
//        // 用transaction唯一标识一个请求
//        request.transaction = String.valueOf(System.currentTimeMillis());
//        request.message = weiboMessage;
//        
//        // 3. 发送请求消息到微博，唤起微博分享界面
////        mWeiboShareAPI.sendRequest(mContext,request);
//        
//        AuthInfo weiboAuthInfo=new AuthInfo(mContext, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
//        WeiboAuthListener arg4 =new WeiboAuthListener() {
//            @Override
//            public void onWeiboException(WeiboException arg0) {
//                arg0.printStackTrace();
//            }
//            
//            @Override
//            public void onComplete(Bundle arg0) {
//                LogUtil.i("成功 "+arg0.toString());
//            }
//            
//            @Override
//            public void onCancel() {
//                LogUtil.i("取消");
//            }
//        };
//        mWeiboShareAPI.sendRequest(mContext, request, weiboAuthInfo, "", arg4 );
//    }
//
//    /**
//     * 创建多媒体（音频）消息对象。
//     * 
//     * @return 多媒体（音乐）消息对象。
//     */
//    private VoiceObject getVoiceObj() {
//        // 创建媒体消息
//        VoiceObject voiceObject = new VoiceObject();
////        voiceObject.identify = Utility.generateGUID();
////        voiceObject.title = mShareVoiceView.getTitle();            //标题
////        voiceObject.description = mShareVoiceView.getShareDesc();  //描述
////        // 设置 Bitmap 类型的图片到视频对象里
////        voiceObject.setThumbImage(mShareVoiceView.getThumbBitmap());
////        voiceObject.actionUrl = mShareVoiceView.getShareUrl();
////        voiceObject.dataUrl = "www.weibo.com";
////        voiceObject.dataHdUrl = "www.weibo.com";
////        voiceObject.duration = 10;
////        voiceObject.defaultText = "Voice 默认文案";
//        return voiceObject;
//    }
//
//
//    /**
//     * 创建多媒体（视频）消息对象。
//     * 
//     * @return 多媒体（视频）消息对象。
//     */
//    private VideoObject getVideoObj() {
//        // 创建媒体消息
//        VideoObject videoObject = new VideoObject();
////        videoObject.identify = Utility.generateGUID();
////        videoObject.title = mShareVideoView.getTitle();
////        videoObject.description = mShareVideoView.getShareDesc();
////        
////        // 设置 Bitmap 类型的图片到视频对象里
////        videoObject.setThumbImage(mShareVideoView.getThumbBitmap());
////        videoObject.actionUrl = mShareVideoView.getShareUrl();
////        videoObject.dataUrl = "www.weibo.com";
////        videoObject.dataHdUrl = "www.weibo.com";
////        videoObject.duration = 10;
////        videoObject.defaultText = "Vedio 默认文案";
//        return videoObject;
//    }
//    
//
//    /**
//     * 创建多媒体（音乐）消息对象。
//     * 
//     * @return 多媒体（音乐）消息对象。
//     */
//    private MusicObject getMusicObj() {
//        // 创建媒体消息
//        MusicObject musicObject = new MusicObject();
////        musicObject.identify = Utility.generateGUID();
////        musicObject.title = mShareMusicView.getTitle();
////        musicObject.description = mShareMusicView.getShareDesc();
////        
////        // 设置 Bitmap 类型的图片到视频对象里
////        musicObject.setThumbImage(mShareMusicView.getThumbBitmap());
////        musicObject.actionUrl = mShareMusicView.getShareUrl();
////        musicObject.dataUrl = "www.weibo.com";
////        musicObject.dataHdUrl = "www.weibo.com";
////        musicObject.duration = 10;
////        musicObject.defaultText = "Music 默认文案";
//        return musicObject;
//    }
//    
//    /**
//     * 创建多媒体（网页）消息对象。
//     * 
//     * @return 多媒体（网页）消息对象。
//     */
//    private BaseMediaObject getWebpageObj() {
//        WebpageObject mediaObject = new WebpageObject();
//        mediaObject.identify = Utility.generateGUID();
//        mediaObject.title =shareString;
//        mediaObject.description = ""; //描述
//        
//        // 设置 Bitmap 类型的图片到视频对象里
//        mediaObject.setThumbImage(shareBitmap);
//        mediaObject.actionUrl = urlString;
//        mediaObject.defaultText = "Webpage 默认文案";
//        return mediaObject;
//    }
//    
//    /**
//     * 分享图片消息
//     * @return
//     */
//    private ImageObject getImageObj() {
//        ImageObject imageObject = new ImageObject();
//        if (shareBitmap!=null) {
//            imageObject.setImageObject(shareBitmap);
//        }
//        return imageObject;
//    }
//    
//    /**
//     * 分享文本
//     * @return
//     */
//    private TextObject getTextObj() {
//        TextObject txtObject =new TextObject();
//        if (!TextUtils.isEmpty(shareString)) {
//            txtObject.text =shareString;
//        }
//        return txtObject;
//    }
//}
