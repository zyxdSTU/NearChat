package nearchat.xiaoxiong.com.nearchat.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import nearchat.xiaoxiong.com.nearchat.ChatActivity;
import nearchat.xiaoxiong.com.nearchat.R;
import nearchat.xiaoxiong.com.nearchat.UserInfoActivity;
import nearchat.xiaoxiong.com.nearchat.http.HttpManager;
import nearchat.xiaoxiong.com.nearchat.javabean.User;
import nearchat.xiaoxiong.com.nearchat.util.Manager;
import nearchat.xiaoxiong.com.nearchat.util.PreferenceManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static nearchat.xiaoxiong.com.nearchat.http.HttpManager.DOWNLOAD_IMAGE;

/**
 * Created by Administrator on 2017/5/20.
 */

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private List<User> mList;
    private Context mContext;

    private int position;

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        CircleImageView headView;
        TextView nameText;
        View contactView;

        public ViewHolder(View view) {
            super(view);
            contactView = view;
            headView = (CircleImageView)view.findViewById(R.id.head_image);
            nameText = (TextView)view.findViewById(R.id.name_text);
            contactView.setOnCreateContextMenuListener(this);
        }

        /**以后优化**/
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(0, ContextMenu.FIRST+1, 0, "删除");
        }
    }

    public ContactAdapter(List<User> mList) {
        this.mList = mList;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_contact_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        User user = mList.get(position);
        holder.nameText.setText(user.getNickName());

        holder.contactView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                User user = mList.get(position);
                String jsonUser = new Gson().toJson(user);
                Intent intent = new Intent(mContext, UserInfoActivity.class);
                intent.putExtra("User", jsonUser);
                mContext.startActivity(intent);
            }
        });

        holder.contactView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                setPosition(holder.getAdapterPosition());
                return false;
            }
        });

        loadImage(holder);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public void loadImage(final ViewHolder holder) {
        int position = holder.getAdapterPosition();
        final User user = mList.get(position);

        /**如果缓存有直接从缓存中加载**/
        if (!PreferenceManager.getInstance().preferenceManagerGet(user.getPhoneNumber()).equals("")) {
            String imageString = PreferenceManager.getInstance().preferenceManagerGet(user.getPhoneNumber());
            byte[] imageByte = Base64.decode(imageString.getBytes(), Base64.DEFAULT);
            Glide.with(Manager.getInstance().getContent()).load(imageByte).into(holder.headView);
        } else {
            /**从网络加载进缓存**/
            HttpManager.getInstance().sendRequest(DOWNLOAD_IMAGE + user.getPhoneNumber(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("MainActivity", "从网络加载图片失败");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final byte[] imageByte = response.body().bytes();
                    if (mContext instanceof Activity) {
                        Activity mActivity = (Activity) mContext;
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (imageByte.length > 0) {
                                    Glide.with(Manager.getInstance().getContent()).load(imageByte).into(holder.headView);
                                    /**添加进缓存**/
                                    Manager.getInstance().updateImagePreference(user.getPhoneNumber(), imageByte);
                                } else {
                                    /**如果没有更新头像，就加载默认头像**/
                                    holder.headView.setImageResource(R.drawable.head);
                                }
                            }
                        });
                    }
                }
            });
        }
    }
}
