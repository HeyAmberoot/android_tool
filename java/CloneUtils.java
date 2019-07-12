package com.example.cuanbo.Tool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by xww on 18/4/28.
 */

public class CloneUtils {
    @SuppressWarnings("unchecked")
    public static <T> T clone(T obj){

        T clonedObj = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            clonedObj = (T) ois.readObject();
            ois.close();

        }catch (Exception e){
            e.printStackTrace();
        }

        return clonedObj;
    }
}
