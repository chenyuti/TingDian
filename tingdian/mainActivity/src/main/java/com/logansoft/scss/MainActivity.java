package com.logansoft.scss;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.logansoft.UIEngine.fragment.BackHandledInterface;
import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.fragment.BaseFragmentActivity;
import com.logansoft.UIEngine.fragment.BaseFragmentManager;
import com.logansoft.UIEngine.parse.xmlview.BaseView;
import com.logansoft.UIEngine.utils.Configure;
import com.logansoft.UIEngine.utils.GlobalConstants;
import com.logansoft.UIEngine.utils.LogUtil;
import com.logansoft.UIEngine.utils.StringUtil;
import com.logansoft.UIEngine.utils.http.RequestManager;
import com.logansoft.UIEngine.utils.images.BitmapUtil;
import com.logansoft.UIEngine.view.wheelView.OnWheelChangedListener;
import com.logansoft.UIEngine.view.wheelView.OnWheelScrollListener;
import com.logansoft.UIEngine.view.wheelView.WheelView;
import com.logansoft.UIEngine.view.wheelView.adapter.NumericWheelAdapter;
import com.logansoft.scss.signature.SignatureActivity;
import com.mining.app.zxing.MipcaActivityCapture;
import com.sdses.BtReaderClient;
import com.sdses.IClientCallBack;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

public class MainActivity extends BaseFragmentActivity implements BackHandledInterface {
	BaseFragmentManager manager;
	public BaseFragment mBackHandedFragment;
	public Handler mHandler = new Handler();

