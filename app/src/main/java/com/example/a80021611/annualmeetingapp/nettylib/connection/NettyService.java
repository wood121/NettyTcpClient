package com.example.a80021611.annualmeetingapp.nettylib.connection;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * 1.监听网络状态
 * 2.设置监听，消息与状态
 * 3.建立tcp连接
 */
public class NettyService extends Service {
    public static final String TAG = "NettyService";
    private NetworkReceiver receiver;
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
                            Log.e(TAG, getConnectionType(info.getType()) + "连上");
                            if (networkConnectStatus == NETWORK_CONNECT_STATUS_DISCONNECT) {
                                Log.e(TAG, "网络断开后重新连接上");
                                mBinder.connect();
                            }
                            networkConnectStatus = NETWORK_CONNECT_STATUS_CONNECTED;
                        }
                    } else {
                        networkConnectStatus = NETWORK_CONNECT_STATUS_DISCONNECT;
                        Log.e(TAG, getConnectionType(info.getType()) + "断开");
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

    @Override
    public void onCreate() {
        super.onCreate();
        receiver = new NetworkReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private NettyServiceBinder mBinder = new NettyServiceBinder();

    public static class NettyServiceBinder extends Binder {
        private NettyClient nettyClient;

        public void setNettyClient(NettyClient nettyClient) {
            this.nettyClient = nettyClient;
        }

        public void connect() {
            if (!nettyClient.getConnectStatus()) {
                Thread clientThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        nettyClient.connect();//连接服务器
                    }
                });
                clientThread.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        Log.e(TAG, "onDestroy");
    }
}
