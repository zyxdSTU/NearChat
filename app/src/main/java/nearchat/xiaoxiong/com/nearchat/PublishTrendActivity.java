package nearchat.xiaoxiong.com.nearchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.gson.Gson;
import com.hyphenate.chat.EMClient;
import nearchat.xiaoxiong.com.nearchat.adapter.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import nearchat.xiaoxiong.com.nearchat.http.HttpManager;
import nearchat.xiaoxiong.com.nearchat.javabean.Trend;
import nearchat.xiaoxiong.com.nearchat.util.Manager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static nearchat.xiaoxiong.com.nearchat.http.HttpManager.ADD_TREND;
import static nearchat.xiaoxiong.com.nearchat.http.HttpManager.UPLOAD_IMAGE;

public class PublishTrendActivity extends AppCompatActivity {

    private ImageButton addPhotoButton;
    private ImageButton backButton;
    private Button publishButton;
    private TextView addressText;
    private RecyclerView recyclerView;
    private EditText contentText;
    private LocationClient locationClient;
    private ProgressDialog progressDialog;

    private PhotoAdapter adapter;
    private List<String> mList = new ArrayList<>();

    private ArrayList<String> mSelectPath = new ArrayList<>();

    private final static int REQUEST_IMAGE = 11;
    private final static int UPLOAD_PHOTO_SUCCESS = 111;
    private final static int UPLOAD_TREND_SUCCESS = 112;

    private int photoNumber = 0;
    private boolean photoAlready = false;
    private boolean trendAlready = false;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            int id = msg.what;
            switch(id) {
                case UPLOAD_PHOTO_SUCCESS:
                    photoNumber++;
                    if(photoNumber == mList.size()) {
                        photoAlready = true;
                        if(trendAlready) {
                            if(progressDialog.isShowing()) progressDialog.dismiss();
                            Toast.makeText(PublishTrendActivity.this, "动态发表成功", Toast.LENGTH_SHORT).show();
                            finish();
                            photoAlready = false;
                            trendAlready = false;
                        }
                    }
                    break;
                case UPLOAD_TREND_SUCCESS:
                    trendAlready = true;
                    if(photoAlready || mList.size() == 0) {
                        if(progressDialog.isShowing()) progressDialog.dismiss();
                        Toast.makeText(PublishTrendActivity.this, "动态发表成功", Toast.LENGTH_SHORT).show();
                        finish();
                        photoAlready = false;
                        trendAlready = false;
                    }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_trend);

        checkPermission();
        registerLocationListener();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);

        addPhotoButton = (ImageButton) findViewById(R.id.add_photo_imageButton);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        addressText = (TextView) findViewById(R.id.address_text);
        backButton = (ImageButton) findViewById(R.id.back_button);
        publishButton = (Button) findViewById(R.id.publish_button);
        contentText = (EditText) findViewById(R.id.content);

        addPhotoButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                selectPhoto();
            }
        });
        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /*发表动态, 上传照片*/
        publishButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Trend trend = new Trend();
                        trend.setCurrentTime(System.currentTimeMillis());
                        trend.setTrendId(EMClient.getInstance().getCurrentUser() + "-" + trend.getCurrentTime());
                        if(addressText.getText().toString().equals("")) return;
                        trend.setLocation(addressText.getText().toString());

                        String content = contentText.getText().toString();
                        Log.d("MainActivity", content);
                        int image = mList.size();

                        if(content.equals("") && image == 0) {
                            Toast.makeText(PublishTrendActivity.this, "动态内容不能为空", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        trend.setImage(image);
                        trend.setText(content);
                        trend.setPhoneNumber(EMClient.getInstance().getCurrentUser());

                        progressDialog.setMessage("正在发表动态");
                        progressDialog.show();

                        /*上传照片*/
                        for(int i = 1; i <= image; i++) {
                            File file = new File(mList.get(i-1));
                            String photoId = trend.getTrendId() + String.valueOf(i);
                            Log.d("MainActivity", photoId);
                            HttpManager.getInstance().uploadImage(UPLOAD_IMAGE + photoId, file, new okhttp3.Callback(){
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(PublishTrendActivity.this,"上传图片失败", Toast.LENGTH_SHORT).show();
                                            Log.d("MainActivity", "上传图片失败");
                                        }
                                    });
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(PublishTrendActivity.this,"上传图片成功", Toast.LENGTH_SHORT).show();
                                            Log.d("MainActivity", "上传图片成功");
                                            Message message = new Message();
                                            message.what = UPLOAD_PHOTO_SUCCESS;
                                            handler.sendMessage(message);
                                        }
                                    });
                                }
                            });
                        }

                        String jsonTrend = new Gson().toJson(trend);
                        HttpManager.getInstance().sendPost(jsonTrend, ADD_TREND, new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                Message message = new Message();
                                message.what = UPLOAD_TREND_SUCCESS;
                                handler.sendMessage(message);
                            }
                        });
                    }
                });
            }
        });

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PhotoAdapter(mList);
        recyclerView.setAdapter(adapter);

    }

    public void registerLocationListener() {

        locationClient = new LocationClient(Manager.getInstance().getContent());

        locationClient.registerLocationListener(new MyLocationListener());

        LocationClientOption option  = new LocationClientOption();

        //option.setCoorType("bd09ll");

        //option.setOpenGps(true);

        option.setIsNeedAddress(true);

        locationClient.setLocOption(option);

        locationClient.start();
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
                    Toast.makeText(PublishTrendActivity.this, "获取权限成功1", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PublishTrendActivity.this, "获取权限失败1", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }


    class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StringBuilder currentPosition = new StringBuilder();
                    currentPosition.append(bdLocation.getDistrict());
                    currentPosition.append(bdLocation.getStreet());
                    addressText.setText("当前地址:" + currentPosition.toString());
                }
            });
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }


    @Override
    protected void onDestroy() {
        locationClient.stop();
        super.onDestroy();
    }


    public void selectPhoto() {
        Intent intent = new Intent(this, MultiImageSelectorActivity.class);
        // whether show camera
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
        // max select image amount
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 9);
        // select mode (MultiImageSelectorActivity.MODE_SINGLE OR MultiImageSelectorActivity.MODE_MULTI)
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_MULTI);
        // default select images (support array list)
        intent.putStringArrayListExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, mSelectPath);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE){
            if(resultCode == RESULT_OK){
                // Get the result list of select image paths
                List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                mList.clear();
                mList.addAll(path);
                adapter.notifyDataSetChanged();
            }
        }
    }
}
