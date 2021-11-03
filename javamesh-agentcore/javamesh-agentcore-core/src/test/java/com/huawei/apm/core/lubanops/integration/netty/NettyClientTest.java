package com.huawei.apm.core.lubanops.integration.netty;

import static org.mockito.Mockito.mock;

import com.huawei.apm.core.lubanops.integration.transport.netty.client.ClientHandler;
import com.huawei.apm.core.lubanops.integration.transport.netty.client.NettyClient;
import com.huawei.apm.core.lubanops.integration.transport.netty.pojo.Message;

import io.netty.channel.embedded.EmbeddedChannel;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ClientHandler单元测试
 */
public class NettyClientTest {
    private NettyClient nettyClient;

    @Before
    public void setUp() {
        nettyClient = mock(NettyClient.class);
    }

    /**
     * 测试入站消息
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
     * 测试出站消息
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
