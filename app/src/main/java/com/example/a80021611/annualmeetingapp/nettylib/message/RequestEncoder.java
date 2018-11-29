package com.example.a80021611.annualmeetingapp.nettylib.message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * <p>描述：(这里用一句话描述这个类的作用)</p>
 * 作者： wood121<br>
 * 日期： 2018/11/14 10:46<br>
 * 版本： v2.0<br>
 */

@ChannelHandler.Sharable
public abstract class RequestEncoder extends MessageToByteEncoder<Request> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Request request, ByteBuf byteBuf) {
        bussinessEncode(channelHandlerContext, request, byteBuf);
    }

    protected abstract void bussinessEncode(ChannelHandlerContext channelHandlerContext, Request request, ByteBuf byteBuf);
}
