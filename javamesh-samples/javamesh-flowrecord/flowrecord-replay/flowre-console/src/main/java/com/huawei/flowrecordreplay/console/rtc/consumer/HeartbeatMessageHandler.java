/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.rtc.consumer;

import com.huawei.flowrecordreplay.console.rtc.common.kafka.HeartbeatMessage;
import com.huawei.flowrecordreplay.console.rtc.common.redis.RedisUtil;

import com.alibaba.fastjson.JSON;
import com.lmax.disruptor.WorkHandler;

import io.lettuce.core.RedisCommandTimeoutException;
import io.lettuce.core.RedisException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 该类的作用是从ringbuffer中读取心跳数据，然后存入redis中
 *
 * @author hanpeng
 * @since 2021-04-07
 */
@Component("heartbeatHandler")
@Primary
@Scope("prototype")
public class HeartbeatMessageHandler implements WorkHandler<HeartbeatMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeatMessageHandler.class);

    /**
     * redis工具类实例
     */
    @Autowired
    private RedisUtil redis;

    /**
     * 表示区别流控数据的标志
     */
    @Value("${heartbeatflag::heartbeat}")
    private String heartbeatFlag;

    /**
     * 获取所有应用的名字，以该值作为key值，按set存app名字
     */
    @Value("${appnameKey:appnames}")
    private String appnameKey;

    /**
     * WorkHandler的一个覆盖方法，用于监听disruptor中的心跳信息
     *
     * @param event 心跳数据
     */
    @Override
    public void onEvent(HeartbeatMessage event) {
        try {
            String app = event.getApp();
            // 存应用名字
            RedisUtil.asyncSetSet(appnameKey, app);

            // 按hash存
            String ip = event.getIp();
            String key = app + heartbeatFlag;
            String value = JSON.toJSONString(event);
            RedisUtil.asyncSetHash(key, ip, value);
        } catch (RedisCommandTimeoutException e) {
            redis.setAlive(false);
            redis.checkState();
        } catch (RedisException e) {
            LOGGER.error("error saving HeartbeatMessage to redis：", e);
        }
    }
}
