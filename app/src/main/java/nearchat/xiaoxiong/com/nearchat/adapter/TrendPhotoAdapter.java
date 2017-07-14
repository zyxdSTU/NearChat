package nearchat.xiaoxiong.com.nearchat.adapter;

/**
 * Created by Administrator on 2017/6/16.
 */

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.List;

import nearchat.xiaoxiong.com.nearchat.R;
import nearchat.xiaoxiong.com.nearchat.http.HttpManager;
import nearchat.xiaoxiong.com.nearchat.util.Manager;
import nearchat.xiaoxiong.com.nearchat.util.PreferenceManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static nearchat.xiaoxiong.com.nearchat.http.HttpManager.DOWNLOAD_IMAGE;

/**
 * Created by Administrator on 2017/6/15.
 */

public class TrendPhotoAdapter extends RecyclerView.Adapter<TrendPhotoAdapter.ViewHolder>{

    private List<String> mList;
    private Context mContext;

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            imageView = (ImageView)view.findViewById(R.id.photo_view);
        }
    }


    public TrendPhotoAdapter(List<String> mList) {
        this.mList = mList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.mContext = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trend_photo_item, parent, false);
        return new ViewHolder(view);
    }


    /*从缓存加载，从网络加载*/
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        /*imageString图片ID*/
        final String imageString = mList.get(position);
        /*缓存里面有*/
        if(!PreferenceManager.getInstance().preferenceManagerGet(imageString).equals("")) {
            String image = PreferenceManager.getInstance().preferenceManagerGet(imageString);
            byte[] imageByte = Base64.decode(image.getBytes(), Base64.DEFAULT);
            Glide.with(Manager.getInstance().getContent()).load(imageByte).into(holder.imageView);
        }else {
            HttpManager.getInstance().sendRequest(DOWNLOAD_IMAGE + imageString, new Callback() {
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
                                Glide.with(Manager.getInstance().getContent()).load(imageByte).into(holder.imageView);
                                Manager.getInstance().updateImagePreference(imageString, imageByte);
                            } else {
                                /**如果没有更新头像，就加载默认头像**/
                                holder.imageView.setImageResource(R.drawable.head);
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}

