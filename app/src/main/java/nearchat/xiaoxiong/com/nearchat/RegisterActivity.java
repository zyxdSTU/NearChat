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

import com.google.gson.Gson;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import nearchat.xiaoxiong.com.nearchat.javabean.User;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText passwordText;
    private EditText passwordConfirmText;
    private EditText nameText;
    private EditText schoolText;
    private EditText ageText;
    private EditText sexText;
    private EditText personalityText;
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
        ageText = (EditText) findViewById(R.id.age_text);
        sexText = (EditText) findViewById(R.id.sex_text);
        personalityText = (EditText) findViewById(R.id.personality_text);
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
        if (tempPassword.length() < 6 || tempPassword.length() > 12) return 3;

        //密码确认错误
        if (!tempPassword.equals(tempPasswordConfirm)) return 4;

        //学校为空
        String tempSchool = schoolText.getText().toString().trim();
        if (tempSchool.equals("")) return 5;

        //年龄格式错误
        String tempAge = ageText.getText().toString().trim();
        try {
            int age = Integer.parseInt(tempAge);
            if(age <=0 || age >= 100) return 6;
        }catch(NumberFormatException e) {
            return 6;
        }

        //性别格式错误
        String tempSex = sexText.getText().toString().trim();
        if(!tempSex.equals("男") && !tempSex.equals("女")) return 7;

        return 0;
    }

    /**
     * 验证并跳转(phone number 顺便发送)
     */
    public void gotoVerifyActivity() {
        if (allThingRight() == 0) {
            String phoneNumber = phoneNumberText.getText().toString().trim();
            String password = passwordText.getText().toString().trim();
            String school = schoolText.getText().toString().trim();
            int age = Integer.parseInt(ageText.getText().toString().trim());
            String sex = sexText.getText().toString().trim();
            String personality = personalityText.getText().toString().trim();
            String nickName = nameText.getText().toString().trim();
            User user = new User();
            user.setNickName(nickName); user.setPhoneNumber(phoneNumber);
            user.setPassword(password); user.setSchool(school);
            user.setAge(age); user.setSex(sex);
            user.setPersonality(personality);

            Intent intent = new Intent(RegisterActivity.this, VerifyActivity.class);
            intent.putExtra("user", new Gson().toJson(user));
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
                    Toast.makeText(RegisterActivity.this, "年龄格式错误", Toast.LENGTH_SHORT).show();
                case 7:
                    Toast.makeText(RegisterActivity.this, "性别格式错误", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }

}
