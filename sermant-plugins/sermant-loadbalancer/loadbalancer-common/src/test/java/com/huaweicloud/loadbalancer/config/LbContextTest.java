/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.loadbalancer.config;

import org.junit.Assert;
import org.junit.Test;

/**
 * test load balancing
 *
 * @author zhouss
 * @since 2022-08-16
 */
public class LbContextTest {
    /**
     * test load balancing
     */
    @Test
    public void isTargetLb() {
        LbContext.INSTANCE.setCurLoadbalancerType(LbContext.LOADBALANCER_DUBBO);
        Assert.assertTrue(LbContext.INSTANCE.isTargetLb(LbContext.LOADBALANCER_DUBBO));
        LbContext.INSTANCE.setCurLoadbalancerType(LbContext.LOADBALANCER_RIBBON);
        Assert.assertTrue(LbContext.INSTANCE.isTargetLb(LbContext.LOADBALANCER_RIBBON));
        LbContext.INSTANCE.setCurLoadbalancerType(LbContext.LOADBALANCER_SPRING);
        Assert.assertTrue(LbContext.INSTANCE.isTargetLb(LbContext.LOADBALANCER_SPRING));
    }
}
