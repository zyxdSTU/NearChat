package nearchat.xiaoxiong.com.nearchat;
import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
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

import nearchat.xiaoxiong.com.nearchat.R;
import nearchat.xiaoxiong.com.nearchat.adapter.FragmentAdapter;
import nearchat.xiaoxiong.com.nearchat.fragment.ContactListFragment;
import nearchat.xiaoxiong.com.nearchat.fragment.MessageFragment;
import nearchat.xiaoxiong.com.nearchat.javabean.Constant;
import nearchat.xiaoxiong.com.nearchat.util.Manager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private ImageButton contactListButton;
    private ImageButton messageButton;
    private ImageButton functionButton;

    private PopupWindow mPopUpWindow;
    private View functionView;

    private ImageButton powerButton;
    private ImageButton mapButton;

    private ViewPager viewPager;

    private BroadcastReceiver broadcastReceiver;
    private LocalBroadcastManager broadcastManager;

    private ContactListFragment contactListFragment;
    private MessageFragment messageFragment;
    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        functionView = getLayoutInflater().inflate(R.layout.fuction_view, null);
        mPopUpWindow = new PopupWindow(functionView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopUpWindow.setTouchable(true);
        mPopUpWindow.setOutsideTouchable(true);
        mPopUpWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));

        powerButton = (ImageButton) functionView.findViewById(R.id.power_button);
        mapButton = (ImageButton) functionView.findViewById(R.id.map_button);
        powerButton.setOnClickListener(this);
        mapButton.setOnClickListener(this);

        contactListButton = (ImageButton) findViewById(R.id.contactList_button);
        messageButton = (ImageButton) findViewById(R.id.message_button);
        functionButton = (ImageButton) findViewById(R.id.function_button);

        contactListButton.setOnClickListener(this);
        messageButton.setOnClickListener(this);
        functionButton.setOnClickListener(this);

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
                mPopUpWindow.showAtLocation(v, Gravity.BOTTOM, 320, 180);
                Toast.makeText(MainActivity.this, EMClient.getInstance().getCurrentUser(), Toast.LENGTH_LONG).show();
                break;
            case R.id.contactList_button:
                changeTab(0);
                break;
            case R.id.message_button:
                changeTab(1);
                break;
            case R.id.power_button:
                Manager.getInstance().signOut();
                finish();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                break;
            case R.id.map_button:
                gotoMapActivity();
            default:
                break;
        }
    }


    private void changeTab(int position) {
        contactListButton.setSelected(false);
        messageButton.setSelected(false);
        switch (position) {
            case 0:
                index = 0;
                contactListButton.setSelected(true);
                break;
            case 1:
                index = 1;
                messageButton.setSelected(true);
                break;
            default:
                break;
        }
        viewPager.setCurrentItem(position);
    }

    public void gotoMapActivity() {
        startActivity(new Intent(MainActivity.this, MapActivity.class));
    }

    private void registerBroadcastReceiver() {
        broadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.MESSAGE_GET);
        intentFilter.addAction(Constant.INVITE_ADD); //接受请求
        intentFilter.addAction(Constant.INVITE_ACCEPT);//请求被接受
        intentFilter.addAction(Constant.INVITE_DELETE); //好友别删除

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("MainActivity", "onReceive: ");

                String action = intent.getAction();
                Log.d("MainActivity", action);
                if (action.equals(Constant.MESSAGE_GET) && index == 1) {
                    if (messageFragment != null) {
                        messageFragment.refresh();
                    }
                    return;
                }

                if (action.equals(Constant.INVITE_ADD) || action.equals(Constant.INVITE_ACCEPT)) {
                    if(index == 0 && contactListFragment != null) {
                        contactListFragment.refresh();
                    }
                    return;
                }
            }
        };
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter);
    }


    @Override
    protected void onStop() {
        super.onStop();
        /**销毁广播监听**/
        broadcastManager.unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /**注册广播监听**/
        registerBroadcastReceiver();

        Log.d("MainActivity", "onResume: ");
        if(index == 0) messageFragment.refresh();
        if(index == 1) contactListFragment.refresh();
    }
}