package com.example.cuanbo.Tool;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by xww on 2018/12/21.
 */

public class CRC16 {

    public CRC16() {

    }

    public static int CRC16GetResult(byte[] bytes) {
        int result = 0;
        //16进制数直接转字符串
        String str = bytes2HexString(bytes);
        Log.i("CRC16", str);
        //16进制字符串单个字符转成int
        for (int i = 0; i<str.length(); i++) {
            String cha = str.substring(i,i+1);
            int num = Integer.valueOf("0"+cha,16);//Integer.getInteger("0"+cha,16);
            int num2 = (int) (num*Math.pow(16,str.length()-1-i));
            result += num2;
//            Log.i("CRC16", cha);
//            Log.i("CRC16", String.valueOf(num));
        }

        Log.i("CRC16", String.valueOf(result));
        return result;
    }


    private static String bytes2HexString(byte[] array) {
        StringBuilder builder = new StringBuilder();

        for (byte b : array) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            builder.append(hex);
        }

        return builder.toString().toUpperCase();
    }

}
