/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.spring.feign.provider;

import com.huaweicloud.spring.common.flowcontrol.Constants;
import com.huaweicloud.spring.common.flowcontrol.provider.ProviderController;
import com.huaweicloud.spring.feign.api.FlowControlService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流控测试
 *
 * @author zhouss
 * @since 2022-07-29
 */
@Controller
@ResponseBody
public class FlowControlServiceImpl extends ProviderController implements FlowControlService {
    private final Map<String, Integer> counterMap = new ConcurrentHashMap<>();

    /**
     * 实例隔离接口测试
     *
     * @return 实例隔离
     * @throws InterruptedException 线程中断抛出
     */
    @Override
    @RequestMapping("instanceIsolation")
    public String instanceIsolation() throws InterruptedException {
        Thread.sleep(Constants.SLEEP_TIME_MS);
        return Constants.HTTP_OK;
    }

    /**
     * 重试测试
     *
     * @param invocationId 每次的调用ID UUID
     * @return retry
     * @throws Exception 模拟抛出异常进行重试
     */
    @Override
    @RequestMapping(value = "retry", method = RequestMethod.GET)
    public String retry(@RequestParam("invocationId") String invocationId) throws Exception {
        counterMap.putIfAbsent(invocationId, 0);
        counterMap.put(invocationId, counterMap.get(invocationId) + 1);

        int retry = counterMap.get(invocationId);

        if (retry >= Constants.RETRY_TIMES) {
            return String.valueOf(retry);
        }
        throw new Exception("retry!");
    }
}
