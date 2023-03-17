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

package com.huaweicloud.sermant.router.spring.service;

import com.huaweicloud.sermant.core.plugin.service.PluginService;

import java.util.List;
import java.util.Map;

/**
 * LaneHandlerInterceptor的service
 *
 * @author provenceee
 * @since 2023-02-20
 */
public interface LaneService extends PluginService {
    /**
     * 获取泳道标记
     *
     * @param path 请求路径
     * @param methodName http方法
     * @param headers http请求头
     * @param parameters url参数
     * @return 泳道标记
     */
    Map<String, List<String>> getLaneByParameterArray(String path, String methodName, Map<String, List<String>> headers,
            Map<String, String[]> parameters);

    /**
     * 获取泳道标记
     *
     * @param path 请求路径
     * @param methodName http方法
     * @param headers http请求头
     * @param parameters url参数
     * @return 泳道标记
     */
    Map<String, List<String>> getLaneByParameterList(String path, String methodName, Map<String, List<String>> headers,
            Map<String, List<String>> parameters);
}
