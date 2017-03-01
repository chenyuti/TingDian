
package com.logansoft.UIEngine.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.logansoft.UIEngine.keyboard.AESEncodeDecode;
import com.logansoft.UIEngine.parse.XmlParser;
import com.logansoft.UIEngine.parse.UIEntity.VersionInfo;
import com.logansoft.UIEngine.provider.LuaStorage;
import com.logansoft.UIEngine.utils.http.RequestListener;
import com.logansoft.UIEngine.utils.http.RequestManager;
import com.logansoft.UIEngine.utils.http.RequestOptions;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Debug;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;
//import app.util.AESEncodeDecode;
import hdz.base.Function;

public class Configure {
	private static boolean translucent_status;
    public static boolean isInit = false;

    public static int sdkVersion = 0;

    public static String appVersion = "1.0.1";

    public static int PAGE_SIZE = 9;

    public static boolean DEBUG_MODE = false;

    // public static int ratio ;

    public static boolean pwCorrect = false;

    public static boolean nameCorrect = false;

    public static int screenHeight = 0;

    public static int screenWidth = 0;

    public static float screenDensity = 0;

    public static String macAddress = "";
    
    public static String model = "";

    public static String checkKey = "";

    public static String checkEnd = "ASDFGHJKL";

    public static String apkSignature = "";

    public static String mPackName = "";

    public static String spacePackName = "";

    public static String xmlRootPath;
    public static String luaRootPath;
    public static String jsonRootPath;
    

    public static String PAGE_DATA_PATH = "DataSource";
    public static String PAGE_LUA_PATH = "LUAXml";
    public static String PAGE_XML_PATH = "UIXML";


    public static String PAGE_IMAGES_PATH = "Images";

    public static String cacheDir;

    public static String filesPath;
	private static String proxyAddressURL;

    
    private static HashMap<String,String> urlTagReplace;
    static{
    	urlTagReplace=new HashMap<String,String>();
    }
   
