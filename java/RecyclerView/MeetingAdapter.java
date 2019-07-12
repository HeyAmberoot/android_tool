package com.cuanbo.cb_iot.View.Presentation.Adapter;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.cuanbo.cb_iot.Controller.TCP.SocketSendThread;
import com.cuanbo.cb_iot.MainActivity;
import com.cuanbo.cb_iot.Model.LoginUser;
import com.cuanbo.cb_iot.Model.Meeting;
import com.cuanbo.cb_iot.Model.MeetingRoom;
import com.cuanbo.cb_iot.Model.SQL.ReadSQL;
import com.cuanbo.cb_iot.Model.SQL.UpdateSQL;
import com.cuanbo.cb_iot.R;
import com.cuanbo.cb_iot.Tool.Const;
import com.cuanbo.cb_iot.Tool.ListenerManager;
import com.cuanbo.cb_iot.Tool.MyListener;
import com.cuanbo.cb_iot.Tool.Utility;
import com.cuanbo.cb_iot.View.Presentation.AddAppointmentActivity;

import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.List;

/**
 * Created by xww on 18/5/4.
 */

public class MeetingAdapter extends RecyclerView.Adapter<MeetingAdapter.ViewHolder> implements MyListener {

    private Context context;
    private List<Meeting> meetingList;
    private Utility utility = new Utility();
    private String MeetingStaffsSet;

    //利用结构函数传入数据
    public MeetingAdapter(List<Meeting> meetingList) {
        ListenerManager.getInstance().registerListtener(this);
        this.meetingList = meetingList;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //获取context
        if (context == null) {
            context = parent.getContext();
        }
        //绑定RecyclerView的子布局即item_list.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_meeting,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        //根据position获取相应的数据
        final Meeting meeting = meetingList.get(position);
        //给RecyclerView子布局中的控件设定相应的数据
        holder.textMeetingName.setText(meeting.getMeetingName());
        holder.textAttendNumber.setText(meeting.getAttendMeetingNumber()+"");
        holder.textMeetingRoom.setText(meeting.getMeetingRoom());
        String time = meeting.getMeetingStartTime()+" ~ "+meeting.getMeetingEndTime();
        holder.textMeetingDate.setText(time);
        String metState = "即将开始";
        switch (meeting.getMetState()) {
            case "0":
                metState = "即将开始";
                holder.textState.setTextColor(Color.BLUE);
                holder.btnAddPerson.setVisibility(View.VISIBLE);
                holder.btnCancelAppointment.setText("取消预约");
                holder.btnChangeAppointment.setVisibility(View.VISIBLE);
                holder.btnEndMeeting.setVisibility(View.GONE);
                break;
            case "1":
                metState = "正在开会";
                holder.textState.setTextColor(Color.GREEN);
                holder.btnAddPerson.setVisibility(View.INVISIBLE);
                holder.btnCancelAppointment.setVisibility(View.INVISIBLE);
                holder.btnChangeAppointment.setVisibility(View.INVISIBLE);
                holder.btnEndMeeting.setVisibility(View.VISIBLE);
                break;
            case "2":
                metState = "会议结束";
                holder.textState.setTextColor(Color.RED);
                holder.btnAddPerson.setVisibility(View.INVISIBLE);
                holder.btnCancelAppointment.setText("删除会议");
                holder.btnChangeAppointment.setVisibility(View.INVISIBLE);
                holder.btnEndMeeting.setVisibility(View.GONE);

                break;
            case "3":
                metState = "会议终止";
                holder.btnEndMeeting.setVisibility(View.GONE);
                break;
        }
        holder.textState.setText(metState);
        int number = meeting.getAttendMeetingNumber();
        holder.textAttendNumber.setText("与会人数 "+String.valueOf(number));
        //与会人员名单
        holder.btnAttendList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String lis = meeting.getAttendMeetingPeople();
//                Snackbar.make(v, lis, Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

                utility.creatMeetingStaffsDialog(context,position);

            }
        });
        //修改预约
        holder.btnChangeAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListenerManager.listenerManager.sendBroadCast(Const.MODIFY_APPOINTMENT,meeting.getMeetingName());
            }
        });

        //取消预约
        final String finalMetState = metState;
        holder.btnCancelAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (finalMetState.equals("会议结束")) {
                    utility.showQuestDilog(context,"确定要删除会议-"+meeting.getMeetingName()+"-吗？",meeting.getMeetingName());

                }else {
                    utility.showQuestDilog(context,"确定要取消预约-"+meeting.getMeetingName()+"-吗？",meeting.getMeetingName());

                }

            }
        });

        //添加人员
        holder.btnAddPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                creatAllStaffsDialog(context,meeting.getMeetingName(),meeting.getMeetingRoom());
            }
        });
        //会议记录
        holder.btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListenerManager.getInstance().sendBroadCast(Const.OPEN_CHAT_ACTIVITY,meeting.getMeetingName());
            }
        });
        //结束会议
        holder.btnEndMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListenerManager.getInstance().sendBroadCast(Const.END_MEETING,meeting.getMeetingName());

