package com.cuanbo.wisroom;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.cuanbo.wisroom.ActivityUtil.BaseActivity;
import com.cuanbo.wisroom.ActivityUtil.MyApplication;


public class MainActivity extends BaseActivity {

   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textTitle = findViewById(R.id.textView_title);
        textTitle.setOnTouchListener(new OnDoubleClickListener(new OnDoubleClickListener.DoubleClickCallback() {
                    @Override
                    public void onDoubleClick() {
                        Intent intent=new Intent(MainActivity.this, DownloadActivity.class);
                        startActivity(intent);
                    }
                }));

        //实例化Handler，用于进程间的通信//////////////////
        myhandler = new MyHandler(this);


    }

   

    class MyHandler extends Handler {//在主线程处理Handler传回来的message

        //把Handler对MainActivity进行弱引用，防止内存泄漏
        private WeakReference<MainActivity> activityWeakReference;

        private MyHandler(MainActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            //String currentActivityName = ActivityName.getName();
            switch (msg.what) {
                case 1:
                    String str = (String) msg.obj;
                    Log.i("received:",str);

                    break;
                case Const.ConnectSuccess:
                    Log.i("handleMessage_Socket", "连接成功");
                    Toast.makeText(MyApplication.getContext(),
                            "连接成功！", Toast.LENGTH_SHORT).show();

                    break;
                case Const.ServiceDisconnect:
                    Log.i("handleMessage_Socket", "服务端连接断开");
                    Toast.makeText(MyApplication.getContext(),
                            "与服务器断开连接！", Toast.LENGTH_SHORT).show();

                    break;
                case Const.ConnectFail:
                    Log.i("handleMessage_Socket", "连接失败");
                    Toast.makeText(MyApplication.getContext(),
                            "连接失败", Toast.LENGTH_LONG).show();

                    break;

                default:
                    break;

            }
        }

    }


   
}
