package org.g_okuyama.productinfo.lite;

import java.util.HashMap;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SummaryAdapter extends ArrayAdapter<String>{
    public static final String TAG = "SummaryAdapter";
    private String[] items;
    private LayoutInflater inflater;
    Context mContext;
    static HashMap<String, Integer> mHashMap = null;
    
    static{
    	mHashMap = new HashMap<String, Integer>();
    	mHashMap.put("1.00", R.drawable.star_1_0);
    	mHashMap.put("1.50", R.drawable.star_1_5);
    	mHashMap.put("2.00", R.drawable.star_2_0);
    	mHashMap.put("2.50", R.drawable.star_2_5);
    	mHashMap.put("3.00", R.drawable.star_3_0);
    	mHashMap.put("3.50", R.drawable.star_3_5);
    	mHashMap.put("4.00", R.drawable.star_4_0);
    	mHashMap.put("4.50", R.drawable.star_4_5);
    	mHashMap.put("5.00", R.drawable.star_5_0);
    }

    public SummaryAdapter(Context context, int textViewResourceId, String[] items) {
        super(context, textViewResourceId, items);
        //Log.d(TAG, "enter SummaryAdapter");
        this.mContext = context;
        this.items = items;   
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);   
    }
        
    @Override  
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;   
        int padding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                3, mContext.getResources().getDisplayMetrics());

        if (view == null) {   
            view = inflater.inflate(R.layout.summary, null);
            // 背景画像をセットする   
            //view.setBackgroundResource(R.drawable.back);   
            //view.setBackgroundColor(Color.rgb(203, 241, 162));
        }   

        // 表示すべきデータの取得   
        String item = (String)items[position];
        String tmp[] = item.split(",");
        if(tmp.length != 2){
            return null;
        }
        String title = tmp[0];
        String score = tmp[1];
        
        //Log.d(TAG, "title = " + title);
        //Log.d(TAG, "score = " + score);
        
        if (item != null) {   
            TextView titleview = (TextView)view.findViewById(R.id.titletext);
            titleview.setPadding(padding, padding, padding, 0);
            titleview.setText(title);

        	ImageView imageview = (ImageView)view.findViewById(R.id.imageview);
            imageview.setPadding(padding, padding, padding, padding);
            if(score != null && mHashMap.get(score) != null){
            	imageview.setImageResource(mHashMap.get(score));
            }
            else{
            	//評価がない場合はデフォルトを入れるか？
            	//imageview.setImageResource("");
            }
        }
        
        return view;   
    }
}
