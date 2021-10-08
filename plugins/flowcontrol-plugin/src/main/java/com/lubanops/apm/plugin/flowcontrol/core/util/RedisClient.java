/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowcontrol.core.util;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.lubanops.apm.plugin.flowcontrol.core.config.CommonConst;
import com.lubanops.apm.plugin.flowcontrol.core.config.ConfigConst;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;

import java.util.ArrayList;
import java.util.List;

/**
 * Lettuce通用工具类
 *
 * @author liyi
 * @since 2020-08-26
 */
public class RedisClient {
    private RedisClusterClient client;
    private StatefulRedisClusterConnection<String, String> connection;

    public RedisClient() {
        try {
            String redisUris = PluginConfigUtil.getValueByKey(ConfigConst.REDIS_URIS);
            RecordLog.info("redis urls contains : " + redisUris);
            String[] redisUriArr = redisUris.split(CommonConst.COMMA_SIGN);
            List<RedisURI> list = new ArrayList<RedisURI>();
            for (String uri : redisUriArr) {
                try {
                    list.add(RedisURI.create(uri));
                } catch (Exception e) {
                    RecordLog.warn(String.format("detected invalid uri {%s} when init redis", uri));
                }

            }

            client = RedisClusterClient.create(list);
            connection = client.connect();
        } catch (Exception e) {
            RecordLog.error("Redis connect failed, please check your config!" + e);
        }
    }

    /**
     * 往Redis中存储值
     *
     * @param key   key值
     * @param value value值
     */
    public void set(String key, String value) {
        RedisAdvancedClusterCommands<String, String> commands = connection.sync();
        commands.set(key, value);
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (connection != null) {
            connection.close();
        }
        if (client != null) {
            client.shutdown();
        }
    }
}
