package nearchat.xiaoxiong.com.nearchat;

import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import java.util.List;

import nearchat.xiaoxiong.com.nearchat.util.Manager;

public class StartActivity extends AppCompatActivity {

    /**延迟3秒**/
    private static final int sleepTime = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

                    Long endTime = System.currentTimeMillis();

                    if(endTime - startTime < sleepTime) {
                        try {
                            Thread.sleep(sleepTime + startTime - endTime);
                        }catch(InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
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
