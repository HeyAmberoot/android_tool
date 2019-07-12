package com.example.cuanbo.Tool;

import android.os.Environment;

/**
 * Created by xww on 18/3/20.
 */

public class Const {
    /**
     * "android.net.conn.CONNECTIVITY_CHANGE"
     */
    public final static String networkChange_action = "android.net.conn.CONNECTIVITY_CHANGE";
    /**
     * Path = "/Android/data/com.example.cuanbo.easycontrol/files/Documents"
     */
    public final static String path = MyApplication.getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath
    public final static String pathC = Environment.getExternalStorageDirectory().getPath()+"/Cuanbo";


    public final static int ConnectFail = 3;
    public final static int ServiceDisconnect = 2;
    public final static int ConnectSuccess = 0;
    public final static int TransducerConnecFail = 1;
    public final static int StartDownloadProject = 4;
    public final static int DownloadProjectBreakOff = 5;
    public final static int EndDownloadProject = 6;
    
    public final static String DATABASE_USER_NAME = "sa";
    public final static String DATABASE_PASSWORD = "123456";
    public final static String DATABASE_IP = "192.168.88.120";
    public final static String DATABASE_NAME = "SchDBA";
    
    public final static String CONNECT_SQL_FAIL = "connect_sql_fail";
    public final static String CONNECT_SQL_SUCCESS = "connect_sql_success";
    public final static String READ_SQL_SUCCESS = "update_sql_success";
    public final static String READ_SQL_FAIL = "update_sql_fail";
    public final static String UPDATE_SQL_SUCCESS = "update_sql_success";
    public final static String UPDATE_SQL_FAIL = "update_sql_fail";
    
    public final static String SERVICE_DISCONNECT = "service_disconnect";



}
