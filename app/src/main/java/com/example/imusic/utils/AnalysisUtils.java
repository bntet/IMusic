package com.example.imusic.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AnalysisUtils {
    //获取用户名
    public static String readLoginUserName(Context context) {
        SharedPreferences sp = context.getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        String userName = sp.getString("loginUserName", "");
        return userName;
    }
}
