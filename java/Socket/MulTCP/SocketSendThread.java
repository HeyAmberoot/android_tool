package com.cuanbo.wisroom.TCP;

/**
 * Created by xww on 18/3/19.
 */
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.cuanbo.wisroom.ActivityUtil.MyApplication;
import com.cuanbo.wisroom.MainActivity;
import com.cuanbo.wisroom.Model.CodeList;
import com.cuanbo.wisroom.tool.Const;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static com.cuanbo.wisroom.ActivityUtil.MyApplication.getContext;


public class SocketSendThread implements Runnable {

    private OutputStream out = null;
    public static boolean writing = false;
    private Socket socketRunning;
    //true发送数据给传感器，false发送数据给矩阵
    public static boolean sendToTransducer = false;
    //用Handler把读取到的信息发到主线程
    private Message msg0 = MainActivity.myhandler.obtainMessage();

    //建立构造函数来获取socket对象的输入流
    public SocketSendThread(Socket socket) throws IOException {
        out = socket.getOutputStream();//得到socket的输出流
        socketRunning = socket;
    }
    @Override
    public void run() {
        // 发送请求数据
        while (MainActivity.running || MainActivity.TransducerRunning) {
            try {

                while (writing) {
                    writing = false;
                    if (out == null) {
                        return;
                    }

                    if (socketRunning == StartSocketThread.socketTransducer && sendToTransducer) {
                        Log.i("socket","指令发送到传感器");
                        byte[] codeToSend = CodeList.getCodeToSend();
                        out.write(codeToSend);

                    }else if (!sendToTransducer){
                        byte[] codeToSend = CodeList.getCodeToSend();
                        out.write(codeToSend);
                        String str = new String(codeToSend);
                        Log.i("SocketSendThread","指令发送到矩阵："+str);
                    }

                }
                //数据最后加上换行符才可以让服务器端的readline()停止阻塞
//                out.write(("i am amberoot"+"\n").getBytes("utf-8"));

            } catch (IOException e) {
                e.printStackTrace();
                if (socketRunning == StartSocketThread.socketTransducer) {
                    MainActivity.TransducerRunning = false;
                }else {
                    MainActivity.running = false;
                    msg0.what = Const.ServiceDisconnect;
                    MainActivity.myhandler.sendMessage(msg0);//发送信息通知用户客户端已关闭
                }
                Toast.makeText(MyApplication.getContext(),
                        "指令发送失败", Toast.LENGTH_SHORT).show();
                Log.e("SocketSendThread","指令发送失败-"+e.getMessage());
            }
        }
    }
}