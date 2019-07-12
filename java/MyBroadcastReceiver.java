package com.example.cuanbo.Tool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.cuanbo.Presentation.CustomActivity;

/**
 * Created by xww on 18/3/26.
 */

public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO 自动生成的方法存根
        String state = intent.getStringExtra("connectState");
//        CustomActivity.stateImage
//        CustomActivity.switchState(state);


    }


}
