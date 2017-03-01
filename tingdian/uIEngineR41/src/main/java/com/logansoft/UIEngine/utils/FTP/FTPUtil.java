package com.logansoft.UIEngine.utils.FTP;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 与FTP服务器进行交互的工具类
 * @author Prosper.Z
 *
 */
public  class FTPUtil {
    private static FTPUtil ftpUtil;
    private String ftpUsername;
    private String ftpUserpwd;
    private String ftpUrl;
    private int ftpPort;
    private FTPClient ftpClient; 
    
    private  FTPUtil() {
        ftpClient =new FTPClient();
    }
    
    /**
     * 单例模式,只能存在一个ftpClient
     * @return
     */
    public static FTPUtil instance(){
        if (ftpUtil==null) {
             ftpUtil=new FTPUtil();
        }
        return ftpUtil;
    }
    
    /**
     * 初始化Ftp 
     * @param ftpUrl
     * @param ftpPort
     * @param userName
     * @param userPwd
     * @return
     */
    public boolean initFTPSetting(String ftpUrl,int ftpPort,String userName,String userPwd){
        try {
                ftpUsername=userName;
                ftpUserpwd =userPwd;
                this.ftpUrl =ftpUrl;
                this.ftpPort=ftpPort;
                
            ftpClient.connect(this.ftpUrl, this.ftpPort);
            ftpClient.login(ftpUsername, ftpUserpwd);
            int reply = ftpClient.getReplyCode();  
            //判断是否连接成功
             if (!FTPReply.isPositiveCompletion(reply)) {
                 ftpClient.disconnect();//断开连接
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 上传文件到ftp
     * @param filePath  文件的绝对路径
     * @param fileName  上传到ftp后的文件名称
     * @param ftpPath  ftp上的目录
     * @return
     */
    public boolean uploadFile(String filePath,String fileName,String ftpPath){
        if(!ftpClient.isConnected()){
            if (!initFTPSetting(ftpUrl, ftpPort, ftpUsername, ftpUserpwd)) {
                return false;
            }
        }
        
        try {
            //设置存储路径
              //创建目录
            ftpClient.makeDirectory(ftpPath);
              //切换至 '/data' 工作目录
            ftpClient.changeWorkingDirectory(ftpPath);
            //设置基本信息
              //设置缓存大小
            ftpClient.setBufferSize(2048); 
              //设置编码格式
            ftpClient.setControlEncoding("utf-8");
            ftpClient.enterLocalPassiveMode();     
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE); 
            
            //开始文件上传
            FileInputStream fileInputStream = new FileInputStream(filePath);  
            ftpClient.storeFile(fileName, fileInputStream);  
            
            fileInputStream.close();
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }finally{
            try {
                //退出登陆FTP，关闭ftpCLient的连接  
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }  
        }
        
    }
    
    
    /**
     * 从ftp上下载文件
     * @param filePath  文件的绝对路径
     * @param fileName  上传到ftp后的文件名称
     * @param ftpPath  ftp上的目录
     * @return
     */
    public boolean downloadFile(String filePath,String fileName,String ftpPath){
        if(!ftpClient.isConnected()){
            if (!initFTPSetting(ftpUrl, ftpPort, ftpUsername, ftpUserpwd)) {
                return false;
            }
        }
        try {
            // 转到指定下载目录  
            ftpClient.changeWorkingDirectory(ftpPath);  
            //获取指定名称的文件
            FTPFile[] ftpFiles = ftpClient.listFiles();
            for (FTPFile ftpFile : ftpFiles) {
             if(ftpFile.getName() == fileName){
                 
              //根据绝对路径初始化文件  
                File localFile = new File(filePath);  
                // 输出流  
                OutputStream outputStream = new FileOutputStream(localFile);  
                // 下载文件  
                ftpClient.retrieveFile(ftpFile.getName(), outputStream);  
                //关闭流  
                outputStream.close(); 
                break ;
             }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
}
