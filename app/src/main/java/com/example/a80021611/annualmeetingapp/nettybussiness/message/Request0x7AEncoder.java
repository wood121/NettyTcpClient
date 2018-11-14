package com.example.a80021611.annualmeetingapp.nettybussiness.message;


import android.util.Log;

import com.example.a80021611.annualmeetingapp.nettybussiness.TCPConfig;
import com.example.a80021611.annualmeetingapp.nettybussiness.message.Request0x7A;
import com.example.a80021611.annualmeetingapp.nettylib.message.Request;
import com.example.a80021611.annualmeetingapp.nettylib.message.RequestEncoder;
import com.example.a80021611.annualmeetingapp.nettylib.util.ByteUtil;

import java.io.IOException;
import java.util.Arrays;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * <p>描述：(这里用一句话描述这个类的作用)</p>
 * 作者： wood121<br>
 * 日期： 2018/11/13 14:13<br>
 * 版本： v2.0<br>
 */
public class Request0x7AEncoder extends RequestEncoder {
    @Override
    protected void bussinessEncode(ChannelHandlerContext channelHandlerContext, Request request, ByteBuf byteBuf) {
        Request0x7A request0x7A = (Request0x7A) request;
        if (null == request0x7A) {
            throw new IllegalArgumentException("request is null");
        }
        try {
            byte[] sendMsgByte = request0x7A.getSendMsgByte();
            byteBuf.writeBytes(sendMsgByte);
            Log.e("Request0x7AEncoder", ByteUtil.bytes2hexString(sendMsgByte));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


//        byteBuf.writeByte(request0x7A.getProtocal_header());
//        byteBuf.writeShort(request0x7A.getLength());
//        byteBuf.writeShort(request0x7A.getProtocal_version());
//        byteBuf.writeInt(request0x7A.getSerialNumber());
//        byteBuf.writeByte(request0x7A.getIsNeedResponse());
//        byteBuf.writeShort(request0x7A.getGroupId());
//        byteBuf.writeShort(request0x7A.getSendId());
//        byteBuf.writeShort(request0x7A.getReceiveId());
//        byteBuf.writeByte(request0x7A.getCommandPriority());
//        byteBuf.writeShort(request0x7A.getCommandType());
//        byteBuf.writeBytes(request0x7A.getCommandContent());
