package com.example.a80021611.annualmeetingapp.nettylib;

/**
 * <p>描述：(这里用一句话描述这个类的作用)</p>
 * 作者： wood121<br>
 * 日期： 2018/11/14 11:24<br>
 * 版本： v2.0<br>
 */
public interface DefaultConfig {
    //重连次数
    int RECONNECTION_TIME = 3;
    //重连超时时间
    long RECONNECT_INTERVAL_TIME = 2 * 1000;
    //连接超时时间
    int CONNECTION_TIMEOUT = 5 * 1000;

    //心跳时间间隔(单位:s)
    int HEARTBEAT_TIME = 60;
}
