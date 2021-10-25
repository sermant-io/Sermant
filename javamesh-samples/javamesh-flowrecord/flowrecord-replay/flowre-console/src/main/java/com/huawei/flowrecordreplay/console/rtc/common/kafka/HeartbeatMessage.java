/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.rtc.common.kafka;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

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
    private String ip;
    /**
     * 心跳版本
     */
    private Date heartbeatVersion;
    /**
     * 在存入redis出错的情况下标记重试的次数
     */
    private transient int retryTimes;

    /**
     * 验证字符是否合规
     *
     * @return true为合规。false为不合规
     */
    public boolean validate() {
        return StringUtils.isNoneBlank(app, hostname, ip)
                && heartbeatVersion != null;
    }
}
