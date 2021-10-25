/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.oap.redis.provider;

import com.huawei.oap.redis.complement.CompleteTimer;
import com.huawei.oap.redis.module.RedisClusterConfig;
import com.huawei.oap.redis.module.RedisOperationModule;
import com.huawei.oap.redis.service.IRedisService;
import com.huawei.oap.redis.service.RedisServiceImpl;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.cluster.RedisClusterClient;
import org.apache.skywalking.oap.server.library.module.ModuleConfig;
import org.apache.skywalking.oap.server.library.module.ModuleDefine;
import org.apache.skywalking.oap.server.library.module.ModuleProvider;
import org.apache.skywalking.oap.server.library.module.ModuleStartException;
import org.apache.skywalking.oap.server.library.module.ServiceNotProvidedException;

import java.time.Duration;
import java.util.ArrayList;

/**
 * redis集群查询提供者
 *
 * @author zhouss
 * @since 2020-11-28
 */
public class RedisClusterQueryProvider extends ModuleProvider {
    private StatefulConnection<String, String> connect;

    private RedisServiceImpl redisService;

    /**
     * redis集群查询配置类
     */
    private RedisClusterConfig config;

    public RedisClusterQueryProvider() {
        config = new RedisClusterConfig();
    }

    @Override
    public String name() {
        return "cluster";
    }

    @Override
    public Class<? extends ModuleDefine> module() {
        return RedisOperationModule.class;
    }

    @Override
    public ModuleConfig createConfigBeanIfAbsent() {
        return config;
    }

    @Override
    public void prepare() throws ServiceNotProvidedException, ModuleStartException {
        ArrayList<RedisURI> list = new ArrayList<>();
        String nodes = config.getNodes();
        String[] nodeArray = nodes.split(",");
        if (nodeArray.length != 0) {
            for (String node : nodeArray) {
                RedisURI redisUri = RedisURI.create(node);
                redisUri.setTimeout(Duration.ofMillis(config.getTimeout()));
                redisUri.setDatabase(config.getDatabase());
                list.add(redisUri);
            }
        }

        connect = RedisClusterClient.create(list).connect();

        redisService = new RedisServiceImpl(connect);
        this.registerServiceImplementation(IRedisService.class, redisService);
    }

    @Override
    public void start() throws ServiceNotProvidedException, ModuleStartException {
    }

    @Override
    public void notifyAfterCompleted() throws ServiceNotProvidedException, ModuleStartException {
        CompleteTimer.INSTANCE.start(getManager());
    }

    @Override
    public String[] requiredModules() {
        return new String[0];
    }
}
