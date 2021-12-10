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

package com.huawei.flowrecordreplay.console.rtc.common.kafka;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * 心跳数据的实体类
 *
 * @author hanpeng
 * @since 2021-04-07
 */
@Getter
@Setter
@ToString
public class HeartbeatMessage extends AbstractMessage {
    /**
     * 序列版本号
     */
    private static final long serialVersionUID = 1L;
    /**
     * 心跳数据的数据格式是一个json对象的字符串
     * <p>
     * 应用名称，如app=org.example.WebApp
     */
    private String app;
    /**
     * 主机名，如hostname=3JZCYAO09XBD3ES
     */
    private String hostname;
    /**
     * IP地址
     */
    private List<String> ip;
    /**
     * 心跳版本
     */
    private Date heartbeatVersion;
    /**
     * 在存入redis出错的情况下标记重试的次数
     */
    private transient int retryTimes;

    private String name;

    private String appType;

    private Date lastHeartbeat;

    /**
     * 验证字符是否合规
     *
     * @return true为合规。false为不合规
     */
    public boolean validate() {
        return StringUtils.isNoneBlank(app, hostname)
                && heartbeatVersion != null && ip.size() > 0;
    }
}
