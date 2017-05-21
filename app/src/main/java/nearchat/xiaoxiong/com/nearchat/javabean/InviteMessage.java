package nearchat.xiaoxiong.com.nearchat.javabean;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/5/20.
 */

public class InviteMessage extends DataSupport{
    private String from;
    private String to;
    private int status;
    private long currentTime;
    private String reason;

    public String getFrom() {return from;}
    public String getTo() {return to;}
    public int getStatus() {return status;}
    public String getReason() {return reason;}
    public long getCurrentTime() {return currentTime;}

    public void setFrom(String from) {this.from = from;}
    public void setTo(String to) {this.to = to;}
    public void setStatus(int status) {this.status = status;}
    public void setReason(String reason) {this.reason = reason;}
    public void setCurrentTime(long currentTime) {this.currentTime = currentTime;}
}
