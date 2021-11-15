/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.rules;

import com.alibaba.fastjson.JSONObject;
import com.huawei.route.server.constants.RouteConstants;
import com.huawei.route.server.entity.InstanceHeartBeat;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 标签库实例名称与IP映射处理
 *
 * @author zhouss
 * @since 2021-10-14
 */
@Component("tagInstanceIpMapper")
public class TagInstanceIpMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(TagInstanceIpMapper.class);

    @Getter
    private final Map<String, InstanceHeartBeat> instanceNameIpMapper = new HashMap<>();

    /**
     * 用于服务名的映射，主要用于适配标签库的服务名
     * key : 标签库使用服务名
     * value : 注册中心关联的服务名集合
     */
    @Getter
    private final Map<String, Set<String>> serviceMapper = new HashMap<>();

    /**
     * 添加新的心跳映射
     *
     * @param instanceName 标签库实例名
     * @param heartbeatJson 心跳json
     */
    public void updateMapper(String instanceName, JSONObject heartbeatJson) {
        updateServiceMapper(heartbeatJson);
        updateInstanceMapper(instanceName, heartbeatJson);
    }

    private void updateServiceMapper(JSONObject heartbeatJson) {
        final String tagServiceName = heartbeatJson.getString("serviceName");
        final Set<String> registerServices =
                serviceMapper.getOrDefault(tagServiceName, new HashSet<>(RouteConstants.INIT_SERVICE_MAPPER_CAPACITY));
        final String registerServiceName = heartbeatJson.getString("registerServiceName");
        if (StringUtils.isNotEmpty(registerServiceName)) {
            registerServices.add(registerServiceName);
            serviceMapper.put(tagServiceName, registerServices);
        }
    }

    private void updateInstanceMapper(String instanceName, JSONObject heartbeatJson) {
        final InstanceHeartBeat instanceHeartBeat = InstanceHeartBeat.build(heartbeatJson);
        if (!instanceHeartBeat.isValid()) {
            LOGGER.debug("invalid heartbeat json [{}], if it happened in the init stage, please ignore", heartbeatJson);
            return;
        }
        instanceNameIpMapper.put(instanceName, instanceHeartBeat);
    }

}
