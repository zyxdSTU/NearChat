package nearchat.xiaoxiong.com.nearchat;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText phoneNumText;
    private EditText passwordText;
    private Button loginButton;
    private Button registerButton;

    private String phoneNum;
    private String password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_login);
        phoneNumText = (EditText) findViewById(R.id.phone_text);
        passwordText = (EditText) findViewById(R.id.password_text);
        loginButton = (Button) findViewById(R.id.login_button);
        registerButton = (Button) findViewById(R.id.register_button);

        //转到chat Activity
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            /**
             * 跳转到首页
             */
            public void onClick(View view) {
                phoneNum = phoneNumText.getText().toString();
                password = passwordText.getText().toString();
                Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
                startActivity(intent);

            }
        });


        //转到login Activity
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            /**
             * 跳转到注册页面
             */
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}
