package com.logansoft.UIEngine.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class contains a public static thread pool and can be easily used by the
 * other class.
 * 
 * @author Reginald
 * 
 */
public class ThreadUtil {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = Integer.MAX_VALUE;
    private static final int MAXIMUM_QUEUE_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE = 30;

    private static BlockingQueue<Runnable> sBlockingQueue = new LinkedBlockingQueue<Runnable>(MAXIMUM_QUEUE_SIZE);

    /**
     * A thread pool which core size is {@code CPU_COUNT + 1} and max pool size
     * is non-border.
     */
    public static ExecutorService sThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
	    TimeUnit.SECONDS, sBlockingQueue);
    
//    public static ExecutorService sThreadPool1 =Executors.newCachedThreadPool();
}
