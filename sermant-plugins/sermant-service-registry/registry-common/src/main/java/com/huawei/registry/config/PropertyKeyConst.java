/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.registry.config;

/**
 * nacos注册参数静态常量
 *
 * @author chengyouling
 * @since 2022-10-25
 */
public class PropertyKeyConst {
    /**
     * 冒号
     */
    public static final String HTTP_URL_COLON = ":";

    /**
     * 节点
     */
    public static final String ENDPOINT = "endpoint";

    /**
     * 节点端口
     */
    public static final String ENDPOINT_PORT = "endpointPort";

    /**
     * 服务名
     */
    public static final String SERVER_NAME = "serverName";

    /**
     * 命名空间
     */
    public static final String NAMESPACE = "namespace";

    /**
     * 用户名
     */
    public static final String USERNAME = "username";

    /**
     * 用户密码
     */
    public static final String PASSWORD = "password";

    /**
     * ak值
     */
    public static final String ACCESS_KEY = "accessKey";

    /**
     * sk值
     */
    public static final String SECRET_KEY = "secretKey";

    /**
     * 服务地址
     */
    public static final String SERVER_ADDR = "serverAddr";

    /**
     * 集群名称
     */
    public static final String CLUSTER_NAME = "clusterName";

    /**
     * 开始是否naming加载缓存
     */
    public static final String NAMING_LOAD_CACHE_AT_START = "namingLoadCacheAtStart";

    /**
     * nacos日志文件名
     */
    public static final String NACOS_NAMING_LOG_NAME = "com.alibaba.nacos.naming.log.filename";

    /**
     * 是否使用云命名空间
     */
    public static final String IS_USE_CLOUD_NAMESPACE_PARSING = "isUseCloudNamespaceParsing";

    /**
     * 是否使用节点解析规则
     */
    public static final String IS_USE_ENDPOINT_PARSING_RULE = "isUseEndpointParsingRule";

    /**
     * 节点查询参数
     */
    public static final String ENDPOINT_QUERY_PARAMS = "endpointQueryParams";

    /**
     * 角色名称
     */
    public static final String RAM_ROLE_NAME = "ramRoleName";

    /**
     * 上下文路径
     */
    public static final String CONTEXT_PATH = "contextPath";

    /**
     * 加密
     */
    public static final String ENCODE = "encode";

    /**
     * 配置拉取超时
     */
    public static final String CONFIG_LONG_POLL_TIMEOUT = "configLongPollTimeout";

    /**
     * 重试次数
     */
    public static final String CONFIG_RETRY_TIME = "configRetryTime";

    /**
     * 最大重试
     */
    public static final String MAX_RETRY = "maxRetry";

    /**
     * 远程同步配置
     */
    public static final String ENABLE_REMOTE_SYNC_CONFIG = "enableRemoteSyncConfig";

    /**
     * naming注册缓存文件
     */
    public static final String NAMING_CACHE_REGISTRY_DIR = "namingCacheRegistryDir";

    /**
     * naming客户端心跳线程计数
     */
    public static final String NAMING_CLIENT_BEAT_THREAD_COUNT = "namingClientBeatThreadCount";

    /**
     * naming拉取线程计数
     */
    public static final String NAMING_POLLING_THREAD_COUNT = "namingPollingThreadCount";

    /**
     * naming请求最大重试计数
     */
    public static final String NAMING_REQUEST_DOMAIN_RETRY_COUNT = "namingRequestDomainMaxRetryCount";

    /**
     * naming空推送
     */
    public static final String NAMING_PUSH_EMPTY_PROTECTION = "namingPushEmptyProtection";

    /**
     * udp端口
     */
    public static final String PUSH_RECEIVER_UDP_PORT = "push.receiver.udp.port";

    /**
     * 构造方法
     */
    private PropertyKeyConst() {
    }
}
