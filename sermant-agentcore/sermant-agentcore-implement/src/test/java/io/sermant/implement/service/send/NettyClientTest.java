/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sermant.implement.service.send;

import static org.mockito.Mockito.mock;

import io.netty.channel.embedded.EmbeddedChannel;
import io.sermant.implement.service.send.netty.ClientHandler;
import io.sermant.implement.service.send.netty.NettyClient;
import io.sermant.implement.service.send.netty.pojo.Message;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ClientHandler Unit Test
 */
public class NettyClientTest {
    private NettyClient nettyClient;

    @Before
    public void setUp() {
        nettyClient = mock(NettyClient.class);
    }

    /**
     * Test write inbound
     */
    @Test
    public void testWriteInBound() {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new ClientHandler(nettyClient));
        boolean writeInBound = embeddedChannel.writeInbound(Message.ServiceData.newBuilder().build());
        Assert.assertTrue(writeInBound);
        Assert.assertTrue(embeddedChannel.finish());

        Object object = embeddedChannel.readInbound();
        Assert.assertNotNull(object);
    }

    /**
     * Test write outbound
     */
    @Test
    public void testWriteOutBound() {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new ClientHandler(nettyClient));
        boolean writeOutBound = embeddedChannel.writeOutbound(Message.ServiceData.newBuilder().build());
        Assert.assertTrue(writeOutBound);
        Assert.assertTrue(embeddedChannel.finish());

        Object object = embeddedChannel.readOutbound();
        Assert.assertNotNull(object);
    }
}
