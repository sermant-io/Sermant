package com.huawei.javamesh.backend;

import static org.mockito.Mockito.mock;

import com.huawei.javamesh.backend.common.conf.DataTypeTopicMapping;
import com.huawei.javamesh.backend.pojo.Message;
import com.huawei.javamesh.backend.server.ServerHandler;

import io.netty.channel.embedded.EmbeddedChannel;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Test;

public class NettyServerTest {
    private KafkaProducer<String, byte[]> producer;
    private KafkaConsumer<String, String> consumer;

    private DataTypeTopicMapping topicMapping;

    @Before
    public void setUp() {
        producer = mock(KafkaProducer.class);
        topicMapping = mock(DataTypeTopicMapping.class);
    }

    /**
     * 测试入站消息
     */
    @Test
    public void testWriteInBound() {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new ServerHandler(producer, consumer, topicMapping));
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
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new ServerHandler(producer, consumer, topicMapping));
        boolean writeOutBound = embeddedChannel.writeOutbound(Message.ServiceData.newBuilder().build());
        Assert.assertTrue(writeOutBound);
        Assert.assertTrue(embeddedChannel.finish());

        Object object = embeddedChannel.readOutbound();
        Assert.assertNotNull(object);
    }

}
