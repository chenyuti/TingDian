package com.logansoft.UIEngine.view.barcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.logansoft.UIEngine.R;
import com.logansoft.UIEngine.utils.GlobalConstants;
import com.logansoft.UIEngine.view.barcode.camera.CameraManager;
import com.logansoft.UIEngine.view.barcode.decode.CaptureActivityHandler;
import com.logansoft.UIEngine.view.barcode.decode.InactivityTimer;
import com.logansoft.UIEngine.view.barcode.view.ViewfinderView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import java.io.IOException;
import java.util.Vector;

public class CaptureActivity extends Activity implements Callback {

	private CaptureActivityHandler handler;
	private SurfaceView surfaceView;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private TextView txtResult;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	private ImageView btnFlashLight ;
	public static final String KEY_FRONT_LIGHT = "flash_light" ;
	private SharedPreferences pref ;

	/** Called when the activity is first created. */
	public static String FUNCTIONNAME="function_name";
	private String function_name="";
	
	private boolean flashOn = true ;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.main);
		//初始化 CameraManager
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		flashOn = pref.getBoolean(KEY_FRONT_LIGHT, true);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);//取消状态栏	 
		
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏
		
		function_name=this.getIntent().getStringExtra(FUNCTIONNAME);
		
		//全屏状态
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
                WindowManager.LayoutParams.FLAG_FULLSCREEN);  
		
		FrameLayout framelayout=new FrameLayout(this);
		framelayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		
		CameraManager.init(getApplication());
		
		surfaceView = new SurfaceView(this);
		surfaceView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		
		viewfinderView = new ViewfinderView(this);
		txtResult = new TextView(this);
		
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		
		btnFlashLight = new ImageView(this);
		android.widget.FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100, android.widget.FrameLayout.LayoutParams.MATCH_PARENT);
		params.gravity = Gravity.RIGHT ;
		btnFlashLight.setScaleType(ScaleType.CENTER);
		btnFlashLight.setLayoutParams(params);
		btnFlashLight.setBackgroundResource(R.drawable.barcode_gray_color_sel);
		
		if(flashOn) {
		    btnFlashLight.setImageResource(R.drawable.barcode_flash_on);
		}else {
		    btnFlashLight.setImageResource(R.drawable.barcode_flash_off);
		}

		btnFlashLight.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(flashOn) {
                    CameraManager.handleFlashlight(flashOn=!flashOn);
                    btnFlashLight.setImageResource(R.drawable.barcode_flash_off);
                }else {
                    CameraManager.handleFlashlight(flashOn=!flashOn);
                    btnFlashLight.setImageResource(R.drawable.barcode_flash_on);
                }
                
            }
        });
		
		
		framelayout.addView(surfaceView);
		framelayout.addView(viewfinderView);
		framelayout.addView(txtResult);
		framelayout.addView(btnFlashLight);
		
//		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
		
		setContentView(framelayout);
		
	}


	@Override
	protected void onResume() {
		super.onResume();
//		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
		
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
		Editor editor = pref.edit();
		editor.putBoolean(KEY_FRONT_LIGHT, flashOn);
		editor.commit();
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats,
					characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	//对返回的message进行处理
	public void handleDecode(Result obj, Bitmap barcode) {
		inactivityTimer.onActivity();
//		if(barcode!=null) {
//			viewfinderView.drawResultBitmap(barcode);
//		}
//		playBeepSoundAndVibrate();
//		txtResult.setText("扫描完成，条形码信息是:"+obj.getBarcodeFormat().toString() + ":"
//				+ obj.getText());
	
		
//   		String she="Callback kfc=new HttpCallback();" +
//   				"kfc.setResponseTextbystorage(\"responsetext\");kfc.setResponseCodebystorage(\"responsecode\");" +
//   						"alert(kfc.getResponseCode());"+function_name+"(kfc);";
		
   		playBeepSoundAndVibrate();
		
   		Intent intent=new Intent();
        intent.putExtra(GlobalConstants.EXTRA_BARCODE,obj.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
   		
	}

	
	@Override
	public void finish() {
		overridePendingTransition(R.anim.setting_in,R.anim.more_out);
		super.finish();
	}
	
	
	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

}