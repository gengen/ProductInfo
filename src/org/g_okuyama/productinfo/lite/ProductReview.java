package org.g_okuyama.productinfo.lite;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import mediba.ad.sdk.android.openx.MasAdView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.*;

public class ProductReview extends FragmentActivity {
	public static final String TAG = "ProductReview";
	private static final String APP_ID = "pckU8l2xg66VRFXV7.99mX9RQxULG.WErQeZT73FYIcwrK9Sk1gtqMOCFivZZsU-";
    private static final String USER_ID = "neg_1125";
    private static final int REQUEST_CODE = 1111;
    private static final int RESULT_CODE = 9999;
	String mProductID = null;
	static String[] sReviewAbst = null;
	static String[] sReviewData = null;
  	private String mProductURL = null;
  	//レビューがあるか否かのフラグ
  	private boolean mFound = false;
	private boolean mUnknownFlag = false;
	private boolean mFromAmazon = false;
	
	//for mediba ab
	private static MasAdView mAd = null;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extras = getIntent().getExtras();
        mProductID = extras.getString("productID");
        
        //Log.d(TAG, "productID = " + mProductID);
        
        //Amazonのレビューページが無くて、Yahooを探しにきた場合
        String frag = extras.getString("from");
        if(frag != null){
        	if(frag.equals("AmazonNotFound")){
        		mUnknownFlag = true;
        	}
        	
        	if(frag.equals("Amazon")){
        		mFromAmazon = true;
        	}
        }
        
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
        
        searchProductInfo();

        if(mFound == false){
        	return;
        }
        
        setContentView(R.layout.fragment);

