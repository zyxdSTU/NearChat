package nearchat.xiaoxiong.com.nearchat;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import java.util.ArrayList;
import java.util.List;
import nearchat.xiaoxiong.com.nearchat.adapter.MsgAdapter;
import nearchat.xiaoxiong.com.nearchat.javabean.Msg;
import nearchat.xiaoxiong.com.nearchat.javabean.User;

public class ChatActivity extends AppCompatActivity {

    private User user;
    private String toPhoneNumber;
    private String currentPhoneNumber;

    private List<Msg> msgList = new ArrayList<Msg>();
    private RecyclerView msgRecyclerView;
    private MsgAdapter adapter;
    private EMMessageListener mMessageListener;
    private EMConversation mConversation;

    private EditText inputText;
    private Button send;
    private TextView toName;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    EMMessage emMessage = (EMMessage) message.obj;
                    EMTextMessageBody body = (EMTextMessageBody) emMessage.getBody();
                    Msg msg = new Msg(body.getMessage(), Msg.TYPE_RECEIVED);
                    msgList.add(msg);
                    /**插入到最后一行**/
                    adapter.notifyItemInserted(msgList.size() - 1);
                    msgRecyclerView.scrollToPosition(msgList.size() - 1);
                    /**标记已读**/
                    mConversation.markMessageAsRead(emMessage.getMsgId());
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        String jsonUser = getIntent().getStringExtra("chatUser");
        user = new Gson().fromJson(jsonUser, User.class);
        toPhoneNumber = user.getPhoneNumber();
        currentPhoneNumber = EMClient.getInstance().getCurrentUser();

        mConversation = EMClient.getInstance().chatManager().getConversation(toPhoneNumber, null, true);
        mConversation.markAllMessagesAsRead();

        initMsgs(); // 初始化消息数据

        inputText = (EditText) findViewById(R.id.input_text);
        send = (Button) findViewById(R.id.send);
        toName = (TextView) findViewById(R.id.toName_text);
        toName.setText(user.getNickName());

        msgRecyclerView = (RecyclerView) findViewById(R.id.msg_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter = new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);

        /**消息监听**/
        mMessageListener = new EMMessageListener() {
            @Override
            public void onMessageReceived(List<EMMessage> messages) {
                //收到消息
                for (EMMessage message : messages) {
                    Message msg = new Message();
                    msg.what = 0;
                    msg.obj = message;
                    mHandler.sendMessage(msg);
                }
            }

            @Override
            public void onCmdMessageReceived(List<EMMessage> messages) {
                //收到透传消息
            }

            @Override
            public void onMessageRead(List<EMMessage> messages) {
                //收到已读回执
            }

            @Override
            public void onMessageDelivered(List<EMMessage> message) {
                //收到已送达回执
            }

            @Override
            public void onMessageChanged(EMMessage message, Object change) {
                //消息状态变动
            }
        };

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String content = inputText.getText().toString();
                if (!"".equals(content)) {
                    Msg msg = new Msg(content, Msg.TYPE_SENT);
                    msgList.add(msg);
                    /**插入到最后一行**/
                    adapter.notifyItemInserted(msgList.size() - 1);
                    msgRecyclerView.scrollToPosition(msgList.size() - 1);
                    inputText.setText(""); // 清空输入框中的内容

                    /**发送消息**/
                    EMMessage message = EMMessage.createTxtSendMessage(content, toPhoneNumber);
                    EMClient.getInstance().chatManager().sendMessage(message);

                    /**消息回调**/
                    message.setMessageStatusCallback(new EMCallBack() {
                        @Override
                        public void onSuccess() {
                            // 消息发送成功，打印下日志，正常操作应该去刷新ui
                        }

                        @Override
                        public void onError(int i, String s) {
                            // 消息发送失败，打印下失败的信息，正常操作应该去刷新ui
                        }

                        @Override
                        public void onProgress(int i, String s) {
                            // 消息发送进度，一般只有在发送图片和文件等消息才会有回调，txt不回调
                        }
                    });

                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 添加消息监听
        EMClient.getInstance().chatManager().addMessageListener(mMessageListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 移除消息监听
        EMClient.getInstance().chatManager().removeMessageListener(mMessageListener);
    }

    private void initMsgs() {
        if(mConversation.getLastMessage() != null) {
            String lastMsgId = mConversation.getLastMessage().getMsgId();
            List<EMMessage> emMessageList = mConversation.loadMoreMsgFromDB(lastMsgId, 20);
            emMessageList.add(mConversation.getLastMessage());
            for (EMMessage emMessage : emMessageList) {
                EMTextMessageBody body = (EMTextMessageBody) emMessage.getBody();
                String fromUser = emMessage.getFrom();
                if(fromUser.equals(currentPhoneNumber)) {
                    msgList.add(new Msg(body.getMessage(), Msg.TYPE_SENT));
                } else {
                    msgList.add(new Msg(body.getMessage(), Msg.TYPE_RECEIVED));
                }
            }
        }
    }
}
