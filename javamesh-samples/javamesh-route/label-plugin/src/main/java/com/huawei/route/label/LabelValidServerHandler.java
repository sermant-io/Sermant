/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.label;

import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import com.huawei.apm.core.lubanops.api.JSONImpl;
import com.huawei.route.common.constants.LabelConstants;
import com.huawei.route.common.label.observers.LabelObservers;
import com.huawei.route.common.label.observers.LabelProperties;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 标签生效的服务端处理类，用于处理客户端发送过来的数据
 *
 * @author zhanghu
 * @since 2021-05-21
 */
@ChannelHandler.Sharable
public class LabelValidServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LogFactory.getLogger();
    private static final String IS_LABEL_ON_KEY = "on";
    private static final String LABEL_VALUE_KEY = "value";
    private static final JSONImpl JSON = new JSONImpl();

    ByteBuf in;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            in = (ByteBuf) msg;
            String transformData = in.toString(CharsetUtil.UTF_8);
            final Properties labelProperties = JSON.parseObject(transformData, Properties.class);
            LabelProperties.getAllLabelProperties().put((String) labelProperties.get(LabelConstants.LABEL_NAME_KEY), labelProperties);
            printLabel(labelProperties);
            ctx.channel().writeAndFlush(Unpooled.copiedBuffer(LabelConstants.STRING_FOR_TRUE, LabelConstants.DEFAULT_CHARSET));
            // 通知所有观察者更新标签数据
            LabelObservers.INSTANCE.notifyAllObservers(String.valueOf(labelProperties.get(LabelConstants.LABEL_NAME_KEY)),
                    labelProperties);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Read data failed. ", e);
            ctx.channel().writeAndFlush(Unpooled.copiedBuffer(LabelConstants.STRING_FOR_FALSE, LabelConstants.DEFAULT_CHARSET));
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) {
        LOGGER.log(Level.SEVERE, "Exception occurred.", cause);
        ctx.close();
    }

    private void printLabel(Properties labelProperties) {
        final Set<Map.Entry<Object, Object>> entries = labelProperties.entrySet();
        for (Map.Entry<Object, Object> entry : entries) {
            LOGGER.log(Level.FINE, String.format(Locale.ENGLISH, "[ key=%s, value=%s ]", entry.getKey(),
                    entry.getValue()));
        }
    }
}
