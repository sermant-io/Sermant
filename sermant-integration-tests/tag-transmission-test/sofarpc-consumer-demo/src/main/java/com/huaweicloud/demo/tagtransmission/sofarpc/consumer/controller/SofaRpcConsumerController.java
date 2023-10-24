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

package com.huaweicloud.demo.tagtransmission.sofarpc.consumer.controller;

import com.huaweicloud.demo.tagtransmission.rpc.api.sofarpc.SofaRpcTagTransmissionService;

import com.alipay.sofa.rpc.config.ConsumerConfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用于验证sofarpc 透传流量标签
 *
 * @author daizhenyu
 * @since 2023-10-13
 **/
@RestController
@RequestMapping(value = "sofaRpc")
public class SofaRpcConsumerController {
    private static final int SOFARPC_TIMEOUT = 10000;

    @Value("${sofa.rpc.url}")
    private String sofaRpcUrl;

    private ConsumerConfig<SofaRpcTagTransmissionService> sofaConsumerConfig;

    /**
     * 验证sofarpc透传流量标签
     *
     * @return 流量标签值
     */
    @RequestMapping(value = "testSofaRpc", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String testSofaRpc() {
        if (sofaConsumerConfig == null) {
            synchronized (this) {
                if (sofaConsumerConfig == null) {
                    sofaConsumerConfig = new ConsumerConfig<SofaRpcTagTransmissionService>()
                            .setInterfaceId(SofaRpcTagTransmissionService.class.getName())
                            .setDirectUrl(sofaRpcUrl)
                            .setConnectTimeout(SOFARPC_TIMEOUT);
                }
            }
        }
        return sofaConsumerConfig.refer().transmitTag();
    }
}
