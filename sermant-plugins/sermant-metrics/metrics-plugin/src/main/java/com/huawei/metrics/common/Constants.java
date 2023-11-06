/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.metrics.common;

/**
 * 常量类
 *
 * @author zhp
 * @since 2023-10-17
 */
public class Constants {
    /**
     * 进程名连接符
     */
    public static final String PROCESS_NAME_LINK = "@";

    /**
     * 进程名连接符
     */
    public static final String FILE_PATH_LINK = "/";

    /**
     * L7客户端角色
     */
    public static final String CLIENT_ROLE = "client";

    /**
     * L7服务端角色
     */
    public static final String SERVER_ROLE = "server";

    /**
     * TCP协议
     */
    public static final String TCP_PROTOCOL = "tcp";

    /**
     * TCP协议
     */
    public static final String UDP_PROTOCOL = "udp";

    /**
     * 角色连接符
     */
    public static final String CONNECT = "_";

    /**
     * SSL开关的KEY
     */
    public static final String SSL_ENABLE = "sslEnabled";

    /**
     * 服务调用使用的CLIENT下表字段名
     */
    public static final String CLIENT_INDEX = "index";

    /**
     * CLIENT集合名称
     */
    public static final String CLIENTS_NAME = "clients";

    /**
     * DUBBO TCP协议集合
     */
    public static final String TCP_PROTOCOLS = "dubbo,rmi,http,https";

    /**
     * 连接信息头
     */
    public static final String LINK_HEAD = "sermant_l7_link";

    /**
     * 连接信息头
     */
    public static final String RPC_HEAD = "sermant_l7_rpc";

    /**
     * SSL开启标识
     */
    public static final String SSL_OPEN = "ssl";

    /**
     * SSL未开启标识
     */
    public static final String SSL_CLOSE = "no_ssl";

    /**
     * 消费者标识
     */
    public static final String CONSUMER_SIDE = "consumer";

    /**
     * 提供者标识
     */
    public static final String PROVIDER_SIDE = "provider";

    /**
     * 客户端 服务端标识的KEY
     */
    public static final String SIDE_KEY = "side";

    /**
     * 字符串写下标保存到context局部变量中的key
     */
    public static final String WRITE_INDEX_KEY = "writeIndex";

    /**
     * 字符串读下标保存到context局部变量中的key
     */
    public static final String READ_INDEX_KEY = "readIndex";

    /**
     * 连接信息保存到context局部变量中的key
     */
    public static final String LINK_INFO_KEY = "linkInfo";

    /**
     * RPC信息保存到context局部变量中的key
     */
    public static final String RPC_INFO_KEY = "rpcInfo";

    /**
     * 服务调用开始时间保存到context局部变量中的key
     */
    public static final String START_TIME_KEY = "startTime";

    /**
     * 异常线程ID
     */
    public static final String EXCEPTION_PID = "-1";

    /**
     * 指标值的连接符
     */
    public static final String METRICS_LINK = "|";

    private Constants() {
    }
}
