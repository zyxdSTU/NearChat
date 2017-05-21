package nearchat.xiaoxiong.com.nearchat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.hyphenate.chat.EMClient;

import java.util.ArrayList;
import java.util.List;

import nearchat.xiaoxiong.com.nearchat.adapter.NotifierAdapter;
import nearchat.xiaoxiong.com.nearchat.db.InviteMessageDao;
import nearchat.xiaoxiong.com.nearchat.javabean.InviteMessage;
import nearchat.xiaoxiong.com.nearchat.javabean.InviteMessageStatus;

public class InviteNotifierActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
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
    }

    public void initList() {
      mList = new InviteMessageDao().getInviteMessageList();
    }

    public void refresh() {
        mList.clear();
        mList.addAll(new InviteMessageDao().getInviteMessageList());
        adapter.notifyDataSetChanged();
    }
}
