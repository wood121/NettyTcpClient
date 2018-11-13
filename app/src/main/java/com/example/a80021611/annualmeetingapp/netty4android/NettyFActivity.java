package com.example.a80021611.annualmeetingapp.netty4android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.example.a80021611.annualmeetingapp.R;
import com.example.a80021611.annualmeetingapp.netty4android.connection.ConnectionManager;
import com.example.a80021611.annualmeetingapp.netty4android.connection.NettyClient;
import com.example.a80021611.annualmeetingapp.netty4android.connection.NettyListener;
import com.example.a80021611.annualmeetingapp.netty4android.message.Request;
import com.example.a80021611.annualmeetingapp.netty4android.message.ResponseListener;
import com.example.a80021611.annualmeetingapp.netty4android.service.NettyService;
import com.example.a80021611.annualmeetingapp.netty4android.util.LogUtils;
import com.example.a80021611.annualmeetingapp.netty4android.util.TCPConfig;

import java.io.IOException;

public class NettyFActivity extends AppCompatActivity implements ConnectionManager.ConnectionListener, View.OnClickListener {
    private Intent mService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_netty_f);
        mService = new Intent(getApplication(), NettyService.class);
        startService(mService);
        ConnectionManager.getInstance().registerListener(this);

        initView();
    }

    private void initView() {
        findViewById(R.id.btn_need).setOnClickListener(this);
        findViewById(R.id.btn_not_need).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_need:
                sendMessage(1);
                break;
            case R.id.btn_not_need:
                sendMessage(-1);
                break;
        }
    }

    /**
     * 发送消息测试
     */
    private void sendMessage(int type) {
        Request request = NettyClient.getInstance()
                .newRequest(new byte[]{0x5e, 0x66, 0x01, 0x08},
                        type == 1,
                        TCPConfig.SEND_ID_6,
                        TCPConfig.SEND_ID_8,
                        0,
                        type == 1 ? 0x1000 : 0x0000)
                .setCallback(new ResponseListener() {
                    @Override
                    public void onSuccess(Request resopnseRequest) throws IOException {
                        LogUtils.logError("NettyFActivity", "sendMessage:onSuccess" + resopnseRequest.getSendMsgHexString());
                    }

                    @Override
                    public void onFail(int errCode) {
                        LogUtils.logError("NettyFActivity", errCode + "");
                    }
                });
        try {
            NettyClient.getInstance().sendMessage(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 监听连接状态
     */
    @Override
    public void onConnectionStatusChange(int status) {
        if (status == NettyListener.STATUS_CONNECT_SUCCESS) {
            Toast.makeText(getApplicationContext(), "连接成功", Toast.LENGTH_SHORT).show();
        } else if (status == NettyListener.STATUS_CONNECT_CLOSED) {
            Toast.makeText(getApplicationContext(), "连接关闭", Toast.LENGTH_SHORT).show();
        } else if (status == NettyListener.STATUS_CONNECT_ERROR) {
            Toast.makeText(getApplicationContext(), "连接错误", Toast.LENGTH_SHORT).show();
        } else if (status == NettyListener.STATUS_CONNECT_RECONNECT) {
            Toast.makeText(getApplicationContext(), "正在重连", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ConnectionManager.getInstance().unregisterListener(this);
        stopService(mService);
    }
}
