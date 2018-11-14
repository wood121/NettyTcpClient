package com.example.a80021611.annualmeetingapp.nettybussiness;

/**
 * <p>描述：(the constant of the tcp params)</p>
 * 作者： wood121<br>
 * 日期： 2018/8/28 16:47<br>
 * 版本： v1.0<br>
 */
public interface TCPConfig {
    /**
     * the ip and port to create TCP connection
     */
    String TCP_HOST = "10.8.4.58";
    int TCP_PORT = 2089;    //our server is 2089

    /**
     * connect time out
     */
    //连接超时时间
    int CONNECTION_TIMEOUT = 5 * 1000;
    //重连超时时间
    long RECONNECT_INTERVAL_TIME = 2 * 1000;
    //心跳时间间隔(单位:s)
    int HEARTBEAT_TIME = 100;

    int RECONNECTION_TIME = 3;

    /**
     * the field of normal message packet and heartbeat packet
     */
    String P_PROTOCAL_HEADER = "protocal_header";
    String P_LENGTH = "length";
    String P_PROTOCAL_VERSION = "protocal_version";
    String P_SERIALNUMBER = "serialNumber";
    String P_ISNEEDRESPONSE = "isNeedResponse";
    String P_GROUPID = "groupId";
    String P_SENDID = "sendId";
    String P_RECEIVEID = "receiveId";
    String P_COMMANDPRIORITY = "commandPriority";
    String P_COMMANDTYPE = "commandType";
    String P_COMMANDCONTENT = "commandContent";
    String P_VERIFYCODE = "verifyCode";

    int PROTOCAL_HEADER = 0x7A;

    int HEARTBEAT_LENGTH = 0x0011;
    int LENGTH_SIZE = 16;

    int PROTOCAL_VERSION = 0x0100;
    int HEARTBEAT_PROTOCAL_VERSION = 0x1000;

    int PROTOCAL_NO_NEED_RESPONSE = 0x00;
    int PROTOCAL_NEED_RESPONSE = 0x01;

    int HEARTBEAT_GROUPID = 0x0000;

    int SEND_ID_8 = 0x0010;   //8se 0x0010, 6x 0x0011
    int SEND_ID_6 = 0x0011;
    int HEARTBEAT_RECEIVEID = 0x8000;

    int HEARTBEAT_COMMAND_PRIORITY = 0x00;

    int PROTOCAL_COMMAND_TYPE_SEND = 0x1000;
    int PROTOCAL_COMMAND_TYPE_REPLY = 0x0000;
    int HEARTBEAT_COMMAND_TYPE = 0x1001;

    byte[] HEARTBEAT_COMMAND_CONTENT_BYTE = {0x00};

    int HEARTBEAT_VERIFY_CODE = 0xDB87;
}
