package nearchat.xiaoxiong.com.nearchat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;

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

public class UserInfoActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView headImageBig;
    private CircleImageView headImageSmall;
    private TextView nameText;
    private TextView ageText;
    private ImageView sexImage;
    private TextView personalityText;
    private TextView phoneNumberText;
    private TextView schoolText;
    private Button  sendButton;
    private User user;
    private String jsonUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_user_info);

        jsonUser = getIntent().getStringExtra("User");
        if(jsonUser == null) return;
        user = new Gson().fromJson(jsonUser, User.class);

        headImageBig = (ImageView) findViewById(R.id.image_view);
        headImageSmall = (CircleImageView) findViewById(R.id.head_image);
        nameText = (TextView)findViewById(R.id.name_text);
        ageText = (TextView) findViewById(R.id.age_text);
        sexImage = (ImageView) findViewById(R.id.sex_image);
        personalityText = (TextView) findViewById(R.id.personality_text);
        phoneNumberText = (TextView) findViewById(R.id.phoneNumber_text);
        schoolText = (TextView) findViewById(R.id.school_text);
        sendButton = (Button) findViewById(R.id.send_button);

        nameText.setText(user.getNickName());
        ageText.setText(String.valueOf(user.getAge()));
        if(user.getSex().equals("男")){
            sexImage.setImageResource(R.drawable.boy);
        } else{
            sexImage.setImageResource(R.drawable.girl);
        }
        personalityText.setText(user.getPersonality());
        phoneNumberText.setText(user.getPhoneNumber());
        schoolText.setText(user.getSchool());

        sendButton.setOnClickListener(this);
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(user.getNickName());

        loadImage();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.send_button:
                finish();
                Intent intent = new Intent(UserInfoActivity.this, ChatActivity.class);
                intent.putExtra("chatUser", jsonUser);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    public void loadImage() {
        /**如果缓存有直接从缓存中加载**/
        if(!PreferenceManager.getInstance().preferenceManagerGet(user.getPhoneNumber()).equals("")) {
            String imageString = PreferenceManager.getInstance().preferenceManagerGet(user.getPhoneNumber());
            byte[] imageByte = Base64.decode(imageString.getBytes(), Base64.DEFAULT);
            Glide.with(Manager.getInstance().getContent()).load(imageByte).into(headImageBig);
            Glide.with(Manager.getInstance().getContent()).load(imageByte).into(headImageSmall);
        } else {
            /**从网络加载进缓存**/
            HttpManager.getInstance().sendRequest(DOWNLOAD_IMAGE + user.getPhoneNumber(), new Callback() {
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

                                /**添加进缓存**/
                                Manager.getInstance().updateImagePreference(user.getPhoneNumber(), imageByte);
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
