package com.example.a80021611.annualmeetingapp.nettybussiness.message;


import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.a80021611.annualmeetingapp.nettybussiness.NettyBussinessManager;
import com.example.a80021611.annualmeetingapp.nettylib.connection.NettyListener;

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
    private Map<Integer, Request0x7A> mDataSendMap = new HashMap();

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg != null) {
                int serialNumber = msg.arg1;
                Request0x7A request0x7A = mDataSendMap.get(serialNumber);
                if (mDataSendMap.size() > 0 && mDataSendMap.get(serialNumber) != null) {
                    request0x7A.getCallback().onFail(NettyListener.STATUS_CONNECT_CLOSED);
                    mDataSendMap.remove(serialNumber);
                    if (!request0x7A.isResend()) {  //重发的消息只发送一次，不再重复
                        request0x7A.setResend(true);
                        resend(request0x7A);
                    }
                }
            }
        }
    };

    /**
     * 请求重发
     *
     * @param request0x7A
     */
    public void resend(Request0x7A request0x7A) {
        try {
            NettyBussinessManager.getInstance().sendMessage(request0x7A);
            Log.e(TAG, "resend === resend");
        } catch (IOException e) {
            Log.e("resend", "resend error:" + e.toString());
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
     * @param request0x7A
     */
    public void add(Request0x7A request0x7A) {
        if (request0x7A != null) {
            mDataSendMap.put(request0x7A.getSerialNumber(), request0x7A);

            Message handlerMsg = Message.obtain();
            handlerMsg.arg1 = request0x7A.getSerialNumber();
            this.mHandler.sendMessageDelayed(handlerMsg, 5 * 1000);
        }
    }

    public Request0x7A getRequest(int serialNumber) {
        return mDataSendMap.get(serialNumber);
    }

    public void removeRequestBySerialNumber(int serialNumber) {
        mHandler.removeMessages(serialNumber);
        Iterator<Map.Entry<Integer, Request0x7A>> it = mDataSendMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Request0x7A> entry = it.next();
            if (serialNumber == entry.getKey()) {
                it.remove();
                return;
            }
        }
    }
}

