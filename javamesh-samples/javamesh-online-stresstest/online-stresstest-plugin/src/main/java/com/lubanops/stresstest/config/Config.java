/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.stresstest.config;

import com.huawei.apm.core.lubanops.bootstrap.api.APIService;
import com.huawei.apm.core.lubanops.bootstrap.api.JSONAPI;
import com.lubanops.stresstest.config.bean.DataSourceInfo;
import com.lubanops.stresstest.config.bean.MongoSourceInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置基类，用来获取压测相关的信息
 *
 * @author yiwei
 * @since 2021/10/25
 */
public abstract class Config implements Constant {
    private Map<?, ?> dbMap;

    private Map<?, ?> redisMap;

    private Map<?, ?> mongoMap;

    /**
     * 获取改key对应的值，没有则返回默认值
     * @param key key
     * @param defaultValue 默认值
     * @return key对应的值或者默认值
     */
    abstract String getValue(String key, String defaultValue);

    /**
     * 返回url对应的影子库信息
     * @param url 原始的url，格式 host:port/database
     * @return 影子连接信息
     */
    public DataSourceInfo getShadowDataSourceInfo(String url) {
        JSONAPI jsonapi = APIService.getJsonApi();
        if (dbMap == null) {
            String value = this.getValue(DB, "");
            dbMap = jsonapi.parseObject(value, Map.class);
        }
        String content = jsonapi.toJSONString(dbMap.get(url));
        return jsonapi.parseObject(content, DataSourceInfo.class);
    }

    /**
     * 返回影子redis信息
     * @return 影子redis信息
     */
    public Map<?, ?> getShadowRedis() {
        if (redisMap == null) {
            JSONAPI jsonapi = APIService.getJsonApi();
            String value = this.getValue(REDIS_REPOSITORY, "");
            try {
                redisMap = jsonapi.parseObject(value, Map.class);
            } catch (Exception exception) {
                // 不处理无效数据
            }
            if (redisMap == null) {
                redisMap = new HashMap<>();
            }
        }
        return redisMap;
    }

    /**
     * 测试topic前缀
     *
     * @return 测试topic前缀
     */
    public String getTestTopicPrefix() {
        return getValue(TEST_TOPIC, SHADOW);
    }

    /**
     * 测试redis key前缀
     *
     * @return 测试redis key前缀
     */
    public String getTestRedisPrefix() {
        return getValue(REDIS_KEY, SHADOW);
    }

    /**
     * redis shadowType
     *
     * @return 是否使用影子redis库。
     */
    public boolean isRedisShadowRepositories() {
        return getShadowRedis().size() > 0;
    }

    /**
     * 测试redis key前缀
     *
     * @return 测试redis key前缀
     */
    public String getTestMongodbPrefix() {
        return getValue(MONGO_KEY, SHADOW);
    }

    /**
     * 返回database对应的影子库信息
     * @param database 原始的database 名字
     * @return 影子连接信息
     */
    public MongoSourceInfo getShadowMongoSourceInfo(String database) {
        JSONAPI jsonapi = APIService.getJsonApi();
        String content = jsonapi.toJSONString(getShadowMongo().get(database));
        return jsonapi.parseObject(content, MongoSourceInfo.class);
    }

    /**
     * redis shadowType
     *
     * @return 是否使用影子redis库。
     */
    public boolean isMongoShadowRepositories() {
        return getShadowMongo().size() > 0;
    }

    private Map<?, ?> getShadowMongo() {
        JSONAPI jsonapi = APIService.getJsonApi();
        if (mongoMap == null) {
            String value = this.getValue(MONGO_REPOSITORY, "");
            try {
                mongoMap = jsonapi.parseObject(value, Map.class);
            } catch (Exception exception) {
                // 不处理无效数据
            }
            if (mongoMap == null) {
                mongoMap = new HashMap<>();
            }
        }
        return mongoMap;
    }
}
