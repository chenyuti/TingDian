
package com.logansoft.UIEngine.utils;

//import app.util.AESEncodeDecode;
import hdz.base.Function;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.logansoft.UIEngine.fragment.BaseFragmentManager;
import com.logansoft.UIEngine.keyboard.AESEncodeDecode;
import com.logansoft.UIEngine.utils.http.RequestListener;
import com.logansoft.UIEngine.utils.http.RequestManager;
import com.logansoft.UIEngine.utils.http.data.UIEngineStringRequest;

public class FileUtil {

    /**
     * The method judge whether the file exists 这个方法判断文件是否存在
     * 
     * @param path Path of the file
     * @return Return true if the file exists
     */
    public static boolean ifFileExistsOrNot(String path) {
        if (path == null) {
            return false;
        }
        if (new File(path).exists())
            return true;
        return false;
    }
        
    public static String getMd5ByEncodeIS(File file){
        FileInputStream in;
        try {
            in = new FileInputStream(file);
            byte[] content  = StreamUtil.toByteArray(in);
            byte[] decodeContent = AESEncodeDecode.AESDecode(content,  Function.MD5_GetByteS("AD_" + Configure.macAddress));
            if(in!=null)
               in.close();
            return Function.MD5_GetString(decodeContent);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } 
        return "";
    }
    
    public static  String getMd5ByInputStream(File file){
        FileInputStream in =null;
        ByteArrayOutputStream out =null;
        String md5="";
        try {
          in = new FileInputStream(file);
          md5 =getMd5ByInputStream(in ,file.length());
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            try {
                if (in!=null) 
                    in.close();
            } catch (Exception e) {
            }
            try {
                if (out!=null) 
                    out.close();
            } catch (Exception e) {
            }
        }
        return md5;
    }
    
