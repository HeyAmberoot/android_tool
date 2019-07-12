package com.cuanbo.signalswitch;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

/**
 * Created by xww on 2019/7/8.
 */

public class MulticastServer {
    private String TAG = "MulticastServer";
    private final int MAX_SIZE = (64 * 1024);// 5000Kb
    private int portRx = 8888;
    private int portTx = 8889;
    private String address = "224.0.0.1";
    private MulticastSocket multicastSocket = null;
    private UdpCallback call;
    private boolean threadEn = false;

    MulticastServer(UdpCallback call) {
        this.call = call;
    }

    void start(String address, int portRx, int portTx) {
        this.address = address;
        this.portRx = portRx;
        this.portTx = portTx;
        receiveMessage();
        sendMessage();
    }

    private void stop() {
        threadEn = false;
        if (multicastSocket != null) {
            try {
                InetAddress group = InetAddress.getByName(address);
                multicastSocket.leaveGroup(group);
            } catch (IOException e) {
                e.printStackTrace();
            }
            multicastSocket.disconnect();
            multicastSocket.close();
        }
        Log.d(TAG, "MulticastServer Stop:" + portRx);
    }

    void send(final byte[] data, final int length) {

        new Thread() {
            public void run() {
                try {
                    onSend(portTx, data, length);
                    Log.i("socket","指令发送");
                } catch (IOException e) {
                    // TODO 自动生成的 catch 块
                    Log.e(TAG, "send error");
                    e.printStackTrace();
                }
            }
        }.start();

    }

    ArrayList<byte[]> sendList = new ArrayList<>();

    private void sendMessage() {

        new Thread() {
            public void run() {

                while (multicastSocket != null) {
                    if (sendList.size() != 0) {

                        byte[] data = sendList.get(0);
                        sendList.remove(0);
                        int length = data.length;
                        try {
                            InetAddress group = InetAddress.getByName(address);
                            DatagramPacket datagramPacket = new DatagramPacket(data, length, group, portTx);
                            multicastSocket.send(datagramPacket);
                        } catch (IOException e) {
                            // TODO 自动生成的 catch 块
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            // TODO 自动生成的 catch 块
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.start();
    }

    private void receiveMessage() {
        if (multicastSocket != null) {
            stop();
        }
        new Thread() {
            public void run() {
                byte[] buffer;
                DatagramPacket datagramPacket;
                Log.d(TAG, "MulticastServer Start:" + portRx);
                try {
                    InetAddress group = InetAddress.getByName(address);
                    multicastSocket = new MulticastSocket(portRx);
                    multicastSocket.setSendBufferSize(MAX_SIZE);
                    multicastSocket.setReceiveBufferSize(MAX_SIZE);
                    multicastSocket.joinGroup(group);
                    buffer = new byte[MAX_SIZE];
                    datagramPacket = new DatagramPacket(buffer, buffer.length);
                    threadEn = true;
                } catch (IOException e) {
                    Log.e(TAG, "MulticastServer Error" + e);
                    e.printStackTrace();
                    return;
                }

                while (threadEn) {
                    try {
                        multicastSocket.receive(datagramPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                    if (datagramPacket.getLength() != 0) {
                        onReceive(datagramPacket.getPort(), datagramPacket.getData(), datagramPacket.getLength());
                    }
                }

                threadEn = false;
                if (multicastSocket != null) {
                    multicastSocket.disconnect();
                    multicastSocket.close();
                    multicastSocket = null;
                }
                Log.d(TAG, "MulticastServer Thread Stop:" + portRx);
            }
        }.start();
    }

    private int UDP_SEND_MAX = (62 * 1024);
    private int UDP_HEAD_MAX = (20);

    private int byteArrayToInt(byte[] b, int offset) {
        return (b[offset + 3] & 0xFF) | ((b[offset + 2] & 0xFF) << 8) | ((b[offset + 1] & 0xFF) << 16) | ((b[offset + 0] & 0xFF) << 24);
    }

    private void intTobyteArray(byte[] b, int offset, int val) {
        b[offset] = (byte) ((val >> 24) & 0xFF);
        b[offset + 1] = (byte) ((val >> 16) & 0xFF);
        b[offset + 2] = (byte) ((val >> 8) & 0xFF);
        b[offset + 3] = (byte) (val & 0xFF);
    }

    private byte[] startHead(int length, int num) {

        byte[] head = new byte[UDP_HEAD_MAX];

        head[0] = (byte) 0xAA;
        head[1] = (byte) 0xEE;
        intTobyteArray(head, 2, length);
        intTobyteArray(head, 6, num);

        return head;
    }

    private void onSend(int port, byte[] data, int length) throws IOException {
        InetAddress group = InetAddress.getByName(address);
        if (length < UDP_SEND_MAX) {
            DatagramPacket datagramPacket = new DatagramPacket(data, length, group, port);
            multicastSocket.send(datagramPacket);
        } else {
            int num = (length / UDP_SEND_MAX);
            int rema = (length % UDP_SEND_MAX);
            byte[] head = startHead(length, num);

            //发送数据头
            //Log.d(TAG, "发送数据头" + ", length:" + length + ", num:" + num + ", rema:" + rema);

            DatagramPacket datagramPacket = new DatagramPacket(head, head.length, group, port);
            multicastSocket.send(datagramPacket);

            for (int i = 0; i < num; i++) {
                datagramPacket.setData(data, (UDP_SEND_MAX * i), UDP_SEND_MAX);
                multicastSocket.send(datagramPacket);
            }
            if (rema != 0) {
                datagramPacket.setData(data, (UDP_SEND_MAX * num), rema);
                multicastSocket.send(datagramPacket);
            }
        }
    }

    private byte[] longData = null;
    private int recvLength = 0;

    private boolean onReceiveLongData(int port, byte[] data, int length) {
        if (length == UDP_HEAD_MAX) {
            if (data[0] == (byte) 0xAA && data[1] == (byte) 0xEE) {
                //开始接收长数据
                int recv = byteArrayToInt(data, 2);
                longData = new byte[recv];
                recvLength = 0;
                //Log.d(TAG, "开始接收长数据  = " + recv);
                return true;
            } else if (data[0] == (byte) 0xAA && data[1] == (byte) 0xEF) {
                //结束接收长数据
                if (recvLength == longData.length) {
                    //Log.d(TAG, "数据接收成功,长度相等 = " + recvLength);
                    call.onReceive(port, longData, longData.length);
                } else {
                    Log.e(TAG, "数据接收失败,长度不相等！");
                }
                longData = null;
                return true;
            }
        }

        if (longData != null && (recvLength + length) <= longData.length) {
            System.arraycopy(data, 0, longData, recvLength, length);
            recvLength += length;
            //Log.d(TAG, "接收长数据中  = " + recvLength);

            if (recvLength == longData.length) {
                //Log.d(TAG, "数据接收成功,长度相等 = " + recvLength);
                call.onReceive(port, longData, longData.length);
            }

            return true;
        } else if (longData != null) {
            recvLength = 0;
            longData = null;
            //Log.e(TAG, "数据接收失败,长度过大！");
        }
        return false;
    }

    private void onReceive(int port, byte[] data, int length) {
        if (!onReceiveLongData(port, data, length)) {
            call.onReceive(port, data, length);
        }
    }
}
