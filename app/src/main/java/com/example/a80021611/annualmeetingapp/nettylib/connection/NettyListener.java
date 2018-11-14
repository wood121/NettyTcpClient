package com.example.a80021611.annualmeetingapp.nettylib.connection;


import com.example.a80021611.annualmeetingapp.nettylib.message.Request;

import java.io.IOException;

import io.netty.channel.ChannelHandlerContext;

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
     * 发送心跳包
     *
     * @param ctx
     */
    void sendHeartBeatMessage(ChannelHandlerContext ctx);

    /**
     * 当服务状态发生变化时触发
     */
    void onServiceStatusConnectChanged(int statusCode);

    /**
     * 对消息的处理
     *
     * @param responseRequest0x7A
     */
    void onMessageResponse(Request responseRequest0x7A) throws IOException;
}
