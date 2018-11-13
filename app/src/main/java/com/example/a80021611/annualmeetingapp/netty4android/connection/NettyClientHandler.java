package com.example.a80021611.annualmeetingapp.netty4android.connection;


import android.util.Log;

import com.example.a80021611.annualmeetingapp.R;
import com.example.a80021611.annualmeetingapp.netty4android.message.Request;
import com.example.a80021611.annualmeetingapp.netty4android.message.ResponseListener;
import com.example.a80021611.annualmeetingapp.netty4android.util.ByteUtil;
import com.example.a80021611.annualmeetingapp.netty4android.util.LogUtils;
import com.example.a80021611.annualmeetingapp.netty4android.util.TCPConfig;

import java.util.HashMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.internal.StringUtil;

/**
 * 1.连接与断开的回调
 * 2.接收数据的回调、状态改变的重连
 */

public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    private static final String TAG = "NettyClientHandler";
    private NettyListener listener;
    private String mMessage;

    public NettyClientHandler(NettyListener listener) {
        this.listener = listener;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.e(TAG, "channelActive");
        NettyClient.getInstance().setConnectStatus(true);
        listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_SUCCESS);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Log.e(TAG, "channelInactive");
        NettyClient.getInstance().setConnectStatus(false);
        listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_CLOSED);
        NettyClient.getInstance().reconnect();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);

        Log.e(TAG, "channelRead=1111=" + ByteUtil.bytes2hexString(req));

        Request reponseRequest = ByteUtil.getResponseRequest(req);
        if (reponseRequest.getCommandType() != 0x1001) {
            if (reponseRequest.getReponseVerifyCode() == reponseRequest.getVerifyCode()) {
                listener.onMessageResponse(reponseRequest);
            } else {
                Log.e(TAG, "the message is not safe,may changed by the others");
            }
        } else {
            Log.e(TAG, "channelRead=2222=" + reponseRequest.getSendMsgHexString(true));
        }
    }

    /**
     * this.length = reqInfo.get("length");
     * this.serialNumber = reqInfo.get("serialNumber");
     * this.isNeedResponse = reqInfo.get("isNeedResponse");
     * this.sendId = reqInfo.get("sendId");
     * this.receiveId = reqInfo.get("receiveId");
     * this.commandPriority = reqInfo.get("commandPriority");
     * this.commandType = reqInfo.get("commandType");
     * this.commandContent = reqInfo.get("commandContent");
     * this.verifyCode = reqInfo.get("verifyCode");
     * this.callback = callback;
     */
    private int serialNumber = 2;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                sendMsgByte(ctx);
            } else if (event.state() == IdleState.READER_IDLE) {
                // 响应超时进行网络重连
                NettyClient.getInstance().setConnectStatus(false);
                NettyClient.getInstance().reconnect();
            }
        }
    }


    private void sendMsgByte(ChannelHandlerContext ctx) {
        try {
            Request request = new Request();
            if (serialNumber == Integer.MAX_VALUE) {
                serialNumber = 2;
            }
            request.setSerialNumber(serialNumber);
            serialNumber++;
            byte[] sendMsgByte = request.getSendMsgByte(true);
            String sendMsgHexString = StringUtil.toHexString(sendMsgByte);
            this.mMessage = sendMsgHexString;
            ctx.channel().writeAndFlush(Unpooled.copiedBuffer(sendMsgByte)).addListener(new FutureListener() {
                @Override
                public void success() {
                    Log.e(TAG, " --发送成功:" + mMessage);
                }

                @Override
                public void error() {

                }
            });
        } catch (Exception e) {

        }
    }

}
