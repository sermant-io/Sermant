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

package com.huawei.flowcontrol.common.core.resolver;

import com.huawei.flowcontrol.common.core.rule.SystemRule;
import org.junit.Assert;

/**
 * 系统规则解析测试
 *
 * @author xuezechao1
 * @since 2022-12-13
 */
public class SystemRuleResolverTest extends AbstractRuleResolverTest<SystemRule> {

    private static final double SYSTEM_LOAD = 1.0;

    private static final double CPU_USAGE = 0.6;

    private static final double QPS = 500;

    private static final long AVE_RT = 200;

    private static final long THREAD_NUM = 20;

    private static final double DELTA = 1 >> 6;

    @Override
    public AbstractResolver<SystemRule> getResolver() {
        return new SystemRuleResolver();
    }

    @Override
    public String getConfigKey() {
        return SystemRuleResolver.CONFIG_KEY;
    }

    @Override
    public String getValue() {
        return "systemLoad: 1.0\n"
                + "cpuUsage: 0.6\n"
                + "qps: 500\n"
                + "aveRt: 200\n"
                + "threadNum: 20";
    }

    @Override
    public void checkAttrs(SystemRule rule) {
        Assert.assertEquals(SYSTEM_LOAD, rule.getSystemLoad(), DELTA);
        Assert.assertEquals(CPU_USAGE, rule.getCpuUsage(), DELTA);
        Assert.assertEquals(QPS, rule.getQps(), DELTA);
        Assert.assertEquals(AVE_RT, rule.getAveRt(), DELTA);
        Assert.assertEquals(THREAD_NUM, rule.getThreadNum(), DELTA);
    }
}
