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

package com.huaweicloud.sermant.implement.service.dynamicconfig.kie.client.kie;

import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.service.dynamicconfig.config.KieDynamicConfig;
import com.huaweicloud.sermant.implement.service.dynamicconfig.kie.client.AbstractClient;
import com.huaweicloud.sermant.implement.service.dynamicconfig.kie.client.ClientUrlManager;
import com.huaweicloud.sermant.implement.service.dynamicconfig.kie.client.http.HttpClient;
import com.huaweicloud.sermant.implement.service.dynamicconfig.kie.client.http.HttpResult;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * kie客户端
 *
 * @author zhouss
 * @since 2021-11-17
 */
public class KieClient extends AbstractClient {
    /**
     * 缺省版本号
     */
    private static final String ABSENT_REVISION = "0";

    private final ResultHandler<KieResponse> defaultHandler = new ResultHandler.DefaultResultHandler();

    private final String kieApiTemplate = "/v1/%s/kie/kv?";

    private String kieApi;

    /**
     * kei客户端构造器
     *
     * @param clientUrlManager kie url管理器
     * @param timeout          超时时间
     */
    public KieClient(ClientUrlManager clientUrlManager, int timeout) {
        this(clientUrlManager, ConfigManager.getConfig(KieDynamicConfig.class).getProject(), timeout);
    }

    /**
     * kei客户端构造器
     *
     * @param clientUrlManager kie url管理器
     * @param project          命名空间
     * @param timeout          超时时间
     */
    public KieClient(ClientUrlManager clientUrlManager, String project, int timeout) {
        this(clientUrlManager, null, project, timeout);
    }

    /**
     * kei客户端构造器
     *
     * @param clientUrlManager kie url管理器
     * @param httpClient       指定请求器
     * @param project          命名空间
     * @param timeout          超时时间
     */
    public KieClient(ClientUrlManager clientUrlManager, HttpClient httpClient, String project, int timeout) {
        super(clientUrlManager, httpClient, timeout);
        kieApi = String.format(kieApiTemplate, project);
    }

    public void setProject(String project) {
        this.kieApi = String.format(kieApiTemplate, project);
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
     * @param request         请求体
     * @param responseHandler http结果处理器
     * @param <T>             转换后的目标类型
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
     * @param key     请求键
     * @param labels  标签
     * @param content 配置
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

    /**
     * 更新配置
     *
     * @param keyId   key-id
     * @param content 更新内容
     * @param enabled 是否开启
     * @return 是否更新成功
     */
    public boolean doUpdateConfig(String keyId, String content, boolean enabled) {
        final Map<String, Object> params = new HashMap<>();
        params.put("value", content);
        params.put("status", enabled ? "enabled" : "disabled");
        final HttpResult httpResult = this.httpClient.doPut(buildKeyIdUrl(keyId), params);
        return httpResult.getCode() == HttpStatus.SC_OK;
    }

    private String buildKeyIdUrl(String keyId) {
        return String.format(Locale.ENGLISH, "%s/%s",
            clientUrlManager.getUrl() + kieApi.substring(0, kieApi.length() - 1), keyId);
    }

    /**
     * 删除方法
     *
     * @param keyId key编号
     * @return 是否删除成功
     */
    public boolean doDeleteConfig(String keyId) {
        final HttpResult httpResult = this.httpClient.doDelete(buildKeyIdUrl(keyId));
        return httpResult.getCode() == HttpStatus.SC_OK;
    }

    private String formatNullString(String val) {
        if (val == null || val.trim().length() == 0) {
            // 当版本号为空时，默认设置版本号为"0", 当有版本更新的数据会立即返回, 避免阻塞问题
            return ABSENT_REVISION;
        }
        return val;
    }
}
