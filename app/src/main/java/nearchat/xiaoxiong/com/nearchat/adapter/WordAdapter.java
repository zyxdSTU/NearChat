package nearchat.xiaoxiong.com.nearchat.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import nearchat.xiaoxiong.com.nearchat.NearUserInfoActivity;
import nearchat.xiaoxiong.com.nearchat.R;
import nearchat.xiaoxiong.com.nearchat.db.UserDao;
import nearchat.xiaoxiong.com.nearchat.http.HttpManager;
import nearchat.xiaoxiong.com.nearchat.javabean.Trend;
import nearchat.xiaoxiong.com.nearchat.javabean.User;
import nearchat.xiaoxiong.com.nearchat.javabean.Word;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static nearchat.xiaoxiong.com.nearchat.http.HttpManager.SELECT_USER;

/**
 * Created by Administrator on 2017/6/17.
 */

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.ViewHolder> {
    private List<Word> mList;
    private Context mContext;

    public WordAdapter(List<Word> mList) {
        this.mList = mList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView wordText;

        public ViewHolder(View view) {
            super(view);
            nameText = (TextView) view.findViewById(R.id.name_text);
            wordText = (TextView) view.findViewById(R.id.word_text);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.word_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Word word = mList.get(position);
        loadUserInfo(holder, word.getPhoneNumber());
        holder.wordText.setText(word.getText());
        holder.nameText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, NearUserInfoActivity.class);
                intent.putExtra("NearUserInfo", (String)v.getTag());
                mContext.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }


    /*加载个人信息*/
    public void loadUserInfo(final WordAdapter.ViewHolder holder, String phoneNumber) {
        /*在好友列表*/
        User user = new UserDao().getContact(phoneNumber);
        if (user != null) {
            holder.nameText.setTag(new Gson().toJson(user));
            holder.nameText.setText(user.getNickName() + ":");
        } else {
            HttpManager.getInstance().sendRequest(SELECT_USER + phoneNumber, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("MainActivity", "获得用户信息失败");
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    final String jsonUser = response.body().string();
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            holder.nameText.setTag(jsonUser);
                            User user = new Gson().fromJson(jsonUser, User.class);
                            holder.nameText.setText(user.getNickName() + ":");
                        }
                    });
                }
            });
        }
    }
}
