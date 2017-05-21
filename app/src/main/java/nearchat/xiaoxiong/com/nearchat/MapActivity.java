package nearchat.xiaoxiong.com.nearchat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

public class MapActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView textView;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        button = (Button) findViewById(R.id.add);
        textView = (TextView) findViewById(R.id.text_view);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id) {
            case R.id.add:
                addFriend();
                break;
        }
    }

    public void addFriend() {
        String phoneNumber = textView.getText().toString().trim();
        try {
            EMClient.getInstance().contactManager().addContact(phoneNumber, "加个好友");
        }catch(HyphenateException e) {
            e.printStackTrace();
        }
    }
}
