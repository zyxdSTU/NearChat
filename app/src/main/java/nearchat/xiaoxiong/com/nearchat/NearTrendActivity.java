package nearchat.xiaoxiong.com.nearchat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.hyphenate.chat.EMClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nearchat.xiaoxiong.com.nearchat.adapter.TrendAdapter;
import nearchat.xiaoxiong.com.nearchat.db.TrendDao;
import nearchat.xiaoxiong.com.nearchat.db.UserDao;
import nearchat.xiaoxiong.com.nearchat.http.HttpManager;
import nearchat.xiaoxiong.com.nearchat.javabean.Trend;
import nearchat.xiaoxiong.com.nearchat.javabean.User;
import nearchat.xiaoxiong.com.nearchat.javabean.Word;
import nearchat.xiaoxiong.com.nearchat.util.Manager;
import nearchat.xiaoxiong.com.nearchat.util.PreferenceManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static nearchat.xiaoxiong.com.nearchat.http.HttpManager.ADD_WORD;
import static nearchat.xiaoxiong.com.nearchat.http.HttpManager.DOWNLOAD_IMAGE;
import static nearchat.xiaoxiong.com.nearchat.http.HttpManager.GET_TREND_WORD_INFO;

public class NearTrendActivity extends AppCompatActivity {

    public final static int TREND_WORD_INFO = 99;
    public final static int POP_SEND_COMMENT = 999;
    private String jsonTrendWord;

    private ImageView headView;
    private TextView nameText;
    private SwipeRefreshLayout swipeRefresh;

    private RecyclerView recyclerView;

    private User currentUser;
    private TrendAdapter adapter;

    private PopupWindow mPopUpWindow;
    private View sendCommentView;
    private EditText inputText;
    private Button sendButton;

    private List<Trend> mList = new LinkedList<>();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int id = msg.what;
            switch (id) {
                case TREND_WORD_INFO:
                    /*存进数据库, 刷新界面*/
                    refresh_new();
                    break;
                case POP_SEND_COMMENT:
                    popSendComment();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_trend);
        //addTrendButton = (Button) findViewById(R.id.add_trend_button);
        headView = (ImageView) findViewById(R.id.head_view);
        nameText = (TextView) findViewById(R.id.name_text);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        /*读取当前用户信息*/
        currentUser = new Gson().fromJson(PreferenceManager.getInstance().preferenceManagerGet("currentUserInfo"), User.class);
        /*加载个人头像*/
        loadImage();
        nameText.setText(currentUser.getNickName());

        initList();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new TrendAdapter(mList, handler);
        recyclerView.setAdapter(adapter);

        /*评论框*/
        sendCommentView = getLayoutInflater().inflate(R.layout.layout_send_comment, null);
        inputText = (EditText) sendCommentView.findViewById(R.id.input_text);
        sendButton = (Button) sendCommentView.findViewById(R.id.send);


