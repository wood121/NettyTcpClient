package com.example.a80021611.annualmeetingapp.nettybussiness.message;

import com.example.a80021611.annualmeetingapp.nettylib.message.RequestDecoder;
import com.example.a80021611.annualmeetingapp.nettylib.util.ByteUtil;

import java.util.Arrays;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * <p>描述：
 * 1.将byte数组解析成Request0x7A消息对象
 * 2.上一步是ByteToMessageDecoder，解析的消息直接return
 * 3.这一步是MessageToMessageDecoder,将ByteBuf转为对象，out.add转出来
 * </p>
 * 作者： wood121<br>
 * 日期： 2018/11/13 14:11<br>
 * 版本： v2.0<br>
 */
public class Request0x7ADecoder extends RequestDecoder {

    public Request0x7ADecoder() {
    }

    @Override
    protected void bussinessDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in == null) {
            return;
        }

        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        int length = bytes.length;

        Request0x7A request0x7A = new Request0x7A();

        byte[] b_length = Arrays.copyOfRange(bytes, 1, 3);
        request0x7A.setLength(ByteUtil.byteArrayToInt(b_length));

        byte[] b_serialNumber = Arrays.copyOfRange(bytes, 5, 9);
        request0x7A.setSerialNumber(ByteUtil.byteArrayToInt(b_serialNumber));

        byte[] b_isNeedResponse = Arrays.copyOfRange(bytes, 9, 10);
        request0x7A.setIsNeedResponse(ByteUtil.byteArrayToInt(b_isNeedResponse));

        byte[] b_sendId = Arrays.copyOfRange(bytes, 12, 14);
        request0x7A.setSendId(ByteUtil.byteArrayToInt(b_sendId));

        byte[] b_receiveId = Arrays.copyOfRange(bytes, 14, 16);
        request0x7A.setReceiveId(ByteUtil.byteArrayToInt(b_receiveId));

        byte[] b_commandPriority = Arrays.copyOfRange(bytes, 16, 17);
        request0x7A.setCommandPriority(ByteUtil.byteArrayToInt(b_commandPriority));

        byte[] b_command_type = Arrays.copyOfRange(bytes, 17, 19);
        request0x7A.setCommandType(ByteUtil.byteArrayToInt(b_command_type));

        byte[] b_commandContent = Arrays.copyOfRange(bytes, 19, length - 2);
        request0x7A.setCommandContent(b_commandContent);

        byte[] b_crc = Arrays.copyOfRange(bytes, length - 2, length);
        request0x7A.setVerifyCode(ByteUtil.byteArrayToInt(b_crc));

        byte[] toCrc = Arrays.copyOfRange(bytes, 1, length - 2);
        request0x7A.setReponseVerifyCode(ByteUtil.computeChecksum(toCrc, toCrc.length));

        out.add(request0x7A);
    }
}
