package com.cuanbo.cb_iot.View.Presentation;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cuanbo.cb_iot.Controller.TCP.SocketSendThread;
import com.cuanbo.cb_iot.MainActivity;
import com.cuanbo.cb_iot.Model.LocalUserMsg;
import com.cuanbo.cb_iot.Model.Meeting;
import com.cuanbo.cb_iot.Model.SQL.ReadSQL;
import com.cuanbo.cb_iot.Model.SQL.UpdateSQL;
import com.cuanbo.cb_iot.R;
import com.cuanbo.cb_iot.Tool.Const;
import com.cuanbo.cb_iot.Tool.ImageUtil;
import com.cuanbo.cb_iot.Tool.ListenerManager;
import com.cuanbo.cb_iot.Tool.MyListener;
import com.cuanbo.cb_iot.Tool.Utility;
import com.cuanbo.cb_iot.View.Presentation.Adapter.MeetingAdapter;
import com.cuanbo.cb_iot.View.activityUtil.ActivityCollector;
import com.cuanbo.cb_iot.View.activityUtil.BaseActivity;

import org.litepal.crud.DataSupport;

import java.sql.Time;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MeetingListActivity extends BaseActivity  implements MyListener {

    private MeetingAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Utility utility = new Utility();
    private List<Meeting> meetingList = new ArrayList<>();
    private Timer timer = new Timer();
    private String Tag = "MeetingListActivity";
//    private NotificationManager notificationManager;
    /**
     * 会议列表查看选择
     */
    private String meetingListType = "ALL";
    /**
     * 会议数据
     */
    public static List<Meeting> meetingList_temp = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_list);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //获取Toolbar布局
        Toolbar toolbar = findViewById(R.id.toolbar);
        //方法将Toolbar实例传入
        setSupportActionBar(toolbar);
        //添加系统返回按钮在toolbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //注册监听器
        ListenerManager.getInstance().registerListtener(this);
        initFloatBtn();
        initMeetings();
        initRecyclerView();
        initSwipeRefreshLayout();
        initTimer();
    }



    private void initFloatBtn() {
        //悬浮按钮
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "会议已经删除", Snackbar.LENGTH_LONG)
//                        .setAction("撤销", new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                Toast.makeText(MeetingListActivity.this,"撤回",Toast.LENGTH_SHORT).show();
//                            }
//                        }).show();
                Intent intent = new Intent(MeetingListActivity.this,AddAppointmentActivity.class);
                startActivity(intent);
                if (timer != null) {
                    timer.cancel();
                    timer = new Timer();
                }

            }
        });

        FloatingActionButton fabState = findViewById(R.id.fab_state);
        fabState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMeetingStateDialog();
            }
        });

    }

    private void showMeetingStateDialog() {
        final Dialog dialog;
        dialog = new android.app.AlertDialog.Builder(MeetingListActivity.this).create();
        dialog.show();

        Window window = dialog.getWindow();
        window.setContentView(R.layout.layout_meeting_state);
        window.setGravity(Gravity.CENTER);//设置dialog显示的位置居中
        //window.setWindowAnimations(R.style.alpha_anim);//添加动画效果
        //设置对话框背景透明，AlertDialog无效，才有效
        window.setBackgroundDrawableResource(R.color.FullTransparent);

        //设置对话框的宽度和高度
        int height = utility.getScreenHeight();
        int width = utility.getScreenWidth();
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();  //获取对话框当前的参数值
        p.width = width/2;
        p.height = height/4;
        dialog.setCanceledOnTouchOutside(true);// 设置点击屏幕Dialog不消失
        dialog.getWindow().setAttributes(p);//设置生效

        //只看今天的会议
        Button btnTodayMeeting=window.findViewById(R.id.btn_today_meeting);
        btnTodayMeeting.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                meetingListType = "TODAY";
                showTodayMeeting();
                dialog.dismiss();
            }
        });
        //查看所有存在过的会议
        Button btnAllMeeting = window.findViewById(R.id.btn_all_meeting);
        btnAllMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                meetingListType = "ALL";
                //获取用户权限
                SharedPreferences project = getSharedPreferences("USER", Context.MODE_PRIVATE);
                String localUserPerlevel = project.getString("LocalUserPerlevel","");
                if (!localUserPerlevel.equals(Const.USER_LEVERLS_MANAGER)) {
                    showMyMeeting();
                }else {
                    initMeetings();
                }
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        //不看已经结束的会议
        Button btnLiveMeeting = window.findViewById(R.id.btn_live_meeting);
        btnLiveMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                meetingListType = "USING";
                showUseMeeting();
                dialog.dismiss();
            }
        });
        //只看我的会议
        Button btnMeMeeting = window.findViewById(R.id.btn_me_meeting);
        btnMeMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                meetingListType = "MY";
                showMyMeeting();
                dialog.dismiss();
            }
        });
        //获取
        SharedPreferences project = getSharedPreferences("USER", Context.MODE_PRIVATE);
        String localUserPerlevel = project.getString("LocalUserPerlevel","");
        if (!localUserPerlevel.equals(Const.USER_LEVERLS_MANAGER)) {
            btnMeMeeting.setVisibility(View.GONE);
        }else {
            btnMeMeeting.setVisibility(View.VISIBLE);
        }
    }

    //不看已经结束的会议
    private void showUseMeeting() {
        List<Meeting> meetings = DataSupport.findAll(Meeting.class);
        List<Meeting> meetings_temp = new ArrayList<>();
        meetingList.clear();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        //获取用户权限
        SharedPreferences project = getSharedPreferences("USER", Context.MODE_PRIVATE);
        String localUserPerlevel = project.getString("LocalUserPerlevel","");
        for (Meeting meeting : meetings) {
            //非管理员用户自能看自己有参与的会议
            if (!localUserPerlevel.equals(Const.USER_LEVERLS_MANAGER)) {
                LocalUserMsg localUser = DataSupport.findAll(LocalUserMsg.class).get(0);
                String staffs = meeting.getAttendMeetingPeople();
                String[] staffsList = staffs.split(":");
                boolean isMyMeeting = false;
                for (String staff : staffsList) {
                    if (staff.equals(localUser.getUserName())) {
                        isMyMeeting = true;
                        break;
                    }
                }
                if (!isMyMeeting) {
                    continue;
                }
            }
            //查看还没结束的会议
            String endTime = meeting.getMeetingEndTime();
            Date meetingEndDate = new Date();
            try {
                meetingEndDate = format.parse(endTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (curDate.before(meetingEndDate)) {
                meetings_temp.add(meeting);
            }
        }
        meetingList_temp = OrderListByTime(meetings_temp);
        meetingList.addAll(meetingList_temp);
        adapter.notifyDataSetChanged();
    }

    private void showMyMeeting() {
        List<Meeting> meetings = DataSupport.findAll(Meeting.class);
        LocalUserMsg localUser = DataSupport.findAll(LocalUserMsg.class).get(0);
        List<Meeting> meetings_temp = new ArrayList<>();
        meetingList.clear();
        for (Meeting meeting : meetings) {
            String staffs = meeting.getAttendMeetingPeople();
            String[] staffsList = staffs.split(":");
            for (String staff: staffsList) {
                if (staff.equals(localUser.getUserName())) {
                    meetings_temp.add(meeting);
                }
            }
        }
        meetingList_temp = OrderListByTime(meetings_temp);
        meetingList.addAll(meetingList_temp);
        adapter.notifyDataSetChanged();
    }

    private void showTodayMeeting() {
        List<Meeting> meetings = DataSupport.findAll(Meeting.class);
        List<Meeting> meetings_temp = new ArrayList<>();
        meetingList.clear();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String[] strCurDate = curDate.toString().split(" ");
        String curDay = strCurDate[2];
        String curMonth = strCurDate[1];
        String curYear = strCurDate[5];
        //获取用户权限
        SharedPreferences project = getSharedPreferences("USER", Context.MODE_PRIVATE);
        String localUserPerlevel = project.getString("LocalUserPerlevel","");

        for (Meeting meeting:meetings) {
            //非管理员用户自能看自己有参与的会议
            if (!localUserPerlevel.equals(Const.USER_LEVERLS_MANAGER)) {
                LocalUserMsg localUser = DataSupport.findAll(LocalUserMsg.class).get(0);
                String staffs = meeting.getAttendMeetingPeople();
                String[] staffsList = staffs.split(":");
                boolean isMyMeeting = false;
                for (String staff : staffsList) {
                    if (staff.equals(localUser.getUserName())) {
                        isMyMeeting = true;
                        break;
                    }
                }
                if (!isMyMeeting) {
                    continue;
                }
            }
            //只显示今天的会议
            String startTime = meeting.getMeetingStartTime();
            String endTime = meeting.getMeetingEndTime();
            Date meetingStartDate = new Date();
            Date meetingEndDate = new Date();
            try {
                meetingStartDate = format.parse(startTime);
                meetingEndDate = format.parse(endTime);
            }catch (ParseException e) {
                e.printStackTrace();
            }
            String[] strStartTime = meetingStartDate.toString().split(" ");
            String StartDay = strStartTime[2];
            String StartMonth = strStartTime[1];
            String StartYear = strStartTime[5];
            String[] strEndTime = meetingEndDate.toString().split(" ");
            String EndDay = strEndTime[2];
            //会议开始时间是今年这个月
            if (StartYear.equals(curYear) && StartMonth.equals(curMonth)) {
                //会议开始时间是今天
                if (StartDay.equals(curDay)) {
                    meetings_temp.add(meeting);
                }else if (curDate.after(meetingStartDate) && curDate.before(meetingEndDate)) {
                    meetings_temp.add(meeting);
                }else if (EndDay.equals(curDay)) {
                    meetings_temp.add(meeting);
                }
            }

        }
        if (meetings_temp.size() != 0) {
            meetingList_temp = OrderListByTime(meetings_temp);
            meetingList.addAll(meetingList_temp);
            adapter.notifyDataSetChanged();
        }else {
            utility.showAlertDialog(MeetingListActivity.this,"今天暂时还没有任何会议。");
        }
    }

    private void showUserDetailDialog() {
        final Dialog dialog;
        dialog = new android.app.AlertDialog.Builder(MeetingListActivity.this).create();
        dialog.show();

        Window window = dialog.getWindow();
        window.setContentView(R.layout.layout_user);
        window.setGravity(Gravity.CENTER);//设置dialog显示的位置居中
        //window.setWindowAnimations(R.style.alpha_anim);//添加动画效果
        //设置对话框背景透明，AlertDialog无效，才有效
        window.setBackgroundDrawableResource(R.color.FullTransparent);

        //设置对话框的宽度和高度
//        int height = utility.getScreenHeight();
//        int width = utility.getScreenWidth();
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();  //获取对话框当前的参数值
//        p.width = 400;
//        p.height = 600;
        dialog.setCanceledOnTouchOutside(true);// 设置点击屏幕Dialog不消失
        dialog.getWindow().setAttributes(p);//设置生效

        TextView textUserName = window.findViewById(R.id.text_user_name);
        TextView textUserPerlevels = window.findViewById(R.id.text_user_perlevel);
        TextView textUserGender = window.findViewById(R.id.text_user_gender);
        TextView textUserAge = window.findViewById(R.id.text_user_age);
        TextView textAddr = window.findViewById(R.id.text_user_address);
        TextView textDescripe = window.findViewById(R.id.text_user_descripe);
        ImageView imageUserIco = window.findViewById(R.id.image_user_ico);

        LocalUserMsg localUser = DataSupport.findAll(LocalUserMsg.class).get(0);//DataSupport.find(LocalUserMsg.class,0);

        String strIco = localUser.getIco();
        Bitmap userIco = ImageUtil.getInstance().StringToBitmap(strIco);
        imageUserIco.setImageBitmap(userIco);

        textUserName.setText(localUser.getUserName());
        //获取用户权限
        SharedPreferences project = getSharedPreferences("USER", Context.MODE_PRIVATE);
        String localUserPerlevel = project.getString("LocalUserPerlevel","");
        textUserPerlevels.setText(localUserPerlevel);
        textUserAge.setText(localUser.getAge()+"岁");
        String gender = localUser.getSex();
        if (gender.equals("M")) {
            gender = "男";
        }else {
            gender = "女";
        }
        textUserGender.setText(gender);
        textAddr.setText(localUser.getAddr());
        textDescripe.setText(localUser.getDescripe());

        Button btnSwitchUser = window.findViewById(R.id.btn_swith_user);
        btnSwitchUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(MeetingListActivity.this,LoginActivity.class);
                intent.putExtra("SwitchUser","true");
                startActivity(intent);

            }
        });


    }

    private void initTimer() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date curDate = new Date(System.currentTimeMillis());//获取当前时间
                for (Meeting meeting : meetingList) {
                    String startTime = meeting.getMeetingStartTime();
                    String endTime = meeting.getMeetingEndTime();
                    String oldState = meeting.getMetState();
                    Date meetingStartDate = new Date();
                    Date meetingEndDate = new Date();
                    try {
                        meetingStartDate = format.parse(startTime);
                        meetingEndDate = format.parse(endTime);
                    }catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String MetState = Const.MEETING_HAVE_NOT_BEGUN;
                    final String MetName = meeting.getMeetingName();
                    if (curDate.before(meetingStartDate)) {
                        MetState = Const.MEETING_HAVE_NOT_BEGUN;
                        long diff = meetingStartDate.getTime() - curDate.getTime();//这样得到的差值是微秒级别
                        long days = diff / (1000 * 60 * 60 * 24);
                        long hours = (diff-days*(1000 * 60 * 60 * 24))/(1000* 60 * 60);
                        long minutes = (diff-days*(1000 * 60 * 60 * 24)-hours*(1000* 60 * 60))/(1000* 60);
                        //距离会议开始还有10分钟
                        if (days == 0 && hours == 0 && minutes == 10) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    utility.showAlertDialog(MeetingListActivity.this,"距离会议-"+MetName+"-开始还有10分钟！");
//                                    postNotification("您有会议快要开始了！","距离会议-"+MetName+"-开始还有10分钟！");
                                }
                            });
