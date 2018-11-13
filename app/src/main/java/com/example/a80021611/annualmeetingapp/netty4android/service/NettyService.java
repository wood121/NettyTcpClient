package com.example.a80021611.annualmeetingapp.netty4android.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.example.a80021611.annualmeetingapp.netty4android.connection.ConnectionManager;
import com.example.a80021611.annualmeetingapp.netty4android.connection.FutureListener;
import com.example.a80021611.annualmeetingapp.netty4android.connection.NettyClient;
import com.example.a80021611.annualmeetingapp.netty4android.connection.NettyListener;
import com.example.a80021611.annualmeetingapp.netty4android.message.Request;
import com.example.a80021611.annualmeetingapp.netty4android.message.RequestManager;
import com.example.a80021611.annualmeetingapp.netty4android.util.ByteUtil;
import com.example.a80021611.annualmeetingapp.netty4android.util.LogUtils;
import com.example.a80021611.annualmeetingapp.netty4android.util.TCPConfig;

import java.io.IOException;

/**
 * 1.监听网络状态
 * 2.设置监听，消息与状态
 * 3.建立tcp连接
 */
public class NettyService extends Service implements NettyListener {
    public static final String TAG = "NettyService";
    private NetworkReceiver receiver;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private static final int PUSHTYPE_REQUEST = 1;
    private static final int NETWORK_CONNECT_STATUS_CONNECTED = 1;
    private static final int NETWORK_CONNECT_STATUS_DISCONNECT = -1;
    private int networkConnectStatus = 0;

    public class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                NetworkInfo info = intent
                        .getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (info != null) {
                    //如果当前的网络连接成功并且网络连接可用
                    if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {
                        if (info.getType() == ConnectivityManager.TYPE_WIFI
                                || info.getType() == ConnectivityManager.TYPE_MOBILE) {
                            LogUtils.log(TAG, getConnectionType(info.getType()) + "连上");
                            if (networkConnectStatus == NETWORK_CONNECT_STATUS_DISCONNECT) {
                                LogUtils.log(TAG, "网络断开后重新连接上");
                                connect();
                            }
                            networkConnectStatus = NETWORK_CONNECT_STATUS_CONNECTED;
                        }
                    } else {
                        networkConnectStatus = NETWORK_CONNECT_STATUS_DISCONNECT;
                        LogUtils.log(TAG, getConnectionType(info.getType()) + "断开");
                    }
                }
            }
        }
    }

    private String getConnectionType(int type) {
        String connType = "";
        if (type == ConnectivityManager.TYPE_MOBILE) {
            connType = "移动数据";
        } else if (type == ConnectivityManager.TYPE_WIFI) {
            connType = "WIFI网络";
        }
        return connType;
    }

    public NettyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        receiver = new NetworkReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NettyClient.getInstance().setContext(getApplicationContext());
        NettyClient.getInstance().setListener(this);
        connect();
        return super.onStartCommand(intent, flags, startId);
    }

    private void connect() {
        if (!NettyClient.getInstance().getConnectStatus()) {
            Thread clientThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    NettyClient.getInstance().connect();//连接服务器
                }
            });
            clientThread.start();
        }
    }

    @Override
    public void onMessageResponse(final Request responseRequest) throws IOException {
        Log.e(TAG, "onMessageResponse === " + responseRequest.toString());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                switch (responseRequest.getCommandType()) {
                    case TCPConfig.PROTOCAL_COMMAND_TYPE_SEND:
                        //the new msg,if isNeedResponse is 01,you've to reply,or you've to deal with it.
                        try {
                            onNewMessage(responseRequest);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case TCPConfig.PROTOCAL_COMMAND_TYPE_REPLY:
                        //the message you've sent,check it and remove the storage
                        try {
                            onReplyMessage(responseRequest);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });
    }

    //0x0000,校验发送结果
    private void onReplyMessage(Request responseRequest) throws IOException {
        //TODO send_id need to modify if used in project
        int serialNumber = responseRequest.getSerialNumber();
        Request request = RequestManager.getInstance().getRequest(serialNumber);
        if (request != null) {
            request.getCallback().onSuccess(responseRequest);
            RequestManager.getInstance().removeRequestBySerialNumber(serialNumber);
        }
        LogUtils.logError(TAG, "onReplyMessage:::the message has been removed == " + (null == RequestManager.getInstance().getRequest(serialNumber)));
    }

    /**
     * 0x1000，
     * 需要回复的，类型修改为0x0000，校验码更新；
     * 将数据内容下发
     */
    private void onNewMessage(Request responseRequest) throws IOException {
        if (TCPConfig.PROTOCAL_NEED_RESPONSE == responseRequest.getIsNeedResponse()) {
            responseRequest.setCommandType(0x0000);
            NettyClient.getInstance().sendMessage(responseRequest, new FutureListener() {
                @Override
                public void success() {
                    LogUtils.logError(TAG, "onNewMessage===消息回复成功");
                }

                @Override
                public void error() {

                }
            });
        }

        //取出消息分发出去  TODO
        byte[] commandContent = responseRequest.getCommandContent();
        LogUtils.logError(TAG, "onNewMessage == content" + ByteUtil.bytes2hexString(commandContent));
    }

    @Override
    public void onServiceStatusConnectChanged(final int statusCode) {
        if (statusCode == NettyListener.STATUS_CONNECT_SUCCESS) {
            LogUtils.logError(TAG, "connect sucessful statusCode = " + statusCode + " 连接成功");
        } else {
            if (statusCode == NettyListener.STATUS_CONNECT_CLOSED) {
                LogUtils.logError(TAG, "connect fail statusCode = " + statusCode + " 服务器断开连接");
            } else if (statusCode == NettyListener.STATUS_CONNECT_RECONNECT) {
                LogUtils.logError(TAG, "connect fail statusCode = " + statusCode + " 尝试重新连接");
            }
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                    LogUtils.log(TAG, "this is main thread");
                }
                ConnectionManager.getInstance().dispatch(statusCode);
            }
        });
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        NettyClient.getInstance().setReconnectNum(0);
        NettyClient.getInstance().disconnect();
        Log.e(TAG, "onDestroy");
    }
}
