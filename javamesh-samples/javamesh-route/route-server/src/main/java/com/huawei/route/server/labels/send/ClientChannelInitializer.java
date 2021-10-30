/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.send;

import com.huawei.route.server.labels.exception.CustomGenericException;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.huawei.route.server.labels.constant.LabelConstant.ERROR_CODE;

/**
 * netty client channel初始化器
 *
 * @author zhanghu
 * @since 2021-05-25
 */
public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    /**
     * 获取服务端响应超时时间
     */
    private static final int TIMEOUT_SECONDS = 10;
    private static final int MAX_LENGTH = 1024;

    private String data;
    private CountDownLatch countDownLatch;
    private String response;

    public ClientChannelInitializer(String data) {
        this.data = data;
    }

    public String getResponse() {
        try {
            countDownLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new CustomGenericException(ERROR_CODE, "连接被打断");
        }
        if (response == null) {
            throw new CustomGenericException(ERROR_CODE, "服务端响应超时");
        }
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
        countDownLatch.countDown();
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        countDownLatch = new CountDownLatch(1);
        socketChannel.pipeline().addLast(new ReadTimeoutHandler(TIMEOUT_SECONDS));
        socketChannel.pipeline().addLast(new ClientHandler(data, this));
        socketChannel.pipeline().addLast(new LineBasedFrameDecoder(MAX_LENGTH));
        socketChannel.pipeline().addLast(new StringDecoder());
    }
}
