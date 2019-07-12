package com.example.cuanbo.Tool;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;

import static java.lang.Integer.parseInt;

/**
 * Created by xww on 18/3/17.
 */

public class ImageUtil {


    /**
     * 单例模式
     */
    public static ImageUtil imageUtil;
    /**
     * 获得单例对象
     */
    public static ImageUtil getInstance()
    {
        if(imageUtil == null)
        {
            imageUtil = new ImageUtil();
        }
        return imageUtil;
    }

    public Bitmap StringToBitmap(String string){
        //将字符串转换成Bitmap类型
        Bitmap bitmap=null;
        try {
            byte[]bitmapArray;
            bitmapArray= Base64.decode(string, Base64.DEFAULT);
            bitmap= BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     *
     * @param string 颜色字符串
     * @return  颜色
     */
    public int StringToColor(String string) {

        String[] ARGB = string.split(":");
        int Alpha = Integer.parseInt(ARGB[0]);
        int Red = Integer.parseInt(ARGB[1]);
        int Green = Integer.parseInt(ARGB[2]);
        int Blue = Integer.parseInt(ARGB[3]);

        return Color.argb(Alpha,Red,Green,Blue);
    }

}