package com.example.a80021611.annualmeetingapp.netty4android.message;


import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.example.a80021611.annualmeetingapp.netty4android.connection.NettyClient;
import com.example.a80021611.annualmeetingapp.netty4android.connection.NettyListener;
import com.example.a80021611.annualmeetingapp.netty4android.util.LogUtils;
import com.example.a80021611.annualmeetingapp.netty4android.util.TCPConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * 1.消息发送成功的确认
 * 2.5s超时的确认、超时重发
 */
public class RequestManager {
    private static final String TAG = "RequestManager";
    private static RequestManager sRequestManager = new RequestManager();
    private Map<Integer, Request> mDataSendMap = new HashMap();

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg != null) {
                int serialNumber = msg.arg1;
                Request request = mDataSendMap.get(serialNumber);
                if (mDataSendMap.size() > 0 && mDataSendMap.get(serialNumber) != null) {
                    request.getCallback().onFail(NettyListener.STATUS_CONNECT_CLOSED);
                    mDataSendMap.remove(serialNumber);
                    if (!request.isResend()) {  //重发的消息只发送一次，不再重复
                        request.setResend(true);
                        resend(request);
                    }
                }
            }
        }
    };

    /**
     * 请求重发
     *
     * @param request
     */
    public void resend(Request request) {
        try {
            NettyClient.getInstance().sendMessage(request);
            LogUtils.logError(TAG, "resend === resend");
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.logError("resend", "resend error");
        }
    }

    private RequestManager() {
    }

    public static RequestManager getInstance() {
        return sRequestManager;
    }

    /**
     * 缓存需要重发的请求, 5s的超时时间
     *
     * @param request
     */
    public void add(Request request) {
        if (request != null) {
            mDataSendMap.put(request.getSerialNumber(), request);

            Message handlerMsg = Message.obtain();
            handlerMsg.arg1 = request.getSerialNumber();
            this.mHandler.sendMessageDelayed(handlerMsg, 5 * 1000);
        }
    }

    public Request getRequest(int serialNumber) {
        return mDataSendMap.get(serialNumber);
    }

    public void removeRequestBySerialNumber(int serialNumber) {
        mHandler.removeMessages(serialNumber);
        Iterator<Map.Entry<Integer, Request>> it = mDataSendMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Request> entry = it.next();
            if (serialNumber == entry.getKey()) {
                it.remove();
                return;
            }
        }
    }
}

