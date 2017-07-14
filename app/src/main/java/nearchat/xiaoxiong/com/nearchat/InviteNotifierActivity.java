package nearchat.xiaoxiong.com.nearchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.MenuItem;

import com.hyphenate.chat.EMClient;

import java.util.ArrayList;
import java.util.List;

import nearchat.xiaoxiong.com.nearchat.adapter.NotifierAdapter;
import nearchat.xiaoxiong.com.nearchat.db.InviteMessageDao;
import nearchat.xiaoxiong.com.nearchat.javabean.Constant;
import nearchat.xiaoxiong.com.nearchat.javabean.InviteMessage;
import nearchat.xiaoxiong.com.nearchat.javabean.InviteMessageStatus;

public class InviteNotifierActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
    private BroadcastReceiver broadcastReceiver;
    private NotifierAdapter adapter;
    private List<InviteMessage> mList = new ArrayList<InviteMessage>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_notifier);
        initList();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new NotifierAdapter(mList);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        /**注册菜单点击事件**/
        registerForContextMenu(recyclerView);
    }

    public void initList() {
      mList = new InviteMessageDao().getInviteMessageList();
    }

    public void refresh() {
        mList.clear();
        mList.addAll(new InviteMessageDao().getInviteMessageList());
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBroadcastReceiver();
    }



    @Override
    protected void onStop() {
        super.onStop();
        broadcastManager.unregisterReceiver(broadcastReceiver);
    }

    /**注册监听器**/
    public void registerBroadcastReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.INVITE_RECEIVED);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals(Constant.INVITE_RECEIVED)) refresh();
            }
        };
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter);
    }

    /**删除操作**/
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case ContextMenu.FIRST + 1:
                deleteInviteMessage(mList.get(adapter.getPosition()));
                refresh();
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    public void deleteInviteMessage(InviteMessage inviteMessage) {
        new InviteMessageDao().deleteInviteMessage(inviteMessage);
    }
}
