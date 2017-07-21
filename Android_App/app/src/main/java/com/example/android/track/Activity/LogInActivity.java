package com.example.android.track.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.example.android.track.Application.MyApplication;
import com.example.android.track.R;
import com.example.android.track.Util.UserRequester;

import cn.jpush.android.api.JPushInterface;


public class LogInActivity extends AppCompatActivity {
    private static final int LOG_IN_OK = 0;
    private static final int LOG_IN_FAILED = 1;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        // set ToolBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.logInToolBar);
        setSupportActionBar(toolbar);

        // set button listener
        // log in button
        Button button = (Button) findViewById(R.id.login_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(LogInActivity.this);
                progressDialog.setTitle("正在登录");
                progressDialog.setMessage("请稍后");
                progressDialog.setCancelable(false);
                progressDialog.show();
                // create a new Thread to log in
                String user_name = ((EditText) findViewById(R.id.user_name)).getText().toString();
                String password = ((EditText) findViewById(R.id.password)).getText().toString();
                logIn(user_name, password);

            }
        });
        // go to sign up page
        TextView signUp_text = (TextView) findViewById(R.id.signUp_text);

        signUp_text.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(LogInActivity.this,SignUpActivity.class);
                startActivity(intent);
            }
        });
    }


    private void logIn(final String user_name, final String password){
        new Thread(new Runnable() {
            @Override
            public void run() {
                UserRequester requester = new UserRequester();
                String result = requester.logInRequest(user_name, password);
                Message message = new Message();
                // if failed
                if(result.equals("ERROR"))
                    message.what = LOG_IN_FAILED;

                else
                    message.what = LOG_IN_OK;

                handler.sendMessage(message);
            }
        }).start();
    }


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case LOG_IN_OK:
                    // restart home activity, whose startup mode is singleTask
                    Intent intent = new Intent(LogInActivity.this,HomeActivity.class );
                    startActivity(intent);
                    break;
                case LOG_IN_FAILED:
                    progressDialog.cancel();
                    Toast.makeText(getApplicationContext(),"登录失败",Toast.LENGTH_SHORT).show();
                    // clear inputed text on view
                    ((EditText) findViewById(R.id.user_name)).setText("");
                    ((EditText) findViewById(R.id.password)).setText("");
                    break;
            }
        }
    };


}