    /**
     * 
     */
    public static String getMd5ByInputStream(FileInputStream is, long l) {
        String value = null;
        try {
            if (is!=null) {
                MappedByteBuffer byteBuffer = is.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, l);
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(byteBuffer);
                byte[] b = md5.digest();
                BigInteger bi = new BigInteger(1, b);
                value = bi.toString(16);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * The method saves String content to a specific file in sdcard
     * 这个方法将String内容保存到SD卡的指定文件中
     * 
     * @param content String content to save
     * @param file File for saving string content
     */
    public static void SaveFile2SDCard(String content, File localHtmlFile) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(localHtmlFile);
            if (fos != null) {
                fos.write(content.getBytes("utf-8"));
                Log.i("info", "write string to file complete >>" + localHtmlFile.getAbsolutePath());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            StreamUtil.closeResource(null, fos, null, null);
        }

    }

    /**
     * 从sd卡读取文件获得输入流
     * 
     * @param path
     * @return
     */
    public static InputStream readFileFromSDCard(String path) {
        File file = new File(path);
        FileInputStream fis;
        if (!file.exists()) {
            return null;
        }
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return fis;
    }

    /**
     * 从sd卡读取文件获得输入流
     * 
     * @param path
     * @return
     */
    public static FileInputStream readFileFromSDCard(File file) {
        // File file = new File(path);
        FileInputStream fis=null;
        if (!file.exists()) {
            return null;
        }
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return fis;
    }

   

    /**
     * 读取流到数组
     * 
     * @param inputStream
     * @return
     * @throws Exception
     */
    public static byte[] ReadInputStream(InputStream inputStream) throws Exception {
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            outstream.write(buffer, 0, len);
        }
        inputStream.close();
        return outstream.toByteArray();
    }

   

    
    public static void saveByteToFile(byte[] byt,File file){
       	
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(byt);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 保存字符串到文件中
     * 
     * @param str
     * @param file
     */
    public static void saveString2File(String str, File file) {
        saveByteToFile(str.getBytes(), file);
    }
    
    /**
     * 将流保存成文件
     * 
     * @param inStream 要保存的流
     * @param path 路径
     * @param fileName 文件名
     */
    public static void saveCache(InputStream inStream, String path, String fileName) {
        File file = new File(path, fileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            if (!file.exists()) {
                file.createNewFile();
            } else {
                file.delete();
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        StreamUtil.inputStreamToFile(inStream, file);
    }
    
    /**
     * 从文件中读取字符串
     * 
     * @param file
     * @return
     */
    public static String readStringFromFile(File file) {
        String str = null;
        FileInputStream fis = null;
        InputStreamReader in = null;
        BufferedReader reader = null;
        try {
            fis = new FileInputStream(file);
            in = new InputStreamReader(fis, "utf-8");
            reader = new BufferedReader(in);
            str = reader.readLine();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        return str;
    }

    /**
     * 删除文件
     * 
     * @param picture
     */
    public static void deletePicture(String path) {
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    /**
     * 递归删除某文件夹下的所有文件几文件夹
     * 
     * @param file 要删除的根目录
     */
    public static void RecursionDeleteFile(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                // file.delete();
                return;
            }
            for (File f : childFile) {
                RecursionDeleteFile(f);
            }
            // file.delete();
        }
    }

    /**
     * 递归删除文件和文件夹
     * 
     * @param file 要删除的根目录
     */
    public static void deleteFile(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                deleteFile(f);
            }
            file.delete();
        }
    }
    
    /**
     * 
     * @param file
     * @return
     */
    public static float getDirSize(File file) {     
        //判断文件是否存在     
        if (file.exists()) {     
            //如果是目录则递归计算其内容的总大小    
            if (file.isDirectory()) {     
                File[] children = file.listFiles();     
                float size = 0;     
                for (File f : children)     
                    size += getDirSize(f);     
                return size;     
            } else {//如果是文件则直接返回其大小,以“兆”为单位   
                float size = (float) file.length() / 1024 / 1024;        
                return size;     
            }     
        } else {     
            System.out.println("文件或者文件夹不存在，请检查路径是否正确！");     
            return 0.00f;     
        }     
    }
    
    
    
    /** 
     * 取得压缩包中的 文件列表(文件夹,文件自选) 
     * @param zipFileString     压缩包名字 
     * @param bContainFolder    是否包括 文件夹 
     * @param bContainFile      是否包括 文件 
     * @return 
     * @throws Exception 
     */  
    public static List<File> GetFileList(String zipFileString, boolean bContainFolder, boolean bContainFile) {  
        java.util.List<java.io.File> fileList = new java.util.ArrayList<java.io.File>();  
        java.util.zip.ZipInputStream inZip =null;
        try {
            inZip = new java.util.zip.ZipInputStream(new java.io.FileInputStream(zipFileString));  
            java.util.zip.ZipEntry zipEntry;  
            String szName = "";  
              
            while ((zipEntry = inZip.getNextEntry()) != null) {  
                szName = zipEntry.getName();  
              
                if (zipEntry.isDirectory()) {  
              
                    // get the folder name of the widget  
                    szName = szName.substring(0, szName.length() - 1);  
                    File folder = new File(szName);  
                    if (bContainFolder) {  
                        fileList.add(folder);  
                    }  
              
                } else {  
                    File file = new File(szName);  
                    if (bContainFile) {  
                        fileList.add(file);  
                    }  
                }  
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if (inZip!=null) {
                try {
                    inZip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }  
            }
        }
       
          
        return fileList;  
    }  
    
    /**
     * 解压到指定目录
     * @param in
     * @param path
     */
    public static void unZipFile(InputStream in,String path){
        LogUtil.i("解压的路径:"+path);
        ZipInputStream zipInputStream =new ZipInputStream(in);
        try {
            ZipEntry zipEntry = null;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                String name=zipEntry.getName();
                if (zipEntry.isDirectory()) {//文件夹
                    File folder = new File(path + File.separator + name);    
                    folder.mkdirs();
                }else {//文件
                     name = name.replace("\\", File.separator);
                     String[] sname = name.split(File.separator);
                     File file =null;
                     if ("xml".equals(sname[0])) {
                         file =  new  File( Configure.xmlRootPath +File.separator +sname[1]); 
                     }else  if ("lua".equals(sname[0])){
                         file =  new  File( Configure.luaRootPath +File.separator +sname[1]); 
                     }
                     LogUtil.i("------->"+file.getName());
                     if (!file.getParentFile().exists()) {
                         file.getParentFile().mkdirs();
                     }
                     
                     if (file.exists()) {
                         file.delete();
                     }
                     file.createNewFile();  
                     // get the output stream of the file  
                     FileOutputStream out = new FileOutputStream(file);  
                     int len;  
                     byte[] buffer = new byte[2048];  
                     // read (len) bytes into buffer  
                     while ((len = zipInputStream.read(buffer)) != -1) {  
                         // write (len) byte from buffer at the position 0  
                         out.write(buffer, 0, len);  
                         out.flush();  
                     }  
                     out.close(); 
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
    
    /** 
     * 压缩文件,文件夹 
     * @param srcFileString 要压缩的文件/文件夹名字 
     * @param zipFileString 指定压缩的目的和名字 
     * @throws Exception 
     */  
    public static void ZipFolder(String srcFileString, String zipFileString) {  
        //创建Zip包  
        ZipOutputStream outZip=null;
        try {
            outZip = new ZipOutputStream(new FileOutputStream(zipFileString));  
            //打开要输出的文件  
            File file = new File(srcFileString);  
            //压缩  
            ZipFiles(file.getParent()+File.separator, file.getName(), outZip);  
            
            //完成,关闭  
            outZip.finish();  
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                if (outZip!=null) {
                    outZip.close();  
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
      
    } 
    
    /** 
     * 压缩文件 
     * @param folderString 
     * @param fileString 
     * @param zipOutputSteam 
     * @throws Exception 
     */  
    private static void ZipFiles(String folderString, String fileString, ZipOutputStream zipOutputSteam)throws Exception{  
        if(zipOutputSteam == null)  
            return;  
          
        File file = new File(folderString+fileString);  
          
        //判断是不是文件  
        if (file.isFile()) {  
            ZipEntry zipEntry =  new ZipEntry(fileString);  
            FileInputStream inputStream = new FileInputStream(file);  
            zipOutputSteam.putNextEntry(zipEntry);  
              
            int len;  
            byte[] buffer = new byte[4096];  
              
            while((len=inputStream.read(buffer)) != -1){  
                zipOutputSteam.write(buffer, 0, len);  
            } 
            inputStream.close();
            zipOutputSteam.closeEntry();  
        }else {  
            //文件夹的方式,获取文件夹下的子文件  
            String fileList[] = file.list();  
            //如果没有子文件, 则添加进去即可  
            if (fileList.length <= 0) {  
                ZipEntry zipEntry =  new ZipEntry(fileString+java.io.File.separator);  
                zipOutputSteam.putNextEntry(zipEntry);  
                zipOutputSteam.closeEntry();                  
            }  
              
            //如果有子文件, 遍历子文件  
            for (int i = 0; i < fileList.length; i++) {  
                ZipFiles(folderString, fileString+File.separator+fileList[i], zipOutputSteam);  
            } 
      
        }   
          
    } 
      
}
