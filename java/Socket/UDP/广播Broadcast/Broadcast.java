package com.cuanbo.wisroom.tool;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Created by xww on 2019/7/8.
 */

public class Broadcast extends Thread {

    private int m_Port;
    /**
     * 要发送的数据
     */
    private byte[] m_data;

    //构造函数
    public Broadcast(int Port, byte[] data)
    {
        m_Port = Port;
        m_data = data;
    }
    @Override
    public void run() {
        DatagramSocket datagramSocket = null;
        try {
            
            // 获取本地所有网络接口
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                // getInterfaceAddresses()方法返回绑定到该网络接口的所有 IP 的集合
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    // 不广播回环网络接口
                    if (broadcast  == null) {
                        continue;
                    }
                    datagramSocket = new DatagramSocket();
                    // 发送广播报文
                    try {
                        DatagramPacket sendPacket = new DatagramPacket(m_data,
                                m_data.length, broadcast, m_Port);
                        datagramSocket.send(sendPacket);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.d("发送请求", getClass().getName() + ">>> Request packet sent to: " +
                            broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
            Log.e("computerWake","唤醒电脑失败："+e.getMessage());
        } finally {
            if(datagramSocket != null)
                datagramSocket.close();
        }


    }

    private byte[] macStrToBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }
}
