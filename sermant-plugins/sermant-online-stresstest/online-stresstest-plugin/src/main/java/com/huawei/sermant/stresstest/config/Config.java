/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.sermant.stresstest.config;

import com.huawei.sermant.stresstest.config.bean.DataSourceInfo;
import com.huawei.sermant.stresstest.config.bean.MongoSourceInfo;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置基类，用来获取压测相关的信息
 *
 * @author yiwei
 * @since 2021-10-25
 */
@SuppressWarnings({"checkstyle:IllegalCatch", "checkstyle:RegexpSingleline"})
public abstract class Config {
    private Map<?, ?> dbMap;

    private Map<?, ?> redisMap;

    private Map<?, ?> mongoMap;

    /**
     * 获取改key对应的值，没有则返回默认值
     *
     * @param key key
     * @param defaultValue 默认值
     * @return key对应的值或者默认值
     */
    abstract String getValue(String key, String defaultValue);

    /**
     * 返回url对应的影子库信息
     *
     * @param url 原始的url，格式 host:port/database
     * @return 影子连接信息
     */
    public DataSourceInfo getShadowDataSourceInfo(String url) {

        if (dbMap == null) {
            String value = this.getValue(Constant.DB, "");
            dbMap = JSONObject.parseObject(value, Map.class);
        }
        String content = JSONObject.toJSONString(dbMap.get(url));
        return JSONObject.parseObject(content, DataSourceInfo.class);
    }

    /**
     * 返回影子redis信息
     *
     * @return 影子redis信息
     */
    public Map<?, ?> getShadowRedis() {
        if (redisMap == null) {

            String value = this.getValue(Constant.REDIS_REPOSITORY, "");
            try {
                redisMap = JSONObject.parseObject(value, Map.class);
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
        return getValue(Constant.TEST_TOPIC, Constant.SHADOW);
    }

    /**
     * 测试redis key前缀
     *
     * @return 测试redis key前缀
     */
    public String getTestRedisPrefix() {
        return getValue(Constant.REDIS_KEY, Constant.SHADOW);
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
        return getValue(Constant.MONGO_KEY, Constant.SHADOW);
    }

    /**
     * 返回database对应的影子库信息
     *
     * @param database 原始的database 名字
     * @return 影子连接信息
     */
    public MongoSourceInfo getShadowMongoSourceInfo(String database) {

        String content = JSONObject.toJSONString(getShadowMongo().get(database));
        return JSONObject.parseObject(content, MongoSourceInfo.class);
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
        if (mongoMap == null) {
            String value = this.getValue(Constant.MONGO_REPOSITORY, "");
            try {
                mongoMap = JSONObject.parseObject(value, Map.class);
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
