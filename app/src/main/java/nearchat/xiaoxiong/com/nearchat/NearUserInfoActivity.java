package nearchat.xiaoxiong.com.nearchat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import nearchat.xiaoxiong.com.nearchat.db.UserDao;
import nearchat.xiaoxiong.com.nearchat.http.HttpManager;
import nearchat.xiaoxiong.com.nearchat.javabean.User;
import nearchat.xiaoxiong.com.nearchat.util.Manager;
import nearchat.xiaoxiong.com.nearchat.util.PreferenceManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static nearchat.xiaoxiong.com.nearchat.http.HttpManager.DOWNLOAD_IMAGE;

public class NearUserInfoActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView nameText;
    private ImageView sexImage;
    private TextView ageText;
    private TextView personalityText;
    private TextView phoneNumberText;
    private TextView schoolText;
    private Button adddButton;
    private User nearUser;
    private String jsonNearUserInfo;

    private ImageView headImageBig;
    private ImageView headImageSmall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_near_user_info);

        headImageBig = (ImageView) findViewById(R.id.image_view);
        headImageSmall = (CircleImageView) findViewById(R.id.head_image);

        nameText = (TextView)findViewById(R.id.name_text);
        sexImage = (ImageView)findViewById(R.id.sex_image);
        ageText = (TextView) findViewById(R.id.age_text);
        personalityText = (TextView) findViewById(R.id.personality_text);
        phoneNumberText = (TextView) findViewById(R.id.phoneNumber_text);
        schoolText = (TextView) findViewById(R.id.school_text);
        adddButton = (Button) findViewById(R.id.add_button);

        jsonNearUserInfo = getIntent().getStringExtra("NearUserInfo");
        if(jsonNearUserInfo == null) return;
        nearUser = new Gson().fromJson(jsonNearUserInfo, User.class);

        nameText.setText(nearUser.getNickName());
        if(nearUser.getSex().equals("男")){
            sexImage.setImageResource(R.drawable.boy);
        }else{
            sexImage.setImageResource(R.drawable.girl);
        }
        ageText.setText(String.valueOf(nearUser.getAge()));
        personalityText.setText(nearUser.getPersonality());
        phoneNumberText.setText(nearUser.getPhoneNumber());
        schoolText.setText(nearUser.getSchool());

        adddButton.setOnClickListener(this);

        loadImage();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id) {
            case R.id.add_button:
                sendAddMessage();
                break;
            default:
                break;
        }
    }

    public void sendAddMessage() {
        for(User user : new UserDao().getContactList()) {
            if(user.getPhoneNumber().equals(nearUser.getPhoneNumber())){
                Toast.makeText(this, "你们已经是好友了", Toast.LENGTH_SHORT).show();
                return;
            }
        }
       Intent intent  = new Intent(NearUserInfoActivity.this, AddFriendLaterActivity.class);
       intent.putExtra("NearUserInfo", jsonNearUserInfo);
       startActivity(intent);
    }



    public void loadImage() {
        final String phoneNumber = nearUser.getPhoneNumber();

        /**如果缓存有直接从缓存中加载**/
        if(!PreferenceManager.getInstance().preferenceManagerGet(phoneNumber).equals("")) {
            String imageString = PreferenceManager.getInstance().preferenceManagerGet(phoneNumber);
            byte[] imageByte = Base64.decode(imageString.getBytes(), Base64.DEFAULT);
            Glide.with(Manager.getInstance().getContent()).load(imageByte).into(headImageBig);
            Glide.with(Manager.getInstance().getContent()).load(imageByte).into(headImageSmall);
        } else {
            /**从网络加载进缓存**/
            HttpManager.getInstance().sendRequest(DOWNLOAD_IMAGE + nearUser.getPhoneNumber(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("MainActivity", "从网络加载图片失败");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final byte[] imageByte = response.body().bytes();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(imageByte.length > 0) {
                                Glide.with(Manager.getInstance().getContent()).load(imageByte).into(headImageBig);
                                Glide.with(Manager.getInstance().getContent()).load(imageByte).into(headImageSmall);
                            } else {
                                /**如果没有更新头像，就加载默认头像**/
                                headImageBig.setImageResource(R.drawable.head);
                                headImageSmall.setImageResource(R.drawable.head);
                            }
                        }
                    });
                }
            });
        }
    }
}
