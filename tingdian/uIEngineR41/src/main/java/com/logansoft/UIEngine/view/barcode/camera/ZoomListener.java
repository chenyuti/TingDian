package com.logansoft.UIEngine.view.barcode.camera;
//package com.moretek.salemanagerapp.barcodescan.camera;
//
//import android.hardware.Camera;
//import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
//import android.util.Log;
//import android.widget.Toast;
//
//public class ZoomListener implements OnKeyboardActionListener {
//    private Camera camera;
//    
//    public ZoomListener(Camera camera) {
//        this.camera = camera;
//    }
//
//    public void onKey(int primaryCode, int[] keyCodes) {
//        // TODO Auto-generated method stub
//
//    }
//
//    public void onPress(int primaryCode) {
//        // TODO Auto-generated method stub
//        int z = camera.getParameters().getZoom();
//        if(primaryCode == 175 && z<46) {
//            
//            camera.startSmoothZoom((++z));
//            Log.i("camera", "zoom ++");
//        }else if(primaryCode == 174 && z >0) {
//            camera.startSmoothZoom(--z);
//            Log.i("camera", "zoom --");
//        }
//
//    }
//
//    public void onRelease(int primaryCode) {
//        // TODO Auto-generated method stub
//
//    }
//
//    public void onText(CharSequence text) {
//        // TODO Auto-generated method stub
//
//    }
//
//    public void swipeDown() {
//        // TODO Auto-generated method stub
//
//    }
//
//    public void swipeLeft() {
//        // TODO Auto-generated method stub
//
//    }
//
//    public void swipeRight() {
//        // TODO Auto-generated method stub
//
//    }
//
//    public void swipeUp() {
//        // TODO Auto-generated method stub
//
//    }
//
//}
