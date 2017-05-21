package nearchat.xiaoxiong.com.nearchat.db;

import java.util.List;
import nearchat.xiaoxiong.com.nearchat.javabean.InviteMessage;

/**
 * Created by Administrator on 2017/5/20.
 */

public class InviteMessageDao {
    public InviteMessageDao() {}

    public List<InviteMessage> getInviteMessageList() {
        return DBManager.getInstance().getInviteMessageList();
    }

    public void saveInviteMessage(InviteMessage inviteMessage) {
        DBManager.getInstance().saveInviteMessage(inviteMessage);
    }

    public void updateInviteMessage(InviteMessage inviteMessage) {
        DBManager.getInstance().updateInviteMessage(inviteMessage);
    }
}
