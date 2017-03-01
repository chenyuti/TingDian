package com.logansoft.UIEngine.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.logansoft.UIEngine.UIEngineApplication;

public class VersionUtil {
	private static boolean apkDownloadRun = false;
	private ProgressDialog pd; // 进度条对话框
	private String apkUrl;
	private Context mContext;
	public static VersionUtil versionUtil;

	public static VersionUtil instance(Context context, String apkUrl) {
		if (versionUtil == null) {
			versionUtil = new VersionUtil(context, apkUrl);
		}
		return versionUtil;
	}

	public VersionUtil(Context context, String apkUrl) {
		mContext = context;
		this.apkUrl = apkUrl;
	}

	/*
	 * 从服务器中下载APK
	 */
	protected void downLoadApk() {
		apkDownloadRun = true;
		pd = new ProgressDialog(mContext);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("正在下载更新包");
		pd.show();
		pd.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				apkDownloadRun = false;
			}
		});
		new Thread() {
			@Override
			public void run() {
				try {
					File file = getApkFileFromServer(apkUrl, pd);
					sleep(1000);
					installApk(file); // 安装下载的apk
					pd.dismiss(); // 结束掉进度条对话框
				} catch (Exception e) { // 下载或安装apk失败
					Message msg = new Message();
					msg.what = -2;
					handler.sendMessage(msg);
					e.printStackTrace();
				}
			}
		}.start();

	}

	// 安装apk
	protected void installApk(File file) {
		Intent intent = new Intent();
		// 执行动作
		intent.setAction(Intent.ACTION_VIEW);
		// 执行的数据类型
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		((Activity) mContext).startActivity(intent);
	}

	public File getApkFileFromServer(String path, ProgressDialog pd) throws Exception {
		// 如果相等的话表示当前的sdcard挂载在手机上并且是可用的
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

			URL url = new URL(path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setRequestProperty("Accept-Encoding", "identity");
			// 获取到文件的大小
			pd.setMax(conn.getContentLength());
			InputStream is = conn.getInputStream();
			File file = new File(GlobalConstants.ROOT_PATH, Configure.spacePackName + ".apk");
			if (!file.exists()) {
				file.mkdirs();
			}
			file.delete();
			FileOutputStream fos = new FileOutputStream(file);
			BufferedInputStream bis = new BufferedInputStream(is);
			byte[] buffer = new byte[1024];
			int len;
			int total = 0;
			while ((len = bis.read(buffer)) != -1) {
				if (apkDownloadRun) {
					fos.write(buffer, 0, len);
					total += len;
					// 获取当前下载量
					pd.setProgress(total);
				} else {
					fos.close();
					bis.close();
					is.close();
					throw new Exception("下载被终止！");
				}
			}
			fos.close();
			bis.close();
			is.close();
			return file;
		} else {
			handler.sendEmptyMessage(-3);
			return null;
		}
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 1: // 更新
				showUpdataDialog("检测到最新版本，请及时更新！");
				break;
			case -2:
				// 下载或安装失败
				Toast.makeText(mContext, "软件更新失败，请稍候再次尝试...", Toast.LENGTH_LONG).show();
				pd.dismiss();
				break;
			case -3:
				// 未检测到sd卡
				Toast.makeText(mContext, "未检测到SD卡，请检查SD卡", Toast.LENGTH_LONG).show();
				break;
			}
		}

	};

	/*
	 * 弹出对话框通知用户更新程序 弹出对话框的步骤： 1.创建alertDialog的builder. 2.要给builder设置属性,
	 * 对话框的内容,样式,按钮 3.通过builder 创建一个对话框 4.对话框show()出来
	 */
	public void showUpdataDL() {
		downLoadApk();
	}

	public void showUpdataDialog(String msg) {
		try {
			new Builder(mContext)
					// .setIcon(android.R.drawable.ic_menu_close_clear_cancel)
					.setTitle("更新提示").setMessage(msg).setPositiveButton("更新", new AlertDialog.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							downLoadApk();//
							dialog.dismiss();
						}
					}).setNegativeButton("返回", new AlertDialog.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

							((UIEngineApplication) mContext).exit();
						}
					}).setCancelable(false).show();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
