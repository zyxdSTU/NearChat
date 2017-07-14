package nearchat.xiaoxiong.com.nearchat.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import nearchat.xiaoxiong.com.nearchat.R;
import nearchat.xiaoxiong.com.nearchat.adapter.ConversationAdapter;
import nearchat.xiaoxiong.com.nearchat.db.UserDao;
import nearchat.xiaoxiong.com.nearchat.util.Manager;

/**
 * Created by Administrator on 2017/5/19.
 */

public class MessageFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<EMConversation> mList = new ArrayList<>();
    private ConversationAdapter adapter;
    private SwipeRefreshLayout refreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_conversation_list, container, false);

        initList();
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        adapter = new ConversationAdapter(mList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        return view;
    }

    public void initList() {
        for(EMConversation conversation : Manager.getInstance().loadConversationList()) {
            /**过滤空对话**/
            if(new UserDao().getContact(conversation.conversationId()) != null) {
                mList.add(conversation);
            }
        }
    }

    public void refresh() {
        if(adapter != null) {
            mList.clear();
            for(EMConversation conversation : Manager.getInstance().loadConversationList()) {
                /**过滤空对话**/
                if(new UserDao().getContact(conversation.conversationId()) != null) {
                    mList.add(conversation);
                }
            }
            adapter.notifyDataSetChanged();
            if(refreshLayout.isRefreshing()) refreshLayout.setRefreshing(false);
        }
    }

    /**可见时调用**/
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && adapter != null) {
            //refresh();
        }
    }


}
