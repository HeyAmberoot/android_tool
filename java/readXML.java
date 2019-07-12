package com.example.cuanbo.Tool;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.cuanbo.ProjectData.CodeList;
import com.example.cuanbo.ProjectData.DeviceMsg;
import com.example.cuanbo.ProjectData.DevicePage;
import com.example.cuanbo.ProjectData.ProjectImages;
import com.example.cuanbo.ProjectData.SystemPage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Created by xww on 18/3/20.
 */

public class readXML {

    public readXML() {
        //结构函数
    }

    private String Tag = "readXML";
    private ArrayList<String[]> SingleCodeMsg = new ArrayList<String[]>();
    private Map<String,ArrayList> BtnListWithUID = new HashMap();
    private Map<String,ArrayList> LabelListWithUID = new HashMap<>();
    private Map<String,ArrayList> StateListWithUID = new HashMap();
    private Map<String,ArrayList> PanelListWithUID = new HashMap();
    private Map<String,ArrayList> MatrixBtnListWithUID= new HashMap();
    private Map<String,ArrayList> VideoListWithUID = new HashMap();
    private ArrayList<String[]> allBtnList = new ArrayList<>();//
    private ArrayList<String[]> allLabelList = new ArrayList<>();
    private ArrayList<String[]> allStateList = new ArrayList<>();
    private ArrayList<String[]> allPanelList = new ArrayList<>();
    private ArrayList<String[]> allMatrixBtnList = new ArrayList<>();
    private ArrayList<String[]> allVideoList = new ArrayList<>();
    private String[] singleBtnViewMsg = new String[14];
    private Map<String,ArrayList> CodeWithID = new HashMap();
    private Map<String,String> ImageWithName = new HashMap();
    private Map<String,String[]> PageAttrWithUID = new HashMap();
    private String UID;

