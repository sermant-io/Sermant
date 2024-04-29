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

package io.sermant.tag.transmission.server.service;

import io.sermant.core.plugin.service.PluginService;

import java.util.Map;

/**
 * 用于从http header的value中解析json字符串为rpc服务的header
 *
 * @author daizhenyu
 * @since 2023-09-14
 **/
public interface ServiceCombHeaderParseService extends PluginService {
    /**
     * 将json字符串解析为rpc服务 的header
     *
     * @param header Json字符串
     * @return rpc服务 的header
     */
    Map<String, String> parseHeaderFromJson(String header);
}
