package org.g_okuyama.productinfo.lite;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class ProductInfo extends Activity {
    public static final String TAG = "ProductInfo";
    SurfaceHolder mHolder;
    static FrameView mView = null;
    static ProgressDialog mDialog = null;
    static final int MENU_DISP_PREF = 1;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Toast.makeText(this, R.string.pi_barcode, Toast.LENGTH_LONG).show();

        mView = (FrameView)findViewById(R.id.frameview);

        SurfaceView sv = (SurfaceView)findViewById(R.id.camera);
        
        mHolder = sv.getHolder();
        CameraPreview preview = new CameraPreview(this);
        mHolder.addCallback(preview);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        mDialog = new ProgressDialog(this);
    }
    
    public static void setRect(){
    	//Log.d(TAG, "enter setRect");
        mView.invalidate();
    }
    
    public static void print(){
    	mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	mDialog.setMessage("情報取得中...");
    	mDialog.setCancelable(true);
    	mDialog.show(); 
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //オプションメニュー項目作成(「設定」)
        MenuItem prefItem = menu.add(0, MENU_DISP_PREF, 0 ,"設定");
        prefItem.setIcon(android.R.drawable.ic_menu_preferences);

        return true;
    }
    
    //オプションメニュー選択時のリスナ
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_DISP_PREF:
            //設定画面表示
            Intent pref_intent = new Intent(this, ProductInfoPreference.class);
            //startActivityForResult(pref_intent, REQUEST_CODE);
            startActivity(pref_intent);
            break;
            
        default:
            //何もしない
        }

        return true;
    }
    
    @Override
    public void onResume(){
        super.onResume();
        if(mDialog != null){
            mDialog.dismiss();
        }
    }
    
    public void finish(){
    	new AlertDialog.Builder(this)
    	.setTitle(R.string.pi_finish)
    	.setMessage(getString(R.string.pi_finish_confirm))
    	.setPositiveButton(R.string.pi_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				System.exit(RESULT_OK);
			}
		})
		.setNegativeButton(R.string.pi_ng, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		})
		.show();    	
    }
}