    public void PullParseXml(String Name) throws Exception {
        
        String path = Const.path + "/" + Name + ".xml";
        File file = new File(path);
//        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();

            InputStream in = new FileInputStream(file);
            xmlPullParser.setInput(in,"Utf-8");//为Pull解析器设置要解析的XML数据
            int eventType = xmlPullParser.getEventType();

            String KeyId = "";//保存代码的ID
//            String UID = "";//保存页面UID
            String node_big = "";
//            int count = 0;
            int num = 0;

            try {
                while (eventType!=XmlPullParser.END_DOCUMENT) {
                    String nodeName = xmlPullParser.getName();
                    switch (eventType) {
                        case XmlPullParser.START_DOCUMENT:

                            break;
                        case XmlPullParser.START_TAG:
                            if ("SystemPage".equals(nodeName)) {
                                node_big = "SystemPage";
                                String strBackImage_system = xmlPullParser.getAttributeValue(3);
//                                Bitmap backImage = ImageUtils.StringToBitmap(strBackImage_system);
                                SystemPage.setBackImage(strBackImage_system);
                            }
                            else if(nodeName.equals("CBView"))
                            {
                                node_big = "CBView";
//                                String strCount = xmlPullParser.getAttributeValue(0);
//                                count = Integer.parseInt(strCount);
                            }
                            else if(nodeName.equals("Device0"))
                            {

                                node_big = "Device0";
                                num = 0;
                                String IP = xmlPullParser.getAttributeValue(2);
                                String Port = xmlPullParser.getAttributeValue(3);
                                String strFirstPage = xmlPullParser.getAttributeValue(6);
                                String strStartUpPage = xmlPullParser.getAttributeValue(7);
                                //保存设备IP与port
                                DeviceMsg.setAddress(IP);
                                DeviceMsg.setPort(Port);
                                DevicePage.setFirstPage(strFirstPage);
                                DevicePage.setSetupPage(strStartUpPage);

                            }
                            else if(nodeName.equals("KeyCodeNodeList"))
                            {
                                num = 0;
                                node_big = "KeyCodeNodeList";
                                saveSinglePageMsg();//保存单页信息
                                saveAllPageMsg();//保存所有页面的信息
                                DevicePage.setPagesAttr(PageAttrWithUID);
                            }
                            else if(nodeName.equals("Images"))
                            {
                                num = 0;
                                node_big = "Images";
                                CodeWithID.put(KeyId,SingleCodeMsg);
                                CodeList.setCodeWithID(CodeWithID);//把代码信息传出去
                            }
                            else if(nodeName.equals("ComUart")){
                                num = 0;
                                node_big = "null";
                            }
                            else if(nodeName.equals("ScriptClass")){
                                num = 0;
                                node_big = "null";
                            }
                            else if(nodeName.equals("ComUart")){
                                num = 0;
                                node_big = "null";
                            }
                            ///获取系统页信息////////////////////////////////////////////
                            if (node_big == "CBView") {

                                if (nodeName.equals("CBView0")) {
                                    num = 0;
                                }
                                String strNum = String.valueOf(num);
                                if(nodeName.equals("CBView" + strNum)) {
                                    getPageMsg(xmlPullParser,nodeName);//获取系统页信息

                                    num++;
                                }else if (nodeName.equals("KeyCodeClass" )) {
                                    getSingleBtnMsg(xmlPullParser,"KeyCodeClass");
                                }
                            ///获取设备页信息////////////////////////////////////////////
                            }else if (node_big.equals("Device0")) {

                                if (nodeName.equals("Page0")) {
                                    num = 0;
                                }
                                String strNum = String.valueOf(num);
                                if(nodeName.equals("Page" + strNum)) {
                                    num++;
                                    if (!UID.equals("system")) {
                                        saveSinglePageMsg();//保存单页信息
                                    }

                                    String BackImage_str = xmlPullParser.getAttributeValue(2);
                                    String UseSystemBackImage = xmlPullParser.getAttributeValue(3);
                                    String UseSystemViews = xmlPullParser.getAttributeValue(4);
                                    String[] pageAttr = {BackImage_str,UseSystemBackImage,UseSystemViews};
                                    UID = xmlPullParser.getAttributeValue(6);
                                    PageAttrWithUID.put(UID,pageAttr);
                                    btnNum = 0;

                                }else {
                                    getPageMsg(xmlPullParser,nodeName);//获取设备页信息

                                }
                            ///获取指令信息////////////////////////////////////////////
                            }else if (node_big.equals("KeyCodeNodeList")) {

                                if (nodeName.equals("KeyCodeClass0")) {
                                    num = 0;

                                }
                                String strNum = String.valueOf(num);
                                if(nodeName.equals("KeyCodeClass" + strNum)) {
                                    num++;
                                    if (!KeyId.equals("")) {
                                        //深度拷贝
                                        ArrayList codeList = deepCopy(SingleCodeMsg);
                                        CodeWithID.put(KeyId,codeList);
                                        SingleCodeMsg.clear();
                                    }
                                    
                                    KeyId = xmlPullParser.getAttributeValue(1);

                                }else if(!nodeName.equals("KeyCodeNodeList")){
                                    getCodeMsg(xmlPullParser);//获取代码信息

                                }

                            } else if (node_big.equals("Images")) {
                                if (!nodeName.equals("Images")) {
                                    String imageName = xmlPullParser.getAttributeValue(1);
                                    String ImageStr = xmlPullParser.getAttributeValue(2);
                                    ImageWithName.put(imageName, ImageStr);
                                }
                            }

                            break;

                        case XmlPullParser.END_TAG:

                            if (nodeName.equals("Images")) {
                                ProjectImages.setImageWithName(ImageWithName);
                            }else if (nodeName.equals("SystemPage")) {
                                UID = "system";
                                saveSinglePageMsg();//保存系统页信息
                            }
                            break;


                        default:
                            break;
                    }
                    eventType=xmlPullParser.next();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(Tag,e.getMessage());
            }


//        } catch (XmlPullParserException e) {
//            e.printStackTrace();
//            Log.e(Tag,e.getMessage());
//        } catch (FileNotFoundException e1) {
//
//            e1.printStackTrace();
//            Log.e(Tag,"FileNotFoundException");
//        }

    }

    /**
     * 根据CodeId保存指令信息
     */
    private void getCodeMsg(XmlPullParser parser) {
        String Delay = parser.getAttributeValue(2);
        String CmdStr = parser.getAttributeValue(3);
        String IsHex = parser.getAttributeValue(4);
        String[] SingleCodeList = {Delay,CmdStr,IsHex};

        SingleCodeMsg.add(SingleCodeList);


    }

    /**
     * 保存页面信息
     */
    private void saveAllPageMsg() {
        DevicePage.setBtnListWithUID(BtnListWithUID);
        DevicePage.setLabelListWithUID(LabelListWithUID);
        DevicePage.setStateListWithUID(StateListWithUID);
        DevicePage.setPanelListWithUID(PanelListWithUID);
        DevicePage.setMatrixBtnListWithUID(MatrixBtnListWithUID);
        DevicePage.setVideoListWithUID(VideoListWithUID);
    }
    private void saveSinglePageMsg() {
        try {
            //深度拷贝
            ArrayList btnList = deepCopy(allBtnList);
            ArrayList labelList = deepCopy(allLabelList);
            ArrayList stateList = deepCopy(allStateList);
            ArrayList panelList = deepCopy(allPanelList);
            ArrayList matrixBtnList = deepCopy(allMatrixBtnList);
            ArrayList videoList = deepCopy(allVideoList);
            //
            BtnListWithUID.put(UID,btnList);
            LabelListWithUID.put(UID,labelList);
            StateListWithUID.put(UID,stateList);
            PanelListWithUID.put(UID,panelList);
            MatrixBtnListWithUID.put(UID,matrixBtnList);
            VideoListWithUID.put(UID,videoList);
            allBtnList.clear();
            allLabelList.clear();
            allVideoList.clear();
            allMatrixBtnList.clear();
            allPanelList.clear();
            allStateList.clear();
        }catch (Exception e) {
            e.printStackTrace();
        }



    }





    /**
     * 获取页面信息
     * @param parser
     */
    private void getPageMsg(XmlPullParser parser,String nodeName) {

        if (nodeName.equals("KeyCodeClass")) {
            getSingleBtnMsg(parser,"KeyCodeClass");
            return;
        }
        String ViewType = parser.getAttributeValue(0);
        switch (ViewType) {
            case "CBButton":
                getSingleBtnMsg(parser, "CBButton");
//            allBtnList.add(singleBtnMsg);

                break;
            case "CBLabel":
                String[] singleLabelMsg = getSingleLabelMsg(parser);
                allLabelList.add(singleLabelMsg);
//                LabelListWithUID.put(UID,singleLabelMsg);
                break;
            case "DevicesState":
                String[] singleStateMsg = getSingleStateMsg(parser);
                allStateList.add(singleStateMsg);

                break;
            case "MatrixButton":
                String[] singleMatrixBtnMsg = getSingleMatrixBtnMsg(parser);
                allMatrixBtnList.add(singleMatrixBtnMsg);

                break;
            case "BackPanel":
                String[] singlePanelMsg = getSinglePanelMsg(parser);
                allPanelList.add(singlePanelMsg);

                break;
            case "VideoButton":
                String[] singleVideoMsg = getSingleVideoMsg(parser);
                allVideoList.add(singleVideoMsg);

                break;
            default:
                    break;
        }

    }

    /**
     * 获取单个按钮的信息
     * @param parser
     * @return
     */
    private int btnNum = 0;
    private void getSingleBtnMsg(XmlPullParser parser,String NoteName) {
        String[] singleBtnCodeMsg = new String[5];
        if (NoteName.equals("KeyCodeClass")) {
            btnNum ++;
            String IsLock = parser.getAttributeValue(0);
            String LockPassword = parser.getAttributeValue(1);
            String IsRound = parser.getAttributeValue(2);
            String RoundTime = parser.getAttributeValue(3);
            String RoundSpace = parser.getAttributeValue(4);

            singleBtnCodeMsg[0] = IsLock;
            singleBtnCodeMsg[1] = LockPassword;
            singleBtnCodeMsg[2] = IsRound;
            singleBtnCodeMsg[3] = RoundTime;
            singleBtnCodeMsg[4] = RoundSpace;

            String[] SingleBtnList = new String[singleBtnViewMsg.length + singleBtnCodeMsg.length];
            System.arraycopy(singleBtnViewMsg, 0, SingleBtnList, 0, singleBtnViewMsg.length);
            System.arraycopy(singleBtnCodeMsg, 0, SingleBtnList, singleBtnViewMsg.length, singleBtnCodeMsg.length);

            allBtnList.add(SingleBtnList);
//            allBtnList.put(String.valueOf(btnNum),SingleBtnList);
//            BtnListWithUID.put(UID,SingleBtnList);

        }else {
            String ID = parser.getAttributeValue(1);
            String KeyId = parser.getAttributeValue(2);
            String KeyUID = parser.getAttributeValue(3);
            String Text = parser.getAttributeValue(4);
            String X = parser.getAttributeValue(5);
            String Y = parser.getAttributeValue(6);
            String Width = parser.getAttributeValue(7);
            String Height = parser.getAttributeValue(8);
            String MutexNum = parser.getAttributeValue(9);
            String FontSize = parser.getAttributeValue(10);
            String NormalImage = parser.getAttributeValue(11);
            String PressDownImage = parser.getAttributeValue(12);
            String TextColor = parser.getAttributeValue(13);
            String PageSwitch = parser.getAttributeValue(14);

//            singleBtnViewMsg = {ID, KeyId, KeyUID, Text, X, Y, Width, Height, MutexNum, FontSize, NormalImage, PressDownImage, TextColor, PageSwitch};
            singleBtnViewMsg[0] = ID;
            singleBtnViewMsg[1] = KeyId;
            singleBtnViewMsg[2] = KeyUID;
            singleBtnViewMsg[3] = Text;
            singleBtnViewMsg[4] = X;
            singleBtnViewMsg[5] = Y;
            singleBtnViewMsg[6] = Width;
            singleBtnViewMsg[7] = Height;
            singleBtnViewMsg[8] = MutexNum;
            singleBtnViewMsg[9] = FontSize;
            singleBtnViewMsg[10] = NormalImage;
            singleBtnViewMsg[11] = PressDownImage;
            singleBtnViewMsg[12] = TextColor;
            singleBtnViewMsg[13] = PageSwitch;
//            return singleBtnViewMsg;
        }

    }

    /**
     * 获取单个Label的信息
     * @param parser
     * @return
     */

    private String[] getSingleLabelMsg(XmlPullParser parser) {
        String ID = parser.getAttributeValue(1);
        String Text = parser.getAttributeValue(2);
        String X = parser.getAttributeValue(3);
        String Y = parser.getAttributeValue(4);
        String Width = parser.getAttributeValue(5);
        String Height = parser.getAttributeValue(6);

        String FontSize = parser.getAttributeValue(7);
        String TextColor = parser.getAttributeValue(8);

        return new String[]{ID,Text,X,Y,Width,Height,FontSize,TextColor};
    }

    /**
     * 获取单个状态图标的信息
     * @param parser
     * @return
     */
    private String[] getSingleStateMsg(XmlPullParser parser) {
        String ID = parser.getAttributeValue(1);
        String X = parser.getAttributeValue(2);
        String Y = parser.getAttributeValue(3);
        String Width = parser.getAttributeValue(4);
        String Height = parser.getAttributeValue(5);
        String IcoIsScale = parser.getAttributeValue(6);
        String IcoIsBlink = parser.getAttributeValue(7);
        String OnLineIco = parser.getAttributeValue(8);
        String OffLineIco = parser.getAttributeValue(9);

        return new String[]{ID,X,Y,Width,Height,IcoIsScale,IcoIsBlink,OnLineIco,OffLineIco};
    }

    /**
     * 获取单个矩阵按钮的信息
     * @param parser
     * @return
     */
    private String[] getSingleMatrixBtnMsg(XmlPullParser parser) {
        String ID = parser.getAttributeValue(1);
        String KeyId = parser.getAttributeValue(2);
        String X = parser.getAttributeValue(3);
        String Y = parser.getAttributeValue(4);
        String MatrixType = parser.getAttributeValue(5);
        String MatrixPort = parser.getAttributeValue(6);
        String PressDownImage = parser.getAttributeValue(7);
        String NormalImage = parser.getAttributeValue(8);
        String Width = parser.getAttributeValue(9);
        String Height = parser.getAttributeValue(10);
        String Text = parser.getAttributeValue(11);
        String FontSize = parser.getAttributeValue(12);
        String TextColor = parser.getAttributeValue(13);
//            String MutexNum = parser.getAttributeValue(9);

        return new String[]{ID,KeyId,X,Y,MatrixType,MatrixPort,PressDownImage,NormalImage,Width,Height,Text,FontSize,TextColor};
    }

    private String[] getSinglePanelMsg(XmlPullParser parser) {
        String ID = parser.getAttributeValue(1);
        String X = parser.getAttributeValue(2);
        String Y = parser.getAttributeValue(3);
        String Width = parser.getAttributeValue(4);
        String Height = parser.getAttributeValue(5);
        String BackImage = parser.getAttributeValue(6);

        return new String[]{ID,X,Y,Width,Height,BackImage};
    }
    /**
     * 获取单个视频的信息
     * @param parser
     * @return
     */
    private String[] getSingleVideoMsg(XmlPullParser parser) {
        String ID = parser.getAttributeValue(1);
        String UID = parser.getAttributeValue(2);
        String Text = parser.getAttributeValue(3);
        String ParentTag = parser.getAttributeValue(4);
        String X = parser.getAttributeValue(5);
        String Y = parser.getAttributeValue(6);
        String Width = parser.getAttributeValue(7);
        String Height = parser.getAttributeValue(8);
        String Url = parser.getAttributeValue(9);

        return new String[]{ID,UID,Text,ParentTag,X,Y,Width,Height,Url};
    }

   // ArrayList<String[]>
//
    //关键代码 运行序列化和反序列化  进行深度拷贝
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
}
