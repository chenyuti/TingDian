package com.logansoft.UIEngine.utils.cache;

import com.logansoft.UIEngine.utils.LogUtil;

import java.util.HashMap;

/**
 * 用来管理内存
 * @author Prosper.Z
 *
 */
public class CacheManager {

    private static CacheManager mCacheManager;
//    private MCache sqlCache;
    private HashMap<String, UIAction> cacheListener=new HashMap<String, UIAction>();
    
    private CacheManager(){
    }
    
    public static CacheManager getCacheManager(){
        if (mCacheManager==null) {
            mCacheManager =new CacheManager();
        }
        return mCacheManager; 
    }
    
//    public void runSQLListener(){
//        if(sqlCache==null){
//            LogUtil.e("未设置SQLListener ,请在主方法内添加监听");
//        }else {
//            sqlCache.run();
//        }
//        
//    }
    
    public void runAction(String key,Object c){
        UIAction action = cacheListener.get(key);
        if (action==null) {
            LogUtil.e(key+" 未添加 ;"+key+" Did not add");
            return;
        }
        action.run(c);
    }
    
    
    public void addAction(String key,UIAction action){
        cacheListener.put(key, action);
    }
    
//    public void setSQLCache(MCache cache){
//        sqlCache = cache ;
//    }
//    
    
}
