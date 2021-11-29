package com.huawei.apm.backend;

import static org.mockito.Mockito.mock;

import com.huawei.apm.backend.common.conf.KafkaConf;
import com.huawei.apm.backend.pojo.Message;
import com.huawei.apm.backend.server.ServerHandler;

import io.netty.channel.embedded.EmbeddedChannel;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.utils.Bytes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Test;

public class NettyServerTest {
    private KafkaProducer<String, String> producer;
    private KafkaConsumer<String, String> consumer;

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
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new ServerHandler(producer, consumer, conf));
        boolean writeInbound = embeddedChannel.writeInbound(Message.ServiceData.newBuilder().build());
        Assert.assertTrue(writeInbound);
        Assert.assertTrue(embeddedChannel.finish());

        Object object = embeddedChannel.readInbound();
        Assert.assertNotNull(object);
    }

    /**
     * 测试出站消息
     */
    @Test
    public void testWriteOutBound() {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new ServerHandler(producer, consumer, conf));
        boolean writeOutBound = embeddedChannel.writeOutbound(Message.ServiceData.newBuilder().build());
        Assert.assertTrue(writeOutBound);
        Assert.assertTrue(embeddedChannel.finish());

        Object object = embeddedChannel.readOutbound();
        Assert.assertNotNull(object);
    }

}
