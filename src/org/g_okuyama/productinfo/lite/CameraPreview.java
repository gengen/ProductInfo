package org.g_okuyama.productinfo.lite;

/*
 * Copyright (C) 2007 The Android Open Source Project
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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;

import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.client.androidtest.RGBLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

class CameraPreview implements SurfaceHolder.Callback {
    public static final String TAG = "CameraPreview";
    SurfaceHolder mHolder;
    Camera mCamera;
    Context mContext;
    AutoFocusCallback mFocus = null;
    private Size mSize;
    public static Size mRealSize = null;
    private List<Size> mSupportSizeList;

    CameraPreview(Context context) {
        mContext = context;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        mCamera = Camera.open();   
           
        try {
            Camera.Parameters params = mCamera.getParameters();   
            mSupportSizeList = Reflect.getSupportedPreviewSizes(params);

            if (mSupportSizeList != null && mSupportSizeList.size() > 0) {   
                mSize = mSupportSizeList.get(0);
                params.setPreviewSize(mSize.width, mSize.height);   
                mCamera.setParameters(params);                
            }   

            mCamera.setPreviewDisplay(holder);
               
        } catch (IOException e) {
            Log.e(TAG, "IOException");
            mCamera.release();
            mCamera = null;
        }   
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
        //Log.d(TAG, "enter surfaceDestroyed");
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;

        mSupportSizeList = null;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        //Log.d(TAG, "enter surfaceChanged");
        //Log.d(TAG, "sizeW = " + w);
        //Log.d(TAG, "sizeH = " + h);

        Camera.Parameters params = mCamera.getParameters();   
        params.setPreviewSize(mSize.width, mSize.height);   
        mCamera.setParameters(params);
        
        //Log.d(TAG, "mSize.width = " + mSize.width);
        //Log.d(TAG, "mSize.height = " + mSize.height);
        
        mCamera.startPreview();
        final PreviewCallback previewCallback = new PreviewCallback(this);
        mFocus = new AutoFocusCallback(){
            public void onAutoFocus(boolean success, Camera camera) {
            	if(mCamera != null){
            		camera.setOneShotPreviewCallback(previewCallback);
            	}
            }            
        };
        mCamera.autoFocus(mFocus);
    }
    
    public class PreviewCallback implements Camera.PreviewCallback {
        private CameraPreview mPreview = null;

        PreviewCallback(CameraPreview preview){
            mPreview = preview;
        }

        public void onPreviewFrame(byte[] data, Camera camera) {
            //Log.d(TAG, "enter onPreviewFrame");
            //Log.d(TAG, "data.length = " + data.length);

            //convert to "real" preview size. not size setting before.
            Size size = convertPreviewSize(data);
            if(size == null){
            	return;
            }

            if(mRealSize == null){
                ProductInfo.setRect();
                mRealSize = size;
            }
            
            final int width = size.width;
            final int height = size.height;            
            int[] rgb = new int[(width * height)];
            
            //Log.d(TAG, "width = " + width);
            //Log.d(TAG, "height = " + height);  

            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            
            //Log.d(TAG, "bitmap.x = " + bmp.getWidth());
            //Log.d(TAG, "bitmap.y = " + bmp.getHeight());
            
            decodeYUV420SP(rgb, data, width, height);
            bmp.setPixels(rgb, 0, width, 0, 0, width, height);

            //from androidtest
            LuminanceSource source = new RGBLuminanceSource(bmp);
            BinaryBitmap binary = new BinaryBitmap(new HybridBinarizer(source));

            //Log.d(TAG, "binary.width = " + binary.getWidth());
            //Log.d(TAG, "binary.height = " + binary.getHeight());

            Reader reader = new MultiFormatReader();
            try {
                Result result = reader.decode(binary);

                String productid = result.getText();
                Log.d(TAG, "result = " + productid);
                
                //âêÕíÜÇÃé|Çï\é¶
                ProductInfo.print();
                
                BarcodeFormat bf = result.getBarcodeFormat();
                String format = bf.getName();
                //Log.d(TAG, "format = " + format);

                Intent intent = null;
                if(ProductInfoPreference.getPriority(mContext).equals(ProductInfoPreference.PRIORITY_ITEM)){
                    intent = new Intent(mContext, ProductReview.class);         	                	
                }
                else{
                    intent = new Intent(mContext, WebReview.class);         	                	                	
                }

                if(format.equals("EAN_13") || format.equals("EAN_8")){
                	intent.putExtra("productID", productid);
                }
                else{
                	intent.putExtra("productID", "notsupported");
                }
            	mContext.startActivity(intent);

            } catch (NotFoundException e) {
                Log.e(TAG, "NotFoundException");
                camera.autoFocus(mFocus);
            } catch (ChecksumException e) {
                Log.e(TAG, "ChecksumException");
            } catch (FormatException e) {
                Log.e(TAG, "FormatException");
            }
       }
        
        private Size convertPreviewSize(byte[] data){
            double displaysize = data.length / 1.5;
            Size size;
            int x, y;
            
            if(mSupportSizeList == null){
            	return null;
            }
            for(int i=0; i<mSupportSizeList.size(); i++){
                size = mSupportSizeList.get(i);
                x = size.width;
                y = size.height;
                if((x*y) == displaysize){
                    return size;
                }
            }
            return null;
        }
        
        // YUV420 to BMP 
        public void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) { 
            final int frameSize = width * height; 

            for (int j = 0, yp = 0; j < height; j++) { 
                int uvp = frameSize + (j >> 1) * width, u = 0, v = 0; 
                for (int i = 0; i < width; i++, yp++) { 
                    int y = (0xff & ((int) yuv420sp[yp])) - 16; 
                    if (y < 0) y = 0; 
                    if ((i & 1) == 0) { 
                            v = (0xff & yuv420sp[uvp++]) - 128; 
                            u = (0xff & yuv420sp[uvp++]) - 128; 
                    } 

                    int y1192 = 1192 * y; 
                    int r = (y1192 + 1634 * v); 
                    int g = (y1192 - 833 * v - 400 * u); 
                    int b = (y1192 + 2066 * u); 

                    if (r < 0) r = 0; else if (r > 262143) r = 262143; 
                    if (g < 0) g = 0; else if (g > 262143) g = 262143; 
                    if (b < 0) b = 0; else if (b > 262143) b = 262143; 

                    rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff); 
                } 
            }
        }
    }
}