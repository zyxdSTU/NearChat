package nearchat.xiaoxiong.com.nearchat.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import nearchat.xiaoxiong.com.nearchat.R;
import nearchat.xiaoxiong.com.nearchat.http.HttpManager;
import nearchat.xiaoxiong.com.nearchat.javabean.Msg;
import nearchat.xiaoxiong.com.nearchat.javabean.User;
import nearchat.xiaoxiong.com.nearchat.util.Manager;
import nearchat.xiaoxiong.com.nearchat.util.PreferenceManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static nearchat.xiaoxiong.com.nearchat.http.HttpManager.DOWNLOAD_IMAGE;

/**
 * Created by Administrator on 2017/5/15.
 */

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder> {

    private List<Msg> mMsgList;
    private User chatUser;
    private Context mContext;
    private byte[] byteLeft;
    private byte[] byteRight;

    static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout leftLayout;

        LinearLayout rightLayout;

        TextView leftMsg;

        TextView rightMsg;

        CircleImageView headImageLeft;
        CircleImageView headImageRight;

        public ViewHolder(View view) {
            super(view);
            leftLayout = (LinearLayout) view.findViewById(R.id.left_layout);
            rightLayout = (LinearLayout) view.findViewById(R.id.right_layout);
            leftMsg = (TextView) view.findViewById(R.id.left_msg);
            rightMsg = (TextView) view.findViewById(R.id.right_msg);
            headImageLeft = (CircleImageView) view.findViewById(R.id.headLeft_image);
            headImageRight = (CircleImageView) view.findViewById(R.id.headRight_image);
        }
    }

    public MsgAdapter(List<Msg> msgList, User chatUser) {
        mMsgList = msgList;
        this.chatUser = chatUser;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_item, parent, false);
        this.mContext = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Msg msg = mMsgList.get(position);
        if (msg.getType() == Msg.TYPE_RECEIVED) {
            // 如果是收到的消息，则显示左边的消息布局，将右边的消息布局隐藏
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);
            holder.leftMsg.setText(msg.getContent());
        } else if(msg.getType() == Msg.TYPE_SENT) {
            // 如果是发出的消息，则显示右边的消息布局，将左边的消息布局隐藏
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.leftLayout.setVisibility(View.GONE);
            holder.rightMsg.setText(msg.getContent());
        }
        loadImageLeft(holder);
        loadImageRight(holder);
    }

    @Override
    public int getItemCount() {
        return mMsgList.size();
    }


    public void loadImageLeft(final ViewHolder holder) {
        int position = holder.getAdapterPosition();

        /**如果缓存有直接从缓存中加载**/
        if (!PreferenceManager.getInstance().preferenceManagerGet(chatUser.getPhoneNumber()).equals("")) {
            if(byteLeft != null) {
                Glide.with(Manager.getInstance().getContent()).load(byteLeft).into(holder.headImageLeft);
            }else {
                String imageString = PreferenceManager.getInstance().preferenceManagerGet(chatUser.getPhoneNumber());
                byte[] imageByte = Base64.decode(imageString.getBytes(), Base64.DEFAULT);
                Glide.with(Manager.getInstance().getContent()).load(imageByte).into(holder.headImageLeft);
                byteLeft = imageByte;
            }
        } else {
            /**从网络加载进缓存**/
            HttpManager.getInstance().sendRequest(DOWNLOAD_IMAGE + chatUser.getPhoneNumber(), new Callback() {
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
                                    Glide.with(Manager.getInstance().getContent()).load(imageByte).into(holder.headImageLeft);
                                    /**添加进缓存**/
                                    Manager.getInstance().updateImagePreference(chatUser.getPhoneNumber(), imageByte);
                                } else {
                                    /**如果没有更新头像，就加载默认头像**/
                                    holder.headImageLeft.setImageResource(R.drawable.head);
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    public void loadImageRight(final ViewHolder holder) {
       final User user = new Gson().fromJson(PreferenceManager.getInstance().preferenceManagerGet("currentUserInfo"),User.class);
        /**如果缓存有直接从缓存中加载**/
        if (!PreferenceManager.getInstance().preferenceManagerGet(user.getPhoneNumber()).equals("")) {
            if(byteRight != null) {
                Glide.with(Manager.getInstance().getContent()).load(byteRight).into(holder.headImageRight);
            }else {
                String imageString = PreferenceManager.getInstance().preferenceManagerGet(user.getPhoneNumber());
                byte[] imageByte = Base64.decode(imageString.getBytes(), Base64.DEFAULT);
                Glide.with(Manager.getInstance().getContent()).load(imageByte).into(holder.headImageRight);
                byteRight = imageByte;
            }
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
                                    Glide.with(Manager.getInstance().getContent()).load(imageByte).into(holder.headImageRight);
                                    /**添加进缓存**/
                                    Manager.getInstance().updateImagePreference(user.getPhoneNumber(), imageByte);
                                } else {
                                    /**如果没有更新头像，就加载默认头像**/
                                    holder.headImageRight.setImageResource(R.drawable.head);
                                }
                            }
                        });
                    }
                }
            });
        }
    }
}

