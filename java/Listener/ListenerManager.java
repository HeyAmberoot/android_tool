package com.cuanbo.cb_iot.Tool;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by xww on 2018/5/15.
 */

public class ListenerManager {
    /**
     * 单例模式
     */
    public static ListenerManager listenerManager;

    /**
     * 注册的接口集合，发送广播的时候都能收到
     */
    private List<MyListener> myListeners = new CopyOnWriteArrayList<MyListener>();

    /**
     * 获得单例对象
     */
    public static ListenerManager getInstance()
    {
        if(listenerManager == null)
        {
            listenerManager = new ListenerManager();
        }
        return listenerManager;
    }

    /**
     * 注册监听
     */
    public void registerListtener(MyListener iListener)
    {
        myListeners.add(iListener);
    }

    /**
     * 注销监听
     */
    public void unRegisterListener(MyListener iListener)
    {
        if(myListeners.contains(iListener))
        {
            myListeners.remove(iListener);
        }
    }

    /**
     * 发送广播
     */
    public void sendBroadCast(String name,String data)
    {
        for (MyListener iListener : myListeners)
        {
            iListener.receiveNotification(name,data);
        }
    }
}
