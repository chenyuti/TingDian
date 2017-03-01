package com.logansoft.UIEngine.DB;


import com.logansoft.UIEngine.R;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * 
 * @author yq_wu
 *
 */
public class DBHelper extends SQLiteOpenHelper {
//    private static final String DB_FILE_NAME = "ctrip.db";
//	private static final int DATABASE_VERSION = 1;
	
	private static Map<DBProp,DBHelper> dbHelperMap = new HashMap<DBProp,DBHelper>();
	private final static String DATABASE_PATH = android.os.Environment  
            .getExternalStorageDirectory().getAbsolutePath() + "/database";  
	private static Context context;
	private String dbFileName;
	/**
	 * SQLiteDatabase object
	 */
	private SQLiteDatabase dbObj;
	
	public static DBHelper getInstant(Context context, DBProp prop) {
		DBHelper dbHelper = dbHelperMap.get(prop);
		if (dbHelper == null) {
			dbHelper = new DBHelper(context,prop.dbFileName,prop.dbVersion);
			dbHelperMap.put(prop, dbHelper);
		}
		return dbHelper;
	}
	
	   // 使用外部数据库  
    public SQLiteDatabase openDatabase(Context context ,DBProp prop) {  
        try {  
            // Context context=new TestActivity();  
            String databaseFilename = DATABASE_PATH + "/" + prop.dbFileName;  
            File dir = new File(DATABASE_PATH);  
            if (!dir.exists()) // 如果文件夹不存在创建文件夹  
                dir.mkdir();  
            if (!(new File(databaseFilename)).exists()) { // 如果文件不存在创建文件  
                InputStream is = context.getResources().openRawResource(prop.res);  
                FileOutputStream fos = new FileOutputStream(databaseFilename);  
                byte[] buffer = new byte[8192];  
                int count = 0;  
                while ((count = is.read(buffer)) > 0) {  
                    fos.write(buffer, 0, count);  
                }  
                fos.close();  
                is.close();  
            }  
            dbObj = SQLiteDatabase.openOrCreateDatabase(databaseFilename, null);  
  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return dbObj;  
    }  
	
	
	
	private DBHelper(Context context, String dbName,int dbVersion) {
		super(context, dbName, null, dbVersion);
		DBHelper.context = context;
		this.dbFileName = dbName.split("\\.")[0];
		System.out.println("new dbhelper of name[" + dbName + "]");
	}
	
	/**
	 * 
	 * 数据库第一次创建时onCreate方法会被调用，我们可以执行创建表的语句
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
//		String init_sql = dbFileName+ "_init" ;
//		System.out.println("onCreate:"+init_sql);
//		int pointer = context.getResources().getIdentifier(init_sql,"string", context.getPackageName());
//		if (pointer == 0) {
//			System.out.println("INIT_CREATE_SQL  not defined");
//		} else {
//			String[] createTabelSqls = context.getResources().getString(pointer).split(";");
//			for (String sql : createTabelSqls) {
//				db.execSQL(sql + ";");
//			}
//		}
		
	}
	
	/**
	 * 当系统发现版本变化之后，会调用onUpgrade方法，我们可以执行修改表结构等语句。
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//	    db.execSQL("ALTER TABLE "+dbFileName+" ADD COLUMN other STRING"); 
	}


	public synchronized SQLiteDatabase getSQLiteDatabase() {
		if (dbObj == null || !dbObj.isOpen()) {
			dbObj = getWritableDatabase();
		}
		return dbObj;
	}
	
	public synchronized void closeSQLiteDatabase() {
		if (dbObj != null  && dbObj.isOpen()) {
			dbObj.close();
		}
	}
	
}

