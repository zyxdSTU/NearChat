package nearchat.xiaoxiong.com.nearchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import nearchat.xiaoxiong.com.nearchat.adapter.MapAdapter;
import nearchat.xiaoxiong.com.nearchat.db.UserDao;
import nearchat.xiaoxiong.com.nearchat.http.HttpManager;
import nearchat.xiaoxiong.com.nearchat.javabean.Constant;
import nearchat.xiaoxiong.com.nearchat.javabean.NearUser;
import nearchat.xiaoxiong.com.nearchat.javabean.Trend;
import nearchat.xiaoxiong.com.nearchat.javabean.User;
import nearchat.xiaoxiong.com.nearchat.javabean.UserLocation;
import nearchat.xiaoxiong.com.nearchat.util.Manager;
import nearchat.xiaoxiong.com.nearchat.util.PreferenceManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static nearchat.xiaoxiong.com.nearchat.http.HttpManager.ADD_LOCATION;
import static nearchat.xiaoxiong.com.nearchat.http.HttpManager.SELECT_ALL_USER_INFO;

public class MapActivity extends AppCompatActivity implements View.OnClickListener{
    private RecyclerView recyclerView;
    private MapAdapter adapter;
    private LocationClient locationClient;
    private List<NearUser> mList = new ArrayList<>();
    private SwipeRefreshLayout refreshLayout;

    private static final int GET_SUCCESS = 11;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int id = msg.what;
            switch(id) {
                case GET_SUCCESS:
                    adapter.notifyDataSetChanged();
                    if(refreshLayout.isRefreshing()) refreshLayout.setRefreshing(false);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        checkPermission();
        registerLocationListener();

        refreshLayout = (SwipeRefreshLayout)findViewById(R.id.refresh_layout);
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        adapter = new MapAdapter(mList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(locationClient.isStarted()) {
                    Log.d("MainActivity", "xxxxxxxxxx");
                    locationClient.requestLocation();
                }
            }
        });

    }


    @Override
    public void onClick(View v) {

    }

    class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            Log.d("MainActivity", "xxxx");
            mList.clear();
           UserLocation userLocation = new UserLocation();
           userLocation.setLatitude(bdLocation.getLatitude());
           userLocation.setLongitude(bdLocation.getLongitude());
           userLocation.setPhoneNumber(EMClient.getInstance().getCurrentUser());
           userLocation.setCurrentTime(System.currentTimeMillis());
           String jsonCurrentUserInfo = new Gson().toJson(userLocation);
           PreferenceManager.getInstance().preferenceManagerSave("currentUserLocation", jsonCurrentUserInfo);

            /*更新位置信息*/
            HttpManager.getInstance().sendPost(jsonCurrentUserInfo, ADD_LOCATION, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                }
            });

            /*获取数据信息*/
            HttpManager.getInstance().sendRequest(SELECT_ALL_USER_INFO, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MapActivity.this, "请求数据失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String jsonUserInfo = response.body().string();

                  /*wordList*/
                    String jsonLocationList = null;
                    String jsonUserList = null;

                    try {
                        JSONObject jsonObject = new JSONObject(jsonUserInfo);
                        jsonLocationList = jsonObject.getString("locationList");
                        jsonUserList = jsonObject.getString("userList");
                    }catch(JSONException e) {
                        e.printStackTrace();
                    }

                    JsonParser parser = new JsonParser();
                    Gson gson = new Gson();

                    JsonArray jsonLocationArray = parser.parse(jsonLocationList).getAsJsonArray();
                    JsonArray jsonUserArray = parser.parse(jsonUserList).getAsJsonArray();

                    for(int i = 0; i < jsonLocationArray.size(); i++) {
                        UserLocation userLocation = gson.fromJson(jsonLocationArray.get(i), UserLocation.class);
                        User user = gson.fromJson(jsonUserArray.get(i), User.class);
                        if(!user.getPhoneNumber() .equals(EMClient.getInstance().getCurrentUser())) {
                            mList.add(new NearUser(user, userLocation));
                        }
                    }

                    Message msg = new Message();
                    msg.what = GET_SUCCESS;
                    handler.sendMessage(msg);
                }
            });
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }

    public void registerLocationListener() {

        locationClient = new LocationClient(Manager.getInstance().getContent());

        locationClient.registerLocationListener(new MapActivity.MyLocationListener());

        LocationClientOption option  = new LocationClientOption();

        //option.setCoorType("bd09ll");

       // option.setOpenGps(true);

        option.setIsNeedAddress(true);

        locationClient.setLocOption(option);

        locationClient.start();
    }

    @Override
    protected void onDestroy() {
        locationClient.stop();
        super.onDestroy();
    }


    public void checkPermission() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    Toast.makeText(MapActivity.this, "获取权限成功1", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MapActivity.this, "获取权限失败1", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}
