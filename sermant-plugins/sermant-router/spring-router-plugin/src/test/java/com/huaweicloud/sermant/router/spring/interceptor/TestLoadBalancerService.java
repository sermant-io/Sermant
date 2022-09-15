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

package com.huaweicloud.sermant.router.spring.interceptor;

import com.huaweicloud.sermant.router.spring.service.LoadBalancerService;

import java.util.List;
import java.util.Map;

/**
 * 测试负载均衡服务
 *
 * @author provenceee
 * @since 2022-09-08
 */
public class TestLoadBalancerService implements LoadBalancerService {
    @Override
    public List<Object> getTargetInstances(String targetName, List<Object> serverList, String path,
        Map<String, List<String>> header) {
        serverList.remove(0);
        return serverList;
    }
}