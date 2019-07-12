package com.cuanbo.wisroom.TCP;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Objects;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Message;
import android.util.Log;

import com.cuanbo.wisroom.MainActivity;
import com.cuanbo.wisroom.Model.DeviceMsg;
import com.cuanbo.wisroom.tool.Const;


/**
 * Created by xww on 18/3/19.
 */


public class StartSocketThread implements Runnable {
    public static Socket socket;
    public static Socket socketTransducer;
    private Message msg_main = MainActivity.myhandler.obtainMessage();//////////////

    public StartSocketThread(boolean isTransducer) {
        MainActivity.isTransducer = isTransducer;
    }

    @Override
    public void run() {
        String socket_ip = "";
        String socket_port;
        try {

            if (MainActivity.isTransducer && !MainActivity.TransducerRunning) {
                socket_ip = "192.168.2.20";//DeviceMsg.getTransducerIp();
                socket_port = "8898";//DeviceMsg.getTransducerPort();
                if (socket_ip==null || Objects.equals(socket_port, "")) {
                    return;
                }
                int Socket_port = Integer.parseInt(socket_port);
                Log.i("Socket","连接传感器");

                //连接传感器
                socketTransducer = new Socket(socket_ip,Socket_port);

                if(socketTransducer.isConnected()&&!socketTransducer.isClosed()) {//成功连接
                    MainActivity.TransducerRunning = true;
                    //启动接收数据的线程
                    new Thread(new SocketReceiveThread(socketTransducer)).start();
                    //启动发送数据的线程
                    new Thread(new SocketSendThread(socketTransducer)).start();
                    Log.i("Socket","成功连接传感器");
                    //发送消息到主线程
                    msg_main.what=Const.TransducerConnecSuccess;
                    MainActivity.myhandler.sendMessage(msg_main);
                }

            }else if(!MainActivity.isTransducer && !MainActivity.running){
                socket_ip = DeviceMsg.getAddress();
                socket_port = DeviceMsg.getPort();
                if (socket_ip==null || Objects.equals(socket_port, "")) {
                    return;
                }
                int Socket_port = Integer.parseInt(socket_port);
                Log.i("Socket","连接服务器");
                //连接服务端
                socket = new Socket(socket_ip,Socket_port);

                if(socket.isConnected()&&!socket.isClosed()){//成功连接获取socket对象则发送成功消息
                    MainActivity.running = true;
                    //启动接收数据的线程
                    new Thread(new SocketReceiveThread(socket)).start();
                    //启动发送数据的线程
                    new Thread(new SocketSendThread(socket)).start();
                    //发送消息到主线程
                    msg_main.what=Const.ConnectSuccess;
                    MainActivity.myhandler.sendMessage(msg_main);
                    Log.i("Socket","成功连接服务器");
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
            //利用Handler返回数据到主线程
            if (!socket_ip.equals(DeviceMsg.getAddress())){
                MainActivity.TransducerRunning = false;
                msg_main.what= Const.TransducerConnecFail;
                Log.i("Socket","连接传感器失败");
            }else {
                msg_main.what = Const.ConnectFail;
                MainActivity.running = false;
                Log.i("Socket","连接服务器失败");
            }
            MainActivity.myhandler.sendMessage(msg_main);
            Log.e("StartSocketException",e.getMessage());


        }

    }
}
