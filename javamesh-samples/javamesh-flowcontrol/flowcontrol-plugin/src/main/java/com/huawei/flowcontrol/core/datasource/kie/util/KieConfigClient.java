/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.core.datasource.kie.util;

import com.alibaba.fastjson.JSON;
import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import com.huawei.flowcontrol.core.datasource.kie.util.response.KieConfigResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * kie配置中心类
 *
 * @author hanpeng
 * @since 2020-10-14
 */
public class KieConfigClient {
    private static final CloseableHttpClient CLIENT = HttpClients.createDefault();
    private static final Logger LOGGER = LogFactory.getLogger();

    private KieConfigClient() {
    }

    /**
     * 根据url从kie中获取配置信息
     *
     * @param url 配置中心地址
     * @return kie配置响应实体
     */
    public static KieConfigResponse getConfig(String url) {
        HttpGet httpGet = new HttpGet(url);
        KieConfigResponse kieResponse;
        CloseableHttpResponse response = null;
        try {
            response = CLIENT.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                LOGGER.warning(String.format(Locale.ROOT, "Get config from ServiceComb-kie failed, status code is %d",
                    statusCode));
                return null;
            }

            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "utf-8");
            kieResponse = JSON.parseObject(result, KieConfigResponse.class);
        } catch (IOException e) {
            LOGGER.severe("Get config from ServiceComb-kie failed.");
            return null;
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                LOGGER.severe(String.format(Locale.ROOT, "Response close failed when get config, %s", e));
            }
        }

        return kieResponse;
    }
}
