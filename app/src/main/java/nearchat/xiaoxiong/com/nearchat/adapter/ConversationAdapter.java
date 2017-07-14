package nearchat.xiaoxiong.com.nearchat.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;

import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import nearchat.xiaoxiong.com.nearchat.ChatActivity;
import nearchat.xiaoxiong.com.nearchat.R;
import nearchat.xiaoxiong.com.nearchat.db.UserDao;
import nearchat.xiaoxiong.com.nearchat.http.HttpManager;
import nearchat.xiaoxiong.com.nearchat.javabean.User;
import nearchat.xiaoxiong.com.nearchat.util.Manager;
import nearchat.xiaoxiong.com.nearchat.util.PreferenceManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static nearchat.xiaoxiong.com.nearchat.http.HttpManager.DOWNLOAD_IMAGE;

/**
 * Created by Administrator on 2017/5/21.
 */

public class ConversationAdapter extends  RecyclerView.Adapter <ConversationAdapter.ViewHolder>{

    private List<EMConversation> mList;
    private Context mContext;

    static class ViewHolder extends RecyclerView.ViewHolder{
        CircleImageView headImage;
        TextView nameText;
        TextView lastMessage;
        View conversationItemView;
        TextView unreadText;

        public ViewHolder(View view) {
            super(view);
            headImage = (CircleImageView) view.findViewById(R.id.head_image);
            nameText = (TextView) view.findViewById(R.id.name_text);
            lastMessage = (TextView) view.findViewById(R.id.last_message_text);
            unreadText = (TextView) view.findViewById(R.id.unread_text);
            conversationItemView = view;
        }
    }

    public ConversationAdapter(List<EMConversation> mList) {
        this.mList = mList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_conversation_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        /**头像没有设置**/
        EMConversation conversation = mList.get(holder.getAdapterPosition());
        String phoneNumber = conversation.conversationId();
        EMMessage lastMessage = conversation.getLastMessage();
        String content = null;
        if(lastMessage != null) {
            EMTextMessageBody body = (EMTextMessageBody) lastMessage.getBody();
            content = body.getMessage();
        }

        User user = new UserDao().getContact(phoneNumber);
        if(user == null) {
            return;
        }
        holder.nameText.setText(user.getNickName());
        if(content != null) {
            holder.lastMessage.setText(content);
        }


        if(conversation.getUnreadMsgCount() != 0) {
            holder.unreadText.setText(String.valueOf(conversation.getUnreadMsgCount()));
            holder.unreadText.setVisibility(View.VISIBLE);
        }else{
            holder.unreadText.setVisibility(View.INVISIBLE);
        }

        holder.conversationItemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                EMConversation conversation = mList.get(position);
                String phoneNumber = conversation.conversationId();
                User user = new UserDao().getContact(phoneNumber);
                String jsonUser = new Gson().toJson(user);

                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("chatUser", jsonUser);
                mContext.startActivity(intent);
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
        final User user = new UserDao().getContact(mList.get(position).conversationId());

        /**如果缓存有直接从缓存中加载**/
        if (!PreferenceManager.getInstance().preferenceManagerGet(user.getPhoneNumber()).equals("")) {
            String imageString = PreferenceManager.getInstance().preferenceManagerGet(user.getPhoneNumber());
            byte[] imageByte = Base64.decode(imageString.getBytes(), Base64.DEFAULT);
            Glide.with(Manager.getInstance().getContent()).load(imageByte).into(holder.headImage);
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
                                    Glide.with(Manager.getInstance().getContent()).load(imageByte).into(holder.headImage);
                                    /**添加进缓存**/
                                    Manager.getInstance().updateImagePreference(user.getPhoneNumber(), imageByte);
                                } else {
                                    /**如果没有更新头像，就加载默认头像**/
                                    holder.headImage.setImageResource(R.drawable.head);
                                }
                            }
                        });
                    }
                }
            });
        }
    }
}