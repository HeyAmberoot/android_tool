package com.example.cuanbo.TCP;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Message;
import android.util.Log;

import com.example.cuanbo.Presentation.CustomActivity;
import com.example.cuanbo.Presentation.DownloadActivity;
import com.example.cuanbo.ProjectData.DeviceMsg;
import com.example.cuanbo.Tool.Const;
import com.example.cuanbo.Tool.MyApplication;
import com.example.cuanbo.easycontrol.MainActivity;

/**
 * Created by xww on 18/3/19.
 */


public class StartSocketThread implements Runnable {
    public static Socket socket;

    Message msg_main = MainActivity.myhandler.obtainMessage();

    @Override
    public void run() {
        try {
            String socket_ip = DeviceMsg.getAddress();
            String socket_port = DeviceMsg.getPort();
            if (socket_ip==null || socket_port=="") {
                return;
            }
            int Socket_port = Integer.parseInt(socket_port);
            socket = new Socket(socket_ip,Socket_port);//连接服务端的IP

            MainActivity.running = true;
            //成功连接socket对象
            if(socket.isConnected()&&!socket.isClosed()){

                //启动接收数据的线程
                new Thread(new SocketReceiveThread(socket)).start();
                //启动发送数据的线程
                new Thread(new SocketSendThread(socket)).start();
                //发送消息到主线程
                msg_main.what=Const.ConnectSuccess;
                MainActivity.myhandler.sendMessage(msg_main);
            }

        }catch (Exception e) {
            e.printStackTrace();
            //利用Handler返回数据到主线程
            msg_main.what= Const.ConnectFail;
            MainActivity.myhandler.sendMessage(msg_main);
            MainActivity.running = false;
            Log.e("StartThread:",e.getMessage());
        }

    }
}
