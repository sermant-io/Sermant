/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.stresstest.config;

import com.huawei.apm.bootstrap.lubanops.api.APIService;
import com.huawei.apm.bootstrap.lubanops.api.JSONAPI;
import com.lubanops.stresstest.config.bean.DataSourceInfo;

import java.util.Map;

/**
 * 配置基类，用来获取压测相关的信息
 *
 * @author yiwei
 * @since 2021/10/25
 */
public abstract class Config implements Constant {
    private Map<?, ?> map;

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
        if (map == null) {
            String value = this.getValue(DB, "");
            map = jsonapi.parseObject(value, Map.class);
        }
        String content = jsonapi.toJSONString(map.get(url));
        return jsonapi.parseObject(content, DataSourceInfo.class);
    }

    /**
     * 测试topic前缀
     *
     * @return 测试topic前缀
     */
    public String getTestTopicPrefix() {
        return getValue(TEST_TOPIC, SHADOW);
    }
}
