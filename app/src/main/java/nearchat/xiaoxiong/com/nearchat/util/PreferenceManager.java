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
           mPreferenceManager = new PreferenceManager(context);
        }
    }

    public static synchronized PreferenceManager getInstance() {
        return mPreferenceManager;
    }

    /**存储进缓存**/
    public synchronized void preferenceManagerSave(String key, String value) {
        preferenceManagerRemove(key);
        editor.putString(key, value);
        editor.apply();
    }

    /**从缓存中获得**/
    public synchronized String preferenceManagerGet(String key) {
        return mSharedPreferences.getString(key, "");
    }

    /**从缓存中删除数据**/
    public synchronized void  preferenceManagerRemove(String key) {
        if(mSharedPreferences.contains(key)){
            editor.remove(key);
            editor.commit();
        }
    }
}
