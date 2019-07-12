package com.cuanbo.wisroom.tool;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by xww on 2019/7/3.
 */

public class UDPThread extends Thread{
    /**
     * UDP接收数据的长度为1KB（1024）
     */
    private final int data_length = 1024;

    private String m_IP;
    private String m_Port;
    /**
     * UDP要发送的数据
     */
    private String m_text;

    //构造函数
    public UDPThread(String strIP, String strPort, String strText)
    {
        m_IP = strIP;
        m_Port = strPort;
        m_text = strText;
    }
    @Override
    public void run() {
        // TODO Auto-generated method stub
        final int TIMEOUT = 3000;//3s
        int tries = 0;//数据包可能丢失，这次尝试的次数
        final int maxTries = 5;//最大失败接收数据次数
        boolean receivedResponse = false;//是否接收数据成功的标志位
        int port = Integer.valueOf(m_Port);
        byte[] bytesToSend = m_text.getBytes();//要发送的字节
        byte[] bytesReceived = new byte[data_length];//接收到的字节

        try {
            InetAddress serverAddress = InetAddress.getByName(m_IP);
            DatagramSocket datagramSocket = new DatagramSocket();
            //设置阻塞时间
            datagramSocket.setSoTimeout(TIMEOUT);
            //把要发送的信息打包
            DatagramPacket sendPacket = new DatagramPacket
                    (bytesToSend, bytesToSend.length, serverAddress, port);
            //建立一个空的接收包
            DatagramPacket receivePacket = new DatagramPacket
                    (bytesReceived, bytesReceived.length);
            do {
                try {
                    datagramSocket.send(sendPacket);//发送数据
                    datagramSocket.receive(receivePacket);//接收数据
                    if (!receivePacket.getAddress().equals(serverAddress)) {//检验数据源
                        throw new IOException("Received packet from an unknown source");
                    }
                    receivedResponse = true;

                } catch (IOException e) {//当接收不到信息或接收时间超过3秒时，就向服务器重发请求
                    e.printStackTrace();
                    tries += 1;
                    Log.e("UDP", "尝试接收数据次数：" + tries);
                }
            } while ((!receivedResponse) && (tries < maxTries));


        } catch (IOException e) {
            Log.e("UDP", "发送数据失败");
        }
    }
}
