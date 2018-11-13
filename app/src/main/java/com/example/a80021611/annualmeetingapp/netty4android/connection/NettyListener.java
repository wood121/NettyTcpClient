package com.example.a80021611.annualmeetingapp.netty4android.connection;


import com.example.a80021611.annualmeetingapp.netty4android.message.Request;

import java.io.IOException;

/**
 * <p>描述：(这里用一句话描述这个类的作用)</p>
 * 作者： wood121<br>
 * 日期： 2018/8/28 16:47<br>
 * 版本： v1.0<br>
 */
public interface NettyListener {
    byte STATUS_CONNECT_ERROR = 0;
    byte STATUS_CONNECT_SUCCESS = 1;
    byte STATUS_CONNECT_CLOSED = 2;
    byte STATUS_CONNECT_RECONNECT = 3;

    /**
     * 对消息的处理
     *
     * @param responseRequest
     */
    void onMessageResponse(Request responseRequest) throws IOException;

    /**
     * 当服务状态发生变化时触发
     */
    void onServiceStatusConnectChanged(int statusCode);
}
