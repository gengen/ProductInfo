package org.g_okuyama.productinfo.lite;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class FrameView extends View {
    public static final String TAG = "FrameView";
    int mWidth = 0;
    int mHeight = 0;
    Size mSize = null;
    private static final int MIN_FRAME_WIDTH = 240;
    private static final int MIN_FRAME_HEIGHT = 240;
    private static final int MAX_FRAME_WIDTH = 480;
    private static final int MAX_FRAME_HEIGHT = 360;

	public FrameView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);

	    int x = canvas.getWidth();
	    int y = canvas.getHeight();
        //Log.d(TAG, "width = " + x);
        //Log.d(TAG, "height = " + y);

        mSize = CameraPreview.mRealSize;        
        if(mSize != null){
            mWidth = mSize.width;
            mHeight = mSize.height;
            //Log.d(TAG, "previewWidth = " + mWidth);
            //Log.d(TAG, "previewHeight = " + mHeight);

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.RED);
            /*
            if((width >= mWidth) && (height >= mHeight)){
                int x = (width-mWidth)/2;
                int y = (height-mHeight)/2;
                canvas.drawRect(x, y, x+mWidth, y+mHeight, paint);
                
                paint.setColor(Color.RED);
                canvas.drawRect(width-mWidth, height-mHeight, mWidth, mHeight, paint);
                //canvas.drawRect((width-mWidth)*2, (height-mHeight)*2, mWidth-(width-mWidth), mHeight-(height-mHeight), paint);
            }
            */
            int width = canvas.getWidth() * 3 / 4;
            if (width < MIN_FRAME_WIDTH) {
              width = MIN_FRAME_WIDTH;
            } else if (width > MAX_FRAME_WIDTH) {
              width = MAX_FRAME_WIDTH;
            }
            int height = canvas.getHeight() * 3 / 4;
            if (height < MIN_FRAME_HEIGHT) {
              height = MIN_FRAME_HEIGHT;
            } else if (height > MAX_FRAME_HEIGHT) {
              height = MAX_FRAME_HEIGHT;
            }
            int leftOffset = (canvas.getWidth() - width) / 2;
            int topOffset = (canvas.getHeight() - height) / 2;
            canvas.drawRect(leftOffset, topOffset, leftOffset + width, topOffset + height, paint);
        }
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
	}
}
