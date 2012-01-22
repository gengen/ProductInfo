package org.g_okuyama.productinfo.lite;

import android.app.ProgressDialog;
import android.graphics.Bitmap; 
import android.graphics.Color;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/*現在のバージョンでは使っていない*/
public class CustomWebViewClient extends WebViewClient {
    ProgressDialog mDialog = null;
    
    public CustomWebViewClient(){
        super();
    }
    
    //@Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
    	if(mDialog != null){
    		return;
    	}
        mDialog = new ProgressDialog(view.getContext());
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setMessage(view.getContext().getString(R.string.pi_analyze));
        mDialog.setCancelable(true);
        mDialog.show(); 
    }
    
    //@Override
    public boolean shouldOverrideUrlLoading(WebView view,String url) {
        view.loadUrl(url);
        return true;
    }
    
    //@Override
    public void onPageFinished(WebView view, String url) {
        if(mDialog != null){
            mDialog.dismiss();
        }
    }
    
    //@Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (mDialog != null) {
            mDialog.dismiss();
        }
        
        Toast.makeText(view.getContext(), R.string.pr_nwerror_title, Toast.LENGTH_LONG).show(); 
    } 
}
