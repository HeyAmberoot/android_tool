package com.cuanbo.cb_iot.Model.SQL;

import android.app.ProgressDialog;
import android.os.Message;
import android.util.Log;

import com.cuanbo.cb_iot.MainActivity;
import com.cuanbo.cb_iot.Model.LoginUser;
import com.cuanbo.cb_iot.Model.Meeting;
import com.cuanbo.cb_iot.Model.MeetingRoom;
import com.cuanbo.cb_iot.Tool.Const;
import com.cuanbo.cb_iot.Tool.ListenerManager;
import com.cuanbo.cb_iot.View.Presentation.MeetingListActivity;
import com.cuanbo.cb_iot.View.activityUtil.MyApplication;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.Date;

/**
 * Created by xww on 2018/5/10.
 */

public class ReadSQL implements Runnable{

    private String Tag = "ReadSQL";
    private String SqlTable = "MeetingList";

    public ReadSQL(String sqlTable){
        this.SqlTable = sqlTable;
    }

    //连接sql
    @Override
    public void run() {
        //此处关键，数据库的连接需放在子线程中操作
        String userName = "sa";//用户名
        String password = "123456ht";//密码
        Connection con = null;
        try {
            // 使用Class加载驱动程序
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            //连接数据库
            con = DriverManager.getConnection(
                    "jdbc:jtds:sqlserver://192.168.88.112:1433/CB_IOT", userName,
                    password);

            //连接数据库成功
            try {
                //读取数据
                readSqlData(con);
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            //连接数据库失败
            Log.e(Tag,e.getMessage());
            Message message = MainActivity.myHandler.obtainMessage();
            message.what = Const.connectSqlFail;
            MainActivity.myHandler.sendMessage(message);
            //发通知
            ListenerManager.getInstance().sendBroadCast(Const.CONNECT_SQL_FAIL,null);
        }


    }
    //读sql
    private void readSqlData(Connection con) throws java.sql.SQLException {
        try {
            /*SQL指令对大小不敏感*/
            //查询表名为“MeetingList”的所有内容
            String sql = "SELECT * FROM " + SqlTable;

            //创建Statement,操作数据
            Statement state = con.createStatement();

            //读取数据：生成结果集ResultSet////////////
            ResultSet rs = state.executeQuery(sql);

            //在本地数据库中创建表,若表已存在不创建，表不存在才创建
            Connector.getDatabase();

            switch (SqlTable) {
                case "MeetingList":
                    handleMeetingData(rs);
                    break;
                case "MeetingRoom":
                    handleMeetingRoomData(rs);
                    break;
                case "LoginUser":
                    handleUserData(rs);
                    break;
                default:

                        break;
            }

            Message message = MainActivity.myHandler.obtainMessage();
            message.what = Const.readSqlTableDataSucess;
            MainActivity.myHandler.sendMessage(message);
            rs.close();
            state.close();

        } catch (SQLException e) {
            Log.e(Tag,"读取数据库错误："+e.getMessage());
            Message message = MainActivity.myHandler.obtainMessage();
            message.what = Const.readSqlTableDataFail;
            MainActivity.myHandler.sendMessage(message);
            //发通知
            ListenerManager.getInstance().sendBroadCast(Const.READ_SQL_FAIL,null);
        } finally {
            if (con != null)
                try {
                    con.close();  //数据库的关闭
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        }
    }


    private void handleMeetingData(ResultSet resultSet) throws SQLException {
        DataSupport.deleteAll(Meeting.class);
        boolean isSave = false;
        while (resultSet.next()) {
            //获取数据
            String MetName = resultSet.getString("MetName");
            String MetRoom = resultSet.getString("MetRoom");
            Time MetStartTime = resultSet.getTime("MetStartTime");
            Time MetEndTime = resultSet.getTime("MetEndTime");
            Date MetStartData = resultSet.getDate("MetStartTime");
            Date MetEndData = resultSet.getDate("MetEndTime");
            int MetNumber = resultSet.getInt("MetNumber");
            String attendPeople = resultSet.getString("MetUsers");
            String MetState = resultSet.getString("MetState");
            String MetRecord = resultSet.getString("MetRecord");

            //保存数据到本地litePal数据库
            Meeting meeting = new Meeting();
            meeting.setMeetingName(MetName);
            meeting.setMeetingRoom(MetRoom);
            meeting.setMeetingStartTime(MetStartData.toString()+" "+MetStartTime.toString());
            meeting.setMeetingEndTime(MetEndData.toString()+" "+MetEndTime.toString());
//            meeting.setMeetingDate(MetData.toString());
            meeting.setAttendMeetingNumber(MetNumber);
            meeting.setAttendMeetingPeople(attendPeople);
            meeting.setMetRecord(MetRecord);
            meeting.setMetState(MetState);

            isSave = meeting.save();


        }
        if (isSave) {
            Log.i(Tag, "保存数据到本地成功");
            //发通知
            ListenerManager.getInstance().sendBroadCast(Const.READ_SQL_SUCESS,null);
        } else {
            Log.e(Tag, "保存数据到本地失败");
        }

    }

    private void handleMeetingRoomData(ResultSet resultSet) throws SQLException {
        DataSupport.deleteAll(MeetingRoom.class);
            while (resultSet.next()) {
                //获取数据
                String RoomName = resultSet.getString("RoomName");
                String Address = resultSet.getString("Address");
                String DeviceList = resultSet.getString("DeviceList");
                int UserNumber = resultSet.getInt("UserNumber");
                //保存数据到本地litePal数据库
                MeetingRoom meetingRoom = new MeetingRoom();
                meetingRoom.setRoomName(RoomName);
                meetingRoom.setAddress(Address);
                meetingRoom.setDeviceList(DeviceList);
                meetingRoom.setUserNumber(UserNumber);
                boolean isSave = meetingRoom.save();
                if (isSave) {
                    Log.i(Tag, "保存数据到本地成功");
                } else {
                    Log.e(Tag, "保存数据到本地失败");
                }

            }

    }

    private void handleUserData(ResultSet resultSet) throws SQLException {
        //重新保存列表数据前删掉以前的
        DataSupport.deleteAll(LoginUser.class);
            while (resultSet.next()) {
                //获取数据
                String UserName = resultSet.getString("UserName");
                String UserPwd = resultSet.getString("UserPwd");
                String PerLevels = resultSet.getString("PerLevels");
                String Sex = resultSet.getString("Sex");
                int Age = resultSet.getInt("Age");
                String Addr = resultSet.getString("Addr");
                String Descripe = resultSet.getString("Descripe");
                String State = resultSet.getString("State");
                //保存数据到本地litePal数据库
                LoginUser loginUser = new LoginUser();
                loginUser.setUserName(UserName);
                loginUser.setUserPwd(UserPwd);
                loginUser.setPerLevels(PerLevels);
                loginUser.setSex(Sex);
                loginUser.setAge(Age);
                loginUser.setAddr(Addr);
                loginUser.setDescripe(Descripe);
                loginUser.setState(State);
                boolean isSave = loginUser.save();
                if (isSave) {
                    Log.i(Tag, "保存数据到本地成功");
                } else {
                    Log.e(Tag, "保存数据到本地失败");
                }

            }

    }

}
