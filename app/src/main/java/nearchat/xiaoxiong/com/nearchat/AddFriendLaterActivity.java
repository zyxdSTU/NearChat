package nearchat.xiaoxiong.com.nearchat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import nearchat.xiaoxiong.com.nearchat.javabean.User;

public class AddFriendLaterActivity extends AppCompatActivity {

    private EditText reasonText;
    private Button addButton;
    private LinearLayout linearLayoutOne;
    private LinearLayout linearLayoutTwo;
    private User nearUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend_later);

        reasonText = (EditText)findViewById(R.id.reason_text);
        addButton = (Button) findViewById(R.id.add_button);
        linearLayoutOne = (LinearLayout) findViewById(R.id.layout_one);
        linearLayoutTwo = (LinearLayout) findViewById(R.id.layout_two);

        String jsonNearUserInfo = getIntent().getStringExtra("NearUserInfo");
        if(jsonNearUserInfo == null) return;
        nearUser = new Gson().fromJson(jsonNearUserInfo, User.class);

        addButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                /**发送好友邀请**/
                String reason = reasonText.getText().toString().trim();
                try {
                    EMClient.getInstance().contactManager().addContact(nearUser.getPhoneNumber(), reason);
                }catch(HyphenateException e) {
                    Toast.makeText(AddFriendLaterActivity.this, "添加好友失败", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                linearLayoutOne.setVisibility(View.GONE);
                linearLayoutTwo.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(AddFriendLaterActivity.this, MainActivity.class));
    }
}
