package nearchat.xiaoxiong.com.nearchat.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import nearchat.xiaoxiong.com.nearchat.util.Manager;

/**
 * Created by Administrator on 2017/5/19.
 */

public class MessageFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<EMConversation> mList = new ArrayList<>();
    private ConversationAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_conversation_list, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        adapter = new ConversationAdapter(mList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
        return view;
    }

    public void initList() {
        mList.addAll(Manager.getInstance().loadConversationList());
    }

    public void refresh() {
        if(adapter != null) {
            mList.clear();
            mList.addAll(Manager.getInstance().loadConversationList());
            adapter.notifyDataSetChanged();
        }
    }

    /**可见时调用**/
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && adapter != null) {
            refresh();
        }
    }
}
