package com.example.a80021611.annualmeetingapp.nettybussiness;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.a80021611.annualmeetingapp.R;
import com.example.a80021611.annualmeetingapp.nettybussiness.message.Request0x7A;
import com.example.a80021611.annualmeetingapp.nettylib.message.Request;
import com.example.a80021611.annualmeetingapp.nettylib.message.ResponseListener;

import java.io.IOException;

public class NettyFActivity extends AppCompatActivity implements View.OnClickListener, HBListener {

    private TextView mTvSend;
    private TextView mTvReceive;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_netty_f);
        NettyBussinessManager.getInstance().init(getApplicationContext());
        initView();

        NettyBussinessManager.getInstance().setHBListener(this);
    }

    private void initView() {
        findViewById(R.id.btn_need).setOnClickListener(this);
        findViewById(R.id.btn_not_need).setOnClickListener(this);
        mTvSend = findViewById(R.id.tv_send);
        mTvReceive = findViewById(R.id.tv_receive);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_need:
                sendMessage(1);
                break;
            case R.id.btn_not_need:
//                sendMessage(-1);
                NettyBussinessManager.getInstance().onDestroy();
                break;
        }
    }

    /**
     * 发送消息测试
     */
    private void sendMessage(int type) {
        Request0x7A request0x7A = NettyBussinessManager
                .getInstance()
                .newRequest(new byte[]{0x5e, 0x66, 0x01, 0x08},
                        type == 1,
                        TCPConfig.SEND_ID_6,
                        TCPConfig.SEND_ID_8,
                        0,
                        type == 1 ? 0x1000 : 0x0000)
                .setCallback(new ResponseListener() {

                    @Override
                    public void onSuccess(Request resopnseRequest) {
                        Request0x7A request0x7A = (Request0x7A) resopnseRequest;
                        try {
                            Log.e("NettyFActivity", "request0x7A===" + request0x7A.getSendMsgHexString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFail(int errCode) {
                        Log.e("NettyFActivity", "onFail===" + errCode);
                    }
                });
        try {
            NettyBussinessManager.getInstance().sendMessage(request0x7A);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NettyBussinessManager.getInstance().onDestroy();
    }

    @Override
    public void msgSend(int num) {
        mTvSend.setText("已发送的数据量:" + num);
    }

    @Override
    public void msgReceive(int num) {
        mTvReceive.setText("已接收的数据量:" + num);
    }
}
