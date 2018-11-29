package com.example.a80021611.annualmeetingapp.nettybussiness;

import android.content.Context;
import android.util.Log;

import com.example.a80021611.annualmeetingapp.nettybussiness.message.Request0x7A;
import com.example.a80021611.annualmeetingapp.nettybussiness.message.Request0x7ADecoder;
import com.example.a80021611.annualmeetingapp.nettybussiness.message.Request0x7AEncoder;
import com.example.a80021611.annualmeetingapp.nettybussiness.message.RequestManager;
import com.example.a80021611.annualmeetingapp.nettylib.connection.FutureListener;
import com.example.a80021611.annualmeetingapp.nettylib.connection.LenthDecoderParamsBean;
import com.example.a80021611.annualmeetingapp.nettylib.connection.NettyClient;
import com.example.a80021611.annualmeetingapp.nettylib.connection.NettyListener;
import com.example.a80021611.annualmeetingapp.nettylib.message.Request;
import com.example.a80021611.annualmeetingapp.nettylib.util.ByteUtil;

import java.io.IOException;

import io.netty.channel.ChannelHandlerContext;

/**
 * <p>描述：
 * 1.build模式初始化数据   --ok
 * 2.开启服务   --ok
 * 3.发送数据
 * 4.接收数据，业务处理
 * </p>
 * <p>
 * 作者： wood121<br>
 * 日期： 2018/11/13 15:50<br>
 * 版本： v2.0<br>
 */
public class NettyBussinessManager implements NettyListener {
    public static final String TAG = "NettyBussinessManager";
    private static NettyBussinessManager mNettyClientManager;
    private NettyClient mNettyClient;
    private int heartBeatCount = 1;
    private int serialNumber = 1;
    private HBListener mHbListener;

    private NettyBussinessManager() {

    }

    public static NettyBussinessManager getInstance() {
        if (mNettyClientManager == null) {
            synchronized (NettyBussinessManager.class) {
                if (mNettyClientManager == null) {
                    mNettyClientManager = new NettyBussinessManager();
                }
            }
        }
        return mNettyClientManager;
    }

    public void init(Context context) {
        if (mNettyClient == null) {
            mNettyClient = new NettyClient.Builder()
                    .initcontext(context)
                    .hostAndPort(TCPConfig.TCP_HOST, TCPConfig.TCP_PORT)
                    .decoder(new LenthDecoderParamsBean(33, 1, 2, 2, 0))
                    .requestDecoder(new Request0x7ADecoder())
                    .encoder(new Request0x7AEncoder())
                    .build()
                    .startNettyService()
                    .setNettyListener(this);
        }
    }

    public void onDestroy() {
        if (mNettyClient != null) {
            mNettyClient.onDestroy();
            mNettyClient = null;
        }
    }

    private int receiveNum = 0;

    @Override
    public void onMessageResponse(Request request) throws IOException {
//        Log.e(TAG, "当前线程：" + Thread.currentThread().getName());
        Request0x7A responseRequest0x7A = (Request0x7A) request;
        if (TCPConfig.HEARTBEAT_COMMAND_TYPE != responseRequest0x7A.getCommandType()) {
            Log.e(TAG, "channelRead=1111=" + responseRequest0x7A.getSendMsgHexString());
            if (responseRequest0x7A.getReponseVerifyCode() == responseRequest0x7A.getVerifyCode()) {
                switch (responseRequest0x7A.getCommandType()) {
                    case TCPConfig.PROTOCAL_COMMAND_TYPE_SEND:
                        //the new msg,if isNeedResponse is 01,you've to reply,or you've to deal with it.
                        onNewMessage(responseRequest0x7A);
                        break;
                    case TCPConfig.PROTOCAL_COMMAND_TYPE_REPLY:
                        //the message you've sent,check it and remove the storage
                        onReplyMessage(responseRequest0x7A);
                        break;
                }
            } else {
                Log.e(TAG, "the message is not safe,may changed by the others");
            }
        } else {
            Log.e(TAG, "channelRead=2222=" + responseRequest0x7A.getSendMsgHexString());
            receiveNum++;
            mHbListener.msgReceive(receiveNum);
        }
    }

