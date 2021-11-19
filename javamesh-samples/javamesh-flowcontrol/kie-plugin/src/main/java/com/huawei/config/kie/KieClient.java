/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.config.kie;

import com.huawei.config.client.AbstractClient;
import com.huawei.config.client.ClientUrlManager;
import com.huawei.config.http.HttpClient;
import com.huawei.config.http.HttpResult;

/**
 * kie客户端
 *
 * @author zhouss
 * @since 2021-11-17
 */
public class KieClient extends AbstractClient {

    private final ResultHandler<KieResponse> defaultHandler = new ResultHandler.DefaultResultHandler();

    private final String kieApiTemplate = "/v1/%s/kie/kv?";

    private String kieApi;

    public KieClient(ClientUrlManager clientUrlManager) {
        super(clientUrlManager);
        kieApi = String.format(kieApiTemplate, "default");
    }

    public KieClient(ClientUrlManager clientUrlManager, String project) {
        super(clientUrlManager);
        kieApi = String.format(kieApiTemplate, "project");
    }

    public KieClient(ClientUrlManager clientUrlManager, HttpClient httpClient) {
        super(clientUrlManager, httpClient);
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
                .append("&reversion=")
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

    private String formatNullString(String val) {
        if (val == null || val.trim().length() == 0) {
            return "";
        }
        return val;
    }
}
