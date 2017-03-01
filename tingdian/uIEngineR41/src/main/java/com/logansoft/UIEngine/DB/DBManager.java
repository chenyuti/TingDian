
package com.logansoft.UIEngine.DB;

import com.logansoft.UIEngine.utils.LogUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据库管理类
 * 
 * @author Administrator
 */
public class DBManager {
    private DBHelper dbHelper;

    private SQLiteDatabase db;

    static DBManager dbManager;
    public static HashMap<String, DBManager> dbs;

    private DBManager(Context context, DBProp prop) {
        dbHelper = DBHelper.getInstant(context, prop);
        if (prop.res!=-1) {//有传资源文件
            db =dbHelper.openDatabase(context, prop);
        }else {
            db = dbHelper.getSQLiteDatabase();
        }
    }
    
    
    
    public static void initDBManager(Context context, DBProp prop) {
        dbManager = new DBManager(context, prop);
        createDBManager(context, prop);
    }

    
    public static DBManager getDBInstance() {
        if (dbManager == null) {
            LogUtil.e("DBManager 未初始化..........");
        }
        return dbManager;
    }
    
    public static void createDBManager(Context context, DBProp prop){
        if (dbs==null) {
           dbs= new HashMap<String, DBManager>();
        }
        dbs.put(prop.dbFileName, new DBManager(context, prop));
    }
    
    public static DBManager getDBInstance(String name) {
        if (dbs==null) {
            return null;
        }
        DBManager dbManager=  dbs.get(name);
        if (dbManager == null) {
            LogUtil.e("DBManager 未初始化..........");
        }
        return dbManager;
    }

    /**
     * 创建表
     * 
     * @param tabelName
     * @param tabelMap
     */
    public void create(String tableName, Map<String, String> tableMap) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE ").append(tableName + " (");
        Set<String> keySet = tableMap.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = tableMap.get(key);
            builder.append(key + " " + value);
            builder.append(iterator.hasNext() ? "," : " ) ");
        }
        String sql = builder.toString();
        db.execSQL(sql);
        // db.close();
    }

    /**
     * 关闭数据库
     */
    public void closeDB() {
        db.close();
    }

    /**
     * 创建表
     * 
     * @param tableName 表名
     * @param columns 列<不包括主键>
     * @param primaryKey 主键
     * @return
     */
    public static String createTable(String tableName, String[] columns, String primaryKey) {

        if (TextUtils.isEmpty(tableName)) {
            LogUtil.e("database     创建表语法错误： 无tableName");
            return null;
        }

        if (columns == null || columns.length == 0) {
            LogUtil.e("database     创建表语法错误： 无columns");
            return null;
        }

        if (TextUtils.isEmpty(primaryKey)) {
            primaryKey = "_id";
        }

        StringBuffer sb = new StringBuffer();
        sb.append("CREATE TABLE  IF NOT EXISTS `").append(tableName).append("` (");
        for (String column : columns) {
            sb.append("`").append(column).append("` VARCHAR, ");
        }
        if ("_id".equals(primaryKey)) {
            sb.append("`").append(primaryKey).append("` INTEGER PRIMARY KEY AUTOINCREMENT)");
        } else {
            sb.append("`").append(primaryKey).append("` TEXT PRIMARY KEY)");
        }
        return sb.toString();
    }

    /**
     * 创建表(主键默认_id)
     * 
     * @param tableName 表名
     * @param columns 列
     * @return
     */
    public static String createTable(String tableName, String[] columns) {
        return createTable(tableName, columns, "_id");
    }

    /**
     * 插入
     * 
     * @param sqlList
     */
    public void add(String tableName, List<ContentValues> sqlList) {
        db.beginTransaction(); // 开始事务
        try {
            for (ContentValues sqlMap : sqlList) {
                db.insert(tableName, null, sqlMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.endTransaction();
        // db.close();
    }

    /**
     * 删除数据库内的数据
     */
    public int del(String tableName, List<String> list, Map<String, String> sqlMap) {
        int del = 0;
        try {
            // 表名 , 条件 ,条件值
            StringBuilder where = new StringBuilder();
            String[] whereArgs = new String[sqlMap.size()];
            Set<String> keySet = sqlMap.keySet();
            Iterator<String> iterator = keySet.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                String key = (String)iterator.next();
                where.append(key);
                where.append("= ?");
                if (iterator.hasNext()) {
                    where.append(",");
                }
                whereArgs[i++] = sqlMap.get(key);
            }

            del = db.delete(tableName, where.toString(), whereArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // db.close();
        return del;
    }

    /**
     * 执行sql
     * 
     * @param sql
     */
    public synchronized void execSQL(String sql) {
        try {
             db.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // db.close();
    }

    /**
     * 更新数据库内的数据
     */
    public void updata(Map<String, String> sqlMap) {
        // ContentValues cv = new ContentValues();
        // cv.put("age", "");
        // db.update(dbHelper., cv, "name = ?", new String[]{});
    }

    /**
     * 查询数据库内的数据
     */
    //close cursor while not use!
    public Cursor selected(String sql) {
     //   List<Map<String, String>> selectList = new ArrayList<Map<String, String>>();
        Cursor cursor = db.rawQuery(sql, null);
//        while (cursor.moveToNext()) {
//            Map<String, String> map = new HashMap<String, String>();
//            for (String columnName : cursor.getColumnNames()) {
//                int columnIndex = cursor.getColumnIndex(columnName);
//                String string = cursor.getString(columnIndex);
//                map.put(columnName, string);
//            }
//            selectList.add(map);
//        }
        return cursor;
    }
}
