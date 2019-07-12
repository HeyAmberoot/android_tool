package com.cuanbo.wisroom.tool;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;


import com.cuanbo.wisroom.ActivityUtil.ActivityCollector;
import com.cuanbo.wisroom.Model.DeviceMsg;
import com.cuanbo.wisroom.R;
import com.cuanbo.wisroom.TCP.StartSocketThread;

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
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cuanbo.wisroom.Model.DeviceMsg.getScreenWidth;


/**
 * Created by xww on 18/3/23.
 */

public class Utility {
    private static Utility utility;
    /**
     * 获得单例对象
     */
    public static Utility getInstance()
    {
        if(utility == null)
        {
            utility = new Utility();
        }
        return utility;
    }

    private static Dialog dialog = null;
    private static ProgressDialog progressDialog;

    public void showProgressDialog(Context context) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("正在加载...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public void showAlertDilog(Context context,String mes) {

        if (dialog != null) {
            if (!dialog.isShowing()) {
                dialog.show();
            }
        }else {
            dialog = new android.app.AlertDialog.Builder(context).create();
            dialog.show();

            Window window = dialog.getWindow();
            window.setContentView(R.layout.layout_alert);
            window.setGravity(Gravity.CENTER);//设置dialog显示的位置居中
            //window.setWindowAnimations(R.style.alpha_anim);//添加动画效果
            //设置对话框背景透明，AlertDialog无效，Dialog才有效
            window.setBackgroundDrawableResource(R.color.FullTransparent);

            //设置对话框的宽度和高度
            int height = DeviceMsg.getScreenHeiht();
            int width = getScreenWidth();
            android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();  //获取对话框当前的参数值
            p.width = height/5*2;
            p.height = width/4;
            dialog.setCanceledOnTouchOutside(true);// 设置点击屏幕Dialog不消失
            dialog.getWindow().setAttributes(p);//设置生效

            Button btn_cancel = window.findViewById(R.id.btn_yes);
            btn_cancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO 自动生成的方法存根
                    if (mes.startsWith("重启")){
                        ActivityCollector.finishAll();
                    }
                    dialog.dismiss();
                }
            });

            TextView tvTitle = window.findViewById(R.id.tv_title);
            TextView tvMes = window.findViewById(R.id.tv_message);
            tvTitle.setText("提示");
            tvMes.setText(mes);
        }
    }

    public void showQuestDilog(Context context, final String quest,String meetingName) {
        final Dialog dialog;
        dialog = new android.app.AlertDialog.Builder(context).create();
        dialog.show();

        Window window = dialog.getWindow();
        window.setContentView(R.layout.quest_dialog);
        window.setGravity(Gravity.CENTER);//设置dialog显示的位置居中
        //window.setWindowAnimations(R.style.alpha_anim);//添加动画效果
        //设置对话框背景透明，AlertDialog无效，才有效
        window.setBackgroundDrawableResource(R.color.FullTransparent);

        //设置对话框的宽度和高度
        int height = DeviceMsg.getScreenHeiht();
        int width = DeviceMsg.getScreenWidth();
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();  //获取对话框当前的参数值
        p.width = width/2;
        p.height = height/4;
        dialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        dialog.getWindow().setAttributes(p);//设置生效

        Button btn_cancel=window.findViewById(R.id.btn_cancel2);
        btn_cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 自动生成的方法存根
                dialog.dismiss();
            }
        });

        Button btn_comfirm = window.findViewById(R.id.btn_comfirm2);
        btn_comfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //确认要取消
                if(quest.startsWith("确定要取消")) {

                }
            }
        });

        TextView tvTitle = window.findViewById(R.id.label_title);
        TextView tvMes = window.findViewById(R.id.label_quest);
        tvTitle.setText("提示");
        tvMes.setText(quest);
    }
    /**
     * 获取当前时间
     */
    public static String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date curDate = new Date(System.currentTimeMillis());

        return formatter.format(curDate);
    }

    /**
     * 延时方法2
     * @param time
     */
    public static void delay2(int time){
        new Timer().schedule(new TimerTask() {//延时两秒，如果超出则擦错第一次按键记录
            @Override
            public void run() {
                //1秒后干点事
            }
        }, 1000);
    }

    /**
     * 延时方法1
     * @param time
     */
    public static void delay(int time){
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

            }
        }, time);
    }

    /**
     * 将16进制字符串(例如"3E 43 4F")转换为byte[]
     */
    public static byte[] strToBytes(String str) {
        if(str == null || str.trim().equals("")) {
            return new byte[0];
        }
        
        //去除字符串中的空格转成数组
        String[] array16 = str.split(" ");
        //数组转字符串有,隔开: {3E,43,4F}
        String str16 = Arrays.toString(array16);
        //数组转字符串无,隔开：3E434F
        StringBuilder build = new StringBuilder();
        for (int i = 0; i < array16.length; i++) {
            build.append(array16[i]);
        }
        
        byte[] bytes = new byte[build.length() / 2];
        for(int i = 0; i < build.length() / 2; i++) {
            String subStr = build.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }
        
        return bytes;
    }

    /**
     * 运行序列化和反序列化  进行深度拷贝
     */
    public static <T> ArrayList<T> deepCopy(ArrayList<T> src) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(src);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream in = new ObjectInputStream(byteIn);
        @SuppressWarnings("unchecked")
        ArrayList<T> dest = (ArrayList<T>) in.readObject();
        return dest;
    }

    /**
     * 字符串MD5加密
     */
    public static String md5(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(str.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 字符串多次MD5加密
     */
    public static String md5(String string, int times) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        String md5 = md5(string);
        for (int i = 0; i < times - 1; i++) {
            md5 = md5(md5);
        }
        return md5(md5);
    }

    /**
     * mac地址字符串转Bytes
     */
    private byte[] macStrToBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }

    /**
     * 获取屏幕分辨率
     */
    public void getScreenDensity_ByResources() {
//        DisplayMetrics mDisplayMetrics = getResources().getDisplayMetrics();
//        int width = mDisplayMetrics.widthPixels;
//        int height = mDisplayMetrics.heightPixels;
//        float density = mDisplayMetrics.density;
//        int densityDpi = mDisplayMetrics.densityDpi;
//        Log.i(Tag, "Screen Ratio: [" + width + "x" + height + "],density=" + density + ",densityDpi=" + densityDpi);
//        Log.i(Tag, "Screen mDisplayMetrics: " + mDisplayMetrics);
//        DeviceMsg.setScreenWidth(width);
//        DeviceMsg.setScreenHeiht(height);
    }

    /**
     * 判断是否为IP地址
     * @param addr
     * @return
     */
    public static  boolean isIP(String addr)
    {
        if(addr.length() < 7 || addr.length() > 15 || "".equals(addr))
        {
            return false;
        }
        /**
         * 判断IP格式和范围
         */
        String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pat = Pattern.compile(rexp);
        Matcher mat = pat.matcher(addr);
        boolean ipAddress = mat.find();
        //============对之前的ip判断的bug在进行判断
        if (ipAddress==true){
            String ips[] = addr.split("\\.");
            if(ips.length==4){
                try{
                    for(String ip : ips){
                        if(Integer.parseInt(ip)<0||Integer.parseInt(ip)>255){
                            return false;
                        }
                    }
                }catch (Exception e){
                    return false;
                }
                return true;
            }else{
                return false;
            }
        }
        return ipAddress;
    }

    /**
     * 消除字符串中的空格
     * @param src
     * @return
     */
    public static String replaceBlank(String src) {
        String dest = "";
        if (src != null) {
            Pattern pattern = Pattern.compile("\t|\r|\n|\\s*");
            Matcher matcher = pattern.matcher(src);
            dest = matcher.replaceAll("");
        }
        return dest;
    }

}
