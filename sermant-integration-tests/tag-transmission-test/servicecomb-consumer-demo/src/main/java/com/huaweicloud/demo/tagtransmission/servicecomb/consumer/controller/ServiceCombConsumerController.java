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

package com.huaweicloud.demo.tagtransmission.servicecomb.consumer.controller;

import com.huaweicloud.demo.tagtransmission.rpc.api.servicecomb.ServiceCombTagTransmissionService;

import org.apache.servicecomb.provider.pojo.RpcReference;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * servicecomb consumer端controll，用于验证流量标签透传
 *
 * @author daizhenyu
 * @since 2023-10-13
 **/
@RestSchema(schemaId = "ConsumerController")
@RequestMapping(value = "serviceCombConsumer")
public class ServiceCombConsumerController {
    @RpcReference(schemaId = "ProviderController", microserviceName = "servicecomb-provider")
    private ServiceCombTagTransmissionService tagTransmissionService;

    /**
     * 验证servicecomb rpc透传流量标签
     *
     * @return 流量标签值
     */
    @RequestMapping(value = "testServiceCombRpc", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String testServiceCombRpc() {
        return tagTransmissionService.transmitTag();
    }
}
