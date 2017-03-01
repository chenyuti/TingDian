package com.logansoft.UIEngine.view.barcode.decode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.BitmapFactory.Options;

public class Imagechange {

  private static final int BLACK = 0xFF000000;
  private static final int WHITE = 0xFFFFFFFF;
	
  int Color=WHITE;
  
	public byte[] change(byte[] data,int offset,int width,int height) {//旋转90度
		byte[] temp=new byte[width*height];
		try{
			for(int i=0;i<height;i++) {
				for(int j=0;j<width;j++) {
					temp[(j+1)*height-i-1]=data[offset+j+i*width];
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return temp;
	}
	
	
	public byte[] changeImage(byte[] data,int width,int height){

		byte[] rotatedData = new byte[data.length];
	
		for (int y = 0; y < height; y++) {
	
		    for (int x = 0; x < width; x++)
	
		        rotatedData[x * height + height - y - 1] = data[x + y * width];
		}

		return rotatedData;
		
	}

	
	public Bitmap change(Bitmap image) {  
		Matrix matrix = new Matrix();   
		matrix.postRotate(90);   
		// recreate the new Bitmap  
		int width=image.getWidth();
		int height=image.getHeight();
		Bitmap resizedBitmap = Bitmap.createBitmap(image, 0, 0, width, height, matrix, true);  
		return resizedBitmap;
	}
	
	
	public Bitmap renderCroppedGreyscaleBitmap(Bitmap temp) {
		
//	    int width = temp.getWidth();
//	    
//	    int height = temp.getHeight();
//	        
//	    int[] yuvData=new int[width*height];
//	    
//	    temp.getPixels(yuvData, 0, temp.getWidth(), 0, 0, width, height);
//	    
//	    int[] pixels = new int[width * height];
//	    
//	    for (int y = 0; y < height; y++) {
//	      for (int x = 0; x < width; x++) {
//	        int grey = yuvData[y * width+x] & 0xFFFFFFFF;
//	        pixels[y * width+x] = (grey==0xFFFFFFFF?WHITE:BLACK);
//	      }
//	    }
//	   
//
//	    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//	    bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
//	    return bitmap;
		

		  int height = temp.getHeight();
		 
		  int width = temp.getWidth();
		 
		  int[] pix = new int[width * height];
		 
		  temp.getPixels(pix, 0, width, 0, 0, width, height);
		 
		  int R, G, B;
		 
		  for (int y = 0; y < height; y++)
		 
		   for (int x = 0; x < width; x++) {
		 
		    int index = y * width + x;
		 
		    int r = (pix[index] >> 16) & 0xff;
		 
		    int g = (pix[index] >> 8) & 0xff;
		 
		    int b = pix[index] & 0xff;
		 
		    // 图像黑白化
		 
		    R = r * 7 / 10;
			 
		    R = (R < 0) ? 0 : ((R > 255) ? 255 : 0);
		 
		    G = g * 2 / 10;
		 
		    G = (G < 0) ? 0 : ((G > 255) ? 255 : 0);
		 
		    B = b / 10;
		 
		    B = (B < 0) ? 0 : ((B > 255) ? 255 : 0);
		    
//		    R = r * 7 / 10;
//		 
//		    R = (R < 0) ? 0 : ((R > 255) ? 255 : R);
//		 
//		    G = g * 2 / 10;
//		 
//		    G = (G < 0) ? 0 : ((G > 255) ? 255 : G);
//		 
//		    B = b / 10;
//		 
//		    B = (B < 0) ? 0 : ((B > 255) ? 255 : B);
		 
		    pix[index] = 0xff000000 | (R << 16) | (G << 8) | B;
		 
		   }
		 
		  Bitmap temp_6 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		 
		  temp_6.setPixels(pix, 0, width, 0, 0, width, height);

		  return temp_6;
		  
	  }
	
}
