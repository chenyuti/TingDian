package com.logansoft.UIEngine.utils;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;


public class Statistics {
	static public final String TAG="Statistics";
	static final Runtime runtime = Runtime.getRuntime();

	static public class statisticsItem {
		public int count;
		public double costTimeMillis;
		public String itemName;

	}

	public static HashMap<String,statisticsItem> data;
	static {
		data = new HashMap<String,statisticsItem>();
	}

	public static void addItemNameandTimeMillis(String name, long timeMillis) {
//		//if(timeMillis>10)
//		{
//			LogUtil.i(TAG,name+" "+timeMillis+"ms");
//			
//		}
		statisticsItem item = data.get(name);
		if (item == null) {
			item = new statisticsItem();
			item.itemName = name;
			data.put(name, item);
		}
		item.costTimeMillis += timeMillis;
		item.count += 1;
	}
	public static void addItemNameandTimeNano(String name, long timeNano) {
//		//if(timeMillis>10)
//		{
//			LogUtil.i(TAG,name+" "+((float)timeNano)/1000000+"ms");
//			
//		}
		statisticsItem item = data.get(name);
		if (item == null) {
			item = new statisticsItem();
			item.itemName = name;
			data.put(name, item);
		}
		item.costTimeMillis += (timeNano/1000000.0);
		item.count += 1;
	}
	public static void show()
	{
		Iterator<Entry<String, statisticsItem>> iter = data.entrySet().iterator();
		LogUtil.d(TAG,"start show detail");
		while (iter.hasNext()) {
			Entry<String, statisticsItem> entry = (Entry<String, statisticsItem>) iter.next();
			statisticsItem item = (statisticsItem) entry.getValue();
			if(item.count!=0)
			    LogUtil.d(TAG,item.itemName+" costs:"+item.costTimeMillis+ "ms counts:"+item.count+" average time:"+item.costTimeMillis/item.count+"ms ;");
			else
			    LogUtil.d(TAG,item.itemName+" counts:"+item.count+" ;");

		}
		LogUtil.d(TAG,"end show detail");
	}
	public static void showMemoryInfo(){
		double total=(runtime.totalMemory()>>15)/32.0;
		double used=total-(runtime.freeMemory()>>15)/32.0;
		double persent=100-used/total*100.0;
		LogUtil.i(TAG,String.format("heap %.2f %% free of sizes:%.3fMB/%.3fMB",persent,used,total));
	}
	
	public static void clear()
	{
		data.clear();
	}
}
