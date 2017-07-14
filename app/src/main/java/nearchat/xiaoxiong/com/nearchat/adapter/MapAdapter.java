package nearchat.xiaoxiong.com.nearchat.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import nearchat.xiaoxiong.com.nearchat.NearUserInfoActivity;
import nearchat.xiaoxiong.com.nearchat.R;
import nearchat.xiaoxiong.com.nearchat.http.HttpManager;
import nearchat.xiaoxiong.com.nearchat.javabean.NearUser;
import nearchat.xiaoxiong.com.nearchat.javabean.User;
import nearchat.xiaoxiong.com.nearchat.javabean.UserLocation;
import nearchat.xiaoxiong.com.nearchat.util.Manager;
import nearchat.xiaoxiong.com.nearchat.util.PreferenceManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import static nearchat.xiaoxiong.com.nearchat.http.HttpManager.DOWNLOAD_IMAGE;

/**
 * Created by yucong on 2017/5/15.
 */

public class MapAdapter extends RecyclerView.Adapter<MapAdapter.ViewHolder> {
    private List<NearUser> mList ;
    private Context mContext;

    static class ViewHolder extends RecyclerView.ViewHolder{
        CircleImageView headImage;
        TextView name;
        TextView distance ;
        ImageView sexImage ;
        LinearLayout itemLayout;

        public ViewHolder(View view){
            super(view);
            headImage = (CircleImageView) view.findViewById(R.id.imageView);
            name = (TextView) view.findViewById(R.id.name_text);
            distance = (TextView) view.findViewById(R.id.distance_text);
            sexImage = (ImageView) view.findViewById(R.id.sex_image);
            itemLayout = (LinearLayout) view.findViewById(R.id.item_layout);
        }
    }
    public MapAdapter(List<NearUser> mList){
        this.mList = mList ;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_map_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder ;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        NearUser nearUser = mList.get(position);
        holder.name.setText(nearUser.getUser().getNickName());
        if(nearUser.getUser().getSex().equals("女")){
            holder.sexImage.setImageResource(R.drawable.girl);
        }else{
            holder.sexImage.setImageResource(R.drawable.boy);
        }

        UserLocation userLocationOne = nearUser.getUserLocation();
        double longitudeOne = userLocationOne.getLongitude();
        double latitudeOne = userLocationOne.getLatitude();

        String strUserLocationTwo = PreferenceManager.getInstance().preferenceManagerGet("currentUserLocation");
        UserLocation userLocationTwo = new Gson().fromJson(strUserLocationTwo, UserLocation.class);
        double longitudeTwo = userLocationTwo.getLongitude();
        double latitudeTwo  = userLocationTwo.getLatitude();

        double distance = Manager.getInstance().gps2m(longitudeOne, latitudeOne, longitudeTwo, latitudeTwo);

        holder.distance.setText(dealDistance(distance));


        holder.itemLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                NearUser nearUser = mList.get(position);
                Intent intent = new Intent(mContext, NearUserInfoActivity.class);
                String jsonNearUserInfo = new Gson().toJson(nearUser.getUser());
                intent.putExtra("NearUserInfo",jsonNearUserInfo);
                mContext.startActivity(intent);
            }
        });
        loadImage(holder);
    }

    @Override
    public int getItemCount() {
        return mList.size() ;
    }


    /**处理距离显示的问题**/
    public String dealDistance(double distance) {
        if(distance > 1000){
            distance = distance / 1000;
            return String.format("%.2f", distance) + "km";
        } else {
            int distanceInt = (int)distance;
            return String.valueOf(distanceInt) + "m";
        }
    }

    public void loadImage(final ViewHolder holder) {
        int position = holder.getAdapterPosition();
        final User user = mList.get(position).getUser();

        /**如果缓存有直接从缓存中加载, 不更新缓存**/
        if (!PreferenceManager.getInstance().preferenceManagerGet(user.getPhoneNumber()).equals("")) {
            String imageString = PreferenceManager.getInstance().preferenceManagerGet(user.getPhoneNumber());
            byte[] imageByte = Base64.decode(imageString.getBytes(), Base64.DEFAULT);
            Glide.with(Manager.getInstance().getContent()).load(imageByte).into(holder.headImage);
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
                                    Glide.with(Manager.getInstance().getContent()).load(imageByte).into(holder.headImage);
                                } else {
                                    /**如果没有更新头像，就加载默认头像**/
                                    holder.headImage.setImageResource(R.drawable.head);
                                }
                            }
                        });
                    }
                }
            });
        }
    }
}

