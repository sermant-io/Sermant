/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.common.metric;

import com.huawei.sermant.core.service.ServiceManager;
import com.huawei.sermant.core.service.send.GatewayClient;

import java.io.UnsupportedEncodingException;

/**
 * netty指标发送
 * 基于agent core 核心功能 {@link com.huawei.sermant.core.service.send.GatewayClient}
 *
 * @author zhouss
 * @since 2021-12-07
 */
public class NettyMetricSender extends AbstractMetricSender {
    /**
     * 关联backend的数据类型
     */
    private static final int FLOW_CONTROL_METRIC_TYPE = 11;

    private final GatewayClient gatewayClient;

    public NettyMetricSender() {
        this.gatewayClient = ServiceManager.getService(GatewayClient.class);
    }

    @Override
    public void sendMetric(Object data) {
        try {
            gatewayClient.send(String.valueOf(data).getBytes("UTF-8"), FLOW_CONTROL_METRIC_TYPE);
        } catch (UnsupportedEncodingException ignored) {
            // ignored , it would not happen
        }
    }
}
