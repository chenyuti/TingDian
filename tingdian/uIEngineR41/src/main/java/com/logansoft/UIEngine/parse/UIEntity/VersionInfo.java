package com.logansoft.UIEngine.parse.UIEntity;

import com.logansoft.UIEngine.utils.StringUtil;
import com.logansoft.UIEngine.utils.VersionUtil;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class VersionInfo {
    public boolean isAppCompelUpdata() {
        return appCompelUpdata;
    }

    public void setAppCompelUpdata(boolean appCompelUpdata) {
        this.appCompelUpdata = appCompelUpdata;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getAppUpdateTime() {
        return appUpdateTime;
    }

    public void setAppUpdateTime(String appUpdateTime) {
        this.appUpdateTime = appUpdateTime;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

//    public List<String> getUpdateContent() {
//        return updateContent;
//    }

    public void setUpdateContent(List<String> updateContent) {
        this.updateContent = updateContent;
    }

    public void setUpdateContentStr(String updateContent) {
        List<String> updateContentStr=new ArrayList<String>(); 
        updateContentStr.add(updateContent);
        this.updateContent = updateContentStr;
    }

    
    @Override
    public String toString() {
        return "VersionInfo [appCompelUpdata=" + appCompelUpdata + ", appVersion=" + appVersion
                + ", appUpdateTime=" + appUpdateTime + ", appUrl=" + appUrl + ", updateContent="
                + updateContent + "]";
    }

    private static  VersionInfo versionInfo ;
    
    public static VersionInfo instance(){
        if (versionInfo ==null) {
            versionInfo =new VersionInfo();
        }
        return versionInfo;
    }
    
    private boolean appCompelUpdata ;
    private String appVersion;
    private String appUpdateTime  ;
    private String appUrl ;
    private List<String> updateContent ;
    
    public static final String VERSION_APP_COMPEL_UPDATA ="appCompelUpdata";
    public static final String VERSION_APP_VERSION ="appVersion";
    public static final String VERSION_APP_UPDATE_TIME ="appUpdateTime";
    public static final String VERSION_APP_URL ="appUrl";
    public static final String VERSION_UPDATE_CONTENT ="content";
    
    public String getUpdateContent(){
        String updataContents = "";
        if (updateContent!=null) {
            for (int i = 0; i < updateContent.size(); i++) {
                if (!StringUtil.isEmpty(updateContent.get(i))) {
                    updataContents=updataContents+updateContent.get(i)+"\n";
                }
            }
        }
        return updataContents;
    }
    
    public void download(Context context){
        VersionUtil.instance(context, appUrl).showUpdataDialog(getUpdateContent());
    }
    public void downloadUs(Context context){
    	VersionUtil.instance(context, appUrl).showUpdataDL();
    }
}
