package org.g_okuyama.productinfo.lite;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;

public class ProductInfoPreference extends PreferenceActivity implements OnPreferenceChangeListener{
	public static final String PRIORITY_BOOK = "1";
	public static final String PRIORITY_ITEM = "2";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.preference);

        //åªç›ÇÃóDêÊê›íËÇÉTÉ}ÉäÇ…ï\é¶
        String priority = ProductInfoPreference.getPriority(this);
        Preference pref = this.findPreference("search_priority");
        pref.setOnPreferenceChangeListener(this);
        if(priority == null){
        	return;
        }
        
        if(priority.equals(PRIORITY_ITEM)){
            pref.setSummary((CharSequence)getString(R.string.search_priority_item));
        }
        else if(priority.equals(PRIORITY_BOOK)){
        	pref.setSummary((CharSequence)getString(R.string.search_priority_book));        	
        }
        else{
        	return;
        }
    }
    
    public static String getPriority(Context c){
        return PreferenceManager.getDefaultSharedPreferences(c)
                .getString("search_priority", /*default*/PRIORITY_BOOK);
    }

	public boolean onPreferenceChange(Preference pref, Object newValue) {
		final CharSequence value = (CharSequence)newValue;
		if(value == null){
			return false;
		}
		
		if(pref.getKey().equals("search_priority")){
	        if(value.equals(PRIORITY_ITEM)){
	            pref.setSummary((CharSequence)getString(R.string.search_priority_item));
	        }
	        else if(value.equals(PRIORITY_BOOK)){
	        	pref.setSummary((CharSequence)getString(R.string.search_priority_book));        	
	        }
	        else{
	        	return false;
	        }
		}
		
		return true;
	}
}
