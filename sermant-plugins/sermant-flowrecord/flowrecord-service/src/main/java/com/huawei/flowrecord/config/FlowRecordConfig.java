/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowrecord.config;

import com.huawei.sermant.core.config.common.ConfigTypeKey;
import com.huawei.sermant.core.plugin.config.PluginConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigTypeKey("flow.record.plugin")
public class FlowRecordConfig implements PluginConfig {
    /**
     * 流配置中心 zookeeper地址
     */
    private String zookeeperAddress = "127.0.0.1:2181";

    /**
     * 配置中心 zookeeper路径
     */
    private String zookeeperPath = "/record_jobs";

    /**
     * 配置中心 Session超时时间
     */
    private String zookeeperTimeout = "50000";

    /**
     * kafka配置参数 连接服务端地址
     */
    private String kafkaBootstrapServers = "127.0.0.1:9092";

    /**
     * kafka配置参数 key序列化
     */
    private String kafkaKeySerializer = "org.apache.kafka.common.serialization.StringSerializer";

    /**
     * kafka配置参数 value序列化
     */
    private String kafkaValueSerializer = "org.apache.kafka.common.serialization.StringSerializer";

    /**
     * kafka配置参数 request的topic名称
     */
    private String kafkaRequestTopic = "request";

    /**
     * kafka配置参数 response的topic名称
     */
    private String kafkaResponseTopic = "response";

    /**
     * kafka配置参数 producer需要server接收到数据之后发出的确认接收的信号 ack 0,1,all
     */
    private String kafkaAcks = "1";

    /**
     * kafka配置参数 控制生产者发送请求最大大小,默认1M （这个参数和Kafka主机的message.max.bytes 参数有关系）
     */
    private String kafkaMaxRequestSize = "1048576";

    /**
     * kafka配置参数 生产者内存缓冲区大小 32M
     */
    private String kafkaBufferMemory = "33554432";

    /**
     * kafka配置参数 重发消息次数
     */
    private String kafkaRetries = "0";

    /**
     * kafka配置参数 客户端将等待请求的响应的最大时间
     */
    private String kafkaRequestTimeoutMs = "10000";

    /**
     * kafka配置参数 最大阻塞时间，超过则抛出异常
     */
    private String kafkaMaxBlockMs = "60000";

    /**
     * redis ip地址
     */
    private String redisHost = "127.0.0.1";

    /**
     * redis 端口
     */
    private String redisPort = "6379";

    /**
     * redis 密码
     */
    private String redisPassword = "";

    /**
     * redis 集群地址
     */
    private String redisUris = "redis://127.0.0.1:6379";

    /**
     * 自定义录制插件拦截类
     */
    private String customEnhanceClass = "org.apache.dubbo.demo.provider.CustomService";

    /**
     * 自定义录制插件拦截的实例方法（多个方法以逗号分隔）
     */
    private String customEnhanceInstanceMethod = "testCustom,newTest";

    /**
     * 自定义录制插件拦截的静态方法（多个方法以逗号分隔）
     */
    private String customEnhanceStaticMethod = "testCustom2";
    /**
     * 心跳名
     */
    private String heartBeatName = "flowRecord-heartbeat";
}
