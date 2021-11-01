/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.share;

import com.alibaba.fastjson.JSONObject;
import com.huawei.route.server.console.util.RedisClient;
import com.huawei.route.server.config.RouteShareProperties;
import com.huawei.route.server.rules.notifier.PathDataUpdater;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * redis数据共享实现
 *
 * @author zhouss
 * @since 2021-10-18
 */
@Component("redisRouteSharer")
public class RedisRouteSharer<T extends ShareKey> extends RouteSharer<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisRouteSharer.class);

    /**
     * 锁 key
     */
    private final String lock = "shareDataKey";

    @Autowired
    private RouteShareProperties routeShareProperties;

    @Autowired
    private RedisClient redisClient;

    @Autowired
    public RedisRouteSharer(PathDataUpdater pathDataUpdater) {
        super(pathDataUpdater);
    }

    @Override
    @SuppressWarnings("all")
    public boolean shareAllData(T[] dataArray) {
        int count = 0;
        while (count < routeShareProperties.getRedis().getMaxTryLockCount()) {
            if (tryLock()) {
                try {
                    for (T data : dataArray) {
                        if (StringUtils.isEmpty(data.getShareKey())) {
                            continue;
                        }
                        redisClient.setHash(SHARE_KEY, data.getShareKey(), JSONObject.toJSONString(data));
                    }
                    return true;
                } catch (Exception e) {
                    LOGGER.warn("push share data to list failed!", e);
                } finally {
                    unlock();
                }
            }
            try {
                Thread.sleep(routeShareProperties.getRedis().getTryLockIntervalMs());
            } catch (InterruptedException e) {
                LOGGER.warn("thread was interrupted when upload data to redis!");
            }
            count++;
        }
        return false;
    }

    @Override
    public Collection<T> getShareDataList(Class<T> tClass) {
        // 暂定一次性查询所有数据，后续数据量较多考虑数据分批获取
        final Map<String, String> dataMap = redisClient.getHashEntriesByKey(SHARE_KEY);
        if (dataMap == null) {
            return Collections.emptyList();
        }
        return dataMap.values().stream().map(data -> JSONObject.parseObject(data, tClass)).collect(Collectors.toList());
    }

    private boolean tryLock() {
        return redisClient.setIfAbsent(lock, "shareData",
                routeShareProperties.getRedis().getLockMaxTimeMs());
    }

    private void unlock() {
        redisClient.delKey(lock);
    }
}
