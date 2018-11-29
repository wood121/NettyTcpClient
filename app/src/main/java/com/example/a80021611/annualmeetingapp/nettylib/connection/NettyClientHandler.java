package com.example.a80021611.annualmeetingapp.nettylib.connection;


import android.util.Log;

import com.example.a80021611.annualmeetingapp.nettylib.message.Request;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 1.连接与断开的回调
 * 2.接收数据的回调、状态改变的重连
 */
@ChannelHandler.Sharable
public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    private static final String TAG = "NettyClientHandler";
    private NettyListener listener;
    private NettyClient mNettyClient;

    public NettyClientHandler() {

    }

    public void setNettyListener(NettyListener listener, NettyClient nettyClient) {
        this.listener = listener;
        this.mNettyClient = nettyClient;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.e(TAG, "channelActive");
        mNettyClient.setConnectStatus(true);
        listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_SUCCESS);
        listener.sendHeartBeatMessage(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Log.e(TAG, "channelInactive");
        mNettyClient.setConnectStatus(false);
        listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_CLOSED);
        mNettyClient.reconnect();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Request request = (Request) msg;
        listener.onMessageResponse(request);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {   //写操作
                listener.sendHeartBeatMessage(ctx);
            } else if (event.state() == IdleState.READER_IDLE) {    //读操作
                // 响应超时进行网络重连
                mNettyClient.setConnectStatus(false);
                mNettyClient.reconnect();
            }
        }
    }
}
