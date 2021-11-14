/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.datasource.entity.rule.kie.client;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.client.response.KieConfigLabel;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.client.response.KieConfigResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Optional;

/**
 * kie配置中心客户端
 *
 * @author Sherlockhan
 * @since 2020-12-21
 */
@Component
public class KieConfigClient {
    private final CloseableHttpClient client;

    private static final int TIME_OUT = 5000;

    public KieConfigClient() {
        RequestConfig defaultRequestConfig = RequestConfig.custom()
            .setSocketTimeout(TIME_OUT)
            .setConnectTimeout(TIME_OUT)
            .setConnectionRequestTimeout(TIME_OUT)
            .build();
        client = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).build();
    }

    private <T> String toPutMessage(T value) {
        JSONObject object = new JSONObject();
        object.put("value", JSON.toJSONString(value));
        return object.toJSONString();
    }

    private <T> Optional<String> sendToKie(HttpUriRequest request) {
        CloseableHttpResponse response = null;
        try {
            response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                RecordLog.warn(String.format(Locale.ROOT,
                    "Get config from ServiceComb-kie failed, status code is %d",
                    statusCode));
                return Optional.empty();
            }
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "utf-8");
            return Optional.ofNullable(result);
        } catch (IOException e) {
            RecordLog.error("Send to ServiceComb-kie failed.", e);
            return Optional.empty();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                RecordLog.error("Caught IOException in closing httpResponse: ", e);
            }
        }
    }

    private <T> String toPostMessage(String key, T value, KieConfigLabel configLabel) {
        JSONObject object = new JSONObject();
        object.put("key", key);
        object.put("value", JSON.toJSONString(value));

        JSONObject labels = new JSONObject();
        labels.put("service", configLabel.getService());
        labels.put("resource", configLabel.getResource());
        labels.put("systemRuleType", configLabel.getSystemRuleType());

        object.put("labels", labels);
        return object.toJSONString();
    }

    public Optional<KieConfigResponse> getConfig(String url) {
        HttpGet httpGet = new HttpGet(url);
        Optional<String> result = sendToKie(httpGet);

        if (!result.isPresent()) {
            return Optional.empty();
        }

        KieConfigResponse kieResponse = JSON.parseObject(result.get(), KieConfigResponse.class);
        return Optional.ofNullable(kieResponse);
    }

    public <T> Optional<KieConfigResponse> updateConfig(String url, T value) {
        HttpPut httpPut = new HttpPut(url);
        httpPut.setHeader("Content-type", "application/json");
        String bodyStr = toPutMessage(value);

        try {
            httpPut.setEntity(new StringEntity(bodyStr));
        } catch (UnsupportedEncodingException e) {
            RecordLog.error("Trans to json body failed.", e);
            return Optional.empty();
        }

        Optional<String> result = sendToKie(httpPut);

        if (!result.isPresent()) {
            return Optional.empty();
        }

        KieConfigResponse kieResponse = JSON.parseObject(result.get(), KieConfigResponse.class);
        return Optional.ofNullable(kieResponse);
    }

    public <T> Optional<KieConfigResponse> addConfig(String url, String key, T rule, KieConfigLabel label) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-type", "application/json");
        String bodyStr = toPostMessage(key, rule, label);

        try {
            httpPost.setEntity(new StringEntity(bodyStr));
        } catch (UnsupportedEncodingException e) {
            RecordLog.error("Trans to json body failed.", e);
            return Optional.empty();
        }

        Optional<String> result = sendToKie(httpPost);

        if (!result.isPresent()) {
            return Optional.empty();
        }

        KieConfigResponse kieResponse = JSON.parseObject(result.get(), KieConfigResponse.class);
        return Optional.ofNullable(kieResponse);
    }

    public Optional<String> deleteConfig(String urlPrefix, String id) {
        String url = urlPrefix + "/" + id;
        HttpDelete httpDelete = new HttpDelete(url);
        return sendToKie(httpDelete);
    }
}

