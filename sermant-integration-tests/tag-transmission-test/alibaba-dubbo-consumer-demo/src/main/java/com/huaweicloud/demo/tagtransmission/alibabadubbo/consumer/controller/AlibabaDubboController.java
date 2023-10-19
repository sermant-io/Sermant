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

package com.huaweicloud.demo.tagtransmission.alibabadubbo.consumer.controller;

import com.huaweicloud.demo.tagtransmission.rpc.api.alibabadubbo.AlibabaTagTransmissionService;

import com.alibaba.dubbo.config.annotation.Reference;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * alibaba dubbo消费端
 *
 * @author daizhenyu
 * @since 2023-09-08
 **/
@RestController
@RequestMapping("alibabaDubbo")
public class AlibabaDubboController {
    @Reference(loadbalance = "random")
    private AlibabaTagTransmissionService tagTransmissionService;

    /**
     * 验证alibaba dubbo透传流量标签
     *
     * @return 流量标签值
     */
    @RequestMapping(value = "testAlibabaDubbo", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String testAlibabaDubbo() {
        return tagTransmissionService.transmitTag();
    }
}