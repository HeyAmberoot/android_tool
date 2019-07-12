package com.cuanbo.wisroom.TCP;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Message;
import android.util.Log;

import com.cuanbo.wisroom.ActivityUtil.MyApplication;
import com.cuanbo.wisroom.MainActivity;
import com.cuanbo.wisroom.Model.SensorData;
import com.cuanbo.wisroom.tool.CRC16;
import com.cuanbo.wisroom.tool.Common;
import com.cuanbo.wisroom.tool.Const;
import com.cuanbo.wisroom.tool.FileHelper;
import com.cuanbo.wisroom.tool.ListenerManager;
import com.cuanbo.wisroom.tool.MyListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by xww on 18/3/19.
 */

public class SocketReceiveThread implements Runnable,MyListener {//extends Thread{

    private InputStream is;
    private String str_receive;
    //此时此刻的socket是哪一个呢
    private Socket socketRunning;
    public static boolean feedbackFlag;

    //建立构造函数来获取socket对象的输入流
    SocketReceiveThread(Socket socket) throws IOException {
        is = socket.getInputStream();
        socketRunning = socket;
        //注册监听器
        ListenerManager.getInstance().registerListtener(this);
    }

    @Override
    public void run() {
        while (MainActivity.running || MainActivity.TransducerRunning) {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            //用Handler把读取到的信息发到主线程
            Message msg0 = MainActivity.myhandler.obtainMessage();
            String tag = "SocketReceiveThread";
            try {
                //读服务器端发来的数据，阻塞直到收到结束符\n或\r
//                str_receive = br.readLine();

                //非阻塞接收数据，但限定接收数据字节
                byte buffer[] = new byte[1024*30];
                int count = is.read(buffer);
                if(count != -1){
                    str_receive = new String(buffer,0,count);
                }else {
                    MainActivity.running = false;//防止服务器端关闭导致客户端读到空指针而导致程序崩溃
                    msg0.what = Const.ServiceDisconnect;
                    MainActivity.myhandler.sendMessage(msg0);//发送信息通知用户客户端已关闭
                }

                if (socketRunning == StartSocketThread.socketTransducer) {
//                    Log.i(tag, String.valueOf(count));
                    if (count == 23) {
                        byte[] temperature = new byte[] {buffer[5],buffer[6]};
                        byte[] humidity = new byte[] {buffer[3],buffer[4]};
                        byte[] CO2 = new byte[] {buffer[13],buffer[14]};
                        byte[] light = new byte[] {buffer[17],buffer[18],buffer[19],buffer[20]};
                        int temp = CRC16.CRC16GetResult(temperature);
                        int hum = CRC16.CRC16GetResult(humidity);
                        int co2 = CRC16.CRC16GetResult(CO2);
                        int li = CRC16.CRC16GetResult(light);
                        String tempStr = String.valueOf(temp);
                        String humStr = String.valueOf(hum);
                        String co2Str = String.valueOf(co2);
                        String lightStr = String.valueOf(li);
                        SensorData.setTemperature(tempStr);
                        SensorData.setHumility(humStr);
                        SensorData.setCO2(co2Str);
                        SensorData.setLight(lightStr);
                        ListenerManager.getInstance().sendBroadCast("transducer",tempStr+":"+humStr+":"+co2Str+":"+lightStr);
                        Log.i("socket","接收到传感器数据");
                    }
                }

            } catch (NullPointerException e) {
                e.printStackTrace();
                Log.e(tag,e.getMessage());
                if (socketRunning == StartSocketThread.socketTransducer) {
                    MainActivity.TransducerRunning = false;
                }else {
                    MainActivity.running = false;//防止服务器端关闭导致客户端读到空指针而导致程序崩溃
                    msg0.what = Const.ServiceDisconnect;
                    MainActivity.myhandler.sendMessage(msg0);//发送信息通知用户客户端已关闭
                }
                ListenerManager.getInstance().unRegisterListener(this);
                break;

            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            FileHelper fileHelper = new FileHelper();

            if (str_receive != null) {
                if (socketRunning == StartSocketThread.socketTransducer) {
                    continue;//只从传感器接收16进制字节
                }
                if (feedbackFlag) {
                    ListenerManager.getInstance().sendBroadCast("feedback",str_receive);
                }

                if (str_receive.equals("#Project:Break")) {
                    //发信息给主线程
                    msg0.what = 5;
                    MainActivity.myhandler.sendMessage(msg0);
                }else if(str_receive.equals("#Project:End")) {
                    ListenerManager.getInstance().sendBroadCast("downloadComplete",null);
                    //发信息给主线程
                    msg0.what = 6;
                    MainActivity.myhandler.sendMessage(msg0);
                }else if (str_receive.startsWith("#Project")) {
                    String ProjectName = str_receive.substring(9);

                    Log.i(tag,ProjectName);
                    String strTime = Common.getCurrentTime();
                    //SharedPreferences保存工程与其下载时间
                    SharedPreferences.Editor editor = MyApplication.getContext().getSharedPreferences("Project", Context.MODE_PRIVATE).edit();
                    editor.putString("ProjectName",ProjectName);
                    editor.putString(ProjectName+"Time",strTime);
                    editor.apply();
                    //发信息给主线程
                    msg0.what = 4;
                    MainActivity.myhandler.sendMessage(msg0);

                    //创建文件
                    fileHelper.creatFile(ProjectName);
                    MainActivity.isDownloadProject = true;

                }else if (MainActivity.isDownloadProject){
                    SharedPreferences project = MyApplication.getContext().getSharedPreferences("Project",Context.MODE_PRIVATE);
                    String ProjectName = project.getString("ProjectName","");
                    fileHelper.writeDataToFile(str_receive,ProjectName);
                    Log.i(tag,"正在写入工程文件");
                }

            }


            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(tag,e.getMessage());
            }

        }

    }

    @Override
    public void receiveNotification(String name, String data) {

    }
}
