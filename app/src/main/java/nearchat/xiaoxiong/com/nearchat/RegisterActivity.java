package nearchat.xiaoxiong.com.nearchat;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
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

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private String phoneNumber;
    private String password;

    private EditText passwordText;
    private EditText passwordConfirmText;
    private EditText nameText;
    private EditText schoolText;
    private EditText emailText;
    private EditText phoneNumberText;
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_register);

        passwordText = (EditText) findViewById(R.id.password_text);
        passwordConfirmText = (EditText) findViewById(R.id.password_confirm_text);
        phoneNumberText = (EditText) findViewById(R.id.phone_text);
        nameText = (EditText) findViewById(R.id.name_text);
        schoolText = (EditText) findViewById(R.id.school_text);
        emailText = (EditText) findViewById(R.id.email_text);
        nextButton = (Button) findViewById(R.id.next_button);

        nextButton.setOnClickListener(this);
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.next_button:
                gotoVerifyActivity();
                break;
            default:
                break;
        }
    }

    /**
     * 输入是否正确
     */
    private int allThingRight() {
        //手机号码错误
        String tempPhoneNumber = phoneNumberText.getText().toString().trim();
        if (tempPhoneNumber.equals("") || tempPhoneNumber.length() != 11) return 1;

        //用户名为空
        String tempName = nameText.getText().toString().trim();
        if (tempName.equals("")) return 2;

        //密码长度不符合要求
        String tempPassword = passwordText.getText().toString().trim();
        String tempPasswordConfirm = passwordConfirmText.getText().toString().trim();
        if (tempPassword.length() < 6 | tempPassword.length() > 12) return 3;

        //密码确认错误
        if (!tempPassword.equals(tempPasswordConfirm)) return 4;

        //学校为空
        String tempSchool = schoolText.getText().toString().trim();
        if (tempSchool.equals("")) return 5;

        //邮箱为空
        String tempEnvelope = emailText.getText().toString().trim();
        if (tempEnvelope.equals("")) return 6;

        return 0;
    }

    /**
     * 验证并跳转(phone number 顺便发送)
     */
    public void gotoVerifyActivity() {
        if (allThingRight() == 0) {
            phoneNumber = phoneNumberText.getText().toString().trim();
            password = passwordText.getText().toString().trim();
            Intent intent = new Intent(RegisterActivity.this, VerifyActivity.class);
            intent.putExtra("phoneNumber", phoneNumber);
            finish();
            startActivity(intent);
        } else {
            switch (allThingRight()) {
                case 1:
                    Toast.makeText(RegisterActivity.this, "手机号码格式错误", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(RegisterActivity.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(RegisterActivity.this, "密码格式错误", Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    Toast.makeText(RegisterActivity.this, "两次输入密码不同", Toast.LENGTH_SHORT).show();
                    break;
                case 5:
                    Toast.makeText(RegisterActivity.this, "学校不能为空", Toast.LENGTH_SHORT).show();
                    break;
                case 6:
                    Toast.makeText(RegisterActivity.this, "邮箱不能为空", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }
}
