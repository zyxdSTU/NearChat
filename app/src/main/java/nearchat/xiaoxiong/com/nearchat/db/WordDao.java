package nearchat.xiaoxiong.com.nearchat.db;

import java.util.List;

import nearchat.xiaoxiong.com.nearchat.javabean.Trend;
import nearchat.xiaoxiong.com.nearchat.javabean.Word;

/**
 * Created by Administrator on 2017/6/14.
 */

public class WordDao {
    public List<Word> getAllWord() {
        return DBManager.getInstance().getAllWord();
    }

    public void addWord(Word word) {
        DBManager.getInstance().addWord(word);
    }

    public List<Word> getWordOfTrend(String trendId){
        return DBManager.getInstance().getWordOfTrend(trendId);
    }
}
