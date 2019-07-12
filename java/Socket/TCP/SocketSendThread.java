package com.example.cuanbo.TCP;

/**
 * Created by xww on 18/3/19.
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import android.os.Message;
import android.util.Log;

import com.example.cuanbo.Presentation.DownloadActivity;
import com.example.cuanbo.ProjectData.CodeList;
import com.example.cuanbo.easycontrol.MainActivity;

public class SocketSendThread implements Runnable {

    private OutputStream out = null;
    public static boolean writing = false;
    //建立构造函数来获取socket对象的输入流
    public SocketSendThread(Socket socket) throws IOException {
        out = socket.getOutputStream();//得到socket的输出流
    }
    @Override
    public void run() {
        // 发送请求数据
        while (MainActivity.running) {
            try {

                while (writing == true) {
                    writing = false;
                    byte[] codeToSend = CodeList.getCodeToSend();
                    out.write(codeToSend);
                    String str = new String(codeToSend);
                }
                //数据最后加上换行符才可以让服务器端的readline()停止阻塞
//                out.write(("i am amberoot"+"\n").getBytes("utf-8"));


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}