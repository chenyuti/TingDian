
package com.logansoft.UIEngine.utils.images.manage;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.logansoft.UIEngine.utils.FileUtil;
import com.logansoft.UIEngine.utils.LogUtil;
import com.logansoft.UIEngine.utils.ThreadUtil;
import com.logansoft.UIEngine.utils.http.RequestManager;
import com.logansoft.UIEngine.utils.images.cache.BitmapLruCache;
import com.logansoft.UIEngine.utils.images.cache.DiskLruImageCache;
import com.nostra13.universalimageloader.utils.StorageUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import java.io.File;
import java.math.BigDecimal;

/**
 * Implementation of volley's ImageCache interface. This manager tracks the
 * application image loader and cache. Volley recommends an L1 non-blocking
 * cache which is the default MEMORY CacheType.
 * 
 * @author Trey Robinson
 */
public class ImageCacheManager {

    /**
     * Volley recommends in-memory L1 cache but both a disk and memory cache are
     * provided. Volley includes a L2 disk cache out of the box but you can
     * technically use a disk cache as an L1 cache provided you can live with
     * potential i/o blocking.
     */
    public enum CacheType {
        DISK, MEMORY, DISK_MEMORY ,NULL
    }

    private static ImageCacheManager mInstance;

    /**
     * Volley image loader
     */
    private ImageLoader mImageLoader;

    /**
     * Image cache implementation
     */
    private ImageCache mImageCache;

    private BitmapLruCache mImageCacheMemory;

    /**
     * @return instance of the cache manager
     */
    public static ImageCacheManager getInstance() {
        if (mInstance == null)
            mInstance = new ImageCacheManager();

        return mInstance;
    }

    /**
     * Initializer for the manager. Must be called prior to use.
     * 
     * @param context application context
     * @param uniqueName name for the cache location
     * @param cacheSize max size for the cache
     * @param compressFormat file type compression format.
     * @param quality
     */
    public void init(Context context, String uniqueName, int cacheSize,
            CompressFormat compressFormat, int quality, CacheType type) {
        switch (type) {
            case DISK:
                mImageCache = new DiskLruImageCache(context, uniqueName, cacheSize, compressFormat,
                        quality);
                mImageCacheMemory = null;
                break;
            case DISK_MEMORY:
                mImageCache = new DiskLruImageCache(context, uniqueName, cacheSize, compressFormat,
                        quality);
                mImageCacheMemory = new BitmapLruCache(cacheSize);
                break;
            case MEMORY:
                mImageCache = new BitmapLruCache(cacheSize);
                mImageCacheMemory = null;
                break;
            case NULL:
                break;
            default:
                mImageCache = new BitmapLruCache(cacheSize);
                mImageCacheMemory = null;
                break;
        }
        mImageLoader = new ImageLoader(RequestManager.getRequestQueue(), mImageCache);
    }

    /**
     * 清除缓存
     */
    public void clearCache() {
        if (mImageCache instanceof DiskLruImageCache) {
            new Thread() {
                public void run() {
                    ((DiskLruImageCache)mImageCache).getCacheFolder().delete();
                    if (mImageCacheMemory!=null) {
                        mImageCacheMemory.evictAll();
                    }
                };
            }.start();
        } else if (mImageCache instanceof BitmapLruCache) {
            if (((BitmapLruCache)mImageCache).size() > 0) {
                ((BitmapLruCache)mImageCache).evictAll();
            }
        }
    }
    /**
     * 清除缓存
     */
    public void clearCache2() {
        com.nostra13.universalimageloader.core.ImageLoader.getInstance().clearDiscCache();
    }
    
    
    public double getCacheSize2(Context context){
        try {
            File file = StorageUtils.getCacheDirectory(context);
            double size = FileUtil.getDirSize(file);
            BigDecimal b = new BigDecimal(size);
            double f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            return f1;
        } catch (Exception e) {
            e.printStackTrace();
        }
       return 0.0;
    }
    
    /**
     * 获取缓存大小
     * 
     * @return
     */
    public double getCacheSize() {
        if (mImageCache instanceof DiskLruImageCache) {
            File file = ((DiskLruImageCache)mImageCache).getCacheFolder();
            double size = FileUtil.getDirSize(file);
            BigDecimal b = new BigDecimal(size);
            double f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            return f1;
        } 
        return 0.0;
    }

    public Bitmap getBitmap(String url) {
        try {
            Bitmap bitmap = null;
            if (mImageCacheMemory != null) {
                bitmap = mImageCacheMemory.getBitmap(createKey(url));
            }

            if (bitmap == null) {
                bitmap = mImageCache.getBitmap(createKey(url));
            }

            return bitmap;
        } catch (NullPointerException e) {
            throw new IllegalStateException("Disk Cache Not initialized");
        }
    }

    public void putBitmap(String url,final Bitmap bitmap) {
        try {
            
           final String key = createKey(url);
           if (mImageCacheMemory != null) {
               mImageCacheMemory.putBitmap(createKey(url), bitmap);
           }
            if (mImageCache instanceof DiskLruImageCache) {
                 boolean contains = ((DiskLruImageCache)mImageCache).containsKey(key);
                 if (!contains) {
                     ThreadUtil.sThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            mImageCache.putBitmap(key, bitmap);
                        }
                    });
                }
            }
        } catch (NullPointerException e) {
            throw new IllegalStateException("Disk Cache Not initialized");
        }
    }

    /**
     * Executes and image load
     * 
     * @param url location of image
     * @param listener Listener for completion
     */
    public void getImage(String url, ImageListener listener) {
        mImageLoader.get(url, listener);
    }

    /**
     * @return instance of the image loader
     */
    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    /**
     * Creates a unique cache key based on a url value
     * 
     * @param url url to be used in key creation
     * @return cache key value
     */
    private String createKey(String url) {
        return String.valueOf(url.hashCode());
    }

}
