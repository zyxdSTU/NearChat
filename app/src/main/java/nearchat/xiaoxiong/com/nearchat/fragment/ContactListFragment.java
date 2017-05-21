package nearchat.xiaoxiong.com.nearchat.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import nearchat.xiaoxiong.com.nearchat.InviteNotifierActivity;
import nearchat.xiaoxiong.com.nearchat.R;
import nearchat.xiaoxiong.com.nearchat.adapter.ContactAdapter;
import nearchat.xiaoxiong.com.nearchat.db.UserDao;
import nearchat.xiaoxiong.com.nearchat.javabean.User;

/**
 * Created by Administrator on 2017/5/19.
 */

public class ContactListFragment extends Fragment implements View.OnClickListener{
    private LinearLayout inviteNotifier;
    private RecyclerView recyclerView;
    private ContactAdapter adapter;
    private List<User> mList = new ArrayList<User>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_contact_list, container, false);

        initList();

        inviteNotifier = (LinearLayout) view.findViewById(R.id.invite_notifier);
        inviteNotifier.setOnClickListener(this);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new ContactAdapter(mList);
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.invite_notifier:
                gotoInviteNotifierActivity();
                break;
            default:
                break;
        }
    }

    public void gotoInviteNotifierActivity() {
        startActivity(new Intent(getActivity(), InviteNotifierActivity.class));
    }

    public void initList() {
        mList = new UserDao().getContactList();
    }



    /**可见时调用**/
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && adapter != null) {
            refresh();
        }
    }

    public void refresh() {
        if(adapter != null) {
            mList.clear();
            mList.addAll(new UserDao().getContactList());
            adapter.notifyDataSetChanged();
        }
    }
}
