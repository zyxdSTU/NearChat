package nearchat.xiaoxiong.com.nearchat.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nearchat.xiaoxiong.com.nearchat.NearUserInfoActivity;
import nearchat.xiaoxiong.com.nearchat.R;
import nearchat.xiaoxiong.com.nearchat.db.UserDao;
import nearchat.xiaoxiong.com.nearchat.db.WordDao;
import nearchat.xiaoxiong.com.nearchat.http.HttpManager;
import nearchat.xiaoxiong.com.nearchat.javabean.Trend;
import nearchat.xiaoxiong.com.nearchat.javabean.User;
import nearchat.xiaoxiong.com.nearchat.javabean.Word;
import nearchat.xiaoxiong.com.nearchat.util.Manager;
import nearchat.xiaoxiong.com.nearchat.util.PreferenceManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static nearchat.xiaoxiong.com.nearchat.NearTrendActivity.POP_SEND_COMMENT;
import static nearchat.xiaoxiong.com.nearchat.http.HttpManager.DOWNLOAD_IMAGE;
import static nearchat.xiaoxiong.com.nearchat.http.HttpManager.SELECT_USER;

/**
 * Created by Administrator on 2017/6/15.
 */

public class TrendAdapter extends RecyclerView.Adapter<TrendAdapter.ViewHolder> {

    private Context mContext;
    private PopupWindow popupWindow;
    private List<Trend> mList;
    private Handler handler;

    private final int MAX_LINT_COUNT = 3 ;
    private final int STATE_UNKNOWN = -1 ;
    private final int STATE_NOT_OVERFLOW = 1 ;
    private final int STATE_COLLAPSE = 2 ;
    private final int STATE_EXPAND = 3;
    private SparseArray<Integer> mTextStateList ;

    private int positionTag;

    public TrendAdapter (List<Trend> mList, Handler handler){
        mTextStateList = new SparseArray<>() ;
        this.mList = mList;
        this.handler = handler;
    }

    /*保存回复的position*/
    public void setPosition(int position) {
        this.positionTag = position;
    }

    public int getPosition() {
        return positionTag;
    }

    static public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView headView ;
        public TextView nameText ;
        public TextView content ;
        public TextView expandOrCollapse ;
        public ImageButton commentButton ;
        public RecyclerView recyclerView;
        public RecyclerView wordRecyclerView;
        public TextView addressText;

