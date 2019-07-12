package com.example.Application;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
/**
 * ͨ��һ��List���ݴ���Ȼ���ṩһ��addActivity()����������List���һ�����
 * �ṩ��һ��removeActivity�����������ڴ�List���Ƴ����
 * �ṩһ��finishAll()�������ڽ�List�д洢�Ļȫ�����١�
 */
public class ActivityCollector {
	public static List<Activity> activities=new ArrayList<>();
	
	public static void addActivity(Activity activity) {
		activities.add(activity);
	}
	
	public static void removeActivity(Activity activity) {
		activities.remove(activity);
	}
	
	public static void finishAll() {//�������л�������κεط��˳�����
		for(Activity activity:activities){
			if (!activity.isFinishing()) {
				activity.finish();
			}
		}
		activities.clear();
	}

}
