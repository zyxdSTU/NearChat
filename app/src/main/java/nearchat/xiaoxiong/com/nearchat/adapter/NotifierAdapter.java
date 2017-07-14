package nearchat.xiaoxiong.com.nearchat.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import nearchat.xiaoxiong.com.nearchat.R;
import nearchat.xiaoxiong.com.nearchat.db.InviteMessageDao;
import nearchat.xiaoxiong.com.nearchat.javabean.InviteMessage;
import nearchat.xiaoxiong.com.nearchat.javabean.InviteMessageStatus;
import nearchat.xiaoxiong.com.nearchat.javabean.User;

/**
 * Created by Administrator on 2017/5/20.
 */

public class NotifierAdapter extends RecyclerView.Adapter<NotifierAdapter.ViewHolder> {

    private List<InviteMessage> mList;

    private int position;

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements  View.OnCreateContextMenuListener{
        CircleImageView headView;
        TextView nameText;
        TextView reasonText;
        Button acceptButton;
        Button rejectButton;
        LinearLayout itemView;

        public ViewHolder(View view) {
            super(view);
            headView = (CircleImageView) view.findViewById(R.id.head_image);
            nameText = (TextView) view.findViewById(R.id.name_text);
            reasonText = (TextView) view.findViewById(R.id.reason_text);
            acceptButton = (Button) view.findViewById(R.id.accept_button);
            rejectButton = (Button) view.findViewById(R.id.reject_button);
            itemView = (LinearLayout) view.findViewById(R.id.item_view);
            itemView.setOnCreateContextMenuListener(this);
        }


        /**以后优化**/
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(0, ContextMenu.FIRST+1, 0, "删除");
        }
    }

    public NotifierAdapter(List<InviteMessage> mList) {
        this.mList = mList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_invite_notifier_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        InviteMessage inviteMessage = mList.get(position);
        holder.nameText.setText(inviteMessage.getFrom());
        holder.reasonText.setText(inviteMessage.getReason());

        if(inviteMessage.getStatus() == InviteMessageStatus.BEAGREED) {
            holder.acceptButton.setClickable(false);
            holder.acceptButton.setBackgroundColor(Color.parseColor("#FFFFFF"));
            holder.acceptButton.setTextColor(Color.parseColor("#9E9E9E"));
            holder.acceptButton.setText("已同意");
            holder.rejectButton.setVisibility(View.GONE);
        }

        if(inviteMessage.getStatus() == InviteMessageStatus.BEREFUSED) {
            holder.rejectButton.setClickable(false);
            holder.rejectButton.setBackgroundColor(Color.parseColor("#FFFFFF"));
            holder.rejectButton.setTextColor(Color.parseColor("#9E9E9E"));
            holder.rejectButton.setText("已拒绝");
            holder.acceptButton.setVisibility(View.GONE);
        }

        holder.acceptButton.setOnClickListener(new View.OnClickListener(){
            /**接受好友请求**/
            @Override
            public void onClick(View v) {
                Button button = (Button) v;
                button.setClickable(false);
                button.setBackgroundColor(Color.parseColor("#FFFFFF"));
                button.setTextColor(Color.parseColor("#9E9E9E"));
                button.setText("已同意");
                holder.rejectButton.setVisibility(View.GONE);

                InviteMessage inviteMessage = mList.get(holder.getAdapterPosition());
                try {
                    EMClient.getInstance().contactManager().acceptInvitation(inviteMessage.getFrom());
                }catch(HyphenateException e) {
                    e.printStackTrace();
                }
                inviteMessage.setStatus(InviteMessageStatus.BEAGREED);
                new InviteMessageDao().updateInviteMessage(inviteMessage);
            }
        });

        /**拒绝请求时, 不处理**/
        holder.rejectButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Button button = (Button) v;
                button.setClickable(false);
                button.setBackgroundColor(Color.parseColor("#FFFFFF"));
                button.setTextColor(Color.parseColor("#9E9E9E"));
                button.setText("已拒绝");
                holder.acceptButton.setVisibility(View.GONE);

                InviteMessage inviteMessage = mList.get(holder.getAdapterPosition());
                inviteMessage.setStatus(InviteMessageStatus.BEREFUSED);
                new InviteMessageDao().updateInviteMessage(inviteMessage);
            }
        });


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                setPosition(holder.getAdapterPosition());
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}

