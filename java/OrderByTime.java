package com.cuanbo.cb_iot.Tool;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;


import com.cuanbo.cb_iot.Model.LoginUser;
import com.cuanbo.cb_iot.Model.Meeting;
import com.cuanbo.cb_iot.Model.MeetingSetting;
import com.cuanbo.cb_iot.Model.SQL.ReadSQL;
import com.cuanbo.cb_iot.R;
import com.cuanbo.cb_iot.View.Presentation.Adapter.StaffsAdapter;

import org.litepal.crud.DataSupport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * Created by xww on 18/3/23.
 */

//数据按时间排顺序
private List<Meeting> invertOrderList(List<Meeting> meetings){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1;
        Date d2;
        Meeting meeting = new Meeting();
        //做一个冒泡排序，大的在数组的前列
        for(int i=0; i<meetings.size()-1; i++){
        for(int j=i+1; j<meetings.size();j++){
        ParsePosition pos1 = new ParsePosition(0);
        ParsePosition pos2 = new ParsePosition(0);
        d1 = sdf.parse(meetings.get(i).getMeetingStartTime(), pos1);
        d2 = sdf.parse(meetings.get(j).getMeetingStartTime(), pos2);
        if(d1.after(d2)){//如果队前日期靠前，调换顺序
        meeting = meetings.get(i);
        meetings.set(i, meetings.get(j));
        meetings.set(j, meeting);
        }
        }
        }
        return meetings;
}