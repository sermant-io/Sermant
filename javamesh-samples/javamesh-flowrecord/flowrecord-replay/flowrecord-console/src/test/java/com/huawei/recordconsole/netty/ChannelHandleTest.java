package com.huawei.recordconsole.netty;

import static org.mockito.Mockito.mock;

import com.huawei.recordconsole.netty.common.conf.KafkaConf;
import com.huawei.recordconsole.netty.pojo.Message;
import com.huawei.recordconsole.netty.server.ServerHandler;

import io.netty.channel.embedded.EmbeddedChannel;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.utils.Bytes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * ServerHandler单元测试
 */
@SpringBootTest
public class ChannelHandleTest {
    private KafkaProducer<String, Bytes> producer;

    private KafkaConf conf;

    @Before
    public void setUp() {
        producer = mock(KafkaProducer.class);
        conf = mock(KafkaConf.class);
    }

    /**
     * 测试入站消息
     */
    @Test
    public void testWriteInBound() {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new ServerHandler(producer, conf));
        boolean writeInbound = embeddedChannel.writeInbound(Message.NettyMessage.newBuilder());
        Assert.assertTrue(writeInbound);
        Assert.assertTrue(embeddedChannel.finish());

        Object o = embeddedChannel.readInbound();
        Assert.assertNotNull(o);
    }

    /**
     * 测试出站消息
     */
    @Test
    public void testWriteOutBound() {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new ServerHandler(producer, conf));
        boolean writeOutBound = embeddedChannel.writeOutbound(Message.NettyMessage.newBuilder());
        Assert.assertTrue(writeOutBound);
        Assert.assertTrue(embeddedChannel.finish());

        Object o = embeddedChannel.readOutbound();
        Assert.assertNotNull(o);
    }

}
