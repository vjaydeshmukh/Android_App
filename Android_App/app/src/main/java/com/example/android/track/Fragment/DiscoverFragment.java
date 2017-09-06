package com.example.android.track.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


import com.example.android.track.Application.MyApplication;
import com.example.android.track.Model.Feed;
import com.example.android.track.Adapter.FeedAdapter;
import com.example.android.track.Activity.HomeActivity;
import com.example.android.track.Activity.SearchActivity;
import com.example.android.track.R;

import com.example.android.track.Util.FeedRequester;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.R.id.message;


/**
 * Created by thor on 2017/6/28.
 */

public class DiscoverFragment extends Fragment {
    private List<Feed> feedList = new ArrayList<>();
    private FeedAdapter feedAdapter;
    private List<Feed> moreFeeds = new ArrayList<>();
    private FeedRequester requester = new FeedRequester(); // do not need verify


    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;


    private final static int GET_AFTER_FEED_OK = 0;
    private final static int GET_AFTER_FEED_FAILED = 1;
    private final static int GET_BEFORE_FEED_OK = 2;
    private final static int GET_BEFORE_FEED_FAILED = 3;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_disc_hot, container, false);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // set tool bar
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.discoverToolBar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        // set home activity aroundFragment
        HomeActivity homeActivity = (HomeActivity) getActivity();
        homeActivity.setAroundFragment(false);

        Button search_btn = (Button) getActivity().findViewById(R.id.search_toolBar_btn);
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                getActivity().startActivity(intent);
            }
        });

        Button around_btn = (Button) getActivity().findViewById(R.id.around_btn);
        around_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch_frag ();
            }
        });

        // set Recycle View
        Date nowTime = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String dateStr = sdf.format(nowTime);
        getFeeds("before", dateStr);

        // set refresh layout
        swipeRefresh = (SwipeRefreshLayout) getActivity().findViewById(R.id.swip_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String last_date = feedList.get(0).getDate();
                getFeeds("after", last_date);
            }
        });

        initRecycleView();

    }

    private void switch_frag(){
        FragmentManager fragmentManager =getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        DiscoverAroundFragment fragment = ((HomeActivity)getActivity()).getDiscoverAroundFragment();
        if(fragment == null){
            fragment = new DiscoverAroundFragment();
            ((HomeActivity)getActivity()).setDiscoverAroundFragment(fragment);
        }
        transaction.replace(R.id.fragment,fragment);
        transaction.commit();
    }

    // use feedlist to initialize recycleView
    private void initRecycleView(){
        if(getActivity() == null)
            return;
        recyclerView = (RecyclerView) getActivity().findViewById(R.id.discHot_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        feedAdapter = new FeedAdapter(getActivity(), feedList);
        recyclerView.setAdapter(feedAdapter);
        setRecyclerViewScrollListener();
    }


    // moniter if the recyclerView is scroll to the bottom and continue get more feeds
    private void setRecyclerViewScrollListener(){
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            //用来标记是否正在向最后一个滑动
            boolean isSlidingToLast = false;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                // 当不滚动时
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //获取最后一个完全显示的Item 的 Position
                    int lastVisibleItem = manager.findLastCompletelyVisibleItemPosition();
                    int totalItemCount = manager.getItemCount();

                    // 判断是否滚动到底部，并且是向右滚动
                    if (lastVisibleItem == (totalItemCount - 1) && isSlidingToLast) {
                        //加载更多功能的代码
                        Toast.makeText(getActivity(), "bottom", Toast.LENGTH_SHORT).show();

                        //getFeeds();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //dx用来判断横向滑动方向，dy用来判断纵向滑动方向
                if (dx > 0) {
                    //大于0表示正在向右滚动
                    isSlidingToLast = true;
                } else {
                    //小于等于0表示停止或向左滚动
                    isSlidingToLast = false;
                }
            }
        });

    }


    private void getFeeds(String direction, String time){
        Thread newThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();

                if(direction.equals("after")) {
                    moreFeeds = requester.getHotFeeds(direction, time);

                    if (moreFeeds == null)
                        message.what = GET_AFTER_FEED_FAILED;
                    else {
                        moreFeeds.addAll(feedList);
                        feedList = moreFeeds;

                        message.what = GET_AFTER_FEED_OK;
                    }
                }
                else if(direction.equals("before")){
                    moreFeeds = requester.getHotFeeds(direction, time);

                    if (moreFeeds == null)
                        message.what = GET_BEFORE_FEED_FAILED;
                    else{
                        feedList.addAll(moreFeeds);
                        message.what = GET_BEFORE_FEED_OK;
                    }
                }

                handler.sendMessage(message);
            }
        });
        newThread.start();
    }



    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case GET_AFTER_FEED_OK:
                    feedAdapter.notifyItemRangeInserted(0, moreFeeds.size());
                    break;
                case GET_BEFORE_FEED_OK:
                    feedAdapter.notifyItemChanged(-1, moreFeeds.size());
                    break;

                case GET_BEFORE_FEED_FAILED:
                    if(getActivity() != null)
                        Toast.makeText(MyApplication.getContext(), "get feed failed", Toast.LENGTH_SHORT).show();
                    break;
                case GET_AFTER_FEED_FAILED:
                    // very importtant, if the fragment have already switch to other fragment,
                    // and the http request thread is still run, when handler get a message and handle it
                    // get Activity() will receive a null point,

                    if(getActivity() != null)
                        Toast.makeText(MyApplication.getContext(), "get feed failed", Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    };
}





