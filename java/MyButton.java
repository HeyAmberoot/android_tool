package com.example.cuanbo.Tool;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by xww on 18/3/26.
 */

public class MyButton extends android.support.v7.widget.AppCompatButton {

    public String PageId;
    public String PresentPage;
    public String MutexNum;
    public String switchType;

    public MyButton(Context context) {
        super(context,null);
    }

    public MyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean performClick() {
        return true;
    }

}
