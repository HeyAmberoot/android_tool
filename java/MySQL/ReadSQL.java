package yangxixi.zxinglib.SQL;

import android.content.SharedPreferences;
import android.os.Message;
import android.util.Log;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import yangxixi.zxinglib.Model.Const;
import yangxixi.zxinglib.Model.LoginUser;
import yangxixi.zxinglib.Tool.ListenerManager;

/**
 * Created by xww on 2018/5/10.
 */

public class ReadSQL implements Runnable{

    private String Tag = "ReadSQL";
    private String SqlTable = "Class";
    private static String driver = "com.mysql.jdbc.Driver";//MySQL 驱动
    private static String url = "jdbc:mysql://192.168.199.129:3306/Users";//MYSQL数据库连接Url
    private static String user = "root";//用户名
    private static String password = "cb2684";//数据库密码
    private static Connection con = null;//打开数据库对象
    private PreparedStatement ps=null;//操作整合sql语句的对象
    private ResultSet rs=null;//查询结果的集合

    public ReadSQL(String sqlTable){
        this.SqlTable = sqlTable;
    }

    @Override
    public void run() {
        //数据库的连接需放在子线程中操作

        try {
            // 使用Class加载驱动程序
            Class.forName(driver);
            //连接数据库
            con = DriverManager.getConnection(url,user,password);


            //连接数据库成功
//            try {
                //读取数据
                //readSqlData(con);
                getUserData();
//            } catch (java.sql.SQLException e) {
//                e.printStackTrace();
//            }
        } catch (Exception e) {
            //连接数据库失败
            Log.e(Tag,e.getMessage());
//            Message message = MainActivity.myHandler.obtainMessage();
//            message.what = Const.CONNECT_SQL_FAIL;
//            MainActivity.myHandler.sendMessage(message);
            //发通知
            ListenerManager.getInstance().sendBroadCast(Const.CONNECT_SQL_FAIL,null);
        }


    }

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
//                case "MeetingList":
//                    handleMeetingData(rs);
//                    break;
//                case "MeetingRoom":
//                    handleMeetingRoomData(rs);
//                    break;
                case "LoginUser":
                    handleUserData(rs);
                    break;
                default:

                        break;
            }

//            Message message = MainActivity.myHandler.obtainMessage();
//            message.what = Const.readSqlTableDataSuccess;
//            MainActivity.myHandler.sendMessage(message);
            //发通知
            ListenerManager.getInstance().sendBroadCast(Const.CONNECT_SQL_SUCCESS,null);
            rs.close();
            state.close();

        } catch (SQLException e) {
            Log.e(Tag,"读取数据库错误："+e.getMessage());
//            Message message = MainActivity.myHandler.obtainMessage();
//            message.what = Const.readSqlTableDataFail;
//            MainActivity.myHandler.sendMessage(message);
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

    private List<LoginUser> getUserData(){
        //结果存放集合
        List<LoginUser> list;
        list = new ArrayList<>();
        //MySQL 语句
        String sql="select * from Class";

        try {
            if(con!=null&&(!con.isClosed())){
                ps= con.prepareStatement(sql);
                if(ps!=null){
                    rs= ps.executeQuery();
                    if(rs!=null){
                        while(rs.next()){
                            LoginUser u=new LoginUser();
                            u.setUserName(rs.getString("username"));
                            u.setUserPwd(rs.getString("password"));
                            list.add(u);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        closeAll(con,ps,rs);//关闭相关操作
        return list;
    }





    private void handleUserData(ResultSet resultSet) throws SQLException {
        //重新保存列表数据前删掉以前的
        DataSupport.deleteAll(LoginUser.class);
        boolean isSave = false;
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
                String Ico = resultSet.getString("Ico");

                //保存数据到本地litePal数据库
                LoginUser loginUser = new LoginUser();
                loginUser.setUserName(UserName);
                loginUser.setUserPwd(UserPwd);
                loginUser.setPerLevels(PerLevels);
                loginUser.setSex(Sex);
                loginUser.setAge(Age+"");
                loginUser.setAddr(Addr);
                loginUser.setDescripe(Descripe);
                loginUser.setState(State);
                loginUser.setIco(Ico);
                isSave = loginUser.save();
            }
        if (isSave) {
            Log.i(Tag, "保存数据到本地成功");
            //发通知
            ListenerManager.getInstance().sendBroadCast(Const.READ_SQL_SUCCESS,"LONGINUSER");
        } else {
            Log.e(Tag, "保存数据到本地失败");
        }


    }


    /**
     * 关闭数据库
     * */

    public static void closeAll(Connection conn, PreparedStatement ps){
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 关闭数据库
     * */

    public static void closeAll(Connection conn, PreparedStatement ps, ResultSet rs){
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }




}
