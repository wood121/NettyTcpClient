package com.example.a80021611.annualmeetingapp.nettylib.connection;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.a80021611.annualmeetingapp.nettybussiness.TCPConfig;
import com.example.a80021611.annualmeetingapp.nettylib.DefaultConfig;
import com.example.a80021611.annualmeetingapp.nettylib.message.Request;
import com.example.a80021611.annualmeetingapp.nettylib.message.RequestDecoder;
import com.example.a80021611.annualmeetingapp.nettylib.message.RequestEncoder;
import com.example.a80021611.annualmeetingapp.nettylib.util.ByteUtil;
import com.example.a80021611.annualmeetingapp.nettylib.util.NetworkUtil;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 1.初始化参数
 * 2.启动连接服务、消息接收监听方法
 * 3.消息发送
 */
public class NettyClient {
    public static final String TAG = NettyClient.class.getSimpleName();

    private Context mContext;
    //连接的ip,端口
    private String mHost;
    private int mPort;
    //心跳包管理类
    private IdleStateHandler mIdleStateHandler;
    //数据编解码类
    private RequestEncoder mRequestEncoder;
    private RequestDecoder mRequestDecoder;
    private LengthFieldBasedFrameDecoder mLengthFieldBasedFrameDecoder;
    //消息回调分发处理
    private NettyClientHandler mClientHandler;
    //重连超时时间
    private long mReconnectIntervalTime;
    //重连次数
    private int mReconnectNum;

    //已经连接
    private boolean isConnected = false;
    //正在连接
    private boolean isConnecting = false;
    //正在重连
    private boolean isReconnecting = false;
    //尝试重连
    private boolean isDoReconnect = true;

    private ServiceConnection mServiceConnection;
    private NettyService.NettyServiceBinder mBinder;
    private EventLoopGroup group;
    private NettyListener listener;
    private Channel channel;

    public NettyClient() {
        this(new NettyClient.Builder());
    }

    NettyClient(Builder builder) {
        this.mHost = builder.host;
        this.mPort = builder.port;
        this.mContext = builder.context;
        this.mIdleStateHandler = builder.idleStateHandler;
        this.mRequestEncoder = builder.encoder;
        this.mLengthFieldBasedFrameDecoder = builder.decoder;
        this.mRequestDecoder = builder.requestDecoder;
        this.mReconnectNum = builder.reconnectNum;
        this.mReconnectIntervalTime = builder.reconnectIntervalTime;
        this.mClientHandler = builder.clientHandler;
        this.mServiceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBinder = (NettyService.NettyServiceBinder) service;
                mBinder.setNettyClient(NettyClient.this);
                mBinder.connect();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
    }

    public NettyClient.Builder newBuilder() {
        return new NettyClient.Builder(this);
    }

    public NettyClient startNettyService() {
        Intent intent = new Intent(mContext, NettyService.class);
        mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        return this;
    }

    public NettyClient setNettyListener(NettyListener listener) {
        this.listener = listener;
        getClientHandler().setNettyListener(listener, this);
        return this;
    }

    NettyClientHandler getClientHandler() {
        return mClientHandler;
    }

    //set the connection state, called in the NettyClientHandler
    public void setConnectStatus(boolean status) {
        this.isConnected = status;
    }

    //get the connection state,called in the NettyService
    public boolean getConnectStatus() {
        return isConnected;
    }

    //是否重连
    private void setDoReconnect(boolean doReconnect) {
        isDoReconnect = doReconnect;
    }

    public void setReconnectNum(int reconnectNum) {
        mReconnectNum = reconnectNum;
    }

