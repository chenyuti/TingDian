
package com.logansoft.UIEngine.utils.threadUtil;

import com.logansoft.UIEngine.parse.XmlParser;
import com.logansoft.UIEngine.utils.FileUtil;
import com.logansoft.UIEngine.utils.GlobalConstants;
import com.logansoft.UIEngine.utils.http.HttpUtil;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncThread {
    // 为了加快速度，在内存中开启缓存（主要应用于重复图片较多时，或者同一个图片要多次被访问，比如在ListView时来回滚动）
    public Map<String, SoftReference<Drawable>> imageCache = new HashMap<String, SoftReference<Drawable>>();

    private ExecutorService executorService = Executors.newFixedThreadPool(5); // 固定五个线程来执行任务

    private final Handler handler = new Handler();

    /**
     * @param imageUrl 图像url地址
     * @param callback 回调接口
     * @return 返回内存中缓存的图像，第一次加载返回null
     */
    public Drawable loadDrawable(final String imageUrl, final ImageCallback callback) {
        // 如果缓存过就从缓存中取出数据
        if (imageCache.containsKey(imageUrl)) {
            SoftReference<Drawable> softReference = imageCache.get(imageUrl);
            if (softReference.get() != null) {
                return softReference.get();
            }
        }
        // 缓存中没有图像，则从网络上取出数据，并将取出的数据缓存到内存中
        executorService.submit(new Runnable() {
            public void run() {
                try {
                    final Drawable drawable = loadImageFromUrl(imageUrl);

                    imageCache.put(imageUrl, new SoftReference<Drawable>(drawable));

                    handler.post(new Runnable() {
                        public void run() {
                            callback.imageLoaded(drawable);
                        }
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return null;
    }

    // 从网络上取数据方法
    protected Drawable loadImageFromUrl(String imageUrl) {
        try {
            // 测试时，模拟网络延时，实际时这行代码不能有
            SystemClock.sleep(2000);

            return Drawable.createFromStream(new URL(imageUrl).openStream(), "image.png");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 对外界开放的回调接口
    public interface ImageCallback {
        // 注意 此方法是用来设置目标对象的图像资源
        public void imageLoaded(Drawable imageDrawable);
    }
    
    // 对外界开放的回调接口
    public interface DataCallback {
        public void dataLoaded(String imageDrawable);
    }
    
    // 对外界开放的回调接口
    public interface TreadCallback {
        public void treadLoaded(InputStream data);
    }
    
    
    /**
     * @param url url地址
     * @param callback 回调接口
     * @return 返回内存中缓存的图像，第一次加载返回null
     */
    public String loadData(final String url,final HashMap<String, String>  params,final DataCallback callback) {
        executorService.submit(new Runnable() {
            public void run() {
                try {
//                     String a = HttpUtil.parseParams(params);
                     
                    final String reslut = HttpUtil.POST(url, params,true);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.dataLoaded(reslut);
                        }
                    });
                } catch (Exception e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.dataLoaded("网络请求失败");
                        }
                    });
                    e.printStackTrace();
                }
            }
        });
        return null;
    }
    
    
    public String loadThread(final XmlParser parseXMLToView,final String  fileName,final TreadCallback callback) {
        executorService.submit(new Runnable() {
            public void run() {
                try {
                    final InputStream inputStream = FileUtil.readFileFromSDCard(GlobalConstants.TEMP_PATH+fileName);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.treadLoaded(inputStream);
                        }
                    });
                } catch (Exception e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.treadLoaded(null);
                        }
                    });
                    e.printStackTrace();
                }
            }
        });
        return null;
    }
    
    
    
    
}
