/*
 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.core.datasource.kie.util;

import com.huawei.flowcontrol.core.datasource.kie.util.response.KieConfigResponse;
import com.huawei.sermant.core.common.LoggerFactory;

import com.alibaba.fastjson.JSON;

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
    private static final Logger LOGGER = LoggerFactory.getLogger();

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
