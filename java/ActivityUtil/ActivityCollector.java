package com.example.Application;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
/**
 * 通过一个List来暂存活动，然后提供一个addActivity()方法用于向List添加一个活动，
 * 提供了一个removeActivity（）方法用于从List中移除活动，
 * 提供一个finishAll()方法用于将List中存储的活动全部销毁。
 */
public class ActivityCollector {
	public static List<Activity> activities=new ArrayList<>();
	
	public static void addActivity(Activity activity) {
		activities.add(activity);
	}
	
	public static void removeActivity(Activity activity) {
		activities.remove(activity);
	}
	
	public static void finishAll() {//销毁所有活动，可在任何地方退出程序
		for(Activity activity:activities){
			if (!activity.isFinishing()) {
				activity.finish();
			}
		}
		activities.clear();
	}

}
