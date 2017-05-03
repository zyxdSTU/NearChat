package nearchat.xiaoxiong.com.nearchat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class VerifyActivity extends AppCompatActivity implements  View.OnClickListener{
    private static final String APP_KEY = "1c96270cf5510";
    private static final String APP_SECRET = "d005f97406012da585b87734776a4727";
    private String phoneNumber;
    private String verifyNumber;
    private EditText verifyText;
    private TextView restText;
    private Button  nextButton;
    private Boolean verifyStatus = false;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
           if(msg.what == 1) {
               Toast.makeText(VerifyActivity.this,"注册失败, 请重新注册", Toast.LENGTH_SHORT).show();
               return;
           }

           if(msg.what == 0) {
               finish();
               startActivity(new Intent(VerifyActivity.this, RegisterActivity.class));
           }
           restText.setText(msg.what + "秒");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_verify);

        verifyText = (EditText) findViewById(R.id.verify_text);
        restText = (TextView) findViewById(R.id.rest_text);
        nextButton = (Button) findViewById(R.id.next_button);

        nextButton.setOnClickListener(this);
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        SMSSDK.initSDK(this, APP_KEY, APP_SECRET);

        EventHandler eventHandler = new EventHandler() {
            @Override
            //消息回调接口
            public void afterEvent(int event, int result, Object data) {
                if(result == SMSSDK.RESULT_COMPLETE) {
                    //验证成功
                    if(event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    Toast.makeText(VerifyActivity.this, "验证成功", Toast.LENGTH_SHORT).show();
                                    Thread.sleep(1000);
                                }catch(InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        verifyStatus = true;
                        /**
                         * 跳转登入界面
                         */
                        finish();
                        startActivity(new Intent(VerifyActivity.this, LoginActivity.class));
                    }
                    //获得验证码
                    else if(event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(VerifyActivity.this, "获取验证码成功", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else if(event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
                        //返回支持发送验证码的国家列表
                    }
                }
            }
        };
        SMSSDK.registerEventHandler(eventHandler);

        SMSSDK.getVerificationCode("86", phoneNumber);//获取验证码

        /**
         *  开启计数线程
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int i = 60;
                    while(i > 0 && !verifyStatus) {
                        Thread.sleep(1000);
                        Message msg = new Message();
                        msg.what = --i;
                        handler.sendMessage(msg);
                    }
                }catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    protected void onDestroy() {
        // 销毁回调监听接口
        SMSSDK.unregisterAllEventHandler();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id) {
            case R.id.next_button:
                verifyAction();
                break;
            default:
                break;
        }
    }


    public void verifyAction() {
        verifyNumber = verifyText.getText().toString().trim();
        if(verifyNumber.equals("")) {
            Toast.makeText(VerifyActivity.this,"验证码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        SMSSDK.submitVerificationCode("86", phoneNumber, verifyNumber);
    }
}
