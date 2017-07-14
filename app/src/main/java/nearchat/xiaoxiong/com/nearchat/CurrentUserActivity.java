package nearchat.xiaoxiong.com.nearchat;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.hyphenate.chat.EMClient;

import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import nearchat.xiaoxiong.com.nearchat.http.HttpManager;
import nearchat.xiaoxiong.com.nearchat.javabean.User;
import nearchat.xiaoxiong.com.nearchat.util.Manager;
import nearchat.xiaoxiong.com.nearchat.util.PreferenceManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static nearchat.xiaoxiong.com.nearchat.http.HttpManager.DOWNLOAD_IMAGE;
import static nearchat.xiaoxiong.com.nearchat.http.HttpManager.UPLOAD_IMAGE;

public class CurrentUserActivity extends AppCompatActivity implements  View.OnClickListener{
    private TextView nameText;
    private ImageView sexImage;
    private TextView ageText;
    private TextView personalityText;
    private TextView phoneNumberText;
    private TextView schoolText;
    private Button editButton;
    private Button uploadButton;
    private User currentUser;
    private String jsonCurrentUserInfo;

    private CircleImageView headImageSmall;
    private ImageView headImageBig;
    private static final int CHOOSE_PHOTO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_current_user);

        headImageSmall = (CircleImageView) findViewById(R.id.head_image);
        headImageBig =  (ImageView) findViewById(R.id.image_view);
        nameText = (TextView)findViewById(R.id.name_text);
        sexImage = (ImageView)findViewById(R.id.sex_image);
        ageText = (TextView) findViewById(R.id.age_text);
        personalityText = (TextView) findViewById(R.id.personality_text);
        phoneNumberText = (TextView) findViewById(R.id.phoneNumber_text);
        schoolText = (TextView) findViewById(R.id.school_text);
        editButton = (Button) findViewById(R.id.edit_button);
        uploadButton = (Button) findViewById(R.id.upload_button);
        editButton.setOnClickListener(this);
        uploadButton.setOnClickListener(this);

        jsonCurrentUserInfo = PreferenceManager.getInstance().preferenceManagerGet("currentUserInfo");
        if(jsonCurrentUserInfo == null) return;
        currentUser = new Gson().fromJson(jsonCurrentUserInfo, User.class);

        nameText.setText(currentUser.getNickName());
        if(currentUser.getSex().equals("男")){
            sexImage.setImageResource(R.drawable.boy);
        }else{
            sexImage.setImageResource(R.drawable.girl);
        }
        ageText.setText(String.valueOf(currentUser.getAge()));
        personalityText.setText(currentUser.getPersonality());
        phoneNumberText.setText(currentUser.getPhoneNumber());
        schoolText.setText(currentUser.getSchool());

        /*加载头像*/
        loadImage();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id) {
            case R.id.edit_button:
                gotoEditActivity();
                break;
            case R.id.upload_button:
                uploadImage();
                break;
        }
    }

    public void uploadImage() {
        /**判断权限**/
        if(ContextCompat.checkSelfPermission(CurrentUserActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CurrentUserActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            openAlbum();
        }
    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }
    public void gotoEditActivity() {
        finish();
        Intent intent = new Intent(CurrentUserActivity.this, EditActivity.class);
        intent.putExtra("currentUserInfo", jsonCurrentUserInfo);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else{
                    Toast.makeText(this, "获取权限失败", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHOOSE_PHOTO:
                if(resultCode == RESULT_OK) {
                    if(Build.VERSION.SDK_INT >= 19) {
                        /**4.4以上系统**/
                        handleImageOnKitKat(data);
                    } else {
                        /**4.4以下系统**/
                        handImageBeforeKitKat(data);
                    }
                }
        }
    }

    private void handleImageOnKitKat(Intent data) {
        Log.d("MainActivity", "handleImageOnKitKat");
        String imagePath = null;
        Uri uri = data.getData();
        if(DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);
        }else if("file".equalsIgnoreCase(uri.getScheme())) {
            imagePath = uri.getPath();
        }

        /**展示照片**/
        displayImage(imagePath);

        /**上传照片**/
        uploadToServer(imagePath);
    }

    private void handImageBeforeKitKat(Intent data) {
        Log.d("MainActivity", "handleImageBeforeKitKat");
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
        uploadToServer(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Log.d("MainActivity", uri.toString());
        /**通过Uri和selection来获取真实的图片路径**/
        Cursor cursor = getContentResolver().query(uri, null, selection, null ,null);
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
        }
        cursor.close();
        return path;
    }

    private void displayImage(String imagePath) {
        if(imagePath != null) {
            Log.d("MainActivity", imagePath);
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            headImageBig.setImageBitmap(bitmap);
            headImageSmall.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "加载图片失败", Toast.LENGTH_SHORT).show();
        }
    }


    public void uploadToServer(final String imagePath) {
        File file = new File(imagePath);
        HttpManager.getInstance().uploadImage(UPLOAD_IMAGE + currentUser.getPhoneNumber(), file, new okhttp3.Callback(){
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CurrentUserActivity.this,"上传图片失败", Toast.LENGTH_SHORT).show();
                        Log.d("MainActivity", "上传图片失败");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CurrentUserActivity.this,"上传图片成功", Toast.LENGTH_SHORT).show();
                        Log.d("MainActivity", "上传图片成功");
                    }
                });
                /**上传成功，更新缓存**/
                Manager.getInstance().updateImagePreference(EMClient.getInstance().getCurrentUser(), imagePath);
            }
        });
    }


    public void loadImage() {
        final String phoneNumber = EMClient.getInstance().getCurrentUser();

        /**如果缓存有直接从缓存中加载**/
        if(!PreferenceManager.getInstance().preferenceManagerGet(phoneNumber).equals("")) {
            String imageString = PreferenceManager.getInstance().preferenceManagerGet(phoneNumber);
            byte[] imageByte = Base64.decode(imageString.getBytes(), Base64.DEFAULT);
            Glide.with(Manager.getInstance().getContent()).load(imageByte).into(headImageBig);
            Glide.with(Manager.getInstance().getContent()).load(imageByte).into(headImageSmall);
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
                                Glide.with(Manager.getInstance().getContent()).load(imageByte).into(headImageBig);
                                Glide.with(Manager.getInstance().getContent()).load(imageByte).into(headImageSmall);

                                /**添加进缓存**/
                                Manager.getInstance().updateImagePreference(phoneNumber, imageByte);
                            } else {
                                /**如果没有更新头像，就加载默认头像**/
                                headImageBig.setImageResource(R.drawable.head);
                                headImageSmall.setImageResource(R.drawable.head);
                            }
                        }
                    });
                }
            });
        }
    }
}
