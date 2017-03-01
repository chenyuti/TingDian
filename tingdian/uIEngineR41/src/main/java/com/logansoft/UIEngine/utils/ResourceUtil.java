
package com.logansoft.UIEngine.utils;

import hdz.base.Function;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.database.Cursor;
import android.text.TextUtils;
//import app.util.AESEncodeDecode;






import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.logansoft.UIEngine.DB.DBManager;
import com.logansoft.UIEngine.DB.DBProp;
import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.keyboard.AESEncodeDecode;
import com.logansoft.UIEngine.parse.XmlParser;
import com.logansoft.UIEngine.parse.UIEntity.VersionInfo;
import com.logansoft.UIEngine.utils.http.RequestListener;
import com.logansoft.UIEngine.utils.http.RequestManager;
import com.logansoft.UIEngine.utils.http.RequestOptions;

/**
 * 资源文件数据库
 */
public class ResourceUtil {
	
	public BaseFragment page;
	
	public ResourceUtil() {
		// TODO Auto-generated constructor stub
	}
	public ResourceUtil(BaseFragment page){
		this.page=page;
	}
	
    private static final String DB_TABLE_NAME = "resource";

    private static final String DB_RESOURCE_NAME = "RESOURCE_NAME";

    private static final String DB_RESOURCE_TYPE = "RESOURCE_TYPE";
    
    private static final String DB_RESOURCE_MD5 = "RESOURCE_MD5";

    private static final String DB_RESOURCE_PATH = "RESOURCE_PATH";

    public static final String DB_CREATE_MESSAGE = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_NAME
            + " (ID INTEGER PRIMARY KEY AUTOINCREMENT," + DB_RESOURCE_NAME + " TEXT, "
            + DB_RESOURCE_TYPE + " TEXT, " + DB_RESOURCE_PATH + " TEXT, " + DB_RESOURCE_MD5
            + " TEXT )";

    public static final String DB_INSERT_RESOURCE = " INSERT INTO " + DB_TABLE_NAME + " ( "
            + DB_RESOURCE_NAME + "," + DB_RESOURCE_TYPE + "," + DB_RESOURCE_PATH + ", "
            + DB_RESOURCE_MD5 + ") VALUES ( @ )";

    public static final String DB_SELECT_RESOURCE = " SELECT " + DB_RESOURCE_NAME + ","
            + DB_RESOURCE_TYPE + "," + DB_RESOURCE_PATH + ", " + DB_RESOURCE_MD5 + "  FROM "
            + DB_TABLE_NAME;
    
    public static final String DB_UPDATE_RESOURCE = " UPDATE " + DB_TABLE_NAME + " SET "
            + DB_RESOURCE_MD5 + "=@  WHERE "
            + DB_TABLE_NAME +"=$";

    private static final int DB_VERSION = 1;

    private static DBProp prop;

    public static void createResourceDB(Activity activity, String DB_NAME) {
        prop = new DBProp(DB_NAME, DB_VERSION);
        DBManager.createDBManager(activity, prop);
        DBManager.getDBInstance(DB_NAME).execSQL(DB_CREATE_MESSAGE);
    }

    public static void insertResourceDB(String name, String md5,String filePath) {
        if (prop != null) {
            String insert = DB_INSERT_RESOURCE.replace("@", "'" + name +  "','" + name.split("\\.")[1] + "','"
                    + filePath + "','" + md5 + "'");
            DBManager.getDBInstance(prop.dbFileName).execSQL(insert);
        } else {
            LogUtil.i("数据库未创建");
        }
    }
    public static void updateResourceDB(String name,String md5,String filePath) {
        if (prop != null) {
            Cursor cursor=DBManager.getDBInstance(prop.dbFileName).selected( DB_SELECT_RESOURCE +"  where "+DB_RESOURCE_NAME+"='"+name+"'");
            
            String sql = "";
            if(cursor.getCount()==0){
                sql = DB_INSERT_RESOURCE.replace("@", "'" + name + "','" + name.split("\\.")[1] + "','"
                        + filePath + "','" + md5 + "'");
            }else {
            	sql=" UPDATE " + DB_TABLE_NAME + " SET "
                         + DB_RESOURCE_MD5 + "='"+md5+"'  WHERE "
                         + DB_RESOURCE_NAME +"='"+name+"'";
            }
            cursor.close();
            DBManager.getDBInstance(prop.dbFileName).execSQL(sql);
        } else {
            LogUtil.i("数据库未创建");
        }
    }
    
    
    