        Button btn1 = (Button)this.findViewById(R.id.button1);        
        btn1.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                //WebView view = (WebView)findViewById(R.id.review);
                //view.setWebViewClient(new CustomWebViewClient());
                //view.getSettings().setJavaScriptEnabled(true);
                if(mProductURL != null){
                    Intent i = new Intent(ProductReview.this, WebDetail.class);
                    i.putExtra("url", mProductURL);
                    startActivity(i);
                    //view.loadUrl(mProductURL);
                }
                
            }
        });
        
        Button btn2 = (Button)this.findViewById(R.id.button2);        
        btn2.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent(ProductReview.this, WebReview.class);                 
                intent.putExtra("productID", mProductID);
                //Yahooにレビューがある場合なので
                intent.putExtra("from", "Yahoo");
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
        
        mAd = (MasAdView)findViewById(R.id.adview);
    }
    
    private void searchProductInfo(){
		String abst = "";
    	String review = "";
		int max = 0;
		int idx = 0;
		boolean init = true;
    	
    	HttpClient httpClient = new DefaultHttpClient(); 

    	String str = "http://shopping.yahooapis.jp/ShoppingWebService/V1/reviewSearch?";
    	String encodedID = URLEncoder.encode(APP_ID);
    	String encodedJAN = URLEncoder.encode(mProductID);
    	//String sort = "review_rate";
    	str += "appid=" + encodedID + "&jan=" + encodedJAN 
    		+ "&affiliate_type=yid" +  "&affiliate_id=" + USER_ID;// + "&sort=" + sort;
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
	         
			return;
		}

		int status = response1.getStatusLine().getStatusCode();
		//Log.d(TAG, "responseStatus = " + status);

		if (status == HttpStatus.SC_OK) { 
			ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
			try {
				response1.getEntity().writeTo(baos);
				XmlPullParserFactory factory = null;
				factory = XmlPullParserFactory.newInstance();
				XmlPullParser parser = factory.newPullParser();
				parser.setInput(new StringReader(baos.toString()));
				
				//for debug
				//Log.d(TAG, baos.toString());

				for(int type = parser.getEventType();type != XmlPullParser.END_DOCUMENT; type = parser.next()){
					String tagName = "";
					String tagText = "";
					String tagTitle = "";
					
					switch(type){
					case XmlPullParser.START_TAG:
						tagName = parser.getName();
						//Log.d(TAG, "tag name = " + tagName);
						
						for (int i = 0; i < parser.getAttributeCount(); i++) {
							String attrName = parser.getAttributeName(i);
							//Log.d(TAG, "attribute name = " + attrName);
							if(attrName.equals("totalResultsReturned")){
								String attrValue = parser.getAttributeValue(i);
								//Log.d(TAG, "attribute value = " + attrValue);
								max = Integer.parseInt(attrValue);
							}
						}
						
						if(tagName.equals("ReviewTitle")){
							parser.next();
							tagTitle = parser.getText();
							//Log.d(TAG, "title = " + tagTitle);
							abst += tagTitle;
							//abst += System.getProperty("line.separator");
						}

						if(tagName.equals("Description")){
							parser.next();
							tagText = parser.getText();
							//Log.d(TAG, "description = " + tagText);
							review += tagText;
						}
						
						if(tagName.equals("Ratings")){
							parser.next();
							//Log.d(TAG, "name = " + parser.getName());
							if(parser.getName().equals("Rate")){
								parser.next();
								String rate = parser.getText();
								//Log.d(TAG, "value = " + rate);
                                //abst += getString(R.string.pr_score) + rate + getString(R.string.pr_value);
								abst += "," + rate;
							}
						}

						if(tagName.equals("Code")){
							//一番初めの商品のURLを有効にする
							if(mProductURL != null){
								break;
							}
							parser.next();//store name
							parser.next();//end_tag
							parser.next();//url tag
							parser.next();
							
							mProductURL = parser.getText();
							//Log.d(TAG, "value = " + mProductURL);
						}
						
						if(tagName.equals("Error")){
							Toast.makeText(this, R.string.pr_notfound, Toast.LENGTH_LONG).show();
						}

						break;
						
					case XmlPullParser.END_TAG:
						tagName = parser.getName();
						//Log.d(TAG, "end tag name = " + tagName);
						if(tagName.equals("Result")){
							if(init == true && max != 0){
								sReviewData = new String[max];
								sReviewAbst = new String[max];
								init = false;
							}
							
							if(max != 0 ){
								/*
								Log.d(TAG, "idx = " + idx);
								Log.d(TAG, "max = " + max);
								*/
								sReviewAbst[idx] = new String(abst);
								sReviewData[idx] = new String(review);
								idx++;
								abst = "";
								review = "";
							}							
						}
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
		
		if(max == 0){
            //Toast.makeText(this, R.string.pr_notfound, .LENGTH_LONG).show();
		    //Log.d(TAG, "review is not found");
		    
		    if(mUnknownFlag || mFromAmazon){
				new AlertDialog.Builder(this)
	            .setTitle(R.string.pr_notify)
	            .setMessage(R.string.pr_review_notfound)
	            .setPositiveButton(R.string.pi_ok, new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {
	                    finish();
	                }
	            })
	            .show();
				return;
		    }
		    
		    new AlertDialog.Builder(this)
	            .setTitle(R.string.pr_notfound_title)
	            .setMessage(R.string.pr_notfound)
	            .setPositiveButton(R.string.pi_ok, new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {                
	                    Intent intent = new Intent(ProductReview.this, WebReview.class);                 
	                    intent.putExtra("productID", mProductID);
	                    //Yahooにレビューがない場合なので
                        intent.putExtra("from", "YahooNotFound");
	                    startActivityForResult(intent, REQUEST_CODE);
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
		
		//レビューが見つかった
		mFound = true;
		return;

    }    

    public static class TitlesFragment extends ListFragment {
        int mCurCheckPosition = 0;
                
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            if(ProductReview.sReviewAbst != null){
                //setListAdapter(new ArrayAdapter<String>(getActivity(), R.layout.data, ProductReview.sReviewAbst));              
                setListAdapter(new SummaryAdapter(getActivity(), R.layout.summary, ProductReview.sReviewAbst));              
            }

        	if (savedInstanceState != null) {
                mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
            }

            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            showDetails(mCurCheckPosition);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt("curChoice", mCurCheckPosition);
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            showDetails(position);
        }

        void showDetails(int index) {
            mCurCheckPosition = index;

            getListView().setItemChecked(index, true);

            DetailsFragment details = (DetailsFragment)getFragmentManager().findFragmentById(R.id.detail_frame);
            if (details == null || details.getShownIndex() != index) {
                details = DetailsFragment.newInstance(index);

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.detail_frame, details);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
            }
        }
    }

    public static class DetailsFragment extends Fragment {
        public static DetailsFragment newInstance(int index) {
            DetailsFragment f = new DetailsFragment();

            Bundle args = new Bundle();
            args.putInt("index", index);
            f.setArguments(args);

            return f;
        }

        public int getShownIndex() {
            return getArguments().getInt("index", 0);
        }
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            if (container == null) {
                return null;
            }

            int padding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    10, getActivity().getResources().getDisplayMetrics());

            ScrollView scroller = new ScrollView(getActivity());
            
            //レビュー詳細
            TextView textDetail = new TextView(getActivity());
            textDetail.setPadding(padding, padding, padding, padding);
            scroller.addView(textDetail);
            
            if(sReviewAbst == null){
            	return scroller;
            }
            
            //タイトルと評価点を分ける
            String item = ProductReview.sReviewAbst[getShownIndex()];
            String tmp[] = item.split(",");
            if(tmp.length != 2){
                return null;
            }
            String title = tmp[0];
            String score = tmp[1];
            
            /*
            text.setText(ProductReview.sReviewAbst[getShownIndex()]);
            text.append(System.getProperty("line.separator"));
            text.append(System.getProperty("line.separator"));
            */
            if(title != null){
                //レビューのタイトル
                TextView textTitle = (TextView)getActivity().findViewById(R.id.detail_title);
                textTitle.setPadding(padding, padding, padding, 0);
            	textTitle.setText(title);
            }
            
            if(score != null && SummaryAdapter.mHashMap.get(score) != null){
                //評価点
                ImageView imageScore = (ImageView)getActivity().findViewById(R.id.detail_score);
                imageScore.setPadding(padding, padding, padding, 0);
            	imageScore.setImageResource(SummaryAdapter.mHashMap.get(score));
            }

            textDetail.append(System.getProperty("line.separator"));
            textDetail.append(ProductReview.sReviewData[getShownIndex()]);
            
            mAd.setAuid("112069");
            mAd.start();
            
            return scroller;
        }
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data){
    	//Log.d(TAG, "enter onActivityResult");
        if(data == null){
            return;
        }
        
        if(resultCode == RESULT_CODE){
            String num = data.getStringExtra("result");
            //Yahooの検索結果がなく、Amazonを検索しに行ったときは、
            //戻ってきたときに表示するものがないので、そのままfinishする
            //明示的にAmazonに行ったときは、なにもしない
            if(num.equals("unknown")){
                Log.d(TAG, "unknown");
                finish();
            }
        }
    }
    
    public void finish(){
        Intent intent = new Intent();
        if(mUnknownFlag){
            intent.putExtra("result", "unknown");
            this.setResult(RESULT_CODE, intent);
        }
        
        sReviewAbst = null;
        sReviewData = null;
      	mFound = false;
    	mUnknownFlag = false;
    	super.finish();
    }
}