  	public static void init(final Activity context) {
//        if (isInit)
//            return;
  		context.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    	try {
    		if (screenDensity == 0 || screenWidth == 0 || screenHeight == 0) {
    			DisplayMetrics dm = new DisplayMetrics();
                context.getWindowManager().getDefaultDisplay().getMetrics(dm);
                Configure.screenDensity = dm.density;// dpi
                Configure.screenWidth = dm.widthPixels;
                Configure.screenHeight = dm.heightPixels;
    		}
            PackageManager packageManager = context.getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            spacePackName = packInfo.packageName;
            mPackName = "com.logansoft.UIEngine.parse.field.";
            
            String[] names = spacePackName.split("\\.");
            String pack = names[names.length - 1];
            if (!GlobalConstants.ROOT_PATH.contains(pack)) {
                GlobalConstants.ROOT_PATH += pack;
                GlobalConstants.IMAGE_PATH = GlobalConstants.ROOT_PATH + "/photo/";
                GlobalConstants.TEMP_PATH = GlobalConstants.ROOT_PATH + "/Temp/";
            }
            String appVersion = packInfo.versionName;
            Configure.appVersion = appVersion;

            macAddress = DeviceUuidFactory.getDeviceId(context);
            if (TextUtils.isEmpty(macAddress)) {
                new DeviceUuidFactory(context);
                macAddress = DeviceUuidFactory.getDeviceId(context);
            }

            if (TextUtils.isEmpty(macAddress)) {
                WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = wifi.getConnectionInfo();
                macAddress = info.getMacAddress();
            }
            RequestManager.putShareHeader("CLIENTID", "AD_" + Configure.macAddress);

            long start=System.nanoTime();
            try {
                apkSignature = getApkSignatureMD5(context);
            } catch (Exception e) {
                e.printStackTrace();
            }
            LogUtil.i(Statistics.TAG,"getApkSignatureMD5() "+(System.nanoTime()-start)/1000000.0+"ms");
            Build bd = new Build();  
            model = bd.MODEL; 

            LogUtil.i("********************************************************************************************");
            LogUtil.i("**屏幕宽度：" + screenWidth + "\n屏幕高度：" + screenHeight + "\n屏幕密度："
                    + screenDensity + "\n spacePackName :" + spacePackName + "\n appVersion :"
                    + appVersion + "\n macAddress :" + macAddress+ "\n model :" +model);
             
            LogUtil.i("********************************************************************************************");

             filesPath = context.getFilesDir().getAbsolutePath();
            cacheDir = context.getCacheDir().getAbsolutePath();

        
            initImageLoader(context);
            
            
            SharedPreferences sp=context.getSharedPreferences("lastUpdate",Context.MODE_PRIVATE);
			long time=sp.getLong("time", 0);
			Editor editor=sp.edit();
			boolean needUpdateassets=false;
			if(packInfo.lastUpdateTime!=time){
				editor.putLong("time", packInfo.lastUpdateTime);
				needUpdateassets=true;
			}
	    	
			
			InputStream ins=null;
			String cn=Function.MD5_GetString("config.xml");
			String cp=filesPath+"/xml";
			File cf = new File(cp,cn);
			try{
				byte[] kbs =Function.MD5_GetByteS("AD_"+macAddress);
				if(needUpdateassets || !cf.exists()){
					ins=context.getAssets().open("config.xml");
					ResourceUtil.saveRes(ins,cp,cn,kbs);
					ins.close();
					ins=context.getAssets().open("config.xml");
				}
				else{
					ins = FileUtil.readFileFromSDCard(cf);
					byte[] content = StreamUtil.toByteArray(ins);
					ins.close();
					byte[] encode = AESEncodeDecode.AESDecode(content,kbs);
					ins=new ByteArrayInputStream(encode);
				}
				if (ins != null) 
					getConfigInfo(ins);
				
			}
			catch(Exception e){
	            e.printStackTrace();
			}
			finally{
				if(ins!=null)
					ins.close();
			}	
			if( GlobalConstants.CONFIG_URL!=null && GlobalConstants.CONFIG_URL.length()!=0)
				configUpdata(cf,context);

            if (!TextUtils.isEmpty(GlobalConstants.MULTI_TEMPLATES_URL)) {
                /* 创建数据库 用来存放资源信息 */
                ResourceUtil.createResourceDB(context, pack);
            }

            xmlRootPath=filesPath+"/xml";
            luaRootPath=filesPath+"/lua";
            jsonRootPath=filesPath+"/json";
            
            start=System.nanoTime();
    		if(needUpdateassets){
    			String[] paths;
    			AssetManager AM=context.getAssets();
    			paths =AM.list(PAGE_XML_PATH);
    			saveResource(AM,PAGE_XML_PATH,paths);
    			paths =AM.list(PAGE_LUA_PATH);
    			saveResource(AM,PAGE_LUA_PATH,paths);	
    			paths =AM.list(PAGE_DATA_PATH);
    			saveResource(AM,PAGE_DATA_PATH,paths);
    		}
    	    editor.commit();
    	         
    		LogUtil.i(Statistics.TAG,"savaResouceToFile() "+(System.nanoTime()-start)/1000000.0+"ms");

            if (!TextUtils.isEmpty(GlobalConstants.MULTI_TEMPLATES_URL)) {
            	Thread t=new Thread(){
            		@Override
            		public void run(){
                    	ResourceUtil.selectResourceALL();
            		}
            	};
            	t.start();
            }
      		LuaStorage.initStorageService(context.getApplication());

            isInit = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    private static void saveResource(AssetManager am,String path,String[] paths) throws IOException{
    	byte[] kbs =Function.MD5_GetByteS("AD_"+macAddress);
		for(String subName:paths){
			String subPath=path+"/"+subName;
			if(subName.endsWith(".lua") || subName.endsWith(".xml") || subName.endsWith(".json")){
				String savePath=null;
				if(subName.endsWith(".lua"))
					savePath=luaRootPath;
				else if(subName.endsWith(".xml"))
					savePath=xmlRootPath;
				else if(subName.endsWith(".json"))
					savePath=jsonRootPath;
				else 
					continue;
				if("public.lua".equals(subName)){
					InputStream ins =am.open(subPath);
					try{
			    		File file = new File(savePath,subName);
			    		if (!file.getParentFile().exists()) {
			    			file.getParentFile().mkdirs();
			    		}
			    		if (file.isDirectory()) {
			    			return;
			    		}
			    		if (!file.exists()) {
			    			file.createNewFile();
			    		}
			    		byte[] content = StreamUtil.toByteArray(ins);
						FileUtil.saveByteToFile(content,file);
					}
					catch(IOException e){
			            e.printStackTrace();
					}
					ins.close();
				}
				else{
					subName=Function.MD5_GetString(subName);
					InputStream ins =am.open(subPath);
					ResourceUtil.saveRes(ins,savePath,subName,kbs);
					ins.close();
				}
				
			}
			else if(!subName.contains(".")){
				String[] subPathChildren=am.list(subPath);
				if(subPathChildren.length>0){
					saveResource(am,subPath,subPathChildren);
				}
			}
		}
    }
    
    
    
    public static void checkUpdate(final Context context) {
        if (!TextUtils.isEmpty(GlobalConstants.UPDATA_URL)) {
            RequestListener requestListener=new RequestListener(){
            	@Override
            	public void DidLoad(Object result,Map<String,Object>responseLuaArgs,int callBackType){
            		if (result instanceof Document) {
    					XmlParser.getParser().checkUpdate((Document)result);
                        try {
                              VersionInfo versionInfo = VersionInfo.instance();
                            if (TextUtils.isEmpty(versionInfo.getAppVersion())) {
                               // Toast.makeText(context, "已是最新版本", Toast.LENGTH_LONG).show();
                                return;
                            }
                            String[] versions = versionInfo.getAppVersion().split("\\.");
                            String[] loaclVersions = Configure.appVersion.split("\\.");
                            for (int i = 0; i < versions.length; i++) {
                                int s = Integer.parseInt(versions[i]);
                                int c = Integer.parseInt(loaclVersions[i]);
                                if (s > c) {
                                    versionInfo.download(context);
                                    return;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
            		}
            	}
            };
            HashMap<String,Object> params=new HashMap<String,Object>();
            params.put(RequestOptions.RESPONSETYPE, RequestOptions.RESPONSEXML);
            params.put(RequestOptions.REQUESTURL,GlobalConstants.UPDATA_URL);
            params.put(RequestManager.REQUESTLISTENER, requestListener);
        	params.put(RequestOptions.REQUESTNET,"NetData:");
            RequestManager.request(params);
        }
    }

    public static void setDebugMode(boolean mode) {
        DEBUG_MODE = mode;
    }

    private static void configUpdata(final File cf,Context context) throws IOException {
    	RequestListener requestListener=new RequestListener(){
    		@Override
    		public void DidLoad(Object result,Map<String,Object>responseLuaArgs,int callBackType){
    			if (result instanceof String && !StringUtil.isEmpty((String)result) ) {
    				String response=(String)result;
    				if (response.contains("<config>")) {
    					InputStream ins=null;
    					try{
    						byte[] kbs =Function.MD5_GetByteS("AD_"+macAddress);
    						ins=StreamUtil.stringToInStream(response);
    						ResourceUtil.saveRes(ins,cf.getPath(),cf.getName(),kbs);
    						ins.close();
    						ins=StreamUtil.stringToInStream(response);
    						Configure.getConfigInfo(ins);
    					}
    					catch(Exception e){
    						e.printStackTrace();
    					}
    					finally{
    						if(ins!=null)
								try {
									ins.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
    					}
    				}
    			}
    		}
    	};
    	HashMap<String,Object> params=new HashMap<String,Object>();
    	InputStream ins=null; 
    	try{
    		ins=FileUtil.readFileFromSDCard(cf);
    		byte[] data=StreamUtil.toByteArray(ins);
    		if(data!=null){
    			String md5=Function.MD5_GetString(data);
    			if (!TextUtils.isEmpty(md5)){
    				Map<String,String> headers=new HashMap<String,String>();
    				headers.put("XML", md5);
    				params.put(RequestOptions.REQUESTHEADERS, headers);
    			}
    		}
    	}
        catch(Exception e){
        	e.printStackTrace();
        }
    	finally{
    		ins.close();
    	}
    	
    	params.put(RequestOptions.RESPONSETYPE, RequestOptions.RESPONSECONFIG);
    	params.put(RequestOptions.REQUESTNET,"NetData:");
    	params.put(RequestOptions.REQUESTURL,"config.xml");
    	params.put(RequestManager.REQUESTLISTENER, requestListener);
    	RequestManager.request(params);
    }

    /**
     * 获取APK签名信息,与服务器比对
     * 
     * @param context
     * @return
     * @throws Exception
     */
    public static String getApkSignatureMD5(Activity context) throws Exception {
       
//    	String path = context.getApplicationInfo().publicSourceDir;
//        Class<?> clazz = Class.forName("android.content.pm.PackageParser");
//        Method parsePackageMethod = clazz.getMethod("parsePackage", File.class, String.class,
//                DisplayMetrics.class, int.class);
//        Object packageParser = clazz.getConstructor(String.class).newInstance("");
//        Object packag = parsePackageMethod.invoke(packageParser, new File(path), null, context
//                .getResources().getDisplayMetrics(), 0x0004);
//        Method collectCertificatesMethod = clazz.getMethod("collectCertificates",
//                Class.forName("android.content.pm.PackageParser$Package"), int.class);
//        collectCertificatesMethod.invoke(packageParser, packag, PackageManager.GET_SIGNATURES);
//        Signature mSignatures[] = (Signature[])packag.getClass().getField("mSignatures")
//                .get(packag);

    	PackageInfo  packageInfo = context.getPackageManager().getPackageInfo(spacePackName, PackageManager.GET_SIGNATURES);
        /******* 通过返回的包信息获得签名数组 *******/
    	Signature[] mSignatures = packageInfo.signatures;
     
        Signature apkSignature = mSignatures.length > 0 ? mSignatures[0] : null;
        long start=System.nanoTime();

        if (apkSignature != null) {
            // 说明：没有提供md5的具体实现
            //byte[] keyBytes = Function.MD5_GetByteS("AD_" + Configure.macAddress);
        	byte[] keyBytes = MD5.getMD5("AD_" + Configure.macAddress);
          //  byte[] content = Function.MD5_GetString(apkSignature.toCharsString()).getBytes();
        	byte[] content = MD5.getMD5(apkSignature.toCharsString());
            byte[] apkSignatureEncode = AESEncodeDecode.AESEncode(content, keyBytes);
            String apkSignatureA = StringUtil.byteToStr(apkSignatureEncode);
            return apkSignatureA;
        }
        

        return null;
    }

    public static void getConfigInfo(InputStream in) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(in, "utf-8");
            int eventType = parser.getEventType();
            String tag;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tag = parser.getName();
                        if(tag.equals("tags")){
                        	while(parser.nextTag()!= XmlPullParser.END_TAG){
                        		String name = parser.getName().trim();
                        		String value = parser.nextText().trim();
                        		urlTagReplace.put(name,value);
                        	}
                        }
                        else if(tag.equals("proxyAddress")){
                        	proxyAddressURL=parser.nextText().trim();
                        }
                        else if (tag.equals("serverUrl")) { // 服务
                            String result = parser.nextText().trim();
                            LogUtil.e("ROOT_URL在这里被赋值了");
                            GlobalConstants.ROOT_URL = result;
                        } else if (tag.equals("appUpdateUrl")) { // 更新
                            String result = parser.nextText().trim();
                            GlobalConstants.UPDATA_URL = result;
                        } else if (tag.equals("configUpdateUrl")) { // 配置更新路径
                            String result = parser.nextText().trim();
                            GlobalConstants.CONFIG_URL = result;
                        } else if (tag.equals("templateUrl")) { // 配置更新路径
                            String result = parser.nextText().trim();
                            GlobalConstants.TEMPLATE_URL = result;
                        } else if (tag.equals("multiTemplatesURl")) { // 配置更新路径
                            String result = parser.nextText().trim();
                            GlobalConstants.MULTI_TEMPLATES_URL = result;
                        } else if (tag.equals("imagesPath")) { // 配置 Lua路径
                            String result = parser.nextText().trim();
                            PAGE_IMAGES_PATH = result;
                        } else if (tag.equals("dataPath")) { // 配置 本地数据的路径
                            String result = parser.nextText().trim();
                            PAGE_DATA_PATH = result;
                        } else if (tag.equals("luaPath")) { // 配置 Lua路径
                            String result = parser.nextText().trim();
                            PAGE_LUA_PATH = result;
                        }else if (tag.equals("uiPath")) { // 配置 UIXML路径
                            String result = parser.nextText().trim();
                            PAGE_XML_PATH = result;
                        } 

                        break;
                }
                eventType = parser.next();
            }

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

    }

    public static void initImageLoader(Context context) {
        ImageDownloader imageDownloader =new VolleyImageDownload(context);
        // This configuration tuning is custom. You can tune every option, you
        // may tune some of them,
        // or you can create default configuration by
        // ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                // .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator()).imageDownloader(imageDownloader )
                // .tasksProcessingOrder(QueueProcessingType.FIFO)
                // .writeDebugLogs() // Remove for release app
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }

    public static void getRunningAppProcessInfo(Context context) {
        ActivityManager mActivityManager = (ActivityManager)context
                .getSystemService(Context.ACTIVITY_SERVICE);
        // 获得系统里正在运行的所有进程
        List<RunningAppProcessInfo> runningAppProcessesList = mActivityManager
                .getRunningAppProcesses();
        for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcessesList) {
            // 进程ID号
            int pid = runningAppProcessInfo.pid;
            // 用户ID
            int uid = runningAppProcessInfo.uid;
            // 进程名
            String processName = runningAppProcessInfo.processName;
            if (processName.equals(spacePackName)) {
                // 占用的内存
                int[] pids = new int[] {
                    pid
                };
                Debug.MemoryInfo[] memoryInfo = mActivityManager.getProcessMemoryInfo(pids);
                int memorySize = memoryInfo[0].dalvikPrivateDirty;
                System.out.println("进程名=" + processName + ", 进程ID号=" + pid + ",用户ID=" + uid
                        + ",占用的内存=" + memorySize + "kb");
            }
        }
    }

    public static void setTranslucent_Status(boolean translucent,Activity context){
    	if(VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
            //透明状态栏
    		translucent_status=translucent;
    		int flag=WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
    		//LogUtil.i(context.getWindow().toString());
    		Window w=context.getWindow();
    		//LogUtil.i(w.toString()+" "+w.getDecorView().getParent().toString());
    		if(translucent)
        		w.addFlags(flag);
    		else
    			w.clearFlags(flag);
    	     //  getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
           // getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}
    }
    public static boolean translucent_Status(){
    	return translucent_status;
    }
    public static String getTagReplace(String tag){
    	return urlTagReplace.get(tag);
    }
    
    public static void updateProxyAddress(){
    	RequestListener requestListener=new RequestListener(){
    		@Override
    		public void DidLoad(Object result,Map<String,Object>responseLuaArgs,int callBackType){
    			if (result !=null) {
    				LogUtil.e(result.toString());
    			}
    		}
    	};
    	HashMap<String,Object> params=new HashMap<String,Object>();
    	params.put(RequestOptions.RESPONSETYPE, RequestOptions.RESPONSEJSON);
    	params.put(RequestOptions.REQUESTURL,proxyAddressURL+"/GetProxyList");
    	params.put(RequestManager.REQUESTLISTENER, requestListener);
    	params.put(RequestOptions.METHOD,RequestOptions.METHOD_POST);
    	params.put(RequestOptions.REQUESTNET,"NetData:");
    	RequestManager.request(params);
    }
}
