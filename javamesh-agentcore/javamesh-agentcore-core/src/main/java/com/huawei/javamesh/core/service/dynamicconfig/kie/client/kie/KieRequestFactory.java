/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.javamesh.core.service.dynamicconfig.kie.client.kie;

import com.alibaba.fastjson.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

/**
 * kie请求创建工厂
 *
 * @author zhouss
 * @since 2021-11-17
 */
public class KieRequestFactory {
    /**
     * 创建kie请求
     *
     * @param wait 连接维持时间 单位S
     * @param revision 版本
     * @param labels 标签匹配
     * @return KieRequest
     */
    public static String buildKieRequest(String wait, String revision, String ...labels) {
        final KieRequest kieRequest = new KieRequest();
        return JSONObject.toJSONString(kieRequest.setRevision(revision).setWait(wait).setLabelCondition(buildLabels(labels)));
    }

    /**
     * 创建kie请求
     *
     * @param labelCondition 已格式化的标签 例如label=version:1.0&label=app:helloService
     * @param revision 版本
     * @return KieRequest
     */
    public static String buildKieRequest(String revision, String labelCondition) {
        final KieRequest kieRequest = new KieRequest();
        return JSONObject.toJSONString(kieRequest.setRevision(revision).setLabelCondition(labelCondition));
    }

    /**
     * 创建kie请求
     *
     * @param wait 等待响应时间
     * @param labelCondition 已格式化的标签 例如label=version:1.0&label=app:helloService
     * @param revision 版本
     * @return KieRequest
     */
    public static String buildKieRequest(String wait, String revision, String labelCondition) {
        final KieRequest kieRequest = new KieRequest();
        return JSONObject.toJSONString(kieRequest.setWait(wait).setRevision(revision).setLabelCondition(labelCondition));
    }

    /**
     * 创建kie请求
     *
     * @param labels 标签匹配组
     * @return KieRequest
     */
    public static String buildKieRequest(String ...labels) {
        return buildKieRequest(null, null, labels);
    }

    /**
     * 创建kie请求
     *
     * @param wait 连接维持时间 单位S
     * @param labels 标签匹配组
     * @return KieRequest
     */
    public static String buildKieRequest(String wait, String ...labels) {
        return buildKieRequest(wait, null, labels);
    }

    /**
     * 创建kie请求
     *
     * @param labels 标签匹配组  <app,helloService>, <version,1.0>
     * @return KieRequest
     */
    public static String buildKieRequest(Map<String, String> labels) {
        return buildKieRequest(null, buildMapLabels(labels));
    }

    /**
     * 创建kie请求
     *
     * @param wait 连接维持时间 单位S
     * @param revision 版本
     * @param labels 标签匹配
     * @return KieRequest
     */
    public static String buildKieRequest(String wait, String revision, Map<String, String> labels) {
        final KieRequest kieRequest = new KieRequest();
        kieRequest.setLabelCondition(buildMapLabels(labels));
        return JSONObject.toJSONString(kieRequest.setWait(wait).setRevision(revision));
    }

    /**
     * 多个标签查询全部串起来
     *
     * @param labels 标签组
     * @return labels
     */
    public static String buildLabels(String ...labels) {
        if (labels == null || labels.length == 0) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        for (String label : labels) {
            sb.append(label).append("&");
        }
        return sb.toString();
    }

    /**
     * 构建标签
     *
     * @param key 标签键
     * @param value 标签值
     * @return label=version:1.0
     */
    public static String buildLabel(String key, String value) {
        try {
            return "label=" + URLEncoder.encode(key + ":" + value, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
            // ignored
        }
        return null;
    }

    /**
     * 构建标签
     *
     * @param labels 标签值 <app,helloService>, <version,1.0>
     * @return label=version:1.0&label=app:helloService&
     */
    public static String buildMapLabels(Map<String, String> labels) {
        if (labels != null && !labels.isEmpty()) {
            final ArrayList<String> labelList = new ArrayList<String>();
            for (Map.Entry<String, String> entry : labels.entrySet()) {
                labelList.add(buildLabel(entry.getKey(), entry.getValue()));
            }
            return buildLabels(labelList.toArray(new String[0]));
        }
        return null;
    }
}
