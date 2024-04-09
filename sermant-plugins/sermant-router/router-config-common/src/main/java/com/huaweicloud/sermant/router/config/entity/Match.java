/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.router.config.entity;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;
import java.util.Map;

/**
 * Matching rules
 *
 * @author provenceee
 * @since 2021-10-27
 */
public class Match {
    /**
     * Source, which is the upstream application of the target application
     */
    private String source;

    /**
     * HTTP request method/Dubbo interface method
     */
    private String method;

    /**
     * Dubbo is the interface name, spring is the URL path
     */
    private String path;

    /**
     * protocol: http/dubbo
     */
    private Protocol protocol;

    /**
     * Whether it is a full match
     */
    private boolean fullMatch = true;

    /**
     * Dubbo parameter rules
     */
    @JSONField(deserializeUsing = ValueMatchDeserializer.class)
    private Map<String, List<MatchRule>> args;

    /**
     * dubbo attachments parameter
     */
    @JSONField(deserializeUsing = ValueMatchDeserializer.class)
    private Map<String, List<MatchRule>> attachments;

    /**
     * spring http header rules
     */
    @JSONField(deserializeUsing = ValueMatchDeserializer.class)
    private Map<String, List<MatchRule>> headers;

    /**
     * spring http parameter rules
     */
    @JSONField(deserializeUsing = ValueMatchDeserializer.class)
    private Map<String, List<MatchRule>> parameters;

    /**
     * spring http cookie policy
     */
    @JSONField(deserializeUsing = ValueMatchDeserializer.class)
    private Map<String, List<MatchRule>> cookie;

    /**
     * Tag matching rules for consumers
     */
    @JSONField(deserializeUsing = ValueMatchDeserializer.class)
    private Map<String, List<MatchRule>> tags;

    /**
     * Threshold policy
     */
    private Policy policy;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
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

    public void setAttachments(Map<String, List<MatchRule>> attachments) {
        this.attachments = attachments;
    }

    public Map<String, List<MatchRule>> getAttachments() {
        return attachments;
    }

    public Map<String, List<MatchRule>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<MatchRule>> headers) {
        this.headers = headers;
    }

    public Map<String, List<MatchRule>> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, List<MatchRule>> parameters) {
        this.parameters = parameters;
    }

    public Map<String, List<MatchRule>> getCookie() {
        return cookie;
    }

    public void setCookie(Map<String, List<MatchRule>> cookie) {
        this.cookie = cookie;
    }

    public Map<String, List<MatchRule>> getTags() {
        return tags;
    }

    public void setTags(Map<String, List<MatchRule>> tags) {
        this.tags = tags;
    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }
}