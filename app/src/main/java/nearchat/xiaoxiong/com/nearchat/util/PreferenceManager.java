package nearchat.xiaoxiong.com.nearchat.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2017/5/18.
 */

public class PreferenceManager {
    private static SharedPreferences mSharedPreferences;
    private static PreferenceManager mPreferenceManager;
    private static SharedPreferences.Editor editor;

    private PreferenceManager(Context context) {
        mSharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        editor = mSharedPreferences.edit();
    }

    public static void init(Context context) {
        if(mPreferenceManager == null) {
            new PreferenceManager(context);
        }
    }

    public static synchronized PreferenceManager getInstance() {
        return mPreferenceManager;
    }
}
