/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.logansoft.UIEngine.view.barcode.decode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.logansoft.UIEngine.R;
import com.logansoft.UIEngine.view.barcode.CaptureActivity;
import com.logansoft.UIEngine.view.barcode.camera.CameraManager;
import com.logansoft.UIEngine.view.barcode.view.ViewfinderResultPointCallback;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Vector;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CaptureActivityHandler extends Handler {
    
    /**
     *  <item type="id" name="auto_focus"/>
  <item type="id" name="return_scan_result"/>

  <item type="id" name="encode_succeeded"/>
  <item type="id" name="encode_failed"/>

  <item type="id" name="launch_product_query"/>
  
  <item type="id" name="decode_failed"/>
  <item type="id" name="decode_succeeded"/>
  <item type="id" name="decode"/>

  <item type="id" name="restart_preview"/>
  <item type="id" name="quit"/>
  
  <item type="id" name="search_book_contents_failed"/>
  <item type="id" name="search_book_contents_succeeded"/>
     */
     
    public static final int auto_focus=0x0001;
    public static final int restart_preview=0x0002;

    public static final int return_scan_result=0x0005;
    public static final int launch_product_query=0x0006;
    
    public static final int _quit= 0x0007;

    private static final String TAG = CaptureActivityHandler.class.getSimpleName();

  private final CaptureActivity activity;
  private final DecodeThread decodeThread;
  private State state;

  private enum State {
    PREVIEW,
    SUCCESS,
    DONE
  }

  public CaptureActivityHandler(CaptureActivity activity, Vector<BarcodeFormat> decodeFormats,
      String characterSet) {
    this.activity = activity;
    decodeThread = new DecodeThread(activity, decodeFormats, characterSet,
        new ViewfinderResultPointCallback(activity.getViewfinderView()));
    decodeThread.start();
    state = State.SUCCESS;

    // Start ourselves capturing previews and decoding.
    CameraManager.get().startPreview();
    restartPreviewAndDecode();
  }

  @Override
  public void handleMessage(Message message) {
    switch (message.what) {
      case auto_focus:
        //Log.d(TAG, "Got auto-focus message");
        // When one auto focus pass finishes, start another. This is the closest thing to
        // continuous AF. It does seem to hunt a bit, but I'm not sure what else to do.
        if (state == State.PREVIEW) {
          CameraManager.get().requestAutoFocus(this,auto_focus);
        }
        break;
      case restart_preview:
        Log.d(TAG, "Got restart preview message");
        restartPreviewAndDecode();
        break;
      case DecodeHandler.decode_succeeded:
        Log.d(TAG, "Got decode succeeded message");
        state = State.SUCCESS;
        Bundle bundle = message.getData();
        Bitmap barcode = bundle == null ? null :
            (Bitmap) bundle.getParcelable(DecodeThread.BARCODE_BITMAP);
        activity.handleDecode((Result) message.obj, barcode);
        break;
      case DecodeHandler.decode_failed:
        // We're decoding as fast as possible, so when one decode fails, start another.
        state = State.PREVIEW;
        CameraManager.get().requestPreviewFrame(decodeThread.getHandler(),DecodeHandler.decode);
        break;
      case return_scan_result:
        Log.d(TAG, "Got return scan result message");
        activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
        activity.finish();
        break;
      case launch_product_query:
        Log.d(TAG, "Got product query message");
        String url = (String) message.obj;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        activity.startActivity(intent);
        break;
    }
  }

  public void quitSynchronously() {
    state = State.DONE;
    CameraManager.get().stopPreview();
    Message quit = Message.obtain(decodeThread.getHandler(), _quit);
    quit.sendToTarget();
    try {
      decodeThread.join();
    } catch (InterruptedException e) {
      // continue
    }

    // Be absolutely sure we don't send any queued up messages
    removeMessages(DecodeHandler.decode_succeeded);
    removeMessages(DecodeHandler.decode_failed);
  }

  private void restartPreviewAndDecode() {
    if (state == State.SUCCESS) {
      state = State.PREVIEW;
      CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), DecodeHandler.decode);
      CameraManager.get().requestAutoFocus(this, auto_focus);
      activity.drawViewfinder();
    }
  }

}