    //TCP connection
    public synchronized void connect() {
        if (isConnecting) {
            return;
        }
        isDoReconnect = true;
        ChannelFuture channelFuture = null;
        if (!isConnected) {
            isConnecting = true;
            group = new NioEventLoopGroup();
            Bootstrap mBootstrap = new Bootstrap()
                    .group(group)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DefaultConfig.CONNECTION_TIMEOUT)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast("ping", mIdleStateHandler);
                            pipeline.addLast(mRequestEncoder);
                            pipeline.addLast(mLengthFieldBasedFrameDecoder);
                            pipeline.addLast(mRequestDecoder);
                            pipeline.addLast(mClientHandler);
                        }
                    });
            try {
                channelFuture = mBootstrap
                        .connect(mHost, mPort)
                        .addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture future) {
                                isConnecting = false;
                                isReconnecting = false;
                                if (future != null && future.isSuccess()) {
                                    isConnected = true;
                                    channel = future.channel();
                                    Log.e(TAG, "连接成功");
                                } else {
                                    isConnected = false;
                                    reconnect();
                                    Log.e(TAG, "连接失败");
                                }
                            }
                        }).sync();
                channelFuture.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isConnected = false;
                isConnecting = false;
                isReconnecting = false;
                listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_CLOSED);
                if (null != channelFuture && channelFuture.channel() != null && channelFuture.channel().isOpen()) {
                    channelFuture.channel().close();
                }
                group.shutdownGracefully();
                reconnect();
            }
        }
    }

    //TCP reconnection
    public synchronized void reconnect() {
        Log.e(TAG, "reconnect is called");
        if (!isDoReconnect) {
            return;
        }
        // 有网络且没有重连则进行重连
        if (NetworkUtil.isNetworkAvailable(mContext) && !isReconnecting) {
            if (!isConnected && mReconnectNum > 0) {
                try {
                    Thread.sleep(mReconnectIntervalTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                disconnect();
                connect();
                isReconnecting = true;
                listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_RECONNECT);
                mReconnectNum--;
            } else {
                mReconnectNum = DefaultConfig.RECONNECTION_TIME;
                disconnect();
            }
        }
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        Log.e(TAG, "disconnect is called");
        if (group != null) {
            group.shutdownGracefully();
            isReconnecting = false;
            isConnecting = false;
            isConnected = false;
        }
    }

    /**
     * 断开连接，不自动重连了
     */
    public void onDestroy() {
        if (mContext != null) {
            mContext.unbindService(mServiceConnection);
        }
        if (isConnected) {
            setDoReconnect(false);
            setReconnectNum(0);
            disconnect();
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        }
    }

    /**
     * @param futureListener the callback to know whether you've sent successful
     */
    public void sendMessage(final Request request, FutureListener futureListener) {
        // 判断网络状况，如果有网络并且没有进行重连，则尝试连接
        boolean flag = channel != null && isConnected;
        if (!flag) {
            request.getCallback().onFail(NettyListener.STATUS_CONNECT_CLOSED);
            Toast.makeText(mContext, "Please check your network connection", Toast.LENGTH_SHORT).show();
            if (NetworkUtil.isNetworkAvailable(mContext) && !isReconnecting) {
                connect();
            }
            return;
        }

        // send the request
        if (futureListener == null) {
            channel.writeAndFlush(request)
                    .addListener(new FutureListener() {
                        @Override
                        public void success() throws IOException {
                            Log.e(TAG, "发送成功--->" + ByteUtil.bytes2hexString(request.getSendMsgByte()));
                        }

                        @Override
                        public void error() {
                            Log.e(TAG, "发送失败--->error");
                            // 这里响应后后不移除回调，重连后会重新请求
                            request.getCallback().onFail(NettyListener.STATUS_CONNECT_ERROR);
                            reconnect();
                        }
                    });
        } else {
            channel.writeAndFlush(request).addListener(futureListener);
        }
    }

    public static final class Builder {

        private String host;
        private int port;
        private IdleStateHandler idleStateHandler;
        private RequestEncoder encoder;
        private LengthFieldBasedFrameDecoder decoder;
        private RequestDecoder requestDecoder;
        private int reconnectNum;   //重连次数
        private long reconnectIntervalTime; //重连超时时间
        private Context context;
        private NettyClientHandler clientHandler;

        public Builder() {
            this.reconnectNum = DefaultConfig.RECONNECTION_TIME;
            this.reconnectIntervalTime = DefaultConfig.RECONNECT_INTERVAL_TIME;
            this.idleStateHandler = new IdleStateHandler(0, DefaultConfig.HEARTBEAT_TIME, 0, TimeUnit.SECONDS);
            this.clientHandler = new NettyClientHandler();
        }

        public Builder(NettyClient nettyClient) {
            this.host = nettyClient.mHost;
            this.port = nettyClient.mPort;
            this.idleStateHandler = nettyClient.mIdleStateHandler;
            this.encoder = nettyClient.mRequestEncoder;
            this.decoder = nettyClient.mLengthFieldBasedFrameDecoder;
            this.reconnectNum = nettyClient.mReconnectNum;
            this.reconnectIntervalTime = nettyClient.mReconnectIntervalTime;
            this.context = nettyClient.mContext;
        }

        public Builder initcontext(Context context) {
            this.context = context;
            return this;
        }

        public Builder hostAndPort(String host, int port) {
            this.host = host;
            this.port = port;
            return this;
        }

        public Builder idleStateHandler(IdleStateHandler idleStateHandler) {
            this.idleStateHandler = idleStateHandler;
            return this;
        }

        public Builder encoder(RequestEncoder encoder) {
            this.encoder = encoder;
            return this;
        }

        public Builder decoder(LengthFieldBasedFrameDecoder decoder) {
            this.decoder = decoder;
            return this;
        }

        public Builder requestDecoder(RequestDecoder requestDecoder) {
            this.requestDecoder = requestDecoder;
            return this;
        }

        public Builder reconnectNum(int reconnectNum) {
            this.reconnectNum = reconnectNum;
            return this;
        }

        public Builder reconnectIntervalTime(long reconnectIntervalTime) {
            this.reconnectIntervalTime = reconnectIntervalTime;
            return this;
        }

        public Builder clientHandler(NettyClientHandler clientHandler) {
            this.clientHandler = clientHandler;
            return this;
        }

        public NettyClient build() {
            if (context == null) {
                throw new IllegalArgumentException("the context is null,please call initcontext(Context context) to solve it");
            }

            if (host == null || port == 0) {
                throw new IllegalArgumentException("the host is null or the port is 0,please call hostAndPort(String host,int port) to solve it");
            }

            if (encoder == null) {
                throw new IllegalArgumentException("the encoder can not be null,please call encoder(RequestEncoder encoder) to solve it");
            }

            if (decoder == null || requestDecoder == null) {
                throw new IllegalArgumentException("the decoder or requestDecoder can not be null," +
                        "please call decoder(ByteToMessageDecoder decoder) or decoder(ByteToMessageDecoder decoder) to solve it");
            }

            return new NettyClient(this);
        }
    }
}
