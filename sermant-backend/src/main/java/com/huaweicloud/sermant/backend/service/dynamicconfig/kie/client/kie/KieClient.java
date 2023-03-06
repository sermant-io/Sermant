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

package com.huaweicloud.sermant.backend.service.dynamicconfig.kie.client.kie;

import com.huaweicloud.sermant.backend.service.dynamicconfig.Config;
import com.huaweicloud.sermant.backend.service.dynamicconfig.kie.client.AbstractClient;
import com.huaweicloud.sermant.backend.service.dynamicconfig.kie.client.ClientUrlManager;
import com.huaweicloud.sermant.backend.service.dynamicconfig.kie.client.http.HttpClient;
import com.huaweicloud.sermant.backend.service.dynamicconfig.kie.client.http.HttpResult;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * kie客户端
 *
 * @author zhouss
 * @since 2021-11-17
 */
public class KieClient extends AbstractClient {

    private static final String KIE_API_TEMPLATE = "/v1/%s/kie/kv?";

    private final ResultHandler<KieResponse> defaultHandler = new ResultHandler.DefaultResultHandler();

    private String kieApi;

    /**
     * Constructor.
     *
     * @param clientUrlManager clientUrlManager
     */
    public KieClient(ClientUrlManager clientUrlManager) {
        this(clientUrlManager, Config.getInstance().getProject());
    }

    /**
     * Constructor.
     *
     * @param clientUrlManager clientUrlManager
     * @param project project
     */
    public KieClient(ClientUrlManager clientUrlManager, String project) {
        this(clientUrlManager, null, project);
    }

    /**
     * Constructor.
     *
     * @param clientUrlManager clientUrlManager
     * @param httpClient httpClient
     * @param project project
     */
    public KieClient(ClientUrlManager clientUrlManager, HttpClient httpClient, String project) {
        super(clientUrlManager, httpClient);
        kieApi = String.format(KIE_API_TEMPLATE, project);
    }

    public void setProject(String project) {
        this.kieApi = String.format(KIE_API_TEMPLATE, project);
    }

    /**
     * 插叙Kie配置
     *
     * @param request 请求体
     * @return KieResponse
     */
    public KieResponse queryConfigurations(KieRequest request) {
        return queryConfigurations(request, defaultHandler);
    }

    /**
     * 查询Kie配置
     *
     * @param request 请求体
     * @param responseHandler http结果处理器
     * @param <T> 转换后的目标类型
     * @return 响应结果
     */
    public <T> T queryConfigurations(KieRequest request, ResultHandler<T> responseHandler) {
        if (request == null || responseHandler == null) {
            return null;
        }
        final StringBuilder requestUrl = new StringBuilder().append(clientUrlManager.getUrl()).append(kieApi);
        requestUrl.append(formatNullString(request.getLabelCondition()))
                .append("&revision=")
                .append(formatNullString(request.getRevision()));
        if (request.isAccurateMatchLabel()) {
            requestUrl.append("&match=exact");
        }
        if (request.getWait() != null) {
            requestUrl.append("&wait=").append(formatNullString(request.getWait())).append("s");
        }
        final HttpResult httpResult = httpClient.doGet(requestUrl.toString(), request.getRequestConfig());
        return responseHandler.handle(httpResult);
    }

    /**
     * 发布配置
     *
     * @param key 请求键
     * @param labels 标签
     * @param content 配置
     * @param enabled 状态
     * @return 是否发布成功
     */
    public boolean publishConfig(String key, Map<String, String> labels, String content, boolean enabled) {
        final Map<String, Object> params = new HashMap<>();
        params.put("key", key);
        params.put("value", content);
        params.put("labels", labels);
        params.put("status", enabled ? "enabled" : "disabled");
        final HttpResult httpResult = this.httpClient.doPost(clientUrlManager.getUrl() + kieApi, params);
        return httpResult.getCode() == HttpStatus.SC_OK;
    }

    private String formatNullString(String val) {
        if (val == null || val.trim().length() == 0) {
            return "";
        }
        return val;
    }
}
