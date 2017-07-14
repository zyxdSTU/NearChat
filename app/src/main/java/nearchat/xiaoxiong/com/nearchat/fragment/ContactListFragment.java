package nearchat.xiaoxiong.com.nearchat.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

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
    private SwipeRefreshLayout refreshLayout;
    private List<User> mList = new ArrayList<User>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_contact_list, container, false);

        initList();

        inviteNotifier = (LinearLayout) view.findViewById(R.id.invite_notifier);
        inviteNotifier.setOnClickListener(this);

        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new ContactAdapter(mList);
        recyclerView.setAdapter(adapter);

        /**注册菜单点击事件**/
        registerForContextMenu(recyclerView);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
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
        Log.d("MainActivty", String.valueOf(mList.size()));
    }



    /**可见时调用**/
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && adapter != null) {
            //refresh();
        }
    }

    public void refresh() {
        if(adapter != null) {
            mList.clear();
            mList.addAll(new UserDao().getContactList());
            Log.d("MainActivty", String.valueOf(mList.size()));
            adapter.notifyDataSetChanged();
            if(refreshLayout.isRefreshing()) refreshLayout.setRefreshing(false);
        }
    }

    /**删除操作**/
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case ContextMenu.FIRST + 1:
                deleteContact();
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    public void deleteContact(){
        final int position = adapter.getPosition();
        final User user = mList.get(position);
        final ProgressDialog pr = new ProgressDialog(getActivity());
        pr.setMessage("正在删除");
        pr.setCanceledOnTouchOutside(false);
        pr.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().contactManager().deleteContact(user.getPhoneNumber());
                    pr.dismiss();

                }catch(HyphenateException e) {
                    pr.dismiss();
                    Toast.makeText(getActivity(), "删除失败", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
