package nearchat.xiaoxiong.com.nearchat;

/**
 * 必须优化
 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import java.io.IOException;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import nearchat.xiaoxiong.com.nearchat.db.DBManager;
import nearchat.xiaoxiong.com.nearchat.db.UserDao;
import nearchat.xiaoxiong.com.nearchat.http.HttpManager;
import nearchat.xiaoxiong.com.nearchat.javabean.User;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static nearchat.xiaoxiong.com.nearchat.http.HttpManager.ADD_USER;

public class VerifyActivity extends AppCompatActivity implements  View.OnClickListener{
    private static final String APP_KEY = "1c96270cf5510";
    private static final String APP_SECRET = "d005f97406012da585b87734776a4727";
    private static final int SUCCESS_ONE = 6666;
    private static final int SUCCESS_TWO = 8888;
    private String jsonUser;
    private String phoneNumber;
    private String password;
    private String verifyNumber;
    private EditText verifyText;
    private TextView restText;
    private Button  nextButton;
    private Boolean verifyStatus = false;
    private ProgressDialog progressDialog;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
           if(msg.what == 2) {
               progressDialog.dismiss();
               Toast.makeText(VerifyActivity.this,"注册失败", Toast.LENGTH_SHORT).show();
               return;
           }

           if(msg.what == 0) {
               finish();
               startActivity(new Intent(VerifyActivity.this, RegisterActivity.class));
           }

           if(msg.what == SUCCESS_ONE) {
              registerUserTwo();
              return;
           }


           if(msg.what == SUCCESS_TWO) {
               progressDialog.dismiss();
               Toast.makeText(VerifyActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
               finish();
               startActivity(new Intent(VerifyActivity.this, LoginActivity.class));
               return;
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
        progressDialog = new ProgressDialog(this);
        verifyText = (EditText) findViewById(R.id.verify_text);
        restText = (TextView) findViewById(R.id.rest_text);
        nextButton = (Button) findViewById(R.id.next_button);
        nextButton.setOnClickListener(this);

        jsonUser = getIntent().getStringExtra("user");
        User user = new Gson().fromJson(jsonUser, User.class);
        phoneNumber = user.getPhoneNumber();
        password = user.getPassword();

        SMSSDK.initSDK(this, APP_KEY, APP_SECRET);
        EventHandler eventHandler = new EventHandler() {
            @Override
            //消息回调接口
            public void afterEvent(int event, int result, Object data) {
            if(result == SMSSDK.RESULT_COMPLETE) {
                //验证成功
                if(event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    verifyStatus = true;
                    registerUserOne();
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

        Toast.makeText(this, "验证码已发送", Toast.LENGTH_SHORT).show();
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
        progressDialog.setMessage("正在验证");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "can't back", Toast.LENGTH_SHORT).show();
    }


    public void backRegisterActivity() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                finish();
                startActivity(new Intent(VerifyActivity.this, RegisterActivity.class));
            }
        });
    }

    /**别人服务器注册**/
    private void registerUserOne() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().createAccount(phoneNumber, password);
                    Message msg = new Message();
                    msg.what = SUCCESS_ONE;
                    handler.sendMessage(msg);
                } catch (final HyphenateException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int errorCode = e.getErrorCode();
                            String message = e.getMessage();
                            switch (errorCode) {
                                // 网络错误
                                case EMError.NETWORK_ERROR:
                                    Toast.makeText(VerifyActivity.this, "网络错误 code: " + errorCode + ", message:" + message, Toast.LENGTH_LONG).show();
                                    break;
                                // 用户已存在
                                case EMError.USER_ALREADY_EXIST:
                                    Toast.makeText(VerifyActivity.this, "用户已存在 code: " + errorCode + ", message:" + message, Toast.LENGTH_LONG).show();
                                    break;
                                // 参数不合法，一般情况是username 使用了uuid导致，不能使用uuid注册
                                case EMError.USER_ILLEGAL_ARGUMENT:
                                    Toast.makeText(VerifyActivity.this, "参数不合法，一般情况是username 使用了uuid导致，不能使用uuid注册 code: " + errorCode + ", message:" + message, Toast.LENGTH_LONG).show();
                                    break;
                                // 服务器未知错误
                                case EMError.SERVER_UNKNOWN_ERROR:
                                    Toast.makeText(VerifyActivity.this, "服务器未知错误 code: " + errorCode + ", message:" + message, Toast.LENGTH_LONG).show();
                                    break;
                                case EMError.USER_REG_FAILED:
                                    Toast.makeText(VerifyActivity.this, "账户注册失败 code: " + errorCode + ", message:" + message, Toast.LENGTH_LONG).show();
                                    break;
                                default:
                                    Toast.makeText(VerifyActivity.this, "ml_sign_up_failed code: " + errorCode + ", message:" + message, Toast.LENGTH_LONG).show();
                                    break;
                            }
                            backRegisterActivity();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }



    /**自己服务器注册**/
    public void registerUserTwo() {
        Log.d("MainActivity", "registerUserTwo: ");
        HttpManager.getInstance().sendPost(jsonUser, ADD_USER, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(VerifyActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                        backRegisterActivity();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Message msg = new Message();
                msg.what = SUCCESS_TWO;
                handler.sendMessage(msg);
            }
        });
    }
}



