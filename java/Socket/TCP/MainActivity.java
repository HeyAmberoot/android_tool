package com.example.cuanbo.easycontrol;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
//import android.view.ViewGroup.LayoutParams;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cuanbo.ListView.LeftSlideDeleteListView;
import com.example.cuanbo.Presentation.CustomActivity;
import com.example.cuanbo.Presentation.DownloadActivity;
import com.example.cuanbo.ProjectData.ActivityName;
import com.example.cuanbo.ProjectData.DeviceMsg;
import com.example.cuanbo.ProjectData.DevicePage;
import com.example.cuanbo.ProjectData.ProjectImages;
import com.example.cuanbo.Tool.Common;
import com.example.cuanbo.Tool.Const;
import com.example.cuanbo.Tool.ImageUtils;
import com.example.cuanbo.Tool.FileHelper;
import com.example.cuanbo.Tool.MyApplication;
import com.example.cuanbo.TCP.StartSocketThread;
import com.example.cuanbo.Tool.readXML;

import java.io.IOException;
import java.util.List;



public class MainActivity extends BaseActivity {

    private String Tag = "MainActivity";
    public static Handler myhandler;
    //socket线程是否running的标志位
    public static boolean running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//去除标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        setContentView(R.layout.activity_main);


        //实例化Handler，用于进程间的通信//////////////////
        myhandler = new MyHandler();
        networkChangeReceiver();
        //启动Socket线程
        new Thread(new StartSocketThread()).start();


    }

    /**
     * 获取屏幕分辨率
     */
    public void getScreenDensity_ByResources() {
        DisplayMetrics mDisplayMetrics = getResources().getDisplayMetrics();
        int width = mDisplayMetrics.widthPixels;
        int height = mDisplayMetrics.heightPixels;
        float density = mDisplayMetrics.density;
        int densityDpi = mDisplayMetrics.densityDpi;
        Log.i(Tag, "Screen Ratio: [" + width + "x" + height + "],density=" + density + ",densityDpi=" + densityDpi);
        Log.i(Tag, "Screen mDisplayMetrics: " + mDisplayMetrics);
        DeviceMsg.setScreenWidth(width);
        DeviceMsg.setScreenHeiht(height);
    }

    /**
     * 注册网络通知
     */
    public void networkChangeReceiver()
    {
        broadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {

                //ConnectivityManager是一个系统服务类，专门用于管理网络连接
                ConnectivityManager connectivityManager=(ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);//获取ConnectivityManager实例
                //获取NetworkInfo实例
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if(networkInfo != null && networkInfo.isAvailable()){
//                    Toast.makeText(MyApplication.getContext(), "network is available", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(MyApplication.getContext(), "网络不可用！", Toast.LENGTH_LONG).show();

                }

            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Const.networkChange_action);
        registerReceiver(broadcastReceiver, intentFilter);
    }



    class MyHandler extends Handler{//在主线程处理Handler传回来的message

        @Override
        public void handleMessage(Message msg) {

            String currentActivityName = ActivityName.getName();
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

    private int convertDpToPixel(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return (int)(dp*displayMetrics.density);
    }
    private int convertPixelToDp(int pixel) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return (int)(pixel/displayMetrics.density);
    }

    /**
     * 销毁activity
     */
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);//取消注册广播
        }
        if (StartSocketThread.socket != null) {
            try {
                //关闭Socket线程
                this.running = false;
                StartSocketThread.socket.close();
            } catch (NullPointerException | IOException e) {
                e.printStackTrace();
            }
        }

    }


}