	public final static int PHOTOGRAPH = 500;
	public final static int SIGNATURE = 501;
	private final static int SCANNIN_GREQUEST_CODE = 502;
	private String signatureName;
	private String photoName;

	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		RequestManager.init(this);
		Configure.init(this);
		Configure.DEBUG_MODE = true;
		LogUtil.openGate(true);
		RequestManager.putShareHeader("OS", "android");
		RequestManager.putShareHeader("CLIENTVERSION", Configure.appVersion);
		RequestManager.putShareHeader("OSVERSION", android.os.Build.VERSION.RELEASE);
		RequestManager.putShareHeader("CLIENTID", "AD_" + Configure.macAddress);
		RequestManager.putShareHeader("SIGNATURE", Configure.apkSignature);
		manager = BaseFragmentManager.instance("main", this, getSupportFragmentManager(), R.id.main);
		manager.setCustomAnimations("fade");
		manager.switchFragment(false, "res://welcomePage.xml", BaseFragment.class, null);
		BluetoothReceiver();
		
	}

	private void BluetoothReceiver() {

		// 是否可以使用蓝牙功能
		if (mBluetoothAdapter == null) {
			Toast.makeText(MainActivity.this, "对不起 ，您的设备不具备蓝牙功能，部分功能将无法使用！", 3000).show();
			return;
		}

		// 如果蓝牙还没开启,开启蓝牙
		if (mBluetoothAdapter.getState() != BluetoothAdapter.STATE_ON) {
			mBluetoothAdapter.enable();
		}

		// 注册Receiver来获取蓝牙设备相关的结果
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
		intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(searchDevices, intentFilter);
	}

	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(searchDevices);
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	/**
	 * loading加载动画 showProgreesDialog 开始loading hideProgreesDialog 结束loading
	 */
	public void showProgreesDialog(String message) {
		if (!StringUtil.isEmpty(message)) {
			SCSSProgressDialogUtil.shouPD(this, message);
		}
	}

	public void hideProgreesDialog() {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				SCSSProgressDialogUtil.dismissPD();
			}
		}, 1000);
	}

	/**
	 * 延时
	 */
	public void sleep(final BaseFragment page, final BaseView view, final String lua, long l) {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				page.loadLua(view, lua);
			}
		}, l);
	}

	public String encrypt(String content) {
		if (TextUtils.isEmpty(content)) {
			return "";
		}
		try {
			return AESEncryptor.encrypt("AD_" + Configure.macAddress, content);
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.i("SharedPreferences加密失败");
		}
		return "";
	}

	public String decrypt(String content) {
		if (TextUtils.isEmpty(content)) {
			return "";
		}
		try {
			return AESEncryptor.decrypt("AD_" + Configure.macAddress, content);
		} catch (Exception e) {
			LogUtil.i("SharedPreferences解密失败");
		}
		return "";
	}

	/**
	 * 获取配置文件接口地址前缀
	 */
	public String getPrefixURL() {
		return GlobalConstants.ROOT_URL;
	}

	/**
	 * 获取App版本号
	 */
	public String getAppVersion() {
		return Configure.appVersion;
	}

	/**
	 * 调用拍照
	 */
	public void photograph(String mobile, String business) {
		setPhotoName(mobile, business);
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(getAlbumFile(), this.photoName)));
		startActivityForResult(intent, PHOTOGRAPH);
	}

	/**
	 * 获取保存图片的文件夹地址
	 */
	private File getAlbumFile() {
		File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"SCSSPictures");
		if (!file.exists()) { // 判断文件夹是否存在（不存在则创建这个文件夹）
			file.mkdirs(); // 创建文件夹
		}
		return file;
	}

	/**
	 * 删除图片文件夹，拍照和签名
	 */
	public void deleteSCSSAlbum() {
		deleteFile(getAlbumFile());
		deleteFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"SignaturePad"));
	}

	/**
	 * 删除保存图片的文件夹及文件夹下的图片
	 */
	private void deleteFile(File file) {
		if (file.exists()) { // 判断文件夹是否存在
			if (file.isFile()) {
				file.delete();
				return;
			}

			if (file.isDirectory()) {
				File[] childFiles = file.listFiles();
				if (childFiles == null || childFiles.length == 0) {
					file.delete();
					return;
				}

				for (int i = 0; i < childFiles.length; i++) {
					deleteFile(childFiles[i]);
					sendBroadcast(
							new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + childFiles[i])));
				}
				file.delete();
			}
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file)));
		}
	}

	/**
	 * 用当前时间对图片命名
	 */
	@SuppressLint("SimpleDateFormat")
	private void setPhotoName(String mobile, String business) {
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String time = dateFormat.format(now);
		this.photoName = String.format(mobile + "_" + business + "_cards_%s.jpg", time);
	}

	/**
	 * 获取拍照图片地址
	 */
	public String getPhotoName() {
		return getAlbumFile() + "/" + this.photoName;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case PHOTOGRAPH:
				Bitmap bitmap = getPhotographImage(getAlbumFile() + "/" + this.photoName);
				BitmapUtil.SaveBitmap2SDCard(bitmap, getAlbumFile() + "/" + this.photoName);
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
						Uri.parse("file://" + getAlbumFile() + "/" + this.photoName)));
				BaseFragment baseFragment = manager.getCurrentFragment();
				baseFragment.loadLua(null, "ImageResult(page)");
				break;
			case SIGNATURE:
				signatureName = data.getStringExtra("signature_url");
				Bitmap sBitmap = signatureImage(signatureName);
				BitmapUtil.SaveBitmap2SDCard(sBitmap, signatureName);
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + signatureName)));
				BaseFragment signatureImage = manager.getCurrentFragment();
				signatureImage.loadLua(null, "WriteSignatureResult(page)");
				break;
			case SCANNIN_GREQUEST_CODE:
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("data", data);
				BaseFragment QRCodeBaseFragment = manager.getCurrentFragment();
				QRCodeBaseFragment.loadLua(null, "sweepResult(page,{data})", map);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * 签名特用，签名图片进行缩放8倍
	 */
	private Bitmap signatureImage(String srcPath) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		// 开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空
		newOpts.inJustDecodeBounds = false;
		// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 8;// be=1表示不缩放
		newOpts.inSampleSize = be;// 设置缩放比例
		// 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		return bitmap;// 压缩好比例大小后再进行质量压缩
	}

	/**
	 * 图片缩放与压缩
	 */
	private Bitmap getPhotographImage(String srcPath) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		// 开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		// 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
		float hh = 1280f;// 这里设置高度为800f
		float ww = 720f;// 这里设置宽度为480f
		// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;// be=1表示不缩放
		if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;// 设置缩放比例
		// 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
	}

	private Bitmap compressImage(Bitmap image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;
		while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
			options -= 10;// 每次都减少10
			baos.reset();// 重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
		}

		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
		return getWatermark(bitmap);
	}

	/**
	 * 加入“仅用于办理移动业务 复印无效”水印
	 */
	private Bitmap getWatermark(Bitmap bg) {
		Bitmap wm = null;
		try {
			InputStream is = getAssets().open("Images/bg_watermark.png");
			wm = BitmapFactory.decodeStream(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		int w = bg.getWidth();
		int h = bg.getHeight();

		Matrix matrix = new Matrix();
		// 计算缩放比例
		// float scaleWidth = (float) (w / wm.getWidth());
		// float scaleHeight = (float) (h / wm.getHeight());
		float scaleWidth = (float) Math.ceil((double) ((double) w / (double) wm.getWidth()));
		float scaleHeight = (float) Math.ceil((double) ((double) h / (double) wm.getHeight()));
		// 开始缩放
		matrix.postScale(scaleWidth, scaleHeight);
		// 创建缩放后的图片
		Bitmap newWM = Bitmap.createBitmap(wm, 0, 0, wm.getWidth(), wm.getHeight(), matrix, true);

		Bitmap newBitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
		Canvas cv = new Canvas(newBitmap);
		cv.drawBitmap(bg, 0, 0, null);// 在 0，0坐标开始画入src
		cv.drawBitmap(newWM, 0, 0, null);// 在src画入水印
		cv.save(Canvas.ALL_SAVE_FLAG);// 保存
		cv.restore();// 存储
		return newBitmap;
	}

	/**
	 * 手写签名
	 */
	public void onWriteSignature(String word, String mobile, String business) {
		Intent intent = new Intent(this, SignatureActivity.class);
		intent.putExtra("word", word);
		intent.putExtra("mobile", mobile);
		intent.putExtra("business", business);
		startActivityForResult(intent, SIGNATURE);
	}

	public String getSignatureName() {
		return signatureName;
	}

	/*
	 * 把获取到的值去掉重复的元素并排序
	 */
	public String getSpinnerList(String str) {
		String[] array = str.split(",");
		int[] array2 = new int[array.length];
		Set<Integer> TreeSet = new TreeSet<Integer>();
		for (int i = 0; i < array.length; i++) {
			array2[i] = Integer.parseInt(array[i]);
		}
		for (int num : array2) {
			TreeSet.add(num);
		}
		return TreeSet.toString();
	}
	
	/**
	 * 去除重复的字符串
	 */
	public String getSpinnerLists(String str) {
		String[] array = str.split(",");
		Set<String> TreeSet = new TreeSet<String>();
		for (String num : array) {
			TreeSet.add(num);
		}
		return TreeSet.toString();
	}

	/*
	 * 加入业务单数据及签名，用来生成业务单图片，提交给服务器打印
	 */
	public String getProtocol(String url, String text) {
		Bitmap bg = null;
		try {
			InputStream is = getAssets().open("Images/bg_protocol.png");
			bg = BitmapFactory.decodeStream(is);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Bitmap bmp = BitmapFactory.decodeFile(url);
		int w = bg.getWidth();
		int h = bg.getHeight();
		int ww = bmp.getWidth();
		int hh = bmp.getHeight();
		Bitmap newBitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas canvas = new Canvas(newBitmap);

		canvas.drawBitmap(bg, 0, 0, null);
		canvas.drawBitmap(bmp, w - ww - 150, h - hh - 210, null);// 在src的右下角画入水印

		TextPaint textPaint = new TextPaint();
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(25.0F);
		int slHeight = 0;

		String[] str = text.split("\\^");
		for (int i = 0; i < str.length; i++) {
			StaticLayout sl = new StaticLayout(str[i], textPaint, canvas.getWidth() - 460, Alignment.ALIGN_NORMAL, 1.0f,
					0.0f, false);
			if (i == 0) {
				canvas.translate(230, 350);
			} else if (i == 1) {
				canvas.translate(930, 0);
			} else if (i == 2) {
				canvas.translate(-830, 80);
			} else if (i == 3) {
				canvas.translate(0, 30);
			} else if (i == 4) {
				canvas.translate(0, 100);
				slHeight = sl.getHeight();
			} else if (i > 4) {
				canvas.translate(0, slHeight);
				slHeight = sl.getHeight();
			}
			sl.draw(canvas);
		}

		String name = String.format("bill%s.jpg", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
		String protocolImage = getAlbumFile() + "/" + name;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		newBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片

		BitmapUtil.SaveBitmap2SDCard(bitmap, protocolImage);
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + protocolImage)));
		return protocolImage;
	}

	/**
	 * 选择日期时间
	 */
	private WheelView year;
	private WheelView month;
	private WheelView day;
	String datatime;

	public void DateTimePicker() {
		Calendar c = Calendar.getInstance();
		final int curYear = c.get(Calendar.YEAR);
		final int curMonth = c.get(Calendar.MONTH) + 1;// 通过Calendar算出的月数要+1
		final int curDate = c.get(Calendar.DATE);
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.wheelview, null);

		final PopupWindow menuWindow = new PopupWindow(view, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT); // 后两个参数是width和height
		menuWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);

		menuWindow.setFocusable(true);
		menuWindow.setOutsideTouchable(true);
		menuWindow.update();
		menuWindow.setBackgroundDrawable(new BitmapDrawable());

		year = (WheelView) view.findViewById(R.id.year);
		NumericWheelAdapter numericWheelAdapter1 = new NumericWheelAdapter(this, curYear, curYear + 20);
		numericWheelAdapter1.setLabel("年");
		year.setViewAdapter(numericWheelAdapter1);
		year.setCyclic(true);// 是否可循环滑动
		year.addScrollingListener(scrollListener);

		month = (WheelView) view.findViewById(R.id.month);
		NumericWheelAdapter numericWheelAdapter2 = new NumericWheelAdapter(this, 1, 12, "%02d");
		numericWheelAdapter2.setLabel("月");
		month.setViewAdapter(numericWheelAdapter2);
		month.setCyclic(true);
		month.addScrollingListener(scrollListener);

		day = (WheelView) view.findViewById(R.id.day);
		initDay(curYear, curMonth);
		day.setCyclic(true);

		// 设置显示行数
		year.setVisibleItems(7);
		month.setVisibleItems(7);
		day.setVisibleItems(7);

		year.setCurrentItem(curYear - curYear);
		month.setCurrentItem(curMonth - 1);
		day.setCurrentItem(curDate - 1);

		Button btnSure = (Button) view.findViewById(R.id.sure);
		btnSure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (curYear == (year.getCurrentItem() + curYear) && curMonth > (month.getCurrentItem() + 1)) {
					Toast.makeText(getApplicationContext(), "不能选择过去的时间\n        请重新选择", 0).show();
				} else if (curYear == (year.getCurrentItem() + curYear) && curMonth == (month.getCurrentItem() + 1)
						&& curDate > (day.getCurrentItem() + 1)) {
					Toast.makeText(getApplicationContext(), "不能选择过去的时间\n        请重新选择", 0).show();
				} else {
					datatime = (year.getCurrentItem() + curYear) + "-"
							+ ((month.getCurrentItem() + 1) < 10 ? "0" + (month.getCurrentItem() + 1)
									: (month.getCurrentItem() + 1))
							+ "-" + ((day.getCurrentItem() + 1) < 10 ? "0" + (day.getCurrentItem() + 1)
									: (day.getCurrentItem() + 1));
					BaseFragment baseFragment = manager.getCurrentFragment();
					baseFragment.loadLua(null, "DatePickerResult(page)");
					menuWindow.dismiss();
				}
			}
		});

		Button btnCancle = (Button) view.findViewById(R.id.cancle);
		btnCancle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				menuWindow.dismiss();
			}
		});

		year.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				int selYear = newValue + curYear;
				Object selMonth = (month.getCurrentItem() + 1) < 10 ? "0" + (month.getCurrentItem() + 1)
						: (month.getCurrentItem() + 1);
				Object selDay = (day.getCurrentItem() + 1) < 10 ? "0" + (day.getCurrentItem() + 1)
						: (day.getCurrentItem() + 1);
				boolean is = isDate(selYear + "-" + selMonth + "-" + selDay);
				if (!is) {
					int maxDay = getDay(selYear, month.getCurrentItem() + 1);
					day.setCurrentItem(maxDay - 1);
				}
			}
		});

		month.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				int selYear = year.getCurrentItem() + curYear;
				Object selMonth = (newValue + 1) < 10 ? "0" + (newValue + 1) : (newValue + 1);
				Object selDay = (day.getCurrentItem() + 1) < 10 ? "0" + (day.getCurrentItem() + 1)
						: (day.getCurrentItem() + 1);
				boolean is = isDate(selYear + "-" + selMonth + "-" + selDay);
				if (!is) {
					int maxDay = getDay(selYear, newValue + 1);
					day.setCurrentItem(maxDay - 1);
				}
			}
		});

		menuWindow.showAsDropDown(view);
	}

	OnWheelScrollListener scrollListener = new OnWheelScrollListener() {
		@Override
		public void onScrollingStarted(WheelView wheel) {

		}

		@Override
		public void onScrollingFinished(WheelView wheel) {
			int n_year = year.getCurrentItem() + Calendar.getInstance().get(Calendar.YEAR);// 年
			int n_month = month.getCurrentItem() + 1;// 月

			initDay(n_year, n_month);
		}
	};

	public String getDateTime() {
		return datatime;
	}

	/**
	 * @param year
	 * @param month
	 * @return
	 */
	public int getDay(int year, int month) {
		int day = 30;
		boolean flag = false;
		switch (year % 4) {
		case 0:
			flag = true;
			break;
		default:
			flag = false;
			break;
		}
		switch (month) {
		case 1:
		case 3:
		case 5:
		case 7:
		case 8:
		case 10:
		case 12:
			day = 31;
			break;
		case 2:
			day = flag ? 29 : 28;
			break;
		default:
			day = 30;
			break;
		}
		return day;
	}

	private void initDay(int arg1, int arg2) {
		NumericWheelAdapter numericWheelAdapter = new NumericWheelAdapter(this, 1, getDay(arg1, arg2), "%02d");
		numericWheelAdapter.setLabel("日");
		day.setViewAdapter(numericWheelAdapter);
	}

	/**
	 * 判断日期格式和范围
	 */
	public boolean isDate(String date) {
		String rexp = "^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))";
		Pattern pat = Pattern.compile(rexp);
		Matcher mat = pat.matcher(date);
		boolean dateType = mat.matches();
		return dateType;
	}

	public String getTime(String user_time) {
		String re_time = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date d;
		try {
			d = sdf.parse(user_time);
			long l = d.getTime();
			String str = String.valueOf(l);
			re_time = str.substring(0, 10);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return re_time;
	}

	/**
	 * 信息隐藏
	 */
	public String getHideInfo(String str, int start, int end) {
		int len = str.length();
		LogUtil.e(str+"---"+len);
		if (len >= 6) {
			String hideInfo = str.substring(0, start) + "******" + str.substring(end, len);
			return hideInfo;
		} else {
			return str;
		}
	}

	/*
	 * 扫码
	 */
	public void sweepBarCode() {
		Intent intent = new Intent(MainActivity.this, MipcaActivityCapture.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
	}

	/// ^_^.^_^.^_^.^_^.^_^=============-------------身份证扫描-------------=============^_^.^_^.^_^.^_^.^_^///

	List<String> lstDevices = new ArrayList<String>();
	String deviceList;

	/**
	 * 搜索蓝牙设备
	 */
	public void BluetoothDevice() {
		// 如果蓝牙还没开启,开启蓝牙
		if (mBluetoothAdapter.getState() != BluetoothAdapter.STATE_ON) {
			Toast.makeText(MainActivity.this, "请开启蓝牙", 3000).show();
			return;
		}

		showProgreesDialog("正在搜索");
		if (!mBluetoothAdapter.isDiscovering()) {
			lstDevices.clear();
			deviceList = "";
			// 已匹配连接过的设备
			Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
			if (pairedDevices.size() > 0) {
				for (BluetoothDevice device : pairedDevices) {
					String str = device.getName() + "|" + device.getAddress();
					lstDevices.add(str);
					deviceList = deviceList + str + ",";
				}
			}
			// 搜索
			mBluetoothAdapter.startDiscovery();
		}
	}

	public void stopBluetoothSearch() {
		if (mBluetoothAdapter.isDiscovering())
			mBluetoothAdapter.cancelDiscovery();
	}

	private BroadcastReceiver searchDevices = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(BluetoothDevice.ACTION_FOUND)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device.getName() == null) {
					return;
				}
				String str = device.getName() + "|" + device.getAddress();
				if (lstDevices.indexOf(str) == -1) {// 防止重复添加
					lstDevices.add(str); // 获取设备名称和mac地址
					deviceList = deviceList + str + ",";
				}
			} else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
				Toast.makeText(MainActivity.this, "正在扫描设备，请稍等", 3000).show();
			} else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				Toast.makeText(MainActivity.this, "扫描完成，点击列表中的设备来尝试连接", 3000).show();
				BaseFragment baseFragment = manager.getCurrentFragment();
				baseFragment.loadLua(null, "DeviceResult(page)");
			}
		}
	};

	public String getDeviceList() {
		return deviceList;
	}

	private BtReaderClient mClient;
	private IClientCallBack mBtState;
	private boolean isStopReadCard = false;

	/**
	 * 连接身份证读卡器
	 * 
	 * @param deviceURL
	 *            蓝牙地址
	 */
	public void connectScanning(String bluetoothDevice) {
		// 如果蓝牙还没开启,开启蓝牙
		if (mBluetoothAdapter.getState() != BluetoothAdapter.STATE_ON) {
			Toast.makeText(MainActivity.this, "请开启蓝牙", 3000).show();
			hideProgreesDialog();
			return;
		}

		SharedPreferences sharedPreferences = getSharedPreferences("NEWUSERINDO", Context.MODE_PRIVATE);
		String deviceConnect = decrypt(sharedPreferences.getString("deviceConnect", ""));
		if (deviceConnect.equals("true")) {
			bluetoothDisconnect();
		}

		mClient = new BtReaderClient(this);
		mBtState = new ReceiveBtStateData();
		mClient.setCallBack(mBtState);
		if (mClient != null) {
			String[] values = bluetoothDevice.split("\\|");
			boolean b = mClient.connectBt(values[1]);
			Editor editor = sharedPreferences.edit();
			editor.putString("deviceConnect", encrypt(Boolean.toString(b)));
			if (b) {
				hideProgreesDialog();
				editor.putString("bluetoothDevice", encrypt(bluetoothDevice));
				Log.w("ComShell", "onResume connectBt is true;");
				Toast.makeText(MainActivity.this, "证件扫描仪连接成功", 3000).show();
			} else {
				hideProgreesDialog();
				Log.w("ComShell", "onResume connectBt is false;");
				Toast.makeText(MainActivity.this, "证件扫描仪连接失败", 3000).show();
				bluetoothDisconnect();
			}
			editor.commit();
		}
	}

	class ReceiveBtStateData implements IClientCallBack {
		public void onBtState(final boolean is_connect) {
			SharedPreferences sharedPreferences = getSharedPreferences("NEWUSERINDO", Context.MODE_PRIVATE);
			Editor editor = sharedPreferences.edit();
			editor.putString("deviceConnect", encrypt(Boolean.toString(is_connect)));
			editor.commit();
			if (is_connect == true) {
				// 连接成功
				Log.w("ComShell", "身份证阅读仪连接成功");
			} else if (is_connect == false) {
				// 连接失败
				bluetoothDisconnect();
				Log.w("ComShell", "身份证阅读仪连接因为某些原因断开连接");
			}
		}
	}

	private void bluetoothDisconnect() {
		Log.w("ComShell", "断开连接");
		mClient.disconnectBt();
		mClient.Destroy();
	}

	/**
	 * 读卡
	 */
	public void readCard() {
		new Thread(new GetDataThread()).start();
	}

	String cardDetail;

	private class GetDataThread implements Runnable {
		private String data = null;
		private byte[] cardInfo = new byte[256];
		private Message msg;

		public GetDataThread() {

		}

		public void run() {
			boolean bRet = false;
			if (mClient == null) {
				try {
					if (!mClient.bShellOk)
						return;
					Log.w("ComShell", "after  bShellOk");
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				Log.w("ComShell", "mClient not null");
			}
			try {
				isStopReadCard = false;
				while (!isStopReadCard) {
					Log.w("ComShell", "GetDataThread while...");
					msg = handler.obtainMessage(7, data);
					handler.sendMessage(msg);
					bRet = mClient.SearchCard();
					if ((bRet)) {
						msg = handler.obtainMessage(1, data);
						handler.sendMessage(msg);
						bRet = mClient.SelectCard();
						if ((bRet)) {
							bRet = mClient.ReadCard();
							if (bRet) {
								cardInfo = mClient.GetCardInfoBytes();
								cardDetail = "";
								data = String.format(
										"姓名：%s \r\n性别：%s \r\n民族：%s \r\n出生日期：%s \r\n住址：%s \r\n身份证号：%s \r\n签发机关：%s \r\n有效期：%s-%s",
										mClient.GetName(cardInfo), mClient.GetGender(cardInfo),
										mClient.GetNational(cardInfo), mClient.GetBirthday(cardInfo),
										mClient.GetAddress(cardInfo), mClient.GetIndentityCard(cardInfo),
										mClient.GetIssued(cardInfo), mClient.GetStartDate(cardInfo),
										mClient.GetEndDate(cardInfo));
								cardDetail = mClient.GetName(cardInfo) + "," + mClient.GetAddress(cardInfo) + ","
										+ mClient.GetIndentityCard(cardInfo) + "," + mClient.GetGender(cardInfo) + ",";
								Log.i("", "------------->>>" + data);
								Log.i("", "=============>>>" + cardDetail);
								msg = handler.obtainMessage(0, data);// 发送消息
								handler.sendMessage(msg);
							} else {
								Log.w("ComShell", "GetDataThread readCard error");
								msg = handler.obtainMessage(6, data);// 发送消息
								handler.sendMessage(msg); // readCard error
							}
						} else {
							Log.w("ComShell", "GetDataThread selectCard error");
							msg = handler.obtainMessage(5, data);// 发送消息
							handler.sendMessage(msg); // selectCard error
						}
					} else {
						Log.w("ComShell", "GetDataThread searchCard error");
						msg = handler.obtainMessage(4, data);// 发送消息
						handler.sendMessage(msg); // searchCard error
					}
					Thread.sleep(1500);
					isStopReadCard = true;
				}
				msg = handler.obtainMessage(110, data);// 发送消息
				handler.sendMessage(msg);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String getCard() {
		return cardDetail;
	}

	public Handler handler = new Handler() {// 处理UI绘制
		@SuppressLint("ShowToast")
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				Toast.makeText(MainActivity.this, "读取成功", 2000).show();
				BaseFragment baseFragment = manager.getCurrentFragment();
				baseFragment.loadLua(null, "ScanningResult(page)");
				break;
			case 110:
				break;
			case 4:
				Toast.makeText(MainActivity.this, "寻卡失败", 2000).show();
				break;
			case 5:
				Toast.makeText(MainActivity.this, "选卡失败", 2000).show();
				break;
			case 6:
				Toast.makeText(MainActivity.this, "读卡失败", 2000).show();
				break;
			case 1:
				break;
			case 7:
				break;
			case 87:
				Toast.makeText(MainActivity.this, "读卡初始化中，请稍候...", 2000).show();
				break;
			case 88:
				Toast.makeText(MainActivity.this, "机具信息监听中...", 2000).show();
				break;
			case 99:
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void setSelectedFragment(BaseFragment selectedFragment) {
		mBackHandedFragment = selectedFragment;
	}

	// 返回与退出
	public void onBackPressed() {
		if (mBackHandedFragment != null) {
			mBackHandedFragment.onBackPressed();
		}
	}

	public void exit() {
		this.finish();
	}

	// 与服务器对应的加密解密 start
	// 加密
	public String encryptCs(String sSrc) {
		Log.e("ccccc", "Jiamifdaf d");
		try {
			return AESEncryptor.Encrypt(sSrc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	// 解密
	public static String decryptCs(String sSrc) {
		try {
			return AESEncryptor.Decrypt(sSrc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	// 与服务器对应的加密解密 end
	
	/**
	 * 根据指定的正则切割字符串,给LUA里调用
	 */
	public String[] subString(String str,String regex) {
		
		String[] strings = str.split(regex);
		
		return strings;
		
	}
	
	public String parseResultFromServer(String result, String key) {
		String r = "";
		JSONObject j = null;
		if (!TextUtils.isEmpty(result)) {
			try {
				j = new JSONObject(result);
			} catch (JSONException e) {
				LogUtil.d("json format error");
			}
		}
		if (j == null) {
			r = "";
		} else {
			
			try {
				String s = j.getString("subEntityValues");
				JSONObject jo = new JSONObject(s);
				return jo.getString(key);
			} catch (JSONException e) {
				LogUtil.d("json format error");
			}
		}
		return r;
	}
	
	
	
	
}
