/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.entity;

import com.huawei.route.server.share.ShareKey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * 基础必要的注册数据
 *
 * @author zhouss
 * @since 2021-10-18
 */
@Getter
@Setter
@NoArgsConstructor
public class BaseRegistrarMessage extends ShareKey implements Serializable {
    private static final long serialVersionUID = 128309821938201L;

    /**
     * 当前服务名称
     */
    private String serviceName;

    /**
     * 实例ip地址
     */
    private String ip;

    /**
     * 端口
     */
    private int port;

    /**
     * Ldc名称
     */
    private String ldc;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BaseRegistrarMessage that = (BaseRegistrarMessage) o;
        return port == that.port && ip.equals(that.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }
}
