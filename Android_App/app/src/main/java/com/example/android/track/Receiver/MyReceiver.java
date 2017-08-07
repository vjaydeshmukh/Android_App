package com.example.android.track.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import com.example.android.track.Activity.TalkingActivity;
import com.example.android.track.Application.MyApplication;
import com.example.android.track.Model.ClientInfo;
import com.example.android.track.Model.LitePal_Entity.CommentMeRecord;
import com.example.android.track.Model.LitePal_Entity.LikeMeRecord;
import com.example.android.track.Model.LitePal_Entity.MentionMeRecord;
import com.example.android.track.Model.LitePal_Entity.Acquaintance;
import com.example.android.track.Model.LitePal_Entity.ShareMeRecord;
import com.example.android.track.Util.AcquaintanceManager;
import com.example.android.track.Util.UserRequester;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.concurrent.RunnableFuture;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.content.EventNotificationContent;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.event.NotificationClickEvent;
import cn.jpush.im.android.api.model.Message;

import static cn.jpush.im.android.api.enums.ContentType.eventNotification;
import static cn.jpush.im.android.api.enums.ContentType.file;
import static com.baidu.location.d.j.S;
import static com.baidu.location.d.j.g;
import static org.apache.commons.lang3.StringUtils.split;

/**
 * Created by thor on 2017/7/21.
 */

public class MyReceiver extends BroadcastReceiver {

    public MyReceiver() {
        super();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        // receive custom message
        if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            //String external = bundle.getString(JPushInterface.EXTRA_EXTRA);

            String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
            String title = message.split("#")[0];   // get title
            String json_message = message.replace(title+"#", ""); // remove title
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd HH:mm:ss")
                    .create();


            int old_cnt = MyApplication.getUnReadMsgCnt();
            MyApplication.setUnReadMsgCnt(old_cnt + 1);
            switch (title){
                case "NewLikeMessage":
                    old_cnt = MyApplication.getUnReadLikenCnt();
                    MyApplication.setUnReadLikenCnt(old_cnt + 1);
                    MyApplication.setNewMsg(true);
                    LikeMeRecord likeMeRecord = gson.fromJson(json_message, new TypeToken<LikeMeRecord>() {}.getType());
                    likeMeRecord.setStatus("unRead");
                    likeMeRecord.save();
                    AcquaintanceManager.saveAcquaintance(likeMeRecord.getUser_id());
                    break;
                case "NewCommentMessage":
                    old_cnt = MyApplication.getUnReadCommentCnt();
                    MyApplication.setUnReadCommentCnt(old_cnt + 1);
                    MyApplication.setNewMsg(true);
                    CommentMeRecord commentMeRecord = gson.fromJson(json_message, new TypeToken<CommentMeRecord>(){}.getType());
                    commentMeRecord.setStatus("unRead");
                    commentMeRecord.save();
                    AcquaintanceManager.saveAcquaintance(commentMeRecord.getUser_id());
                    break;
                case "NewShareMessage":
                    old_cnt = MyApplication.getUnReadShareCnt();
                    MyApplication.setUnReadShareCnt(old_cnt + 1);
                    MyApplication.setNewMsg(true);
                    ShareMeRecord shareMeRecord = gson.fromJson(json_message, new TypeToken<ShareMeRecord>(){}.getType());
                    shareMeRecord.setStatus("unRead");
                    shareMeRecord.save();
                    AcquaintanceManager.saveAcquaintance(shareMeRecord.getUser_id());
                    break;
                case "NewMentionMeMessage":
                    old_cnt = MyApplication.getUnReadMentionCnt();
                    MyApplication.setUnReadMentionCnt(old_cnt + 1);
                    MyApplication.setNewMsg(true);
                    MentionMeRecord mentionMeRecord = gson.fromJson(json_message, new TypeToken<MentionMeRecord>(){}.getType());
                    mentionMeRecord.setStatus("unRead");
                    mentionMeRecord.save();
                    AcquaintanceManager.saveAcquaintance(mentionMeRecord.getUser_id());
                    break;
                case "NewFollowFeedMessage":
                    MyApplication.setNewFollowFeed(true);
                    break;
                default:
                    break;
            }
        }
    }

    

}
