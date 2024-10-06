package com.example.medialservice.service.contentcatalogs;

import android.text.TextUtils;
import android.util.Log;

public class Utils {

    public static final String ROOT = "root";
    public static void loge(String... ss){
        Log.e("TAG", TextUtils.join( " | ", ss));
    }
}
