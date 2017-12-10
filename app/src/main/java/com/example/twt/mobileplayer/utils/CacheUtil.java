package com.example.twt.mobileplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author twt
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */

public class CacheUtil {

    public static String getString(Context context,String key){
        SharedPreferences sp = context.getSharedPreferences("netvideo", Context.MODE_PRIVATE);
        return sp.getString(key,"");
    }
    public static void setString(Context context,String key,String value){
        SharedPreferences sp = context.getSharedPreferences("netvideo", Context.MODE_PRIVATE);
        sp.edit().putString(key,value).commit();
    }
    public static int getInt(Context context,String key){
        SharedPreferences sp = context.getSharedPreferences("netvideo", Context.MODE_PRIVATE);
        return sp.getInt(key,1);
    }
    public static void setInt(Context context,String key,int value){
        SharedPreferences sp = context.getSharedPreferences("netvideo", Context.MODE_PRIVATE);
        sp.edit().putInt(key,value).commit();
    }
}
