package org.g_okuyama.productinfo.lite;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

import mediba.ad.sdk.android.openx.MasAdView;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

public class WebReview extends Activity {
	public static final String TAG = "WebReview";
    static final int RESULT_CODE = 9999;
	String mProductID = "";
	String mProductURL = null;
	String mReviewURL = null;
	private boolean mUnknownFlag = false;
	private boolean mErrFlag = false;
	
	//for mediba ab
	private MasAdView mAd = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //setContentView(R.layout.web);
        
        Bundle extras = getIntent().getExtras();
        mProductID = extras.getString("productID");

        if(mProductID.equals("notsupported")){
			new AlertDialog.Builder(this)
			.setTitle(/*"通知"*/R.string.cp_not_supported_title)
            .setMessage(/*"このバーコードのフォーマットには未対応です"*/R.string.cp_not_supported)
            .setPositiveButton(R.string.pi_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            })
            .show();
			return;
        }

        //Yahooのレビューが無くて、Amazonを探しにきた場合
        String frag = extras.getString("from");
        if(frag != null){
        	if(frag.equals("YahooNotFound")){
        		mUnknownFlag = true;
        	}
        }
        
        searchBookInfo();
        if(mErrFlag){
        	return;
        }
        displayReview();
    }
    
    private void searchBookInfo(){
    	HttpClient httpClient = new DefaultHttpClient(); 

    	String str = "http://productinfoserv.appspot.com?";
    	//String str = "http://localhost:8080?";
    	str += "key=" + mProductID;
    	HttpGet get = new HttpGet(str);
    	HttpResponse response1 = null;

    	//Log.d(TAG, "URL = " + str);

    	try {
			response1 = httpClient.execute(get);
		} catch (ClientProtocolException e) {
			Log.e(TAG, "ClientProtocolException");
			return;
		} catch (IOException e) {
			Log.e(TAG, "IOException");
			//Toast.makeText(this, R.string.pr_nwerror, Toast.LENGTH_LONG).show();
			new AlertDialog.Builder(this)
            .setTitle(R.string.pr_nwerror_title)
            .setMessage(R.string.pr_nwerror)
            .setPositiveButton(R.string.pi_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            })
            .show();
			mErrFlag = true;
			return;
		}

		int status = response1.getStatusLine().getStatusCode();
		//Log.d(TAG, "responseStatus = " + status);

		if (status == HttpStatus.SC_OK) { 
			ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
			try {
				response1.getEntity().writeTo(baos);
				
				//for debug
				//Log.d(TAG, "startXML");
				//Log.d(TAG, baos.toString());
				
				XmlPullParserFactory factory = null;
				factory = XmlPullParserFactory.newInstance();
				XmlPullParser parser = factory.newPullParser();
				parser.setInput(new StringReader(baos.toString()));

				for(int type = parser.getEventType();type != XmlPullParser.END_DOCUMENT; type = parser.next()){
					String tagName = "";
					
					switch(type){
					case XmlPullParser.START_TAG:
						tagName = parser.getName();
						//Log.d(TAG, "tag name = " + tagName);
						
						if(tagName.equals("IFrameURL")){
							parser.next();
							mReviewURL = parser.getText();
							//Log.d(TAG, "IFrameURL = " + mUrl);	
						}
						
						if(tagName.equals("DetailPageURL")){
							parser.next();
							mProductURL = parser.getText();
							//Log.d(TAG, "DetailPageURL = " + mProductURL);	
						}

						break;
						
					case XmlPullParser.END_TAG:
						break;
						
					default:
						break;
					}					
				}
			} catch (XmlPullParserException e) {
				Log.d(TAG, "XmlPullParseException");
				return;
			} catch (IOException e) {
				Log.d(TAG, "IOException");
				return;
			}
		}
    }

    public void displayReview(){
    	if(mReviewURL == null){
    		if(mUnknownFlag){
    			new AlertDialog.Builder(this)
                .setTitle(R.string.pr_notify)
                .setMessage(R.string.pr_page_notfound)
                .setPositiveButton(R.string.pi_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    			return;
    		}
    		else{
			
    			new AlertDialog.Builder(this)
    			.setTitle(R.string.pr_notfound_title)
    			.setMessage(R.string.pr_review_page_notfound)
    			.setPositiveButton(R.string.pi_ok, new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {                
    					Intent intent = new Intent(WebReview.this, ProductReview.class);                 
    					intent.putExtra("productID", mProductID);
    					//Amazonにレビューページがない場合なので
    					intent.putExtra("from", "AmazonNotFound");
    					startActivityForResult(intent, 0);
    				}
    			})
    			.setNegativeButton(R.string.pi_ng, new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    					finish();
    				}
    			})
    			.show();
    			return;
    		}
    	}
    	
        setContentView(R.layout.web);

    	Button btn1 = (Button)this.findViewById(R.id.button1);
        btn1.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                if(mProductURL != null){
                    Intent i = new Intent(WebReview.this, WebDetail.class);
                    i.putExtra("url", mProductURL);
                    startActivity(i);
                }
            }
        });
        
        Button btn2 = (Button)this.findViewById(R.id.button2);        
        btn2.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent(WebReview.this, ProductReview.class);                 
                intent.putExtra("productID", mProductID);
                intent.putExtra("from", "Amazon");
                startActivityForResult(intent, 0);
            }
        });
        
        mAd = (MasAdView)findViewById(R.id.adview);
        mAd.setAuid("112069");
        mAd.start();
        
        WebView view = (WebView)findViewById(R.id.review);
        view.setWebViewClient(new CustomWebViewClient());
        view.getSettings().setJavaScriptEnabled(true);
        view.getSettings().setBuiltInZoomControls(true);
        view.loadUrl(mReviewURL);
    }
    
    
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        //Log.d(TAG, "enter onActivityResult");
        if(data == null){
            return;
        }
        
        if(resultCode == RESULT_CODE){
            String num = data.getStringExtra("result");
            //Amazonの検索結果がなく、Yahooを検索しに行ったときは、
            //戻ってきたときに表示するものがないので、そのままfinishする
            if(num.equals("unknown")){
                //Log.d(TAG, "unknown");
                finish();
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        
        //Amazonのレビューがiframeなので別ウィンドウで開いてしまう。
        //このため、ブラウザを閉じたときにWebViewが残ってしまうため、
        //WebViewの画面に戻った時点でfinishする。
        if(mReviewURL != null){
            //finish();
        }
    }
    
    public void finish(){
    	//Log.d(TAG, "enter finish");
        Intent intent = new Intent();
        if(mUnknownFlag){
            intent.putExtra("result", "unknown");
        }
        else{
            intent.putExtra("result", "normal");
        }
        this.setResult(RESULT_CODE, intent);
        mProductID = "";
        mReviewURL = null;
        mProductURL = null;
        mUnknownFlag = false;
        mErrFlag = false;
        super.finish();
    }
}