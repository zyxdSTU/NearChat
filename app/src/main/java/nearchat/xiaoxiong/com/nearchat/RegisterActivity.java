package nearchat.xiaoxiong.com.nearchat;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String APP_KEY = "1c96270cf5510";
    private static final String APP_SECRET = "d005f97406012da585b87734776a4727";
    private String phoneNumber;
    private String verifyNumber;
    private String password;
    private Toolbar toolBar;
    boolean acquireStatus = false;
    boolean verifyStatus = false;

    private EditText passwordText;
    private EditText passwordConfirmText;
    private EditText phoneNumberText;
    private EditText msgVerifyNumText;
    private Button acquireMsgButton;
    private Button registerButton;
    private Drawable errorDrawable;
    private Button verifyButton;
    private ImageButton backButton;

    private final int GET_MSG = 5555;
    private final int VER_MSG = 6666;
    private final int GET_ERROR = 7777;
    private final int VER_ERROR = 8888;


    private Handler handle = new Handler() {
        @Override
        public void handleMessage(Message message) {
            int status = message.arg1;
            int num = message.arg2;

            if(status == GET_MSG && !acquireStatus) {
                acquireMsgButton.setText("等" + (59 - num) + "秒获取");
            }

            if(status == VER_MSG && !verifyStatus) {
                verifyButton.setText((59 - num) + "秒后验证");
            }

            if(status == GET_ERROR && !acquireStatus) {
                acquireMsgButton.setEnabled(true);
                acquireMsgButton.setText("获取验证码");
            }

            if(status == VER_ERROR && !verifyStatus) {
                verifyButton.setEnabled(true);
                verifyButton.setText("验证");
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        verifyButton = (Button) findViewById(R.id.verify_button);
        passwordText = (EditText) findViewById(R.id.password_text);
        passwordConfirmText = (EditText) findViewById(R.id.password_confirm_text);
        phoneNumberText = (EditText) findViewById(R.id.phone_text);
        msgVerifyNumText = (EditText) findViewById(R.id.msg_verify_num_text);
        acquireMsgButton = (Button) findViewById(R.id.acquire_msg_button);
        registerButton = (Button) findViewById(R.id.register_button);
        toolBar = (Toolbar) findViewById(R.id.toolbar);
        backButton = (ImageButton) findViewById(R.id.back_button);
        errorDrawable = (Drawable) getResources().getDrawable(R.drawable.error);
        errorDrawable.setBounds(0, 0, 50, 50);

        registerButton.setOnClickListener(this);
        acquireMsgButton.setOnClickListener(this);
        verifyButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        //初始化SMSS接口
        initSMSSDK();
    }

    public void initSMSSDK() {
        SMSSDK.initSDK(this, APP_KEY, APP_SECRET);
        EventHandler eventHandler = new EventHandler() {
            @Override
            //消息回调接口
            public void afterEvent(int event, int result, Object data) {
                if(result == SMSSDK.RESULT_COMPLETE) {
                    if(event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RegisterActivity.this, "验证成功", Toast.LENGTH_SHORT).show();
                                verifyStatus = true;
                                verifyButton.setEnabled(true);
                                verifyButton.setText("验证");
                            }
                        });
                    }
                    else if(event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RegisterActivity.this, "获取验证码成功", Toast.LENGTH_SHORT).show();
                                acquireStatus = true;
                                acquireMsgButton.setEnabled(true);
                                acquireMsgButton.setText("获取验证码");
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
    }

    protected void onDestroy() {
        // 销毁回调监听接口
        SMSSDK.unregisterAllEventHandler();
        super.onDestroy();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.acquire_msg_button:
                phoneNumber = phoneNumberText.getText().toString();
                if (!TextUtils.isEmpty(phoneNumber)) {
                    SMSSDK.getVerificationCode("86", phoneNumber);//获取短信
                    verifyStatus = false;
                    acquireStatus = false;
                    acquireMsgButton.setEnabled(false);
                    acquireMsgButton.setText("等60秒获取");
                    /**
                     * 开一个新的线程
                     */
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for(int i = 0; i < 60; i++) {
                                try {
                                    Message msg = new Message();
                                    Thread.sleep(1000);
                                    if(!acquireStatus) {
                                        msg.arg1 = GET_MSG;
                                        msg.arg2 = i;
                                        handle.sendMessage(msg);
                                    } else break;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            Message msg = new Message();
                            msg.arg1 = GET_ERROR;
                            handle.sendMessage(msg);
                        }
                    }).start();

                }else {
                    phoneNumberText.setError("电话号码不能为空", errorDrawable);
                }
                break;

            case R.id.verify_button:
                verifyNumber = msgVerifyNumText.getText().toString();
                if(!TextUtils.isEmpty(verifyNumber)) {
                    SMSSDK.submitVerificationCode("86", phoneNumber, verifyNumber);
                    verifyButton.setEnabled(false);
                    verifyButton.setText("60秒后验证");
                    /**
                     * 开一个新的线程
                     */
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for(int i = 0; i < 60; i++) {
                                try {
                                    Message msg = new Message();
                                    Thread.sleep(1000);
                                    if(!verifyStatus) {
                                        msg.arg1 = VER_MSG;
                                        msg.arg2 = i;
                                        handle.sendMessage(msg);
                                    } else break;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            Message msg = new Message();
                            msg.arg1 = VER_ERROR;
                            handle.sendMessage(msg);
                        }
                    }).start();

                } else {
                    msgVerifyNumText.setError("验证码不能为空", errorDrawable);
                }
                break;

            case R.id.register_button:
                int caseId = allThingRight();
                //注册成功
                if(caseId == 0) {
                    verifyStatus = false;
                    password = passwordText.getText().toString();
                    backLoginActivity();
                }
                switch(caseId) {
                    case 1:
                        Toast.makeText(RegisterActivity.this, "请先获得验证码", Toast.LENGTH_SHORT);
                        break;
                    case 2:
                        phoneNumberText.setError("手机号与原先不匹配", errorDrawable);
                        break;
                    case 3:
                        passwordText.setError("密码长度不符合要求", errorDrawable);
                        break;
                    case 4:
                        passwordConfirmText.setError("两次密码输入不同",errorDrawable);
                        break;
                    case 5:
                        msgVerifyNumText.setError("验证码未验证或者有误", errorDrawable);
                        break;
                    default:
                        break;
                }
                break;
            case R.id.back_button:
                backLoginActivity();
                break;
            default:
                break;
        }
    }

    private int allThingRight() {
        //请先获得验证码
        if(phoneNumber == null) return 1;

        //手机号和原来注册不匹配
        String tempPhoneNum = phoneNumberText.getText().toString();
        if(!tempPhoneNum.equals(phoneNumber)) return 2;

        //密码长度不符合要求
        String tempPassword = passwordText.getText().toString();
        String tempPasswordConfirm = passwordConfirmText.getText().toString();
        if(tempPassword.length() < 6 | tempPassword.length() > 12) return 3;

        //密码确认错误
        if(!tempPassword.equals(tempPasswordConfirm))   return 4;

        //验证未通过
        if(verifyStatus == false) return 5;
        return 0;
    }

    private void backLoginActivity() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}