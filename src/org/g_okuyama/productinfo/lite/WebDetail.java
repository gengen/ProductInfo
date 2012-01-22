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
        
        //WebView���ʃE�B���h�E�ŊJ���Ă��܂��B
        //���̂��߁A�u���E�U������Ƃ���WebView���c���Ă��܂����߁A
        //WebView�̉�ʂɖ߂������_��finish����B
        //if(mUrl != null){
        finish();
        //}
    }
}
