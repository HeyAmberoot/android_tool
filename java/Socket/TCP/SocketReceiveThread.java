package com.example.cuanbo.TCP;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Message;
import android.util.Log;

import com.example.cuanbo.ProjectData.DeviceMsg;
import com.example.cuanbo.Tool.Common;
import com.example.cuanbo.Tool.Const;
import com.example.cuanbo.Tool.FileHelper;
import com.example.cuanbo.Presentation.DownloadActivity;
import com.example.cuanbo.Tool.MyApplication;
import com.example.cuanbo.easycontrol.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by xww on 18/3/19.
 */

public class SocketReceiveThread implements Runnable {//extends Thread{

    private InputStream is;
    private String str_receive;
    private String Tag = "SocketReceiveThread";

    //建立构造函数来获取socket对象的输入流
    public SocketReceiveThread(Socket socket) throws IOException {
        is = socket.getInputStream();
    }

    @Override
    public void run() {
        while (MainActivity.running) {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            //用Handler把读取到的信息发到主线程
            Message msg0 = MainActivity.myhandler.obtainMessage();

            try {
                //读服务器端发来的数据，阻塞直到收到结束符\n或\r
//                str_receive = br.readLine();

                //非阻塞接收数据，但数据大小有限制
                byte buffer[] = new byte[1024*30];
                int count = is.read(buffer);
                if(count != -1){
                    str_receive = new String(buffer,0,count);
                }


            } catch (NullPointerException e) {
                e.printStackTrace();
                Log.e(Tag,e.getMessage());
                MainActivity.running = false;//防止服务器端关闭导致客户端读到空指针而导致程序崩溃
                msg0.what = Const.ServiceDisconnect;
                MainActivity.myhandler.sendMessage(msg0);//发送信息通知用户客户端已关闭

                break;

            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            msg0.what = 1;//把读到的内容更新到UI
            msg0.obj = str_receive;
            MainActivity.myhandler.sendMessage(msg0);

            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(Tag,e.getMessage());
            }

        }

    }
}
