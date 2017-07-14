package nearchat.xiaoxiong.com.nearchat;
import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;

import java.util.ArrayList;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import nearchat.xiaoxiong.com.nearchat.R;
import nearchat.xiaoxiong.com.nearchat.adapter.FragmentAdapter;
import nearchat.xiaoxiong.com.nearchat.fragment.ContactListFragment;
import nearchat.xiaoxiong.com.nearchat.fragment.MessageFragment;
import nearchat.xiaoxiong.com.nearchat.javabean.Constant;
import nearchat.xiaoxiong.com.nearchat.util.Manager;
import nearchat.xiaoxiong.com.nearchat.util.PreferenceManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private ImageButton contactListButton;
    private ImageButton messageButton;
    private ImageButton functionButton;

    private TextView contactListText;
    private TextView messageText;
    private TextView functionText;

    private PopupWindow mPopUpWindow;
    private View functionView;

    private ImageButton powerButton;
    private ImageButton nearButton;
    private ImageButton settingButton;
    private ImageButton heartButton;
    private ImageButton friendButton;

    private ViewPager viewPager;

    private BroadcastReceiver broadcastReceiver;
    private LocalBroadcastManager broadcastManager;

    private ContactListFragment contactListFragment;
    private MessageFragment messageFragment;
    private int index = 0;

    private int backNumber = 0;
    private long startBackTime = 0;
    private long endBackTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        functionView = getLayoutInflater().inflate(R.layout.fuction_view, null);
        mPopUpWindow = new PopupWindow(functionView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        mPopUpWindow.setTouchable(true);
        mPopUpWindow.setOutsideTouchable(true);
        mPopUpWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));

        friendButton = (ImageButton) functionView.findViewById(R.id.friend_button);
        nearButton = (ImageButton) functionView.findViewById(R.id.near_button);
        heartButton = (ImageButton) functionView.findViewById(R.id.heart_button);
        settingButton = (ImageButton) functionView.findViewById(R.id.setting_button);
        powerButton = (ImageButton) functionView.findViewById(R.id.power_button);

        friendButton.setOnClickListener(this);
        nearButton.setOnClickListener(this);
        heartButton.setOnClickListener(this);
        settingButton.setOnClickListener(this);
        powerButton.setOnClickListener(this);


        contactListButton = (ImageButton) findViewById(R.id.contactList_button);
        messageButton = (ImageButton) findViewById(R.id.message_button);
        functionButton = (ImageButton) findViewById(R.id.function_button);
        contactListText = (TextView) findViewById(R.id.contactList_text);
        messageText = (TextView) findViewById(R.id.message_text);
        functionText = (TextView) findViewById(R.id.function_text);

        contactListButton.setOnClickListener(this);
        messageButton.setOnClickListener(this);
        functionButton.setOnClickListener(this);

        /**开始其别选中**/
        contactListButton.setSelected(true);
        contactListText.setTextColor(Color.parseColor("#4CAF50"));

        contactListFragment = new ContactListFragment();
        messageFragment = new MessageFragment();
        List<Fragment> mList = new ArrayList<Fragment>();
        mList.add(contactListFragment);
        mList.add(messageFragment);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        FragmentAdapter fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), mList);
        viewPager.setAdapter(fragmentAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                changeTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.function_button:
                mPopUpWindow.showAtLocation(v, Gravity.NO_GRAVITY, 0, 0);
                break;
            case R.id.contactList_button:
                changeTab(0);
                break;
            case R.id.message_button:
                changeTab(1);
                break;
            case R.id.friend_button:
                gotoFriendActivity();
                break;
            case R.id.near_button:
                gotoNearActivity();
                break;
            case R.id.heart_button:
                gotoCurrentUserActivity();
                break;
            case R.id.setting_button:
                break;
            case R.id.power_button:
                Manager.getInstance().signOut();
                finish();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                break;
            default:
                break;
        }
    }

    /*地图*/
    public void gotoNearActivity() {
        Intent intent = new Intent(MainActivity.this, MapActivity.class);
        startActivity(intent);
    }

    private void changeTab(int position) {
        contactListButton.setSelected(false);
        contactListText.setTextColor(Color.parseColor("#8a8a8a"));
        messageText.setTextColor(Color.parseColor("#8a8a8a"));
        messageButton.setSelected(false);
        switch (position) {
            case 0:
                index = 0;
                contactListButton.setSelected(true);
                contactListText.setTextColor(Color.parseColor("#4CAF50"));
                break;
            case 1:
                index = 1;
                messageButton.setSelected(true);
                messageText.setTextColor(Color.parseColor("#4CAF50"));
                break;
            default:
                break;
        }
        viewPager.setCurrentItem(position);
    }

    /*朋友圈*/
    public void gotoFriendActivity() {
        startActivity(new Intent(MainActivity.this, NearTrendActivity.class));
    }

    public void gotoCurrentUserActivity(){
        startActivity(new Intent(MainActivity.this, CurrentUserActivity.class));
    }

    private void registerBroadcastReceiver() {
        broadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.MESSAGE_GET);
        intentFilter.addAction(Constant.INVITE_ACCEPT);//请求被接受
        intentFilter.addAction(Constant.INVITE_DELETE); //好友删除自己

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(Constant.MESSAGE_GET) && index == 1) {
                    if (messageFragment != null) {
                        messageFragment.refresh();
                    }
                    return;
                }

                if (action.equals(Constant.INVITE_ACCEPT)) {
                    if(index == 0 && contactListFragment != null) {
                        contactListFragment.refresh();
                    }
                    return;
                }

                if(action.equals(Constant.INVITE_DELETE)) {
                    if(index == 0 && contactListFragment != null) {
                        contactListFragment.refresh();
                    }

                    if(index == 1 && messageFragment != null) {
                        messageFragment.refresh();
                    }
                    return;
                }
            }
        };
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter);
    }


    @Override
    protected void onStop() {
        /**销毁广播监听**/
        broadcastManager.unregisterReceiver(broadcastReceiver);
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();
        /**注册广播监听**/
        registerBroadcastReceiver();

        if(index == 0) contactListFragment.refresh();
        if(index == 1) messageFragment.refresh();
    }


    /**连续按两次back键退出程序**/
    @Override
    public void onBackPressed() {
        if(backNumber == 0) {
            backNumber++;
            startBackTime = System.currentTimeMillis();
            Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            return;
        }

        if(backNumber == 1) {
            endBackTime = System.currentTimeMillis();
            if(endBackTime - startBackTime < 2000) {
                super.onBackPressed();
            } else {
                backNumber = 0;
                startBackTime = 0;
                endBackTime = 0;
            }
            return ;
        }
    }
}