        mPopUpWindow = new PopupWindow(sendCommentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopUpWindow.setTouchable(true);
        mPopUpWindow.setOutsideTouchable(true);
        mPopUpWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));

        inputText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mPopUpWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    //pop.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(inputText.getText().toString().equals("")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NearTrendActivity.this, "回复不能为空", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    if(mPopUpWindow.isShowing()) mPopUpWindow.dismiss();
                    Word word = new Word();
                    word.setPhoneNumber(EMClient.getInstance().getCurrentUser());
                    word.setText(inputText.getText().toString());
                    word.setCurrentTime(System.currentTimeMillis());
                    word.setTrendId(mList.get(adapter.getPosition()).getTrendId());
                    String jsonWord = new Gson().toJson(word);
                    /*发送网络请求*/
                    HttpManager.getInstance().sendPost(jsonWord, ADD_WORD, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            getTrendWordInfo();
                        }
                     });
                }
            }
        });

        headView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(NearTrendActivity.this, PublishTrendActivity.class);
                startActivity(intent);
                return false;
            }
        });

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(NearTrendActivity.this, "正在刷新", Toast.LENGTH_SHORT).show();
                        getTrendWordInfo();
                    }
                });
            }
        });
    }


    public void getTrendWordInfo() {
        String trendOverTime = String.valueOf(Manager.getInstance().getRecentTrendTime());
        String wordOverTime = String.valueOf(Manager.getInstance().getRecentWordTime());
        String url = GET_TREND_WORD_INFO +"?trendOverTime=" + trendOverTime +"&wordOverTime=" +wordOverTime;
        Log.d("MainActivity", url);
        HttpManager.getInstance().sendRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("MainActivity", "动态信息加载失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                jsonTrendWord = response.body().string();
                Log.d("MainActivity", jsonTrendWord);
                Message message = new Message();
                message.obj = jsonTrendWord; message.what = TREND_WORD_INFO;
                handler.sendMessage(message);
            }
        });
    }

    /*本地刷新*/
    public void refresh_ordinary() {

    }

    /*存储进数据库，刷新ui,网络刷新*/
    public void refresh_new() {
        /*wordList*/
        String jsonTrendList = null;
        String jsonWordList = null;

        try {
            JSONObject jsonObject = new JSONObject(jsonTrendWord);
            jsonTrendList = jsonObject.getString("trendList");
            jsonWordList = jsonObject.getString("wrodList");

            Log.d("MainActivity", jsonTrendList);
            Log.d("MainActivity", jsonWordList);
        }catch(JSONException e) {
            e.printStackTrace();
        }

        if(jsonTrendList.equals("[]") && jsonWordList.equals("[]")) {
            if(swipeRefresh.isRefreshing()) swipeRefresh.setRefreshing(false);
            return;
        }

        JsonParser parser = new JsonParser();
        Gson gson = new Gson();

        JsonArray jsonTrendArray = parser.parse(jsonTrendList).getAsJsonArray();
        List<Trend> listTrendTemp = new ArrayList<>();
        for (JsonElement element : jsonTrendArray) {
            Trend trend = gson.fromJson(element, Trend.class);
            trend.save();
            listTrendTemp.add(trend);
        }

        /*降序排列*/
        Collections.sort(listTrendTemp, new Comparator<Trend>() {
            public int compare(Trend left, Trend right) {
                return new Long(left.getCurrentTime()).compareTo(new Long(right.getCurrentTime()));
            }
        });
        for(Trend trend : listTrendTemp) {
            ((LinkedList<Trend>) mList).addFirst(trend);
        }


        JsonArray jsonWordArray = parser.parse(jsonWordList).getAsJsonArray();
        List<Word> listWordTemp = new ArrayList<>();
        for (JsonElement element : jsonWordArray) {
            Word word = gson.fromJson(element, Word.class);
            word.save();
        }

        adapter.notifyDataSetChanged();
        if(swipeRefresh.isRefreshing()) swipeRefresh.setRefreshing(false);
    }


    public void loadImage() {
        /**如果缓存有直接从缓存中加载**/
        if(!PreferenceManager.getInstance().preferenceManagerGet(currentUser.getPhoneNumber()).equals("")) {
            String imageString = PreferenceManager.getInstance().preferenceManagerGet(currentUser.getPhoneNumber());
            byte[] imageByte = Base64.decode(imageString.getBytes(), Base64.DEFAULT);
            Glide.with(Manager.getInstance().getContent()).load(imageByte).into(headView);
        } else {
            /**从网络加载进缓存**/
            HttpManager.getInstance().sendRequest(DOWNLOAD_IMAGE + currentUser.getPhoneNumber(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("MainActivity", "从网络加载图片失败");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final byte[] imageByte = response.body().bytes();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(imageByte.length > 0) {
                                Glide.with(Manager.getInstance().getContent()).load(imageByte).into(headView);
                                /**添加进缓存**/
                                Manager.getInstance().updateImagePreference(currentUser.getPhoneNumber(), imageByte);
                            } else {
                                /**如果没有更新头像，就加载默认头像**/
                                headView.setImageResource(R.drawable.head);
                            }
                        }
                    });
                }
            });
        }
    }

    public void initList() {
        /*从数据库获取*/
        List<Trend> listTemp = new TrendDao().getAllTrend();
        if(listTemp == null) return;
        else {
            mList.addAll(listTemp);
            /*降序排列*/
            Collections.sort(mList, new Comparator<Trend>() {
                public int compare(Trend left, Trend right) {
                    return new Long(right.getCurrentTime()).compareTo(new Long(left.getCurrentTime()));
                }
            });
        }
    }

    public void popSendComment() {
        mPopUpWindow.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
        inputText.requestFocus();
    }


    @Override
    protected void onResume() {
        super.onResume();
        getTrendWordInfo();
    }
}
