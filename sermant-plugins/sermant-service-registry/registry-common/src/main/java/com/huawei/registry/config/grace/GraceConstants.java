/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.config.grace;

/**
 * 优雅上下线公共变量
 *
 * @author zhouss
 * @since 2022-05-17
 */
public class GraceConstants {
    /**
     * spring loadbalancer缓存的提供实例缓存生产器名称, 基于该实例去刷新指定的服务实例本地缓存
     */
    public static final String SPRING_CACHE_MANAGER_LOADBALANCER_CACHE_NAME = "CachingServiceInstanceListSupplierCache";

    /**
     * 预热时间-键
     */
    public static final String WARM_KEY_TIME = "sermant.grace.warmup.time";

    /**
     * 预热注入时间-键
     */
    public static final String WARM_KEY_INJECT_TIME = "sermant.grace.warmup.inject.time";

    /**
     * 预热权重-键
     */
    public static final String WARM_KEY_WEIGHT = "sermant.grace.warmup.weight";

    /**
     * 预热类型-用于计算流量分配-键
     */
    public static final String WARM_KEY_CURVE = "sermant.grace.warmup.cal.curve";

    /**
     * 被标记要关闭的服务名, 用于response返回给上游
     */
    public static final String MARK_SHUTDOWN_SERVICE_NAME = "sermant.grace.mark.shutdown.service.name";

    /**
     * 被标记要关闭的endpoint, 用于response返回给上游
     */
    public static final String MARK_SHUTDOWN_SERVICE_ENDPOINT = "sermant.grace.mark.shutdown.service.endpoint";

    /**
     * 该地址用于传给下游, 下游基于该地址进行下线通知
     */
    public static final String SERMANT_GRACE_ADDRESS = "sermant.grace.address";

    /**
     * 优雅上下线聚合开关环境变量, 设置为true将开启优雅上下线所有功能
     */
    public static final String ENV_GRACE_ENABLE = "grace.rule.enableGrace";

    /**
     * 默认的通知http端口
     */
    public static final int DEFAULT_NOTIFY_HTTP_SERVER_PORT = 16688;

    /**
     * 默认预热权重
     */
    public static final int DEFAULT_WARM_UP_WEIGHT = 100;

    /**
     * 默认流量分配计算曲线值
     */
    public static final int DEFAULT_WARM_UP_CURVE = 2;

    /**
     * 默认注入时间, 1个小时
     */
    public static final String DEFAULT_WARM_UP_INJECT_TIME_GAP = "0";

    /**
     * 默认预热时间
     */
    public static final String DEFAULT_WARM_UP_TIME = "0";

    /**
     * 默认下游Endpoint过期时间, 120S
     */
    public static final long DEFAULT_ENDPOINT_EXPIRED_TIME = 120L;

    /**
     * 主动通知url路径
     */
    public static final String GRACE_NOTIFY_URL_PATH = "/$$sermant$$/notify";

    /**
     * 下线url路径
     */
    public static final String GRACE_SHUTDOWN_URL_PATH = "/$$sermant$$/shutdown";

    /**
     * 成功响应码
     */
    public static final int GRACE_HTTP_SUCCESS_CODE = 200;

    /**
     * 失败响应码
     */
    public static final int GRACE_HTTP_FAILURE_CODE = 500;

    /**
     * POST方法
     */
    public static final String GRACE_HTTP_METHOD_POST = "POST";

    /**
     * 成功响应消息
     */
    public static final String GRACE_OFFLINE_SUCCESS_MSG = "success";

    /**
     * 实现响应消息
     */
    public static final String GRACE_FAILURE_MSG = "failed";

    /**
     * 下线通知的请求来源KEY
     */
    public static final String GRACE_OFFLINE_SOURCE_KEY = "sermant.grace.source";

    /**
     * 下线通知的请求来源value
     */
    public static final String GRACE_OFFLINE_SOURCE_VALUE = "Sermant-agent";

    /**
     * 缓存上游地址的默认最大大小
     */
    public static final long UPSTREAM_ADDRESS_DEFAULT_MAX_SIZE = 100L;

    /**
     * 缓存上游地址的默认过期时间
     */
    public static final long UPSTREAM_ADDRESS_DEFAULT_EXPIRED_TIME = 60L;

    /**
     * 最大端口
     */
    public static final int MAX_HTTP_SERVER_PORT = 65535;

    /**
     * 最大下线前等待时间
     */
    public static final long MAX_SHUTDOWN_WAIT_TIME = 24 * 3600L;

    private GraceConstants() {
    }
}
