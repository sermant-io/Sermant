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

package io.sermant.registry.service.client;

import com.google.common.eventbus.EventBus;

import org.apache.servicecomb.service.center.client.DiscoveryEvents.PullInstanceEvent;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test SC service discovery event processing
 *
 * @author zhouss
 * @since 2022-06-30
 */
public class ScDiscoveryTest {
    /**
     * Test event release
     */
    @Test
    public void testEvent() {
        final EventBus eventBus = new EventBus();
        final ServiceCenterClient serviceCenterClient = Mockito.mock(ServiceCenterClient.class);
        final ScDiscovery scDiscovery = new ScDiscovery(serviceCenterClient, eventBus);
        final ScDiscovery spy = Mockito.spy(scDiscovery);
        eventBus.register(spy);
        final PullInstanceEvent pullInstanceEvent = new PullInstanceEvent();
        eventBus.post(pullInstanceEvent);
        Mockito.verify(spy, Mockito.times(1)).onPullInstanceEvent(Mockito.any());
    }
}
