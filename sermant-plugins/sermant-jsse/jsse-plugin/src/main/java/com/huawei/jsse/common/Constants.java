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

package com.huawei.jsse.common;

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

    private Constants() {
    }
}