    public static void selectResourceALL() {
    	DBManager dm=DBManager.getDBInstance(prop.dbFileName);
    	Cursor cursor = dm.selected(DB_SELECT_RESOURCE);
        JSONObject jobj = new JSONObject();
        try {
            JSONObject xmlObject = new JSONObject();
            JSONObject luaObject = new JSONObject();
            int typeIndex=cursor.getColumnIndex(DB_RESOURCE_TYPE);
            int nameIndex=cursor.getColumnIndex(DB_RESOURCE_NAME);
            int md5Index=cursor.getColumnIndex(DB_RESOURCE_MD5);
            while (cursor.moveToNext()){
            	String type=cursor.getString(typeIndex);
            	String name=cursor.getString(nameIndex);
            	String md5=cursor.getString(md5Index);
            	if("xml".equals(type)){
                    xmlObject.put(name,md5);
            	}else if("lua".equals(type)){
            		luaObject.put(name,md5);
            	}
            }
            jobj.put("xml", xmlObject);
            jobj.put("lua", luaObject);
        } catch (JSONException e) {
            e.printStackTrace();
            cursor.close();
        }
        
//        params.put("param.json", jobj.toString());
        RequestListener requestListener=new RequestListener(){
        	@Override
        	public void DidLoad(Object result,Map<String,Object>responseLuaArgs,int callBackType){
        		if(result!=null && result instanceof byte[] ) {
        		     InputStream stream =new ByteArrayInputStream((byte[]) result);
                     unZipFile(stream);
        		}
        	}
        };
        HashMap<String,Object> params=new HashMap<String,Object>();
        params.put(RequestOptions.REQUESTENCODE,"true");
        params.put(RequestOptions.REQUESTPARAMS,jobj.toString());
        params.put(RequestOptions.RESPONSETYPE, RequestOptions.RESPONSEBYTE);
        params.put(RequestOptions.REQUESTURL,GlobalConstants.MULTI_TEMPLATES_URL);
        params.put(RequestManager.REQUESTLISTENER, requestListener);
    	params.put(RequestOptions.REQUESTNET,"NetData:");
        RequestManager.request(params);
    }
    
    
    /**
     * 获取数据库里面的模板的参数
     * @return
     */
    public void upDataAll(final String suclua,final String errLua){
    	Cursor cursor = DBManager.getDBInstance(prop.dbFileName).selected(DB_SELECT_RESOURCE);

         JSONObject jobj = new JSONObject();
         try {
             JSONObject xmlObject = new JSONObject();
             JSONObject luaObject = new JSONObject();
             //其实可以直接根据sql语句获得index，但为方便sql结构语句改动，这样动态获取一次index的消耗应该可以接受
             int typeIndex=cursor.getColumnIndex(DB_RESOURCE_TYPE);
             int nameIndex=cursor.getColumnIndex(DB_RESOURCE_NAME);
             int md5Index=cursor.getColumnIndex(DB_RESOURCE_MD5);
             while (cursor.moveToNext()){
            	 String type=cursor.getString(typeIndex);
            	 String name=cursor.getString(nameIndex);
            	 String md5=cursor.getString(md5Index);
            	 if("xml".equals(type)){
                     xmlObject.put(name,md5);
            	 }else if("lua".equals(type)){
            		 luaObject.put(name,md5);
            	 }
             }
             jobj.put("xml", xmlObject);
             jobj.put("lua", luaObject);
         } catch (JSONException e) {
             e.printStackTrace();
             cursor.close();
         }

         
         RequestListener requestListener=new RequestListener(){
         	@Override
         	public void DidLoad(Object result,Map<String,Object>responseLuaArgs,int callBackType){
         		if(result!=null && result instanceof byte[] ) {
         			InputStream stream =new ByteArrayInputStream((byte[]) result);
         			unZipFile(stream);
                    if(!TextUtils.isEmpty(suclua))  
                    	page.loadLua(null, suclua);
         		}
         	}
         	@Override
        	public void exception(Exception e){
                if(!TextUtils.isEmpty(suclua))  
                	page.loadLua(null, errLua);

        	}
         };
         HashMap<String,Object> params=new HashMap<String,Object>();
         params.put(RequestOptions.REQUESTENCODE,"true");
         params.put(RequestOptions.REQUESTPARAMS,jobj.toString());
         params.put(RequestOptions.RESPONSETYPE, RequestOptions.RESPONSEBYTE);
         params.put(RequestOptions.REQUESTURL,GlobalConstants.MULTI_TEMPLATES_URL);
         params.put(RequestManager.REQUESTLISTENER, requestListener);
     	params.put(RequestOptions.REQUESTNET,"NetData:");
         RequestManager.request(params);
    }
    
    
    public void unZipFileByByte(byte[] response){
    	 InputStream stream =new ByteArrayInputStream(response);
         unZipFile(stream);
    }
    
    /**
     * 解压到指定目录
     * @param in
     * @param path
     */
    public static void unZipFile(InputStream in){
    	byte[] kbs =Function.MD5_GetByteS("AD_"+Configure.macAddress);
        ZipInputStream zipInputStream =new ZipInputStream(in);
        try {
        	ZipEntry zipEntry = null;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            	String name=zipEntry.getName();
                if (zipEntry.isDirectory()) {//文件夹
//                    folder.mkdirs();
                }else {//文件
                     name = name.replace("\\", File.separator);
                     String[] sname = name.split(File.separator);
                     String type = sname[0];
                     String fileName = sname[1];
                     String path=null;
                     if ("xml".equals(type)) 
                    	 path=Configure.xmlRootPath;
                     else if("lua".equals(type))
                    	 path=Configure.luaRootPath;
                     else
                    	 continue;
                     if("public.lua".equals(fileName)){
     					try{
     			    		File file = new File(path,fileName);
     			    		if (!file.getParentFile().exists()) {
     			    			file.getParentFile().mkdirs();
     			    		}
     			    		if (file.isDirectory()) {
     			    			return;
     			    		}
     			    		if (!file.exists()) {
     			    			file.createNewFile();
     			    		}
     			    		byte[] content = StreamUtil.toByteArray(zipInputStream);
     						FileUtil.saveByteToFile(content,file);
     					}
     					catch(IOException e){
     			            e.printStackTrace();
     					}
     				}else{
     					saveRes(zipInputStream,path,Function.MD5_GetString(fileName),kbs);
     				}
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                zipInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    protected static void saveRes(InputStream ins,String savePath,String subName,byte[] kbs){
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
			byte[] encode = AESEncodeDecode.AESEncode(content,kbs);
			FileUtil.saveByteToFile(encode,file);
			if (!TextUtils.isEmpty(GlobalConstants.MULTI_TEMPLATES_URL)) {
				//insertResourceDB(subName,Function.MD5_GetString(content),file.getAbsolutePath());
				updateResourceDB(subName,Function.MD5_GetString(content),file.getAbsolutePath());
			}
		}
		catch(IOException e){
            e.printStackTrace();
		}
    }
    
}
