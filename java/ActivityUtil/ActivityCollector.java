package com.example.cuanbo.Tool;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xww on 18/3/29.
 */

public class ActivityCollector {
    public static List<Activity> activities=new ArrayList<>();

    public static void addActivity(Activity activity) {
        activities.add(activity);
    }

    public static void removeActivity(Activity activity) {

        activities.remove(activity);
        if (!activity.isFinishing()) {
            activity.finish();
        }
    }

    public static void finishAll() {
        for(Activity activity:activities){
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
        activities.clear();
    }

    /**
     * 关闭指定的Activity
     * @param activityName 需要关闭的Activity包名类名
     * */
    public static void finishOneActivity(String activityName){
        //在activities集合中找到类名与指定类名相同的Activity就关闭
        for (Activity activity : activities){
            String name= activity.getClass().getName();//activity的包名+类名
            if(name.equals(activityName)){
                if(activity.isFinishing()){
                    //当前activity如果已经Finish，则只从activities清除就好了
                    activities.remove(activity);
                } else {
                    //没有Finish则Finish
                    activity.finish();
                }
            }
        }
    }

    /**
     * 只保留某个Activity，关闭其他所有Activity
     * @param activityName 要保留的Activity类名
     * */
    public static void finishOtherActivity(String activityName){
        for (Activity activity : activities){
            String name= activity.getClass().getName();//activity的类名
            if(!name.equals(activityName)){
                if(activity.isFinishing()){
                    //当前activity如果已经Finish，则只从activities清除就好了
                    activities.remove(activity);
                } else {
                    //没有Finish则Finish
                    activity.finish();
                }
            }
        }
    }

}
