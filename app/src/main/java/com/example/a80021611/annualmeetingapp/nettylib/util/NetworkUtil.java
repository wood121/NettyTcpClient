package com.example.a80021611.annualmeetingapp.nettylib.util;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 获取当前网络状况
 *
 */
public class NetworkUtil {
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        NetworkInfo netWorkInfo = manager.getActiveNetworkInfo();
        if (netWorkInfo == null) {
            return false;
        }
        return netWorkInfo.isAvailable();
    }
}
