package nearchat.xiaoxiong.com.nearchat;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.hyphenate.chat.EMChatManager;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.chat.adapter.EMAChatClient;

import org.litepal.LitePal;

import java.util.Iterator;
import java.util.List;

import nearchat.xiaoxiong.com.nearchat.util.Manager;

import static com.hyphenate.chat.adapter.EMACallRtcImpl.TAG;

/**
 * Created by Administrator on 2017/5/15.
 */

/**
 * 初始化环信SDK
 */
public class MyApplication extends Application {
    private Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        /**
         * 防止初始化两次
         */
        int pid = android.os.Process.myPid();
        String processAppName = getAppName(pid);
        if (processAppName == null || !processAppName.equalsIgnoreCase(mContext.getPackageName())) {
            return;
        }
        LitePal.initialize(mContext);
        Manager.getInstance().init(mContext);
    }

    /**
     * 根据Pid获取当前进程的名字，一般就是当前app的包名
     */
    private String getAppName(int pid) {
        String processName = null;
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List list = activityManager.getRunningAppProcesses();
        Iterator i = list.iterator();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid == pid) {
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}