    //0x0000,校验发送结果
    private void onReplyMessage(Request0x7A responseRequest0x7A) {
        //TODO send_id need to modify if used in project
        int serialNumber = responseRequest0x7A.getSerialNumber();
        Request0x7A request0x7A = RequestManager.getInstance().getRequest(serialNumber);
        if (request0x7A != null) {
            request0x7A.getCallback().onSuccess(responseRequest0x7A);
            RequestManager.getInstance().removeRequestBySerialNumber(serialNumber);
        }
        Log.e(TAG, "onReplyMessage:::the message has been removed == " + (null == RequestManager.getInstance().getRequest(serialNumber)));
    }

    /**
     * 0x1000，
     * 需要回复的，类型修改为0x0000，校验码更新；
     * 将数据内容下发
     */
    private void onNewMessage(Request0x7A responseRequest0x7A) {
        if (TCPConfig.PROTOCAL_NEED_RESPONSE == responseRequest0x7A.getIsNeedResponse()) {
            responseRequest0x7A.setCommandType(0x0000);
            mNettyClient.sendMessage(responseRequest0x7A, new FutureListener() {
                @Override
                public void success() {
                    Log.e(TAG, "onNewMessage===消息回复成功");
                }

                @Override
                public void error() {

                }
            });
        }

        //取出消息分发出去  TODO
        byte[] commandContent = responseRequest0x7A.getCommandContent();
        Log.e(TAG, "onNewMessage == content" + ByteUtil.bytes2hexString(commandContent));
    }

    @Override
    public void onServiceStatusConnectChanged(final int statusCode) {
        if (statusCode == NettyListener.STATUS_CONNECT_SUCCESS) {
            Log.e(TAG, "connect sucessful statusCode = " + statusCode + " 连接成功");
        } else {
            if (statusCode == NettyListener.STATUS_CONNECT_CLOSED) {
                Log.e(TAG, "connect fail statusCode = " + statusCode + " 服务器断开连接");
            } else if (statusCode == NettyListener.STATUS_CONNECT_RECONNECT) {
                Log.e(TAG, "connect fail statusCode = " + statusCode + " 尝试重新连接");
            }
        }
    }

    private int msgSend = 0;

    @Override
    public void sendHeartBeatMessage(ChannelHandlerContext ctx) {
        try {
            if (heartBeatCount > 5000) {
                return;
            }
            if (heartBeatCount == Integer.MAX_VALUE) heartBeatCount = 1;
            Request0x7A request0x7A = new Request0x7A().getHeartBeatRequest(heartBeatCount);
            heartBeatCount++;
            sendMessage(request0x7A, new FutureListener() {
                @Override
                public void success() throws IOException {
                    msgSend++;
                    mHbListener.msgSend(msgSend);
                }

                @Override
                public void error() {

                }
            });
        } catch (Exception e) {

        }
    }

    public Request0x7A newRequest(byte[] comandContent, boolean isNeedResponse, int sendId, int receiveId, int commandPriority, int commandType) {
        Request0x7A request0x7A = new Request0x7A();
        request0x7A.setCommandContent(comandContent);
        request0x7A.setLength(comandContent.length + TCPConfig.LENGTH_SIZE);
        request0x7A.setIsNeedResponse(isNeedResponse ? 1 : 0);
        request0x7A.setSendId(sendId);
        request0x7A.setReceiveId(receiveId);
        request0x7A.setCommandPriority(commandPriority);
        request0x7A.setCommandType(commandType);
        return request0x7A;
    }

    public void sendMessage(Request0x7A request0x7A) throws IOException {
        sendMessage(request0x7A, null);
    }

    public void sendMessage(Request0x7A request0x7A, FutureListener futureListener) {
        /*
        1.重发、回复0x0000都不需要递增 serialNumber
        2.新消息0x1000，添加serialNumber和verifyCode（在拼接字节数组的时候添加的）,要回复的消息添加消息管理
         */
        if (TCPConfig.PROTOCAL_COMMAND_TYPE_SEND == request0x7A.getCommandType() && !request0x7A.isResend()) {
            if (serialNumber == Integer.MAX_VALUE) {
                serialNumber = 1;
            }
            request0x7A.setSerialNumber(serialNumber);
            serialNumber++;
        }

        //0X1000的消息，需要回复的，
        if (TCPConfig.PROTOCAL_COMMAND_TYPE_SEND == request0x7A.getCommandType() && TCPConfig.PROTOCAL_NEED_RESPONSE == request0x7A.getIsNeedResponse()) {
            RequestManager.getInstance().add(request0x7A);
        }

        mNettyClient.sendMessage(request0x7A, futureListener);
    }

    public void setHBListener(HBListener hbListener) {
        this.mHbListener = hbListener;
    }
}
