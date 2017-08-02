package com.example.android.track.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.platform.comapi.map.I;
import com.example.android.track.Adapter.TalkingAdapter;
import com.example.android.track.R;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.api.BasicCallback;

import static cn.jpush.im.android.api.JMessageClient.getSingleConversation;
import static cn.jpush.im.android.api.JMessageClient.sendMessage;

public class TalkingActivity extends AppCompatActivity {

    private List<Message> messageList = new ArrayList<>();
    private EditText input_text;
    private Button send_btn;
    private RecyclerView msg_recycler_view;
    private TalkingAdapter adapter;
    private String with_who;
    private Conversation conversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talking);
        JMessageClient.registerEventReceiver(this); // register message receiver

        getWho();
        initRecord();

        input_text = (EditText)findViewById(R.id.msg_input);
        send_btn = (Button)findViewById(R.id.send);
        msg_recycler_view = (RecyclerView) findViewById(R.id.msg_recycler_view);



        send_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String content = input_text.getText().toString();
                if(!"".equals(content)){
                    // send message
                    TextContent text = new TextContent(content);
                    final Message message = conversation.createSendMessage(text);
                    message.setOnSendCompleteCallback(new BasicCallback() {
                        @Override
                        public void gotResult(int i, String s) {
                            if(i == 0){
                                showNewMessage(message);
                            }
                            else {
                                Toast.makeText(TalkingActivity.this, s, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    JMessageClient.sendMessage(message);
                }
            }
        });
    }

    private void initRecord(){
        // init conversation
        conversation = JMessageClient.getSingleConversation(with_who);
        if (null == conversation) {
            conversation = Conversation.createSingleConversation("tom");
        }
        // init message history record
        messageList = conversation.getAllMessage();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        msg_recycler_view.setLayoutManager(layoutManager);
        adapter = new TalkingAdapter(messageList);
        msg_recycler_view.setAdapter(adapter);
        msg_recycler_view.scrollToPosition(messageList.size()-1); // scroll to latest record
    }

    private void getWho(){
        Intent intent = getIntent();
        with_who = String.valueOf(intent.getIntExtra("with_who", 0));
    }

    private void showNewMessage(Message message){
        // show new message
        messageList.add(message);
        adapter.notifyItemInserted(messageList.size() - 1);
        msg_recycler_view.scrollToPosition(messageList.size()-1);
        input_text.setText("");
    }

    public void onEventMainThread(MessageEvent event) {
        Message msg = event.getMessage();
        switch (msg.getContentType()) {
            case text:
                // 处理文字消息
                showNewMessage(msg);
                break;
        }
    }
}