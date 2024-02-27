/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.dubbo.mapper;

import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.router.common.mapper.AbstractMetadataMapper;
import com.huaweicloud.sermant.router.common.utils.DubboReflectUtils;

import org.apache.dubbo.metadata.MetadataInfo;
import org.apache.dubbo.registry.client.ServiceInstance;

import java.util.HashMap;
import java.util.Map;

/**
 * dubbo3.x注册应用类型instance模式下，获取服务端所有参数，匹配目标版本号的invoker
 *
 * @author chengyouling
 * @since 2024-02-26
 */
public class Dubbo3InstanceMapper extends AbstractMetadataMapper<Object> {
    @Override
    public Map<String, String> apply(Object obj) {
        Object url = DubboReflectUtils.getUrl(obj);
        Object instance = DubboReflectUtils.getInstance(url);
        Map<String, String> params = new HashMap<>();
        if (instance != null && ((ServiceInstance) instance).getAllParams() != null) {
            params.putAll(((ServiceInstance) instance).getAllParams());
        }
        Object metadataInfo = DubboReflectUtils.getMetadataInfo(url);
        String serviceKey = (String) DubboReflectUtils.getProtocolServiceKey(url);
        if (metadataInfo != null && !StringUtils.isEmpty(serviceKey)) {
            params.putAll(((MetadataInfo) metadataInfo).getParameters(serviceKey));
        }
        return params;
    }
}
