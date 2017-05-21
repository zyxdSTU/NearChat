package nearchat.xiaoxiong.com.nearchat.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import nearchat.xiaoxiong.com.nearchat.ChatActivity;
import nearchat.xiaoxiong.com.nearchat.R;
import nearchat.xiaoxiong.com.nearchat.javabean.User;

/**
 * Created by Administrator on 2017/5/20.
 */

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private List<User> mList;
    private Context mContext;

    static class ViewHolder extends RecyclerView.ViewHolder{
        CircleImageView headView;
        TextView nameText;
        TextView personalityText;
        View contactView;

        public ViewHolder(View view) {
            super(view);
            contactView = view;
            headView = (CircleImageView)view.findViewById(R.id.head_image);
            nameText = (TextView)view.findViewById(R.id.name_text);
            personalityText = (TextView) view.findViewById(R.id.personality_text);
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
        holder.personalityText.setText(user.getPersonality());

        holder.contactView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                User user = mList.get(position);
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
