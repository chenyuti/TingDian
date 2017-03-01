package com.logansoft.UIEngine.utils.images;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.logansoft.UIEngine.utils.Configure;
import com.logansoft.UIEngine.utils.FileUtil;
import com.logansoft.UIEngine.utils.GlobalConstants;
import com.logansoft.UIEngine.utils.StreamUtil;
import com.logansoft.UIEngine.utils.ThreadUtil;
import com.logansoft.UIEngine.utils.http.RequestManager;
import com.logansoft.UIEngine.utils.images.cache.BitmapLruCache;
import com.logansoft.UIEngine.utils.ninePatch.ImageLoadingResult;
import com.logansoft.UIEngine.utils.ninePatch.NinePatchChunk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.PorterDuff.Mode;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitmapUtil {
    public BitmapLruCache cache ;
    private static BitmapUtil mInstance;
    
    /**
     * @return instance of the cache manager
     */
    public static BitmapUtil getInstance() {
        if (mInstance == null){
            mInstance = new BitmapUtil();
        }

        return mInstance;
    }
   
    private BitmapUtil(){
        cache =new BitmapLruCache(10*1024*1024);
    }
    
    /**
     * 加载图片,从本地,网络.res中
     * @param context
     * @param mNetworkListener
     * @param errorListener
     * @param fileName
     * @return
     */
    public Drawable getDrawable(Context context,Listener<Drawable> mNetworkListener,ErrorListener errorListener ,String fileName){
        Drawable  d= null;
        if (fileName != null && !"".equals(fileName)){
            Uri uri = Uri.parse(fileName);
            if ("res".equals(uri.getScheme())) {
                    d= getDrawableByAssets(context,uri.getAuthority(),mNetworkListener,errorListener);
            }else if ("http".equals(uri.getScheme())) {
                d=getDrawableByNet(context ,mNetworkListener,errorListener,fileName);
            }else if ("local".equals(uri.getScheme())) {//本地sd卡获取图片
                    String path=uri.getAuthority()+uri.getPath();
                    d =Drawable.createFromPath(path);
            }else {
                d=getDrawableByNet(context,mNetworkListener,errorListener ,GlobalConstants.ROOT_URL
                        +fileName);
            }
        }
        return d ;
    }

    /**
     * 获取资源文件下的图片
     * @param context
     * @param fileName
     * @return
     */
    public  Drawable getDrawableByAssets(Context context,String fileName,Listener<Drawable> mNetworkListener,ErrorListener errorListener) {
        ThreadUtil.sThreadPool.execute(new MyRunable(context, fileName, mNetworkListener, errorListener));
//		InputStream is = null;
//		Drawable drawable =null;
//		try {
//			is = context.getAssets().open(Configure.PAGE_IMAGES_PATH+"/"+fileName);
//			if (fileName.contains(".9.")) {
//			   ImageLoadingResult imageLR= NinePatchChunk.createChunkFromRawBitmap(context, is,240);
//			   drawable= imageLR.getNinePatchDrawable(context.getResources(), fileName);
//            }else {
//                drawable =  Drawable.createFromStream(is, fileName);
//            }
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			if (is != null) {
//				try {
//					is.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//
//			}
//		}
		return null;
	}
    
    class MyRunable implements Runnable {
        Context mContext;
        String mFileName;
        Listener<Drawable> mNetworkListener;
        ErrorListener mErrorListener;
        public MyRunable(Context context,String fileName,Listener<Drawable> networkListener,ErrorListener errorListener) {
              mContext=context;
              mFileName=fileName;
              mNetworkListener=networkListener;
              mErrorListener=errorListener;
        }
        
        public void run() {
            InputStream is = null;
            Drawable drawable =null;
            try {
                is = mContext.getAssets().open(Configure.PAGE_IMAGES_PATH+"/"+mFileName);
                if (mFileName.contains(".9.")) {
                   ImageLoadingResult imageLR= NinePatchChunk.createChunkFromRawBitmap(mContext, is,240);
                   drawable= imageLR.getNinePatchDrawable(mContext.getResources(), mFileName);
                }else {
                    drawable =  Drawable.createFromStream(is, mFileName);
                }
                mNetworkListener.onResponse(drawable);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }
	
	public  Drawable getDrawableByNet(final Context context,final Listener<Drawable> mNetworkListener,final ErrorListener errorListener , String url) {
            ImageListener listener =new ImageListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    errorListener.onErrorResponse(error);
                }
                
                @Override
                public void onResponse(ImageContainer response, boolean isImmediate) {
                    if (response.getBitmap() != null) {
                        mNetworkListener.onResponse(new BitmapDrawable(context.getResources(), response.getBitmap()));
                    }
                }
            };
                RequestManager.getImageLoader().get(url,  listener);
	    return null;
    }
	
	   public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
	        ByteArrayOutputStream output = new ByteArrayOutputStream();
	        bmp.compress(CompressFormat.PNG, 100, output);
	        if (needRecycle) {
	            bmp.recycle();
	        }
	        
	        byte[] result = output.toByteArray();
	        try {
	            output.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        
	        return result;
	    }
	
    /**
     * drawable转bitmap
     * @param drawable
     * @return
     */
	public static Bitmap drawableToBitmap(Drawable drawable) {  
	    int width = drawable.getIntrinsicWidth();  
	    int height = drawable.getIntrinsicHeight();  
	    Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888  
	            : Bitmap.Config.RGB_565;  
	    Bitmap bitmap = Bitmap.createBitmap(width, height, config);  
	    Canvas canvas = new Canvas(bitmap);  
	    drawable.setBounds(0, 0, width, height);  
	    drawable.draw(canvas); 
	    return bitmap;  
	}  
	
	/**
     * 图片缩放  通过matrix比例 缩放Drawable   
     * @param drawable
     * @param w
     * @param h
     * @return
     */
    public static Drawable zoomDrawable(Drawable drawable, int w, int h) {  
        int width = drawable.getIntrinsicWidth();  
        int height = drawable.getIntrinsicHeight();  
        Bitmap oldbmp = drawableToBitmap(drawable);  
        Matrix matrix = new Matrix();  
        float scaleWidth = ((float) w / width);  
        float scaleHeight = ((float) h / height);  
        matrix.postScale(scaleWidth, scaleHeight);  
        Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,  
                matrix, true);  
        return new BitmapDrawable(null, newbmp);  
    } 
	
	/**
	 * 图片缩放   bitmmap
	 * @param photo
	 * @param newHeight
	 * @param context
	 * @return
	 */
	public static Bitmap scaleDownBitmap(Bitmap photo, int newHeight, Context context){                                                                                                                                                                                              
	    final float densityMultiplier = context.getResources().getDisplayMetrics().density;
	    int h = (int) (newHeight * densityMultiplier);
	    int w = (int) (h * photo.getWidth() / ((double) photo.getHeight()));
	    photo = Bitmap.createScaledBitmap(photo, w, h, true);
	    return photo;
	}
	
	public static Bitmap getZoomBitmap(Bitmap bitmap,float scale,float degrees){
		Matrix matrix = new Matrix(); 
		matrix.postScale(scale,scale); //
		matrix.postRotate(degrees);
		Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
		return resizeBmp;
	}
	
	
	/**
     * The method saves bitmap to a specific path in sdcard
     * 
     * @param bitmap
     *            Bitmap to save
     * @param filePath
     *            Path of the Bitmap file
     */
    public static void SaveBitmap2SDCard(Bitmap bitmap, String filePath) {
        // create a new file by file path
        
        File file = new File(filePath);
        if (!FileUtil.ifFileExistsOrNot(file.getParentFile().getAbsolutePath())) {
            file.getParentFile().mkdirs();// make dirs if parent file path are
                                            // not exist
        }
        
        if (file.exists()) {
            file.delete();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap.compress(CompressFormat.JPEG, 100, fos);
        StreamUtil.closeResource(null, null, null, fos);
        Log.i("info", "save bitmap complete>>> " + filePath);
    }
	
    /**
     * 获取指定路径内的指定大小图片 
     * @param filePath The path of source picture
     * @param width Width of output picture
     * @param height Height of output picture
     * @return Insceled picture
     */
    public static Bitmap getScaleBitmap(String filePath,int width,int height){
        
        Options opts = new Options();
        opts.inJustDecodeBounds = true;
        Bitmap bm = BitmapFactory.decodeFile(filePath, opts);
        int xScale = opts.outWidth / width;
        int yScale = opts.outHeight / height;
        opts.inSampleSize = (xScale>yScale)?xScale:yScale;
        opts.inJustDecodeBounds = false;
        
        bm = BitmapFactory.decodeFile(filePath, opts);
        return bm;
    }
    
    /**
     * 读取图片的旋转的角度
     *
     * @param path
     *            图片绝对路径
     * @return 图片的旋转角度
     */
    public  int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }
    
    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm
     *            需要旋转的图片
     * @param degree
     *            旋转角度
     * @return 旋转后的图片
     */
    public  Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;
      
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }
    
    /** 
     * 根据原图添加圆角 
     *  
     * @param source 
     * @return 
     */  
    public Bitmap createRoundConerImage(Bitmap source ,float mRadius)  
    {  
        final Paint paint = new Paint();  
        paint.setAntiAlias(true);  
        Bitmap target = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Config.ARGB_8888);  
        Canvas canvas = new Canvas(target);  
        RectF rect = new RectF(0, 0, source.getWidth(), source.getHeight());  
        canvas.drawRoundRect(rect, mRadius, mRadius, paint);  
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));  
        canvas.drawBitmap(source, 0, 0, paint);  
        source.recycle();
        return target;  
    } 
    /**
     * 获得圆角图片 
     * @param bitmap
     * @param roundPx
     * @return
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {  
        int w = bitmap.getWidth();  
        int h = bitmap.getHeight();  
        Bitmap output = Bitmap.createBitmap(w, h, Config.ARGB_8888);  
        Canvas canvas = new Canvas(output);  
        final int color = 0xff424242;  
        final Paint paint = new Paint();  
        final Rect rect = new Rect(0, 0, w, h);  
        final RectF rectF = new RectF(rect);  
        paint.setAntiAlias(true);  
        canvas.drawARGB(0, 0, 0, 0);  
        paint.setColor(color);  
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);  
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));  
        canvas.drawBitmap(bitmap, rect, rect, paint);  
        return output;  
    }  
    
    public static Bitmap createCircleImage(Bitmap source, int min)  
    {  
        final Paint paint = new Paint();  
        paint.setAntiAlias(true);  
        Bitmap target = Bitmap.createBitmap(min, min, Config.ARGB_8888);  
        /** 
         * 产生一个同样大小的画布 
         */  
        Canvas canvas = new Canvas(target);  
        /** 
         * 首先绘制圆形 
         */  
        canvas.drawCircle(min / 2, min / 2, min / 2, paint);  
        /** 
         * 使用SRC_IN 
         */  
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));  
        /** 
         * 绘制图片 
         */  
        canvas.drawBitmap(source, 0, 0, paint);  
        return target;  
    }  
	
}
