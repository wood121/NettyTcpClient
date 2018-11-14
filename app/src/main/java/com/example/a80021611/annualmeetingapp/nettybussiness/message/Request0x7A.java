package com.example.a80021611.annualmeetingapp.nettybussiness.message;

import com.example.a80021611.annualmeetingapp.nettylib.message.Request;
import com.example.a80021611.annualmeetingapp.nettylib.message.ResponseListener;
import com.example.a80021611.annualmeetingapp.nettylib.util.ByteUtil;
import com.example.a80021611.annualmeetingapp.nettybussiness.TCPConfig;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

import io.netty.util.internal.StringUtil;

/**
 * 协议头	  数据长度	 协议版本	  发送序列号	是否需要回复	集群ID	发送方网关ID	接收方网关ID	命令优先级	命令类型	数据内容	校验值
 * 0x7A	    2	    2	    4	        1	    2	        2	        2	        1	      2	       N	  2
 * eg:心跳包内容 0x7A	0x0010	0x1000	0x0001(需递增)	0x00	0x0000	0x0010	0x8000	0x00	0x1001	0x00	0xDB87
 */
public class Request0x7A implements Serializable, Request {
    private int protocal_header = TCPConfig.PROTOCAL_HEADER;
    private int length;
    private int protocal_version = TCPConfig.PROTOCAL_VERSION;
    private int serialNumber;
    private int isNeedResponse;
    private int groupId = TCPConfig.HEARTBEAT_GROUPID;
    private int sendId;
    private int receiveId;
    private int commandPriority;
    private int commandType;
    private byte[] commandContent;
    private int verifyCode;
    private int reponseVerifyCode;  //回来的数值进行判断
    private boolean isResend;   //是否是重发的消息
    private ResponseListener callback;  //监听接收的消息

    public Request0x7A getHeartBeatRequest(int serialNumber) {
        setLength(TCPConfig.HEARTBEAT_LENGTH);
        setProtocal_version(TCPConfig.HEARTBEAT_PROTOCAL_VERSION);
        setSerialNumber(serialNumber);
        setIsNeedResponse(0);
        setSendId(TCPConfig.SEND_ID_6);
//        setSendId(TCPConfig.SEND_ID_8);
        setReceiveId(TCPConfig.HEARTBEAT_RECEIVEID);
        setCommandPriority(TCPConfig.HEARTBEAT_COMMAND_PRIORITY);
        setCommandType(TCPConfig.HEARTBEAT_COMMAND_TYPE);
        setCommandContent(TCPConfig.HEARTBEAT_COMMAND_CONTENT_BYTE);
        setVerifyCode(TCPConfig.HEARTBEAT_VERIFY_CODE);
        return this;
    }

    public String getSendMsgHexString() throws IOException {
        return StringUtil.toHexString(getSendMsgByte());
    }

    @Override
    public byte[] getSendMsgByte() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(length + 5);
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        dataOutputStream.writeByte(protocal_header);
        dataOutputStream.writeShort(length);
        dataOutputStream.writeShort(protocal_version);
        dataOutputStream.writeInt(serialNumber);
        dataOutputStream.writeByte(isNeedResponse);
        dataOutputStream.writeShort(groupId);
        dataOutputStream.writeShort(sendId);
        dataOutputStream.writeShort(receiveId);
        dataOutputStream.writeByte(commandPriority);
        dataOutputStream.writeShort(commandType);
        dataOutputStream.write(commandContent);
        if (commandType == TCPConfig.HEARTBEAT_COMMAND_TYPE) {
            setVerifyCode(TCPConfig.HEARTBEAT_VERIFY_CODE);
        } else {
            //生成校验码：新消息、回复的0x0000需要计算，重发的计算出来的不变
            byte[] packet = byteArrayOutputStream.toByteArray();
            byte[] toCrc = Arrays.copyOfRange(packet, 1, packet.length);
            setVerifyCode(ByteUtil.computeChecksum(toCrc, toCrc.length));
        }
        dataOutputStream.writeShort(verifyCode);
        return byteArrayOutputStream.toByteArray();
    }

    public int getProtocal_header() {
        return protocal_header;
    }

    public void setProtocal_header(int protocal_header) {
        this.protocal_header = protocal_header;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getProtocal_version() {
        return protocal_version;
    }

    public void setProtocal_version(int protocal_version) {
        this.protocal_version = protocal_version;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getIsNeedResponse() {
        return isNeedResponse;
    }

    public void setIsNeedResponse(int isNeedResponse) {
        this.isNeedResponse = isNeedResponse;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getSendId() {
        return sendId;
    }

    public void setSendId(int sendId) {
        this.sendId = sendId;
    }

    public int getReceiveId() {
        return receiveId;
    }

    public void setReceiveId(int receiveId) {
        this.receiveId = receiveId;
    }

    public int getCommandPriority() {
        return commandPriority;
    }

    public void setCommandPriority(int commandPriority) {
        this.commandPriority = commandPriority;
    }

    public int getCommandType() {
        return commandType;
    }

    public void setCommandType(int commandType) {
        this.commandType = commandType;
    }

    public byte[] getCommandContent() {
        return commandContent;
    }

    public void setCommandContent(byte[] commandContent) {
        this.commandContent = commandContent;
    }

    public int getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(int verifyCode) {
        this.verifyCode = verifyCode;
    }

    public boolean isResend() {
        return isResend;
    }

    public void setResend(boolean resend) {
        isResend = resend;
    }

    public int getReponseVerifyCode() {
        return reponseVerifyCode;
    }

    public void setReponseVerifyCode(int reponseVerifyCode) {
        this.reponseVerifyCode = reponseVerifyCode;
    }

    @Override
    public String toString() {
        return "Request{" +
                "protocal_header=" + protocal_header +
                ", length=" + length +
                ", protocal_version=" + protocal_version +
                ", serialNumber=" + serialNumber +
                ", isNeedResponse=" + isNeedResponse +
                ", groupId=" + groupId +
                ", sendId=" + sendId +
                ", receiveId=" + receiveId +
                ", commandPriority=" + commandPriority +
                ", commandType=" + commandType +
                ", commandContent=" + Arrays.toString(commandContent) +
                ", verifyCode=" + verifyCode +
                ", reponseVerifyCode=" + reponseVerifyCode +
                ", isResend=" + isResend +
                '}';
    }

    @Override
    public Request0x7A setCallback(ResponseListener callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public ResponseListener getCallback() {
        return callback;
    }
}
