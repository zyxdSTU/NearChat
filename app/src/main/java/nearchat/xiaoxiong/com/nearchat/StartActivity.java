package nearchat.xiaoxiong.com.nearchat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import java.util.List;

import nearchat.xiaoxiong.com.nearchat.http.HttpManager;
import nearchat.xiaoxiong.com.nearchat.service.AutoUpdateService;
import nearchat.xiaoxiong.com.nearchat.util.Manager;

import static nearchat.xiaoxiong.com.nearchat.javabean.Constant.LOGIN_SUCCESS;

public class StartActivity extends AppCompatActivity {

    /**延迟3秒**/
    private static final int sleepTime = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_start);
    }

    @Override
    protected void onStart() {
        super.onStart();

        new Thread(new Runnable() {
            @Override
            public void run() {

                /**是否已经登入**/
                if(Manager.getInstance().isLoggedIn()) {

                    Long startTime = System.currentTimeMillis();

                    EMClient.getInstance().chatManager().loadAllConversations();

                    /**本地广播**/
                    LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(StartActivity.this);
                    broadcastManager.sendBroadcast(new Intent(LOGIN_SUCCESS));

                    Long endTime = System.currentTimeMillis();

                    if(endTime - startTime < sleepTime) {
                        try {
                            Thread.sleep(sleepTime + startTime - endTime);
                        }catch(InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    finish();
                    startActivity(new Intent(StartActivity.this, MainActivity.class));
                } else {
                    try {
                        Thread.sleep(sleepTime);
                    }catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                    finish();
                    startActivity(new Intent(StartActivity.this, LoginActivity.class));
                }
            }
        }).start();
    }
}