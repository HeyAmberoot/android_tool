package com.example.cuanbo.Tool;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import android.content.Context;

import com.example.cuanbo.easycontrol.MainActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import static android.content.Context.MODE_APPEND;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by xww on 18/3/19.
 */
//android文件操作方法:getFilesDir，getExternalFilesDir，
// getExternalStorageDirectory，getExternalStoragePublicDirectory

/**
 * String[] fileList():返回该应用数据文件夹的全部文件
 *  File getFilesDir():获取该应用程序的数据文件夹得绝对路径
 *  getDir(String name , int mode):在应用程序的数据文件夹下获取或者创建name对应的子目录
 *  MODE_WORLD_READABLE：表示当前文件可以被其他应用读取；
 *  MODE_WORLD_WRITEABLE：表示当前文件可以被其他应用写入
 */

public class FileHelper {

    private String Tag = "FileHelper";
    //获取SD卡的路径
//    static String path = MyApplication.getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath();
            //Environment.getExternalStorageDirectory().getPath()+"/cuanbo";//"/mnt/sdcard/cuanbo";//
    String path2 = Environment.getExternalStorageDirectory().getPath() ;

    /**
     * 创建文件夹
     */
    public void createFolder() {
        //获取SD卡的路径
//    String path = MyApplication.getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath();

        //getFilesDir()获取你app的内部存储空间
        File Folder = new File(MyApplication.getContext().getFilesDir(), "cuanbo.xml");
//        File Folder = new File(path);
        if (!Folder.exists())//判断文件夹是否存在，不存在则创建文件夹，已经存在则跳过
        {

            Folder.mkdir();//创建文件夹

            //两种方式判断文件夹是否创建成功
            //Folder.isDirectory()返回True表示文件路径是对的，即文件创建成功，false则相反
            boolean isFilemaked1 = Folder.isDirectory();
            //Folder.mkdirs()返回true即文件创建成功，false则相反
            boolean isFilemaked2 = Folder.mkdirs();

            if (isFilemaked1 || isFilemaked2) {
                Log.i(Tag,"成功");
            } else {
                Log.i(Tag,"失败");
            }

        }else {
            Log.i(Tag,"文件已存在");
        }

    }


    /**
     * 获取外部存储的私有文件夹document路径并创建文件夹
     * @param folderName
     * @return
     */
    public File getDocumentsStorageDir(String folderName) {
        // Get the directory for the app's private documents directory.
        File file = new File(MyApplication.getContext().getExternalFilesDir(
                Environment.DIRECTORY_DOCUMENTS), folderName);
        if(!file.mkdirs()) {
            Log.e(Tag, "Directory not created");
        }
        return file;
    }

    /**
     * 创建文件
     * @param fileName
     */
    public void creatFile(String fileName) {
        //新建一个File类型的成员变量，传入文件名路径。
        File mFile = new File(Const.path + "/" + fileName + ".xml");
        //判断文件是否存在，存在就删除
        if (mFile.exists()){
            mFile.delete();
        }
        try {
            //创建文件
            mFile.createNewFile();
            //给一个吐司提示，显示创建成功
//            Toast.makeText(MyApplication.getContext(), "文件创建成功", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("creatXMLFileException",e.getMessage());
        }
    }

    /**
     * 删除文件
     * @param fileName
     */
    public void deleteFile(String fileName) {
        //新建一个File类型的成员变量，传入文件名路径。
        File mFile = new File(Const.path + "/" + fileName + ".xml");
        //判断文件是否存在，存在就删除
        if (mFile.exists()){
            mFile.delete();
        }

    }

    /**
     * 把数据写入文件
     * @param data
     * @param fileName
     */
    public void writeDataToFile(String data,String fileName) {

        FileOutputStream out;
        BufferedWriter writer = null;
        String Path = Const.path + "/" + fileName + ".xml";
        File file = new File(Path);
        try {
            /**
             * Context().openFileOutput(name,mode)方法指定路径创建文件并写入，
             * 只能传入文件名不能传入路径即不能包含"/"
             * Context.MODE_APPEND:会检查文件是否存在，存在就往文件追加内容，否则就创建新文件。
             * Context.MODE_PRIVATE:写入的内容会覆盖原文件的内容
            */
//                out = MyApplication.getContext().openFileOutput(fileName, MODE_PRIVATE);
//                out = MyApplication.getContext().openFileOutput(fileName, MODE_APPEND);

            /**
            * FileOutputStream(name,append)
             * 可传入文件路径，把数据写入指定路径指定文件；
             * 若只传入参数name，写入内容会覆盖原文件的内容；
             * 若增加传入参数append，且为true，则往文件追加内容；
            */
            out = new FileOutputStream(file,true);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(data);


        } catch (Exception e ) {
            e.printStackTrace();
            Log.e(Tag,"写入文件失败");
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            }catch (IOException e) {
                e.printStackTrace();
                Log.e(Tag,e.getMessage());
            }
        }


    }

    /**
     * 读取文件数据
     * @return
     */
    public String read(String fileName) {
        FileInputStream in;
        BufferedReader reader;
        StringBuilder content = new StringBuilder();
        try {
            FileInputStream inStream = MyApplication.getContext().openFileInput(fileName);
            byte[] buffer = new byte[1024];
            int hasRead = 0;
            StringBuilder sb = new StringBuilder();
            while ((hasRead = inStream.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, hasRead));
            }

            inStream.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 在使用外部存储之前，你必须要先检查外部存储的当前状态，以判断是否可用
     * @return
     */
    public boolean isExternalStorageWriteable() {
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
            Log.i("外部存储是否可用","可写可读");
            return true;
        }
        else if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
            Log.i("外部存储是否可用","不可写可读");
            return false;
        } else{
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
            Log.i("外部存储是否可用","可写不可读");
            return false;
        }

    }

    public static ArrayList getPathFilesName(String filePath) {
        File path = new File(filePath);
        // File path = new File("/mnt/sdcard/");
        File[] files = path.listFiles();// 读取文件
        ArrayList<String> filesName = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getName();
            String file = null;
            if (fileName.endsWith(".xml")) {
                file = fileName.substring(0,
                        fileName.lastIndexOf(".")).toString();
                filesName.add(file);
            }
            if (file != null) {
                //file就是我想要获得的以.xml结尾的文件的文件名了
            }
        }
        return filesName;
    }




}
