package com.logansoft.UIEngine.utils;

public class DebugUtil {
    
    private static long time;
    
    public static synchronized long getTime() {
        return time=System.currentTimeMillis();
    }
    
    public static void stratTime(){
        getTime();
    }
    
    public static void endTime(String i){
        long temp=time;
        long a = getTime() - temp;
        LogUtil.d(i+" 用时->"+a);
    }
}
