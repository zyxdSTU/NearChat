package nearchat.xiaoxiong.com.nearchat.javabean;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/6/14.
 */
public class Word extends DataSupport{
    private String trendId;
    private String phoneNumber;
    private String text;
    private long currentTime;

    public String getTrendId() {
        return trendId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getText(){
        return text;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void setTrendId(String trendId) {
        this.trendId = trendId;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setText(String text) {
        this.text =text;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }
}

