package com.huawei.recordconsole.strategy;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

public class TopicHandleStrategyFactoryTest {

    @Autowired
    private TopicHandleStrategyFactory topicHandleStrategyFactory;

    @Autowired
    private final Map<String, InterfaceTopicHandleStrategy> topicHandleStrategyMap = new ConcurrentHashMap<>(16);


    @Test
    public void gettopic() {
        String topic = "request";
        InterfaceTopicHandleStrategy topicHandleStrategy = topicHandleStrategyMap.get(topic);
        assertThat(topicHandleStrategy, not("null"));
    }

}