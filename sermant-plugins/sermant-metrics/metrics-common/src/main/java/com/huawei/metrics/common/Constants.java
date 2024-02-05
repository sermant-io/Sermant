/*
 * Copyright (C) 2023-2024 Huawei Technologies Co., Ltd. All rights reserved.
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
     * DUBBO TCP协议集合
     */
    public static final String TCP_PROTOCOLS = "dubbo,rmi,http,https";

    /**
     * 连接信息头
     */
    public static final String RPC_HEADER = "sermant_l7_rpc";

    /**
     * API连接信息头
     */
    public static final String RPC_API_HEADER = "sermant_l7_rpc_api";

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
     * 客户端 服务端标识的KEY
     */
    public static final String SIDE_KEY = "side";

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

    /**
     * HTTPS协议
     */
    public static final String HTTPS_PROTOCOL = "https";

    /**
     * mysql协议
     */
    public static final String MYSQL_PROTOCOL = "mysql";

    /**
     * 最大成功响应编码
     */
    public static final int MAX_SUCCESS_CODE = 399;

    /**
     * 默认的HTTP失败编码
     */
    public static final int HTTP_DEFAULT_FAILURE_CODE = 9999;

    /**
     * 最大客户端失败编码
     */
    public static final int MAX_CLIENT_ERROR_CODE = 499;

    /**
     * 最大服务端失败编码
     */
    public static final int MAX_SERVER_ERROR_CODE = 599;

    /**
     * MYSQL 客户端异常编码
     */
    public static final int[][] MYSQL_CLIENT_ERROR = {{1, 999}, {2000, 2999}};

    /**
     * MYSQL 服务服务端编码
     */
    public static final int[][] MYSQL_SERVER_ERROR = {{1000, 1999}, {3000, 4000}};

    /**
     * dubbo请求成功
     */
    public static final byte DUBBO_OK = 20;

    /**
     * dubbo客户端异常编码
     */
    public static final byte[] DUBBO_CLIENT_ERROR = {30, 40, 90};

    /**
     * dubbo服务端异常编码
     */
    public static final byte[] DUBBO_SERVER_ERROR = {31, 80, 50, 60, 70, 100};

    /**
     * 时延范围纳秒
     */
    public static final long[] LATENCY_RANGE = {3000000L, 10000000L, 50000000L, 100000000L, 500000000L, 1000000000L,
            10000000000L};

    /**
     * 时延统计的key
     */
    public static final String[] LATENCY_COUNT_KEY = {"COUNT_ZERO_TO_THREE", "COUNT_ZERO_TO_TEN",
            "COUNT_ZERO_TO_FIFTY", "COUNT_ZERO_TO_ONE_HUNDRED", "COUNT_ZERO_TO_FIVE_HUNDRED",
            "COUNT_ZERO_TO_ONE_THOUSAND", "COUNT_ZERO_TO_TEN_THOUSAND"};

    /**
     * 空格字符
     */
    public static final String SPACE = " ";

    private Constants() {
    }
}
