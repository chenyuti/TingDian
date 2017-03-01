package com.logansoft.scss.signature;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.logansoft.UIEngine.utils.LogUtil;
import com.logansoft.scss.R;
import com.logansoft.scss.signature.views.SignaturePad;

public class SignatureActivity extends Activity {

    private SignaturePad mSignaturePad;
    private TextView mTextView;
    private Button mClearButton;
    private Button mSaveButton;
    File photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_signature);

        mSignaturePad = (SignaturePad) findViewById(R.id.signature_pad);
        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onSigned() {
                mSaveButton.setEnabled(true);
                mClearButton.setEnabled(true);
            }

            @Override
            public void onClear() {
                mSaveButton.setEnabled(false);
                mClearButton.setEnabled(false);
            }
        });

        mTextView = (TextView) findViewById(R.id.signature_pad_description); 
        mClearButton = (Button) findViewById(R.id.clear_button);
        mSaveButton = (Button) findViewById(R.id.save_button);
        
		Bundle bundle = getIntent().getExtras();
		String askWord = bundle.getString("word");
        mTextView.setText(askWord);

        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSignaturePad.clear();
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();
                if(addSignatureToGallery(signatureBitmap)) {
                    Intent intent = new Intent(); 
                    intent.putExtra("signature_url", photo.toString());
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(SignatureActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

	public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e("SignaturePad", "Directory not created");
        }
        return file;
    }

    public void saveBitmapToJPG(Bitmap bitmap, File photo) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(photo);
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        stream.close();
    }

    public boolean addSignatureToGallery(Bitmap signature) {
        boolean result = false;
        try {
    		Bundle bundle = getIntent().getExtras();
    		String mobile = bundle.getString("mobile");
    		String business = bundle.getString("business");
            photo = new File(getAlbumStorageDir("SignaturePad"), String.format(mobile+"_"+business+"_signature_%s.jpg", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())));
            saveBitmapToJPG(signature, photo);
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(photo);
            mediaScanIntent.setData(contentUri);
            SignatureActivity.this.sendBroadcast(mediaScanIntent);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
