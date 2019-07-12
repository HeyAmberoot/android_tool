package com.example.cuanbo.ProjectData;

import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by xww on 18/3/20.
 */

public class CodeList {

    private static Map<String,ArrayList<String[]>> CodeWithID;
    private static byte[] CodeToSend;

    public CodeList(Map codeWithID,byte[] codeToSend) {
        super();

        CodeWithID=codeWithID;
        CodeToSend=codeToSend;

    }
    //构造函数
    public CodeList() {

        super();
    }

    ///获取数据//////////////////////////////////////////////


    public static Map getCodeWithID() { return CodeWithID; }

    public static byte[] getCodeToSend() { return CodeToSend; }

    ///存入数据/////////////////////////////////////////


    public static void setCodeWithID(Map codeWithID) { CodeWithID=codeWithID;
    }

    public static void setCodeToSend(byte[] codeToSend) { CodeToSend=codeToSend;
    }
}
