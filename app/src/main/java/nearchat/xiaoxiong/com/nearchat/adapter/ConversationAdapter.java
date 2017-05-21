package nearchat.xiaoxiong.com.nearchat.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import nearchat.xiaoxiong.com.nearchat.ChatActivity;
import nearchat.xiaoxiong.com.nearchat.R;
import nearchat.xiaoxiong.com.nearchat.db.UserDao;
import nearchat.xiaoxiong.com.nearchat.javabean.User;

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
        holder.nameText.setText(user.getNickName());
        if(content != null) {
            holder.lastMessage.setText(content);
        }
        Log.d("MainActivity", String.valueOf(conversation.getUnreadMsgCount()));
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
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}