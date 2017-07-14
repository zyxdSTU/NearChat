package nearchat.xiaoxiong.com.nearchat.http;

import java.io.File;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by Administrator on 2017/5/18.
 */

public class HttpManager {
    private static HttpManager  httpManager;

    public static String SELECT_USER = "http://119.29.61.99:8080/NearChat/selectUser?phoneNumber=";
    public static String UPDATE_USER = "http://119.29.61.99:8080/NearChat/updateUser";
    public static String ADD_USER = "http://119.29.61.99:8080/NearChat/registerUser";
    public static String SELECT_PART_USER = "http://119.29.61.99:8080/NearChat/selectPartUser";

    public static String DOWNLOAD_IMAGE = "http://119.29.61.99:8080/NearChat/downloadImage?id=";
    public static String UPLOAD_IMAGE = "http://119.29.61.99:8080/NearChat/uploadImage?id=";

    public static String ADD_TREND = "http://119.29.61.99:8080/NearChat/insertTrend";
    public static String ADD_WORD = "http://119.29.61.99:8080/NearChat/insertWord";
    public static String GET_TREND_WORD_INFO = "http://119.29.61.99:8080/NearChat/selectTrendOverTime";

    public static String ADD_LOCATION = "http://119.29.61.99:8080/NearChat/addLocation";
    public static String SELECT_ALL_USER_INFO = "http://119.29.61.99:8080/NearChat/selectAllUserInfo";

    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

    public static HttpManager getInstance() {
        if(httpManager == null) {
            httpManager = new HttpManager();
        }
        return httpManager;
    }


    public void sendRequest(String address, okhttp3.Callback callback) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(address).build();
            client.newCall(request).enqueue(callback);
    }

    public void sendPost(String json, String address, okhttp3.Callback callback) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(address)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }


    public void uploadImage(String url, File file, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody=  new MultipartBody.Builder()
                                        .setType(MultipartBody.FORM)
                                        .addFormDataPart("image", file.getName(), RequestBody.create(MEDIA_TYPE_PNG, file))
                                        .build();

        Request request = new Request.Builder().url(url).post(requestBody).build();
        client.newCall(request).enqueue(callback);
    }
}
