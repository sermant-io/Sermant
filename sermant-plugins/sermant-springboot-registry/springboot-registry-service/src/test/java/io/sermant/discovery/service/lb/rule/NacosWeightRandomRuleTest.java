package io.sermant.discovery.service.lb.rule;

import java.util.*;
import java.math.*;

import com.alibaba.nacos.api.naming.pojo.Instance;
import io.sermant.discovery.entity.ServiceInstance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NacosWeightRandomRuleTest {

    @InjectMocks
    private NacosWeightRandomRule nacosWeightRandomRule;

    private List<ServiceInstance> instances;

    @Before
    public void setUp() {
        instances = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ServiceInstance instance = mock(ServiceInstance.class);
            Map<String, String> metadata = new ConcurrentHashMap<>();
            metadata.put("nacos.weight", String.valueOf(1));
            when(instance.getMetadata()).thenReturn(metadata);
            instances.add(instance);
        }
        instances.get(0).getMetadata().put("nacos.weight", "1");
        instances.get(1).getMetadata().put("nacos.weight", "2");
        instances.get(2).getMetadata().put("nacos.weight", "3");
        instances.get(3).getMetadata().put("nacos.weight", "4");
        instances.get(4).getMetadata().put("nacos.weight", "5");
    }

    @Test
    public void testDoChoose_WithWeights_ShouldSelectInstancesAccordingToWeights() throws InterruptedException {
        int count = 10000;
        CountDownLatch countDownLatch = new CountDownLatch(count);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Map<ServiceInstance, Integer> instanceCount = new ConcurrentHashMap<>();

        for (int i = 0; i < count; i++) {
            executorService.submit(() -> {
                try {
                    ServiceInstance instance = nacosWeightRandomRule.doChoose("testService", instances);
                    instanceCount.put(instance, instanceCount.getOrDefault(instance, 0) + 1);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();

        assertEquals(10000, countDownLatch.getCount());
        assertEquals(5, instanceCount.size());

        // Validate the selection ratio according to weights
        assertEquals(1000, instanceCount.get(instances.get(0)), 100);
        assertEquals(2000, instanceCount.get(instances.get(1)), 100);
        assertEquals(3000, instanceCount.get(instances.get(2)), 100);
        assertEquals(4000, instanceCount.get(instances.get(3)), 100);
        assertEquals(5000, instanceCount.get(instances.get(4)), 100);
    }
}
