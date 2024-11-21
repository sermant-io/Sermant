package io.sermant.discovery.service.lb.rule;

import io.sermant.discovery.entity.ServiceInstance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.junit.Assert;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
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
            metadata.put("nacos.weight", String.valueOf(i));
            when(instance.getMetadata()).thenReturn(metadata);
            instances.add(instance);
        }
    }

    @Test
    public void testDoChoose() {

        // Test the selection method of load balancing
        ServiceInstance chosenInstance = nacosWeightRandomRule.doChoose("test-service", instances);

        // Here is the basic test, without doing specific probabilistic verification, assuming that every execution is valid.
        assertNotNull(chosenInstance);

    }

    @Test
    public void lbType() {
        Assert.assertEquals(new NacosWeightRandomRule().lbType(), "NacosWeight");
    }

    @Test
    public void testWeightRandom() {
        // 模拟权重分配
        List<NacosWeightRandomRule.InstanceWithWeight> withWeights = instances.stream()
                .map(instance -> {
                    int weight = Integer.parseInt(instance.getMetadata().get("nacos.weight"));
                    return new NacosWeightRandomRule.InstanceWithWeight(instance, weight);
                }).collect(Collectors.toList());

        // 执行加权随机选择
        ServiceInstance chosenInstance = nacosWeightRandomRule.weightRandom(withWeights);

        // 验证选择的实例不为空
        assertNotNull(chosenInstance);
    }
}