        public ViewHolder(View view){
            super(view);
            headView = (ImageView) view.findViewById(R.id.head_view);
            nameText = (TextView) view.findViewById(R.id.name_text);
            content  = (TextView) view.findViewById(R.id.content);
            expandOrCollapse = (TextView) view.findViewById(R.id.tv_expand_or_collapse);
            commentButton = (ImageButton) view.findViewById(R.id.comment_button);
            recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
            addressText = (TextView) view.findViewById(R.id.address_text);
            wordRecyclerView = (RecyclerView) view.findViewById(R.id.word_recycler_view);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trend_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder,final  int position) {
        /*获得用户信息*/
        Trend trend = mList.get(position);
        holder.content.setText(trend.getText());
        holder.addressText.setText(trend.getLocation());
        loadUserInfo(holder, trend.getPhoneNumber());

        int state = mTextStateList.get(position,STATE_UNKNOWN);
        if(state == STATE_UNKNOWN){
            holder.content.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    int lines = holder.content.getLineCount();
                    holder.content.getViewTreeObserver().removeOnPreDrawListener(this);
                    if(lines>MAX_LINT_COUNT-1){
                        holder.content.setMaxLines(MAX_LINT_COUNT);
                        holder.expandOrCollapse.setVisibility(View.VISIBLE);
                        holder.expandOrCollapse.setText("全文");
                        mTextStateList.put(position,STATE_COLLAPSE);
                    }else{
                        holder.expandOrCollapse.setVisibility(View.GONE);
                        mTextStateList.put(position,STATE_NOT_OVERFLOW);
                    }
                    return true;
                }
            });
            holder.content.setMaxLines(MAX_LINT_COUNT);
        }else {
            switch (state){
                case STATE_NOT_OVERFLOW :
                    holder.expandOrCollapse.setVisibility(View.GONE);
                    break;
                case STATE_COLLAPSE:
                    holder.content.setMaxLines(MAX_LINT_COUNT);
                    holder.expandOrCollapse.setVisibility(View.VISIBLE);
                    holder.expandOrCollapse.setText("全文");
                    break;
                case STATE_EXPAND:
                    holder.content.setMaxLines(Integer.MAX_VALUE);
                    holder.expandOrCollapse.setVisibility(View.VISIBLE);
                    holder.expandOrCollapse.setText("收起");
                    break;
            }
        }

        holder.expandOrCollapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int state =   mTextStateList.get(position,STATE_UNKNOWN) ;
                if(state == STATE_COLLAPSE){
                    holder.content.setMaxLines(Integer.MAX_VALUE);
                    holder.expandOrCollapse.setText("收起");
                    mTextStateList.put(position,STATE_EXPAND);
                }else if(state == STATE_EXPAND){
                    holder.content.setMaxLines(MAX_LINT_COUNT);
                    holder.expandOrCollapse.setText("全文");
                    mTextStateList.put(position,STATE_COLLAPSE);
                }
            }
        });

        holder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPosition(position);
                Message message = new Message();
                message.what = POP_SEND_COMMENT;
                handler.sendMessage(message);
            }
        });

        holder.nameText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, NearUserInfoActivity.class);
                intent.putExtra("NearUserInfo", (String)v.getTag());
                mContext.startActivity(intent);
            }
        });

        /*照片处理*/
        List<String> mPhotoList = new ArrayList<>();
        for(int i = 0; i < trend.getImage(); i++) {
            mPhotoList.add(trend.getTrendId() + String.valueOf(i + 1));
        }

        GridLayoutManager layoutManager = new GridLayoutManager(mContext, 3);
        holder.recyclerView.setLayoutManager(layoutManager);
        TrendPhotoAdapter adapter = new TrendPhotoAdapter(mPhotoList);
        holder.recyclerView.setAdapter(adapter);

        List<Word> mWordList = new ArrayList<Word>();
        if(new WordDao().getWordOfTrend(trend.getTrendId()) != null) {
            mWordList.addAll(new WordDao().getWordOfTrend(trend.getTrendId()));
        }
        Collections.sort(mWordList, new Comparator<Word>() {
            public int compare(Word left, Word right) {
                return new Long(left.getCurrentTime()).compareTo(new Long(right.getCurrentTime()));
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        holder.wordRecyclerView.setLayoutManager(linearLayoutManager);
        WordAdapter wordAdapter = new WordAdapter(mWordList);
        holder.wordRecyclerView.setAdapter(wordAdapter);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    /*加载个人信息*/
    public void loadUserInfo(final ViewHolder holder, String phoneNumber) {
        /*在好友列表*/
        if((new UserDao().getContact(phoneNumber)) != null) {
            User user = new UserDao().getContact(phoneNumber);
            holder.nameText.setTag(new Gson().toJson(user));
            holder.nameText.setText(user.getNickName());
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
                            holder.nameText.setText(user.getNickName());
                        }
                    });
                }
            });
        }

        /*在缓存*/
        if(!PreferenceManager.getInstance().preferenceManagerGet(phoneNumber).equals("")) {
            String imageString = PreferenceManager.getInstance().preferenceManagerGet(phoneNumber);
            byte[] imageByte = Base64.decode(imageString.getBytes(), Base64.DEFAULT);
            Glide.with(Manager.getInstance().getContent()).load(imageByte).into(holder.headView);
        } else {
            HttpManager.getInstance().sendRequest(DOWNLOAD_IMAGE + phoneNumber, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("MainActivity", "从网络加载图片失败");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final byte[] imageByte = response.body().bytes();
                    ((Activity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(imageByte.length > 0) {
                                Glide.with(Manager.getInstance().getContent()).load(imageByte).into(holder.headView);
                            } else {
                                /**如果没有更新头像，就加载默认头像**/
                                holder.headView.setImageResource(R.drawable.head);
                            }
                        }
                    });
                }
            });
        }
    }
}