//                            Toast.makeText(MeetingListActivity.this,"距离会议-"+MetName+"-开始还有10分钟！",Toast.LENGTH_LONG).show();
                        }

                    }else if (curDate.after(meetingStartDate) && curDate.before(meetingEndDate)) {
                        MetState = Const.BE_IN_MEETING;
                    }else if (curDate.after(meetingEndDate)) {
                        MetState = Const.MEETING_FINISH;
                    }
                    if (!oldState.equals(MetState)) {//会议状态改变了才更新表数据
                        //修改表中数据
                        String sql = "UPDATE MeetingList SET MetState = '" + MetState + "' WHERE MetName = '" + MetName + "'";
                        new Thread(new UpdateSQL(sql, "状态改变" + MetState + MetName)).start();
                    }
                }
            }
        },0,36000);

    }

//    public void postNotification(String title,String content) {
//        Notification.Builder builder = new Notification.Builder(MeetingListActivity.this);
//        Intent intent = new Intent(MeetingListActivity.this, MainActivity.class);  //需要跳转指定的页面
//        PendingIntent pendingIntent = PendingIntent.getActivity(MeetingListActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        builder.setContentIntent(pendingIntent);
//        builder.setSmallIcon(R.mipmap.ic_launcher);// 设置图标
//        builder.setContentTitle(title);// 设置通知的标题
//        builder.setContentText(content);// 设置通知的内容
//        builder.setWhen(System.currentTimeMillis());// 设置通知来到的时间
//        builder.setAutoCancel(true); //自己维护通知的消失
//        builder.setTicker("new message");// 第一次提示消失的时候显示在通知栏上的
//        builder.setOngoing(true);
//        builder.setNumber(20);
//
//
//        Notification notification = builder.build();
//        notification.flags = Notification.FLAG_NO_CLEAR;  //只有全部清除时，Notification才会清除
//        notificationManager.notify(0,notification);
//    }

    private void initRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.recycler_meeting);
        GridLayoutManager layoutManager = new GridLayoutManager(this,1);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MeetingAdapter(meetingList);
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);
    }

    private void initMeetings() {
        //获取用户权限
        SharedPreferences project = getSharedPreferences("USER", Context.MODE_PRIVATE);
        String localUserPerlevel = project.getString("LocalUserPerlevel","");
        if (!localUserPerlevel.equals(Const.USER_LEVERLS_MANAGER)) {
            List<Meeting> meetings = DataSupport.findAll(Meeting.class);
            LocalUserMsg localUser = DataSupport.findAll(LocalUserMsg.class).get(0);
            List<Meeting> meetings_temp = new ArrayList<>();
            meetingList.clear();
            for (Meeting meeting : meetings) {
                String staffs = meeting.getAttendMeetingPeople();
                String[] staffsList = staffs.split(":");
                for (String staff: staffsList) {
                    if (staff.equals(localUser.getUserName())) {
                        meetings_temp.add(meeting);
                    }
                }
            }
            meetingList_temp = OrderListByTime(meetings_temp);
            meetingList.addAll(meetingList_temp);
            return;
        }
        meetingList.clear();
        //读取本地数据库litePal数据库
        List<Meeting> meetings = DataSupport.findAll(Meeting.class);
        meetingList_temp = OrderListByTime(meetings);
        meetingList.addAll(meetingList_temp);
        if (meetings.size() == 0) {
            utility.showAlertDialog(MeetingListActivity.this,"还没有任何会议预约，你现在就可以新增预约了！");
        }
    }

    //数据按时间排顺序
    private List<Meeting> OrderListByTime(List<Meeting> meetings){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1;
        Date d2;
        new Meeting();
        Meeting meeting;
        //做一个冒泡排序，大的在数组的前列
        for(int i=0; i<meetings.size()-1; i++){
            for(int j=i+1; j<meetings.size();j++){
                ParsePosition pos1 = new ParsePosition(0);
                ParsePosition pos2 = new ParsePosition(0);
                d1 = dateFormat.parse(meetings.get(i).getMeetingStartTime(), pos1);
                d2 = dateFormat.parse(meetings.get(j).getMeetingStartTime(), pos2);
                if(d1.after(d2)){//如果队前日期靠前，调换顺序
                    meeting = meetings.get(i);
                    meetings.set(i, meetings.get(j));
                    meetings.set(j, meeting);
                }
            }
        }
        return meetings;
        }

    private void initSwipeRefreshLayout(){
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        //设置下拉刷新进度条的颜色
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new ReadSQL("MeetingList")).start();

            }
        });
    }


    //给Toolbar绑定菜单按钮XML文件
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar,menu);
        return true;
    }

    //处理Toolbar上按钮的点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            //系统返回按钮ID
            case android.R.id.home:
