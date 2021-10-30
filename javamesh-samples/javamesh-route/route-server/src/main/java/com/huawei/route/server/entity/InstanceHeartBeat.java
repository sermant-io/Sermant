/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * 标签库心跳
 *
 * @author zhouss
 * @since 2021-10-14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstanceHeartBeat {
    /**
     * 映射IP
     */
    private String ip;

    /**
     * 注册映射端口
     */
    private Integer registerPort;

    /**
     * 标签库服务名称
     */
    private String serviceName;

    /**
     * 简单判断当前心跳数据合法， 三要素
     *
     * @return 是否合法
     */
    public boolean isValid() {
        return !StringUtils.isEmpty(ip) && registerPort != null && !StringUtils.isEmpty(serviceName);
    }

    /**
     * 实例key
     *
     * @return key
     */
    public String getInstanceKey() {
        return String.format(Locale.ENGLISH, "%s@%d", ip, registerPort);
    }

    /**
     * 构建心跳数据
     *
     * @param heartbeatJson  心跳json
     * @return InstanceHeartBeat
     */
    public static InstanceHeartBeat build(JSONObject heartbeatJson) {
        return new InstanceHeartBeat(heartbeatJson.getString("ip"),
                heartbeatJson.getInteger("registerPort"), heartbeatJson.getString("registerServiceName"));
    }
}
