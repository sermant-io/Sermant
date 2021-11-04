/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.route.common.gray.label.entity;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;
import java.util.Map;

/**
 * 匹配规则
 *
 * @author pengyuyi
 * @date 2021/10/27
 */
public class Match {
    /**
     * 来源，即目标应用的上游应用
     */
    private String source;

    /**
     * dubbo为接口名，spring为url路径
     */
    private String path;

    /**
     * 是否全匹配
     */
    private boolean fullMatch;

    /**
     * dubbo参数规则
     */
    @JSONField(deserializeUsing = ValueMatchDeserializer.class)
    private Map<String, List<MatchRule>> args;

    /**
     * spring http header规则
     */
    @JSONField(deserializeUsing = ValueMatchDeserializer.class)
    private Map<String, List<MatchRule>> headers;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isFullMatch() {
        return fullMatch;
    }

    public void setFullMatch(boolean fullMatch) {
        this.fullMatch = fullMatch;
    }

    public Map<String, List<MatchRule>> getArgs() {
        return args;
    }

    public void setArgs(Map<String, List<MatchRule>> args) {
        this.args = args;
    }

    public Map<String, List<MatchRule>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<MatchRule>> headers) {
        this.headers = headers;
    }
}
