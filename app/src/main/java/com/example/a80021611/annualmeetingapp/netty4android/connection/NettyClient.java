package com.example.a80021611.annualmeetingapp.netty4android.connection;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.a80021611.annualmeetingapp.netty4android.message.Request;
import com.example.a80021611.annualmeetingapp.netty4android.message.RequestManager;
import com.example.a80021611.annualmeetingapp.netty4android.util.LogUtils;
import com.example.a80021611.annualmeetingapp.netty4android.util.NetworkUtil;
import com.example.a80021611.annualmeetingapp.netty4android.util.TCPConfig;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
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
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeMap;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.StringUtil;

/**
 * 1.连接服务器
 * 2.数据发送
 */

public class NettyClient {
    public static final String TAG = NettyClient.class.getSimpleName();
    private static NettyClient mNettyClient;
    private long mReconnectIntervalTime = TCPConfig.RECONNECT_INTERVAL_TIME;
    private int mReconnectNum = 10;
    private EventLoopGroup group;
    private NettyListener listener;
    private Channel channel;
    private Context mContext;
    //已经连接
    private boolean isConnect = false;
    //是否正在连接
    private boolean isConnecting = false;
    //是否正在重连
    private boolean isReconnecting = false;
    //是否尝试重连
    private boolean isDoReconnect = true;
    //网关index
    private int mGateIndex = 0;
    private int serialNumber = 1;
    private String mMessage;    //心跳包消息内容

    private NettyClient() {
    }

    public static NettyClient getInstance() {
        if (mNettyClient == null) {
            mNettyClient = new NettyClient();
        }
        return mNettyClient;
    }

    public void setContext(Context ctx) {
        mContext = ctx;
    }

    public Context getContext() {
        return mContext;
    }

