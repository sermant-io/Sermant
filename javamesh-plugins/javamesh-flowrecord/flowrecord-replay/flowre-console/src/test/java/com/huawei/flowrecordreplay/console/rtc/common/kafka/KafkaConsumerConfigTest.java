package com.huawei.flowrecordreplay.console.rtc.common.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

public class KafkaConsumerConfigTest {
    @Test
    public void kafkaConfigNotNull() {
        Properties properties = new KafkaConsumerConfig().producerConfigs();
        Assert.assertNotNull(properties.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        Assert.assertNotNull(properties.getProperty(ConsumerConfig.GROUP_ID_CONFIG));
        Assert.assertNotNull(properties.getProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));
        Assert.assertNotNull(properties.getProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG));
        Assert.assertNotNull(properties.getProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG));
        Assert.assertNotNull(properties.getProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG));
        Assert.assertNotNull(properties.getProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG));
        Assert.assertNotNull(properties.getProperty(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG));
        Assert.assertNotNull(properties.getProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG));
        Assert.assertNotNull(properties.getProperty(ConsumerConfig.ISOLATION_LEVEL_CONFIG));
        Assert.assertNotNull(properties.getProperty(ConsumerConfig.EXCLUDE_INTERNAL_TOPICS_CONFIG));
    }

}