//                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                Date date = new Date(System.currentTimeMillis());
//                String curDateStr = simpleDateFormat.format(date);
//
//                //修改表中数据
//                String sql = "UPDATE MeetingList SET MetState = '2', MetEndTime = '"+curDateStr+"' WHERE MetName = '" + meeting.getMeetingName() + "'";
//                new Thread(new UpdateSQL(sql, "状态改变" + 2 + meeting.getMeetingName())).start();
//                if (MainActivity.running) {
//                    SocketSendThread.writing = true;
//                    SocketSendThread.code = ">MetState:" + meeting.getMeetingName() + ":2";
//                }
//                new Thread(new ReadSQL(Const.TABLE_MEETING_LIST)).start();
            }
        });
    }

    @Override
    public int getItemCount() {
        return meetingList.size();
    }

    @Override
    public void receiveNotification(String name, String data) {
        switch (name) {
            case Const.MEETING_USER_SET:
                if (data != null) {
                    MeetingStaffsSet = data.substring(0, data.length() - 1);
                }
                break;

        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView textMeetingName;
        TextView textMeetingDate;
        TextView textMeetingRoom;
        TextView textState;
        TextView textAttendNumber;
        Button btnChangeAppointment;
        Button btnCancelAppointment;
        Button btnAddPerson;
        Button btnAttendList;
        Button btnRecord;
        Button btnEndMeeting;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            textMeetingName = itemView.findViewById(R.id.text_meetingName);
            textMeetingDate = itemView.findViewById(R.id.text_meetingTime);
            textMeetingRoom = itemView.findViewById(R.id.text_meetingRoom);
            textState = itemView.findViewById(R.id.text_meetingState);
            textAttendNumber = itemView.findViewById(R.id.text_number);
            btnChangeAppointment = itemView.findViewById(R.id.btn_changeAppointment);
            btnCancelAppointment = itemView.findViewById(R.id.btn_cancelMeeting);
            btnAddPerson = itemView.findViewById(R.id.btnAdd);
            btnAttendList = itemView.findViewById(R.id.btn_attendList);
            btnRecord = itemView.findViewById(R.id.btn_record);
            btnEndMeeting = itemView.findViewById(R.id.btn_endMeeting);

        }

    }

    private void creatAllStaffsDialog(Context context, final String meetingName,final String meetingRoomSet) {
        List<LoginUser> userList = new ArrayList<>();
        Dialog dialog;
        dialog = new android.app.AlertDialog.Builder(context).create();
        dialog.show();

        Window window = dialog.getWindow();
        window.setContentView(R.layout.layout_staffs);
        userList.clear();
        //读取本地数据库litePal数据库LoginUser数据
        List<LoginUser> users = DataSupport.findAll(LoginUser.class);
        userList.addAll(users);
        //初始化RecyclerView
        RecyclerView recyclerView = window.findViewById(R.id.recycler_staffs);
        GridLayoutManager layoutManager = new GridLayoutManager(context,4);
        recyclerView.setLayoutManager(layoutManager);
        //获取已经选择的与会人员名单
        List<Meeting> meetingList = DataSupport.findAll(Meeting.class);
        String[] StaffSetList = new String[0];
        for (Meeting meeting : meetingList) {
            if (meetingName.equals(meeting.getMeetingName())) {
                StaffSetList = meeting.getAttendMeetingPeople().split(":");
            }
        }

        //利用adapter的构造函数传递数据
        StaffsAdapter adapterStaffs = new StaffsAdapter(userList,StaffSetList);
        recyclerView.setAdapter(adapterStaffs);
        recyclerView.setNestedScrollingEnabled(false);

        window.setGravity(Gravity.CENTER);//设置dialog显示的位置居中
        //window.setWindowAnimations(R.style.alpha_anim);//添加动画效果
        //设置对话框背景透明，AlertDialog无效，才有效
        window.setBackgroundDrawableResource(R.color.HalfTransparent);

        //设置对话框的宽度和高度
        Utility utility = new Utility();
        int height = utility.getScreenHeight();
        int width = utility.getScreenWidth();
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();  //获取对话框当前的参数值
        p.width = (width/3)*2;
        p.height = (height/3)*2;
        dialog.setCanceledOnTouchOutside(true);// 设置点击屏幕Dialog消失
        dialog.getWindow().setAttributes(p);//设置生效

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (MeetingStaffsSet != null) {
                    String[] staffs = MeetingStaffsSet.split(":");
                    List<MeetingRoom> meetingRooms = DataSupport.findAll(MeetingRoom.class);
                    int staffsNumSet = staffs.length;
                    for (MeetingRoom meetingRoom : meetingRooms) {
                        String roomName = meetingRoom.getRoomName();
                        if (roomName.equals(meetingRoomSet)) {
                            int maxMeetingNum = meetingRoom.getUserNumber();
                            if (staffsNumSet > maxMeetingNum) {
                                ListenerManager.listenerManager.sendBroadCast(Const.STAFFS_NUMBER_OUT_RANGE,null);

                                return;
                            }
                        }

                    }

                    //修改表中数据
                    String sql = "UPDATE MeetingList SET MetUsers = '"+MeetingStaffsSet+"', MetNumber = '"+staffs.length+"' WHERE MetName = '"+meetingName+"'";
                    new Thread(new UpdateSQL(sql,"修改人员")).start();
                    MeetingStaffsSet = null;
                }


            }
        });

    }


}