    public synchronized void connect() {
        isDoReconnect = true;
        if (isConnecting) {
            return;
        }
        ChannelFuture channelFuture = null;
        if (!isConnect) {
            isConnecting = true;
            group = new NioEventLoopGroup();
            Bootstrap mBootstrap = new Bootstrap()
                    .group(group)
                    .option(ChannelOption.TCP_NODELAY, true)//屏蔽Nagle算法试图
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TCPConfig.CONNECTION_TIMEOUT)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast("ping", new IdleStateHandler(0, TCPConfig.HEARTBEAT_TIME, 0, TimeUnit.MICROSECONDS));
//                            pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
//                            pipeline.addLast(new LineBasedFrameDecoder(1024));//黏包处理
//                            pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(33, 1, 2, 2, 0));
                            pipeline.addLast(new NettyClientHandler(listener));
                        }
                    });
            try {
                channelFuture = mBootstrap.connect(TCPConfig.TCP_HOST, TCPConfig.TCP_PORT)
                        .addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture future) {
                                isConnecting = false;
                                isReconnecting = false;
                                if (future != null && future.isSuccess()) {
                                    isConnect = true;
                                    channel = future.channel();
                                    Log.e(TAG, "连接成功");
                                    sendHeartBeatMsg(channel);
                                } else {
                                    isConnect = false;
                                    reconnect();
                                    Log.e(TAG, "连接失败");
                                }
                            }
                        }).sync();

                channelFuture.channel().closeFuture().sync();
                Log.e(TAG, "断开连接");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isConnect = false;
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

    private void sendHeartBeatMsg(Channel channel) {
        try {
            Request request = new Request();
            request.setSerialNumber(1);
            byte[] sendMsgByte = request.getSendMsgByte(true);
            String sendMsgHexString = StringUtil.toHexString(sendMsgByte);
            this.mMessage = sendMsgHexString;
            channel.writeAndFlush(Unpooled.copiedBuffer(sendMsgByte)).addListener(new FutureListener() {
                @Override
                public void success() {
                    Log.e(TAG, " --连接成功时的心跳消息：”发送成功:" + mMessage);
                }

                @Override
                public void error() {

                }
            });
        } catch (Exception e) {

        }

    }

    /**
     * 重连
     *
     * @return
     */
    public synchronized void reconnect() {
        if (!isDoReconnect) {
            return;
        }
        // 有网络且没有重连则进行重连
        if (NetworkUtil.isNetworkAvailable(mContext) && !isReconnecting) {
            if (!isConnect && mReconnectNum > 0) {
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
                mReconnectNum = 10;
                disconnect();
            }
        }
    }

    /**
     * 断开连接|不自动重连了
     */
    public void shutDown() {
        if (isConnect) {
            setDoReconnect(false);
            disconnect();
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        }
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        Log.e(TAG, "调用disconnect主动关闭连接");
        if (group != null) {
            group.shutdownGracefully();
            isReconnecting = false;
            isConnecting = false;
            isConnect = false;
        }
    }

    private Channel getChannel() {
        return channel;
    }

    public Request newRequest(byte[] comandContent, boolean isNeedResponse, int sendId, int receiveId, int commandPriority, int commandType) {
        Request request = new Request();
        request.setCommandContent(comandContent);
        request.setLength(comandContent.length + TCPConfig.LENGTH_SIZE);
        request.setIsNeedResponse(isNeedResponse ? 1 : 0);
        request.setSendId(sendId);
        request.setReceiveId(receiveId);
        request.setCommandPriority(commandPriority);
        request.setCommandType(commandType);
        return request;
    }

    public void sendMessage(Request request) throws IOException {
        sendMessage(request, null);
    }

    /**
     * @param request        the message you gonna send
     * @param futureListener the callback to know whether you've sent successful
     */
    public void sendMessage(Request request, FutureListener futureListener) throws IOException {
        // 判断网络状况，如果有网络并且没有进行重连，则尝试连接
        boolean flag = channel != null && isConnect;
        if (!flag) {
            request.getCallback().onFail(NettyListener.STATUS_CONNECT_CLOSED);
            Toast.makeText(getContext(), "Please check your network connection", Toast.LENGTH_SHORT).show();
            if (NetworkUtil.isNetworkAvailable(mContext) && !isReconnecting) {
                connect();
            }
            return;
        }

        /*
        1.重发、回复0x0000都不需要递增 serialNumber
        2.新消息0x1000，添加serialNumber和verifyCode（在拼接字节数组的时候添加的）,要回复的消息添加消息管理
         */
        if (!request.isResend() && request.getCommandType() != TCPConfig.PROTOCAL_COMMAND_TYPE_REPLY) {
            if (serialNumber == Integer.MAX_VALUE) {
                serialNumber = 1;
            }
            request.setSerialNumber(serialNumber);
            serialNumber++;
        }

        //0X1000的消息，需要回复的
        if (TCPConfig.PROTOCAL_COMMAND_TYPE_SEND == request.getCommandType() && TCPConfig.PROTOCAL_NEED_RESPONSE == request.getIsNeedResponse()) {
            RequestManager.getInstance().add(request);
        }

        final Request requestFinal = request;
        // send the request
        if (futureListener == null) {
            channel.writeAndFlush(Unpooled.copiedBuffer(request.getSendMsgByte()))
                    .addListener(new FutureListener() {
                        @Override
                        public void success() throws IOException {
                            Log.e(TAG, "发送成功--->Method====" + requestFinal.getSendMsgHexString());
                        }

                        @Override
                        public void error() {
                            Log.e(TAG, "发送失败--->");
                            // 这里响应后后不移除回调，重连后会重新请求
                            requestFinal.getCallback().onFail(NettyListener.STATUS_CONNECT_ERROR);
                            reconnect();
                        }
                    });
        } else {
            channel.writeAndFlush(Unpooled.copiedBuffer(request.getSendMsgByte())).addListener(futureListener);
        }
    }

    /**
     * 设置重连次数
     *
     * @param reconnectNum 重连次数
     */
    public void setReconnectNum(int reconnectNum) {
        this.mReconnectNum = reconnectNum;
    }

    /**
     * 设置重连时间间隔
     *
     * @param reconnectIntervalTime 时间间隔
     */
    public void setReconnectIntervalTime(long reconnectIntervalTime) {
        this.mReconnectIntervalTime = reconnectIntervalTime;
    }

    public boolean getConnectStatus() {
        return isConnect;
    }

    /**
     * 在NettyClientHandler的时候监听到相关的数据
     * 设置连接状态
     *
     * @param status
     */
    public void setConnectStatus(boolean status) {
        this.isConnect = status;
    }

    public void setListener(NettyListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener == null ");
        }
        this.listener = listener;
    }

    /**
     * 是否重连
     *
     * @param doReconnect
     */
    public void setDoReconnect(boolean doReconnect) {
        isDoReconnect = doReconnect;
    }

}
