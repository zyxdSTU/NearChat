package nearchat.xiaoxiong.com.nearchat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import nearchat.xiaoxiong.com.nearchat.javabean.User;
import nearchat.xiaoxiong.com.nearchat.util.Manager;
import nearchat.xiaoxiong.com.nearchat.util.PreferenceManager;

public class EditActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText nameText;
    private EditText ageText;
    private EditText schoolText;
    private EditText personalityText;
    private EditText sexText;
    private Button saveButton;
    private String jsonCurrentUserInfo;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if(Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_edit);

        nameText = (EditText) findViewById(R.id.name_text);
        ageText = (EditText) findViewById(R.id.age_text);
        schoolText = (EditText) findViewById(R.id.school_text);
        personalityText = (EditText) findViewById(R.id.personality_text);
        sexText = (EditText) findViewById(R.id.sex_text);
        saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(this);

        jsonCurrentUserInfo = getIntent().getStringExtra("currentUserInfo");
        if(jsonCurrentUserInfo == null) return;
        currentUser = new Gson().fromJson(jsonCurrentUserInfo, User.class);

        nameText.setText(currentUser.getNickName());
        ageText.setText(String.valueOf(currentUser.getAge()));
        schoolText.setText(currentUser.getSchool());
        personalityText.setText(currentUser.getPersonality());
        sexText.setText(currentUser.getSex());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.save_button:
                /**保存**/
               gotoCurrentUserActivity();
                break;
            default:
                break;
        }
    }

    /**
     * 输入是否正确
     */
    private int allThingRight() {
        //用户名为空
        String tempName = nameText.getText().toString().trim();
        if (tempName.equals("")) return 1;

        //学校为空
        String tempSchool = schoolText.getText().toString().trim();
        if (tempSchool.equals("")) return 2;

        //年龄格式错误
        String tempAge = ageText.getText().toString().trim();
        try {
            int age = Integer.parseInt(tempAge);
            if(age <=0 || age >= 100) return 6;
        }catch(NumberFormatException e) {
            return 3;
        }

        //性别格式错误
        String tempSex = sexText.getText().toString().trim();
        if(!tempSex.equals("男") && !tempSex.equals("女")) return 4;

        return 0;
    }

    /**
     * 验证并跳转(phone number 顺便发送)
     */
    public void gotoCurrentUserActivity() {
        if (allThingRight() == 0) {
            String school = schoolText.getText().toString().trim();
            int age = Integer.parseInt(ageText.getText().toString().trim());
            String sex = sexText.getText().toString().trim();
            String personality = personalityText.getText().toString().trim();
            String nickName = nameText.getText().toString().trim();

            User user = new User();
            user.setNickName(nickName); user.setPhoneNumber(currentUser.getPhoneNumber());
            user.setPassword(currentUser.getPassword()); user.setSchool(school);
            user.setAge(age); user.setSex(sex);
            user.setPersonality(personality);

            /**更新缓存**/
            PreferenceManager.getInstance().preferenceManagerRemove("currentUserInfo");
            PreferenceManager.getInstance().preferenceManagerSave("currentUserInfo", new Gson().toJson(user));

            /**更新服务器个人信息**/
            Manager.getInstance().updateCurrentUserInfoServer(user);

            Intent intent = new Intent(EditActivity.this, CurrentUserActivity.class);
            finish();
            startActivity(intent);
        } else {
            switch (allThingRight()) {
                case 1:
                    Toast.makeText(EditActivity.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(EditActivity.this, "学校不能为空", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(EditActivity.this, "年龄格式错误", Toast.LENGTH_SHORT).show();
                case 4:
                    Toast.makeText(EditActivity.this, "性别格式错误", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }
}
