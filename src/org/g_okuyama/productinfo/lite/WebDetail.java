package org.g_okuyama.productinfo.lite;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

public class WebDetail extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.web);

        WebView view = (WebView)findViewById(R.id.review);
        Bundle extras = getIntent().getExtras();        
        String url = extras.getString("url");
        view.loadUrl(url);
    }
    
    @Override
    public void onResume(){
        super.onResume();
        
        //WebViewが別ウィンドウで開いてしまう。
        //このため、ブラウザを閉じたときにWebViewが残ってしまうため、
        //WebViewの画面に戻った時点でfinishする。
        //if(mUrl != null){
        finish();
        //}
    }
}
