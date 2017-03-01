
package com.logansoft.UIEngine.parse.xmlview;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;

import org.w3c.dom.Element;

import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.utils.GlobalConstants;
import com.logansoft.UIEngine.utils.LogUtil;
import com.logansoft.UIEngine.utils.MD5;
import com.logansoft.UIEngine.utils.StreamUtil;
import com.logansoft.UIEngine.utils.ThreadUtil;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.URLUtil;
import android.widget.MediaController;
import android.widget.VideoView;

public class MVideoView extends BaseView implements OnPreparedListener, OnCompletionListener,
        OnErrorListener {
    VideoView mVideoView;

    private String remoteUrl;

    private String localUrl;

    private ProgressDialog progressDialog = null;
    
    boolean loadEndButUnReady =false;

    
    private static final int READY_BUFF = 2000 * 1024;
    private static final int CACHE_BUFF = 500 * 1024;
    private static final String ATTR_ONCOMPLETION="onCompletion"; //视频播放完成
    private static final String ATTR_ONPREPARED="onPrepared"; //准备视频播放
    private static final String ATTR_CONTROLLER="controller"; //控制条

    private boolean isready = false;
    private boolean iserror = false;
    private int errorCnt = 0;
    private int curPosition = 0;
    private long mediaLength = 0;
    private long readSize = 0;
    int playIndex=0;
    
    LinkedList<String> urlLinked=new LinkedList<String>();

    public MVideoView(BaseFragment fragment,GroupView parentView,Element element) {
        super(fragment,parentView,element);
    }
    @Override
    protected void createMyView(){
        mView = mVideoView = new VideoView(mContext){
    		@Override
    		public void draw(Canvas canvas) {
    			bdraw(canvas);
    			super.draw(canvas);
    			adraw(canvas);
    		}
    	};
    }
    @Override
    protected void parseView() {
    	super.parseView();
         String controller = attrMap.get(ATTR_CONTROLLER);
         if (!TextUtils.isEmpty(controller)) {
             mVideoView.setMediaController(new MediaController(mContext)); // 控制条
        }
        String url = attrMap.get("url");
        setUrl(url);     
        getLuaListener();
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnErrorListener(this);
    }
    
    public int getPlayIndex() {
        return playIndex;
    }
    
    public void addUrl(String url) {
        urlLinked.add(url);
    }
    
    public boolean removeUrl(String url){
      return   urlLinked.remove(url);
    }
    
    public void start() {
		mVideoView.start();
    }
    
    public void stop(){
        mVideoView.stopPlayback();
    }
    
    public void pause(){
        mVideoView.pause();
    }
    
    public void resume(){
        mVideoView.resume();
    }
    
   public int getPlayListSize(){
       return urlLinked.size();
   }
    
    
    public void setUrl(String url){
        if (!TextUtils.isEmpty(url)) {
            if (url.startsWith("res://")) {
              remoteUrl= url.replaceAll("res://","");
              File file=new File(mContext.getExternalFilesDir("VideoCache/"), remoteUrl);  //GlobalConstants.ROOT_PATH + "/VideoCache/" + fileName + ".mp4";
              if (!file.exists()) {
                  try {
                      file.getParentFile().mkdirs();
                      file.createNewFile();
                      InputStream is = mContext.getAssets().open("video/"+remoteUrl);
                      StreamUtil.inputStreamToFile(is, file);
                  } catch (IOException e) {
                      e.printStackTrace();
                  }
              }
              remoteUrl =file.getAbsolutePath();
            }else if(URLUtil.isNetworkUrl(url)){
                remoteUrl = url;
            }else {
                remoteUrl = GlobalConstants.ROOT_URL+url;
            }
        }
    }
    
    private void getLuaListener() {
      final String onclick = attrMap.get(ATTR_CLICK);
        mVideoView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!TextUtils.isEmpty(onclick)) {
                        	executeLua(onclick);
                        }
                        break;
                }
                return false;
            }
        });
        
    }

    public void play(){
        try {
            playvideo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
   
    @Override
    public void onPrepared(MediaPlayer mediaplayer) {
        // TODO 视频播放准备监听
        LogUtil.i("onPrepared-----------");
        dismissProgressDialog();
        mVideoView.seekTo(curPosition);
        mVideoView.start();
        String onPrepared = attrMap.get(ATTR_ONPREPARED);
        if (!TextUtils.isEmpty(onPrepared)) {
        	executeLua(onPrepared);
        }
    }
    
    /**
     *  播放完成
     */
    @Override
    public void onCompletion(MediaPlayer mediaplayer) {
        mHandler.removeMessages(VIDEO_STATE_UPDATE);
        LogUtil.i("onCompletion ------------");
        curPosition = 0;
        mVideoView.stopPlayback();
        String onCompletion = attrMap.get(ATTR_ONCOMPLETION);
        if (!TextUtils.isEmpty(onCompletion)) {
        	executeLua(onCompletion);
        }
        
        if(++playIndex<urlLinked.size()){
            LogUtil.i("-----------playIndex-----"+playIndex);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setUrl(urlLinked.get(playIndex));
                    isready=false;
                    play();
                }
            }, 1200);
        }
    }
    /**
     * 播放错误时
     */
    @Override
    public boolean onError(MediaPlayer mediaplayer, int i, int j) {
        LogUtil.i("onError ------------");
        iserror = true;
        errorCnt++;
//        mediaplayer.stop();
//        dismissProgressDialog();
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
        }else {
            mVideoView.stopPlayback();
        }
        
        if (loadEndButUnReady) {
            showProgressDialog();
            mHandler.sendEmptyMessage(CACHE_VIDEO_END);
        } 
        return true;
    }

    // 设置时间进图条
    private void showProgressDialog() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (progressDialog == null) {
                    progressDialog = ProgressDialog.show(mContext, "视频缓存", "正在努力加载中 ...",
                            true, false);
                }
            }
        });
    }

    // 销毁进度条
    private void dismissProgressDialog() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
            }
        });
    }

    private void playvideo() {
        if (TextUtils.isEmpty(this.remoteUrl)) {
            return ;
        }
        // 判断网页是否有效
        if (!URLUtil.isNetworkUrl(this.remoteUrl)) {
            mVideoView.setVideoPath(this.remoteUrl);
            mVideoView.start();
            return;
        }
        showProgressDialog();
         ThreadUtil.sThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                FileOutputStream out = null;
                InputStream is = null;
                try {
                    URL url = new URL(remoteUrl);
                    String suffix = remoteUrl.substring(remoteUrl.lastIndexOf("."), remoteUrl.length()) ;
                    String fileName = MD5.toMD5(remoteUrl);
                    File cacheFile = new File(mContext.getExternalFilesDir("VideoCache/"),fileName+ suffix);
                        // 设置文件路径
                        localUrl =cacheFile.getAbsolutePath();
                        LogUtil.i("localUrl ->"+localUrl);

                    SharedPreferences sp = mContext.getSharedPreferences("VideoCache", Context.MODE_PRIVATE);
                    if (!cacheFile.exists()) {
                        cacheFile.getParentFile().mkdirs();
                        cacheFile.createNewFile();
                    }
                        

                     readSize = cacheFile.length();
                     mediaLength=sp.getLong(fileName+"mediaLength", -1);
                    if(mediaLength==readSize){
                        mHandler.sendEmptyMessage(CACHE_VIDEO_READY);
                        dismissProgressDialog();
                        return ;
                    }
                    
                    
                    HttpURLConnection httpConnection = (HttpURLConnection)url.openConnection();
                    // 将文件转换成文件输出流
                    out = new FileOutputStream(cacheFile, true);
                    // 设置网络将文件转换成IO流
                    httpConnection.setRequestProperty("User-Agent", "NetFox");
                    httpConnection.setRequestProperty("RANGE", "bytes=" + readSize + "-");
                    mediaLength = httpConnection.getContentLength();
                    if(readSize == 0){
                        sp.edit().putLong(fileName+"mediaLength",mediaLength).commit();
                    }
                    
                    
                    is = httpConnection.getInputStream();

                    if (mediaLength == -1) {
                        return;
                    }

                    mediaLength += readSize;

                    byte buf[] = new byte[4 * 1024];
                    int size = 0;
                    long lastReadSize = 0;

                    mHandler.sendEmptyMessage(VIDEO_STATE_UPDATE);

                    while ((size = is.read(buf)) != -1) {
                        try {
                            out.write(buf, 0, size);
                            readSize += size;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (!isready) {
                            if ((readSize - lastReadSize) > READY_BUFF) {
                                lastReadSize = readSize;
                                mHandler.sendEmptyMessage(CACHE_VIDEO_READY);
                            }
                        } else {
                            if ((readSize - lastReadSize) > CACHE_BUFF * (errorCnt + 1)) {
                                lastReadSize = readSize;
                                mHandler.sendEmptyMessage(CACHE_VIDEO_UPDATE);
                            }
                        }
                    }
                    mHandler.sendEmptyMessage(CACHE_VIDEO_END);
                    LogUtil.i("CACHE_VIDEO_END ------------------------");
                    if (!isready) {
                        mHandler.sendEmptyMessage(CACHE_VIDEO_READY);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    dismissProgressDialog();
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            //
                        }
                    }

                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            //
                        }
                    }
                }
            }
        });
    }

    private final static int VIDEO_STATE_UPDATE = 0;

    private final static int CACHE_VIDEO_READY = 1;

    private final static int CACHE_VIDEO_UPDATE = 2;

    private final static int CACHE_VIDEO_END = 3;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case VIDEO_STATE_UPDATE:
                    double cachepercent = readSize * 100.00 / mediaLength * 1.0;
                    String s = String.format("已缓存: [%.2f%%]", cachepercent);
                    if (mVideoView.isPlaying()) {
                        curPosition = mVideoView.getCurrentPosition();
                        int duration = mVideoView.getDuration();
                        duration = duration == 0 ? 1 : duration;

                         double playpercent = curPosition * 100.00 / duration
                         * 1.0;
                         int i = curPosition / 1000;
                         int hour = i / (60 * 60);
                         int minute = i / 60 % 60;
                         int second = i % 60;
                        
                         s += String.format(" 播放: %02d:%02d:%02d [%.2f%%]",
                         hour,
                         minute, second, playpercent);
                    }
                    LogUtil.i("VIDEO_UPDATE"+s);
                    mHandler.sendEmptyMessageDelayed(VIDEO_STATE_UPDATE, 1000);
                    break;

                case CACHE_VIDEO_READY:
                        mVideoView.setVideoPath(localUrl);
                        mVideoView.start();
                        isready = true;
                        if (iserror) {
                            isready = false;
                        }
                    break;

                case CACHE_VIDEO_UPDATE:
                    if (iserror) {
                        mVideoView.setVideoPath(localUrl);
                        mVideoView.start();
                        iserror = false;
                        dismissProgressDialog();
                    }
                    break;

                case CACHE_VIDEO_END:
                    
                    if (iserror) {
                        mVideoView.setVideoPath(localUrl);
                        mVideoView.start();
                        iserror = false;
                        loadEndButUnReady =false;
                    }else {
                        loadEndButUnReady=true;
                    }
                    break;
            }

            super.handleMessage(msg);
        }
    };
    
    @Override 
    public void setValue(String value){
        setUrl(value);        
        if (!mVideoView.isPlaying()) {
           play();        
        }
    }
//    @Override
//    public void updateView(Object dataMap) {
//        }else if (dataMap instanceof Element) {
//            ArrayList<BaseEntity> data = (ArrayList<BaseEntity>)XmlParser.getParser()
//                    .parserListItem((Element)dataMap);
//            for (BaseEntity map : data) {
//                urlLinked.add((String)map.getValue().get("url"));
//            }
//            playIndex=0;
//            if (urlLinked.size()>0) {
//                if (!mVideoView.isPlaying()) {
//                    setUrl(urlLinked.getFirst());        
//                    play();
//                }
//            }
//        }
//    }
}
