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

package com.huawei.flowcontrol.res4j.chain.handler;

import com.huawei.flowcontrol.common.core.ResolverManager;
import com.huawei.flowcontrol.common.core.resolver.SystemRuleResolver;
import com.huawei.flowcontrol.common.entity.FlowControlResult;
import com.huawei.flowcontrol.common.entity.RequestEntity;
import com.huawei.flowcontrol.res4j.chain.HandlerChainEntry;
import com.huawei.flowcontrol.res4j.windows.SystemStatus;
import org.junit.Assert;

public class SystemRuleRequestHandlerTest extends BaseEntityTest implements RequestTest {

    private HandlerChainEntry entry;
    private String sourceName;

    /**
     * 测试限流
     *
     * @author xuezechao1
     * @since 2022-12-12
     */
    @Override
    public void test(HandlerChainEntry entry, String sourceName) {
        this.entry = entry;
        this.sourceName = sourceName;
        SystemStatus.getInstance().setQps(20D);
        Assert.assertEquals(checkHttp(httpServerEntity).buildResponseMsg(), "Trigger qps flow control");
        SystemStatus.getInstance().setQps(5D);
        SystemStatus.getInstance().setCurrentCpuUsage(0.8D);
        Assert.assertEquals(checkHttp(httpServerEntity).buildResponseMsg(), "Trigger cpu flow control");
        SystemStatus.getInstance().setCurrentCpuUsage(0.5D);
        SystemStatus.getInstance().setAveRt(20D);
        Assert.assertEquals(checkHttp(httpServerEntity).buildResponseMsg(), "Trigger rt flow control");
    }

    @Override
    public void publishRule() {
        ResolverManager.INSTANCE.resolve(buildKey(SystemRuleResolver.CONFIG_KEY), getQpsRule(), false);
    }

    @Override
    public void clear() {
        ResolverManager.INSTANCE.resolve(buildKey(SystemRuleResolver.CONFIG_KEY), null, true);
    }

    private FlowControlResult checkHttp(RequestEntity requestEntity) {
        final FlowControlResult flowControlResult = new FlowControlResult();
        entry.onBefore(sourceName, requestEntity, flowControlResult);
        entry.onThrow(sourceName, new Exception("error"));
        entry.onResult(sourceName, new Object());
        return flowControlResult;
    }

    private String getQpsRule() {
        return "systemLoad: 1.0\n"
                + "cpuUsage: 0.6\n"
                + "qps: 10\n"
                + "aveRt: 10\n"
                + "threadNum: 10";
    }
}
