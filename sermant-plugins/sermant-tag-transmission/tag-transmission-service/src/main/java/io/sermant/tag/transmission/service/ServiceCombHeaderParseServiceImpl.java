/*
 *   Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.tag.transmission.service;

import com.alibaba.fastjson.JSON;

import io.sermant.tag.transmission.server.service.ServiceCombHeaderParseService;

import java.util.Map;

/**
 * Implementation class of HeaderParseService, used to parse the header of servicecomb rpc service
 *
 * @author daizhenyu
 * @since 2023-09-14
 **/
public class ServiceCombHeaderParseServiceImpl implements ServiceCombHeaderParseService {
    @Override
    public Map<String, String> parseHeaderFromJson(String header) {
        return JSON.parseObject(header, Map.class);
    }
}