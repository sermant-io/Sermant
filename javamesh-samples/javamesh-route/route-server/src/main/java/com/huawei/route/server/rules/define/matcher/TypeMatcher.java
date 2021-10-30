/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.rules.define.matcher;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.huawei.route.server.rules.define.key.KeyPair;
import lombok.Data;

/**
 * 类型匹配器
 * 基于请求类型  HTTP / DUBBO
 *
 * @author zhouss
 * @since 2021-10-23
 */
@Data
public class TypeMatcher implements Matcher {
    /**
     * 源调用服务，即发起的服务
     * 若该值为空，默认适用于所有的服务请求
     */
    private String source;

    /**
     * 是否匹配所有条件
     */
    @JsonProperty(value = "isFullMatch")
    private boolean isFullMatch;

    /**
     * 匹配路径
     * dubbo : 接口权限定名.方法名
     */
    private String path;

    /**
     * 请求头匹配条件
     */
    private KeyPair<String, Object> headers;
}
