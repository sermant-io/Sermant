/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.integration.service;

import com.huaweicloud.integration.client.ProviderClient;
import com.huaweicloud.integration.constants.Constant;
import com.huaweicloud.integration.entity.LaneTestEntity;

import com.alibaba.dubbo.rpc.RpcContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

/**
 * 泳道测试
 *
 * @author provenceee
 * @since 2023-03-02
 */
public class LaneServiceImpl implements LaneService {
    private static final String PROVIDER_URL = "http://dubbo-integration-provider/lane";

    @Resource(name = "fooService")
    private FooService fooService;

    @Autowired
    @Lazy
    private ProviderClient client;

    @Autowired
    @Lazy
    private RestTemplate restTemplate;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${service_meta_version:${SERVICE_META_VERSION:${service.meta.version:1.0.0}}}")
    private String version;

    @Override
    public Map<String, Object> getLaneByDubbo(String name, LaneTestEntity laneTestEntity, String[] arr,
            List<Integer> list, Map<String, Object> map) {
        RpcContext.getContext().setAttachment(Constant.TAG_KEY, Constant.TAG);

        // dubbo 2.5.0 - 2.5.3存在bug，会传递异步调用，所以需要手动设置为同步
        RpcContext.getContext().setAttachment("async", "false");
        Map<String, Object> result = new HashMap<>(fooService.getAttachments());
        result.put(applicationName, getMetadata());
        return result;
    }

    @Override
    public Map<String, Object> getLaneByFeign(String name, LaneTestEntity laneTestEntity) {
        Map<String, Object> result = new HashMap<>(client.getLane());
        result.put(applicationName, getMetadata());
        return result;
    }

    @Override
    public Map<String, Object> getLaneByRest(String name, LaneTestEntity laneTestEntity) {
        Map<String, Object> map = restTemplate.getForObject(PROVIDER_URL, Map.class);
        Map<String, Object> result = new HashMap<>(map);
        result.put(applicationName, getMetadata());
        return result;
    }

    private Map<String, Object> getMetadata() {
        Map<String, Object> meta = new HashMap<>();
        meta.put("version", version);
        return meta;
    }
}