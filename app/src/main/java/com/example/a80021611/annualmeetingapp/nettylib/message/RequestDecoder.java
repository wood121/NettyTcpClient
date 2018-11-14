package com.example.a80021611.annualmeetingapp.nettylib.message;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

/**
 * <p>描述：(这里用一句话描述这个类的作用)</p>
 * 作者： wood121<br>
 * 日期： 2018/11/14 11:00<br>
 */
public abstract class RequestDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        bussinessDecode(channelHandlerContext, byteBuf, list);
    }

    protected abstract void bussinessDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out);
}
