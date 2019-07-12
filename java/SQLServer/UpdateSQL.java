package com.cuanbo.cb_iot.Model.SQL;

import android.os.Message;
import android.util.Log;

import com.cuanbo.cb_iot.MainActivity;
import com.cuanbo.cb_iot.Model.MeetingSetting;
import com.cuanbo.cb_iot.Tool.Const;
import com.cuanbo.cb_iot.Tool.ListenerManager;
import com.cuanbo.cb_iot.Tool.Utility;
import com.cuanbo.cb_iot.View.Presentation.AddAppointmentActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by xww on 2018/5/16.
 */

public class UpdateSQL implements Runnable{

    private String Tag = "UpdateSQL";
    private String sql;
    private MeetingSetting meetingSetting = MeetingSetting.getInstance();


    public UpdateSQL(String sql) {
        this.sql = sql;
    }

    @Override
    public void run() {
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
                //更新数据
                updateMeetingData(con);
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

    private void updateMeetingData(Connection connection) throws SQLException{
        try {
            /*SQL指令对大小不敏感(数据表中主键必须是唯一的，当前主键是UserName)*/
            //插入数据-INSERT INTO
            String sql9 = "INSERT INTO LoginUser VALUES ('amberoot', '12345', 'www')";
            //插入指定数据到表LoginUser
            String sql10 = "INSERT INTO LoginUser (UserName, UserPwd, PerLevels) VALUES ('haha', '888888','22')";
            //修改表中数据
            String sql11 = "UPDATE LoginUser SET UserPwd = '666' WHERE UserName = 'haha' ";
            //删除表中数据
            String sql12 = "DELETE FROM LoginUser WHERE UserName = 'amber' ";
            //删除表中所有数据
            String sql13 = "DELETE * FROM LoginUser";

//            String MetName = meetingSetting.getMeetingName();
//            String MetStartTime = meetingSetting.getMeetingStartTime();
//            String MetEndTime = meetingSetting.getMeetingEndTime();
//            String MetRecord = "12";
//            String MetNumber = String.valueOf(meetingSetting.getAttendMeetingNumber());
//            String MetUsers = meetingSetting.getMeetingStaffs();
//            String MetRoom = meetingSetting.getMeetingRoom();
//            String MetState = "0";
//            String sql = "INSERT INTO "+ this.sql +" VALUES ('"+MetName+"','"+MetStartTime+"','"+MetEndTime+"','"+MetRecord+"','"+MetNumber+"','"+MetUsers+"','"+MetRoom+"','"+MetState+"')";

            //创建Statement,操作数据
            Statement state = connection.createStatement();
            //插入数据/////////////////////
            int insertSeccess = state.executeUpdate(sql);
            if (insertSeccess == 1) {
                Log.i(Tag,"更新数据库成功");
                //发通知
                ListenerManager.getInstance().sendBroadCast(Const.UPDATE_SQL_SUCESS,null);
            }


            state.close();

        } catch (SQLException e) {
            Log.e(Tag,"更新数据库错误"+e.getMessage());
            //发通知
            ListenerManager.getInstance().sendBroadCast(Const.UPDATE_SQL_FAIL,null);
        }

    }


}
