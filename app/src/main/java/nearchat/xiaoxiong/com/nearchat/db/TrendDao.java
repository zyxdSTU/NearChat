package nearchat.xiaoxiong.com.nearchat.db;

import java.util.List;

import nearchat.xiaoxiong.com.nearchat.javabean.Trend;
import nearchat.xiaoxiong.com.nearchat.javabean.User;

/**
 * Created by Administrator on 2017/6/14.
 */

public class TrendDao {
    public List<Trend> getAllTrend() {
        return DBManager.getInstance().getAllTrend();
    }

    public void addTrend(Trend trend) {
        DBManager.getInstance().addTrend(trend);
    }
}
