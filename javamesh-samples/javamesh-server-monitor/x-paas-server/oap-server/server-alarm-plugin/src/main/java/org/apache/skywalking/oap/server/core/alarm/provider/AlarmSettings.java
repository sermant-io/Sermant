/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.oap.server.core.alarm.provider;

import lombok.Data;
import org.apache.skywalking.oap.server.library.module.ModuleConfig;

/**
 * kafka配置类
 *
 * @author hudeyu
 * @since 2021-07-27
 */
@Data
public class AlarmSettings extends ModuleConfig {
    /**
     * 服务器地址
     */
    private String bootstrapServers;

    /**
     * 分区数
     */
    private int partitions;

    /**
     * 副本数
     */
    private int replicationFactor;

    /**
     * 每轮批量发送的大小，单位byte
     */
    private int batchSize;

    /**
     * 每轮数据停留时间
     */
    private long lingerMs;

    /**
     * 键序列化方式
     */
    private String keySerializer;

    /**
     * 值序列化方式
     */
    private String valueSerializer;

    /**
     * 重试次数
     */
    private int retries;

    /**
     * 确认模式
     */
    private String acks;

    /**
     * 主题
     */
    private String topic;
    /**
     * 是否需要发送kafka
     */
    private boolean isOpenKafkaCallBack;
}
