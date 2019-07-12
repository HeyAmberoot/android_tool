package com.cuanbo.signalswitch;

/**
 * Created by xww on 2019/7/8.
 */

public interface UdpCallback {
    public void onReceive(int port, byte[] data, int length);
}
