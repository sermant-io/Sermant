package io.sermant.discovery.service.lb.rule;

import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import io.sermant.discovery.entity.ServiceInstance;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
/**
 * nacos权重
 *
 * @author xiaozhao
 * @since 2024-11-16
 */
public class NacosWeightRandomRule extends AbstractLoadbalancer {

    @Override
    protected ServiceInstance doChoose(String serviceName, List<ServiceInstance> instances) {
        List<InstanceWithWeight> withWeights = instances.stream()
                .map(instance -> {
                    int weight = Integer.parseInt(instance.getMetadata().get("nacos.weight"));
                    return new InstanceWithWeight(instance, weight);
                }).collect(Collectors.toList());
        return this.weightRandom(withWeights);
    }

    @Override
    public String lbType() {
        return "NacosWeight";
    }

    public static class InstanceWithWeight {
        private ServiceInstance server;
        private Integer weight;

        public InstanceWithWeight(ServiceInstance instance, int weight) {
            this.server = instance;
            this.weight = weight;
        }


        public ServiceInstance getServer() {
            return server;
        }

        public Integer getWeight() {
            return weight;
        }

    }

    /**
     * 根据权重随机
     * 算法参考 https://blog.csdn.net/u011627980/article/details/79401026
     *
     * @param list 实例列表
     * @return 随机出来的结果
     */
    public ServiceInstance weightRandom(List<InstanceWithWeight> list) {
        List<ServiceInstance> instances = Lists.newArrayList();
        for (InstanceWithWeight instanceWithWeight : list) {
            int weight = instanceWithWeight.getWeight();
            for (int i = 0; i <= weight; i++) {
                instances.add(instanceWithWeight.getServer());
            }
        }
        int i = new Random().nextInt(instances.size());
        return instances.get(i);
    }

}
