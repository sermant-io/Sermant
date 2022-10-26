/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.intergration.registry;

import com.huaweicloud.intergration.common.utils.RequestUtils;
import com.huaweicloud.intergration.config.supprt.KieClient;

import org.junit.Assert;
import org.junit.Before;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * SpringBoot注册发现测试
 *
 * @author zhouss
 * @since 2022-10-26
 */
public class BootRegistryTest {
    private static final String URL = "http://localhost:8005/bootRegistry";

    private final RestTemplate restTemplate = new RestTemplate();

    private final KieClient kieClient = new KieClient(restTemplate, null, getLabels());

    @Before
    public void before() throws InterruptedException {
        if (ConfigGlobalStatus.INSTANCE.isOpen(getType())) {
            return;
        }
        publishGrayStrategy();
        Thread.sleep(30000);
        ConfigGlobalStatus.INSTANCE.saveOpenSate(getType());
    }

    /**
     * 发布灰度策略
     */
    public void publishGrayStrategy() {
        kieClient.publishConfig("sermant.plugin.registry", "strategy: all");
    }

    /**
     * 核对请求是否正确
     *
     * @param api 请求路径
     * @param httpMethod 请求方法
     */
    protected void check(String api, HttpMethod httpMethod) {
        final HashSet<String> result = new HashSet<>();
        for (int i = 0; i < 2; i++) {
            result.add(req(api, httpMethod));
        }
        Assert.assertTrue(result.size() > 1);
    }

    /**
     * 请求接口
     *
     * @param api api路径
     * @param httpMethod 方法类型
     * @return 请求结果
     */
    protected String req(String api, HttpMethod httpMethod) {
        if (httpMethod == HttpMethod.GET) {
            return RequestUtils.get(buildUrl(api), Collections.emptyMap(), String.class);
        } else {
            return RequestUtils.post(buildUrl(api), Collections.emptyMap(), String.class);
        }
    }

    private String buildUrl(String api) {
        return getUrl() + "/" + api;
    }

    /**
     * 获取URL地址
     *
     * @return String
     */
    protected String getUrl() {
        return URL;
    }

    /**
     * 获取kie订阅标签
     *
     * @return 订阅标签
     */
    protected Map<String, String> getLabels() {
        final Map<String, String> labels = new HashMap<>();
        labels.put("app", getType());
        labels.put("environment", "development");
        return labels;
    }

    /**
     * 框架类型
     *
     * @return 框架类型
     */
    protected String getType() {
        return "rest";
    }
}
