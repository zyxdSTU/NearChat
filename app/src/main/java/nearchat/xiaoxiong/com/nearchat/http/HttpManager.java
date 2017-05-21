package nearchat.xiaoxiong.com.nearchat.http;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by Administrator on 2017/5/18.
 */

public class HttpManager {
    private static HttpManager  httpManager;

    public static String SELECT_USER = "http://118.89.165.181:8080/NearChat/selectUser?phoneNumber=";
    public static String UPDATE_USER = "http://118.89.165.181:8080/NearChat/updateUser";
    public static String ADD_USER = "http://118.89.165.181:8080/NearChat/registerUser";
    public static String SELECT_PART_USER = "http://118.89.165.181:8080/NearChat/selectPartUser";

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
}
