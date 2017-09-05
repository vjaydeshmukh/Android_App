package com.example.android.track.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.android.track.Adapter.FollowAdapter;
import com.example.android.track.Adapter.MentionAdapter;
import com.example.android.track.Adapter.RemindAdapter;
import com.example.android.track.Model.Follow;
import com.example.android.track.Model.LitePal_Entity.Acquaintance;
import com.example.android.track.R;
import com.example.android.track.Util.FeedRequester;
import com.example.android.track.Util.UserRequester;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thor on 2017/9/5.
 */

public class ChooseMentionActivity extends AppCompatActivity{
    private List<Follow> follows = new ArrayList<>();
    private List<Integer> chooseList = new ArrayList<>();
    private List<String> chooseNames = new ArrayList<>();
    
    // View
    private ProgressBar progressBar;
    private SearchView searchView;
    private Button search_btn;
    private RecyclerView recyclerView;
    
    
    // Signal
    private final static int GET_FOLLOW_OK = 1;
    private final static int GET_SEARCH_RESULT_OK = 2;
    private final static int GET_SEARCH_RESULT_FAILED = 3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_mention);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        
        // display home button
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        searchView = (SearchView) findViewById(R.id.searchView);
        search_btn = (Button) findViewById(R.id.search_usr_btn);
        setToolbarListener();
        
        Intent intent = getIntent();
        chooseList = intent.getIntegerArrayListExtra("chooseList"); // get choose list from last activity
        chooseNames = intent.getStringArrayListExtra("chooseNames");
        getAcquaintance();
    }

    // set toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.choose_mention_toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.ok_btn:
                Intent intent = new Intent();
                ArrayList arrayList1 = new ArrayList();
                ArrayList arrayList2 = new ArrayList();
                arrayList1.addAll(chooseList);
                arrayList2.addAll(chooseNames);
                
                intent.putIntegerArrayListExtra("chooseList", arrayList1);
                intent.putStringArrayListExtra("chooseNames", arrayList2);
                
                setResult(RESULT_OK, intent);
                finish();
                break;
            case android.R.id.home:
                Intent fail_intent = new Intent();
                setResult(RESULT_CANCELED);
                finish();
                break;
            default:
                break;
        }
        
        return true;
    }
    
    private void setToolbarListener(){
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_btn.setVisibility(View.GONE);
                searchView.setVisibility(View.VISIBLE);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(ChooseMentionActivity.this,"search...",Toast.LENGTH_LONG).show();
                if(query.length() > 20){
                    Toast.makeText(ChooseMentionActivity.this,"too long",Toast.LENGTH_LONG).show();
                    return true;
                }
                if(query.equals("")){
                    Toast.makeText(ChooseMentionActivity.this,"nothing",Toast.LENGTH_LONG).show();
                    return true;
                }
                search(query);
                return true;
            }

            // 当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String newText) {
                Toast.makeText(ChooseMentionActivity.this,"typing...",Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        
    }
    

    private void getAcquaintance(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                UserRequester requester = new UserRequester();
                List<Acquaintance> acquaintanceList = DataSupport.select("*").find(Acquaintance.class);
                for(Acquaintance acquaintance : acquaintanceList){
                    Follow follow = new Follow();
                    follow.setUser_name(acquaintance.getUser_name());
                    follow.setUser_id(acquaintance.getUser_id());
                    follow.setState(acquaintance.getRelationship());
                    follow.setportrait_url(requester.getPortraitUrl(acquaintance.getUser_id()));
                }

                Message message = new Message();
                message.what = GET_FOLLOW_OK;
                handler.sendMessage(message);

            }
        }).start();
    }

    private void setRecyclerView(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        MentionAdapter adapter = new MentionAdapter(follows, chooseList, chooseNames, ChooseMentionActivity.this);
        recyclerView.setAdapter(adapter);

        progressBar.setVisibility(View.GONE);
    }

    private void search(String query){
        new Thread(new Runnable() {
            @Override
            public void run() {
                UserRequester requester = new UserRequester();
                follows = requester.searchUser(query);
                Message message = new Message();
                if (follows == null)
                    message.what = GET_SEARCH_RESULT_FAILED;
                else
                    message.what = GET_SEARCH_RESULT_OK;
                handler.sendMessage(message);

            }
        }).start();
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case GET_FOLLOW_OK:
                    setRecyclerView();
                    break;
                case GET_SEARCH_RESULT_OK:
                    setRecyclerView();
                    break;
                case GET_SEARCH_RESULT_FAILED:
                    Toast.makeText(ChooseMentionActivity.this, "没有匹配结果", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };
}
