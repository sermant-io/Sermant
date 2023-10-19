/*
 *  Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.demo.tagtransmission.apachedubbo.provider.serviceimpl;

import com.huaweicloud.demo.tagtransmission.rpc.api.apachedubbo.ApacheTagTransmissionService;
import com.huaweicloud.demo.tagtransmission.util.HttpClientUtils;

import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Value;

/**
 * dubbo服务实现类
 *
 * @author daizhenyu
 * @since 2023-09-08
 **/
@Service
public class ApacheTagTransmissionServiceImpl implements ApacheTagTransmissionService {
    @Value("${common.server.url}")
    private String commonServerUrl;

    @Override
    public String transmitTag() {
        return HttpClientUtils.doHttpUrlConnectionGet(commonServerUrl);
    }
}