//                //MainActivity设置为singleTask启动方法。当activity为singleTask的时候跳转会清空当前activity任务栈上面所有的activity。
//                Intent in = new Intent(MeetingListActivity.this,MainActivity.class);
//                startActivity(in);
//
//                return true;
            case R.id.item_home:
                //MainActivity设置为singleTask启动方法。当activity为singleTask的时候跳转会清空当前activity任务栈上面所有的activity。
                Intent i = new Intent(MeetingListActivity.this,MainActivity.class);
                startActivity(i);

                return true;
            case R.id.item_me:
                showUserDetailDialog();

                return true;


                default:

                    return super.onOptionsItemSelected(item);
        }

    }



    @Override
    public void receiveNotification(final String name, final String data) {
        //根据当前的activity判断是否需要根据通知做出反应
//        int num = ActivityCollector.activities.size()-1;
//        String activityName = ActivityCollector.activities.get(num).getClass().getSimpleName();//getLocalClassName();
//        if (!activityName.equals("MeetingListActivity")) {
//            return;
//        }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //读取SQL数据成功
                    if (name.equals(Const.READ_SQL_SUCCESS)) {
                        switch (meetingListType) {
                            case "TODAY":
                                showTodayMeeting();
                                break;
                            case "ALL":
                                initMeetings();
                                adapter.notifyDataSetChanged();
                                break;
                            case "USING":
                                showUseMeeting();
                                break;
                            case "MY":
                                showMyMeeting();
                                break;
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    switch (name) {
                        case Const.READ_SQL_FAIL:
                            Toast.makeText(MeetingListActivity.this,"刷新失败",Toast.LENGTH_LONG).show();
                            Log.e(Tag,"读取数据库失败!");
                            swipeRefreshLayout.setRefreshing(false);
                            break;
                        case Const.UPDATE_SQL_FAIL:
                            if (data.equals("修改人员")) {
                                Toast.makeText(MeetingListActivity.this, "更改参会人员失败！", Toast.LENGTH_LONG).show();
                            }
                            break;
                        case Const.UPDATE_SQL_SUCCESS:
                            if (data.equals("修改人员")) {
                                Toast.makeText(MeetingListActivity.this, "更改参会人员成功！", Toast.LENGTH_LONG).show();
                                new Thread(new ReadSQL("MeetingList")).start();
                            }
                            break;
                        case Const.CONNECT_SQL_FAIL:
                            Toast.makeText(MeetingListActivity.this,"连接服务器失败！",Toast.LENGTH_LONG).show();
                            Log.e(Tag,"连接数据库失败!");
                            break;
                        case Const.STAFFS_NUMBER_OUT_RANGE:
                            Toast.makeText(MeetingListActivity.this,"设定的参会人数超出选定会议室容量！",Toast.LENGTH_LONG).show();
                            break;
                        case Const.MODIFY_APPOINTMENT:
                            Intent intent = new Intent(MeetingListActivity.this,AddAppointmentActivity.class);
                            intent.putExtra("modifyAppointment",data);
                            startActivity(intent);
                            break;
                        case Const.ADD_APPOINTMENT_ACTIVITY_DESTROY:

                            initTimer();
                            break;
                        case Const.OPEN_CHAT_ACTIVITY:
                            Intent intentChat = new Intent(MeetingListActivity.this,ChatActivity.class);
                            intentChat.putExtra("chatMeetingName",data);
                            startActivity(intentChat);
                            break;
                        case Const.END_MEETING:
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date date = new Date(System.currentTimeMillis());
                            String curDateStr = simpleDateFormat.format(date);
                            //修改表中数据
                            String sql = "UPDATE MeetingList SET MetState = '2', MetEndTime = '"+curDateStr+"' WHERE MetName = '" + data + "'";
                            new Thread(new UpdateSQL(sql, "状态改变" + 2 + data)).start();
                            if (MainActivity.running) {
                                SocketSendThread.writing = true;
                                SocketSendThread.code = ">MetState:" + data + ":2";
                            }
                            //更新列表
                            new Thread(new ReadSQL(Const.TABLE_MEETING_LIST)).start();
                            break;
                        case Const.SOCKET_DISCONNECT:
                            utility.showAlertDialog(MeetingListActivity.this,Const.SERVICE_DISCONNECT);
                            Log.e(Tag,"服务端Socket断开连接!");
//                            Intent intentMain = new Intent(MeetingListActivity.this,MainActivity.class);
//                            startActivity(intentMain);
                            break;
                        case Const.REFRESH_MEETING_LIST:
                            //更新列表
                            new Thread(new ReadSQL(Const.TABLE_MEETING_LIST)).start();
                            break;

                    }
                }
            });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ListenerManager.getInstance().unRegisterListener(this);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
