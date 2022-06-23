/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on org/apache/dubbo/rpc/cluster/loadbalance/ShortestResponseLoadBalance.java
 * from the Apache Dubbo project.
 */

package com.huawei.registry.grace.interceptors;

import com.huawei.registry.config.ConfigConstants;
import com.huawei.registry.config.GraceConfig;
import com.huawei.registry.config.grace.GraceConstants;
import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.support.RegisterSwitchSupport;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * 优雅上下线开关
 *
 * @author zhouss
 * @since 2022-05-17
 */
public class GraceSwitchInterceptor extends RegisterSwitchSupport {
    protected final GraceConfig graceConfig;

    /**
     * 优雅上下线开关
     *
     * @since 2022-05-17
     */
    public GraceSwitchInterceptor() {
        graceConfig = PluginConfigManager.getPluginConfig(GraceConfig.class);
    }

    @Override
    protected boolean isEnabled() {
        return graceConfig.isEnableSpring();
    }

    /**
     * 构建endpoint
     *
     * @param host 域名
     * @param port 端口
     * @return endpoint
     */
    protected String buildEndpoint(String host, int port) {
        return String.format(Locale.ENGLISH, "%s:%s", host, port);
    }

    /**
     * 预热信息
     *
     * @param ip 实例IP
     * @param port 实例端口
     */
    protected void warmMessage(String ip, int port) {
        LoggerFactory.getLogger().fine(String.format(Locale.ENGLISH, "Instance [%s:%s] is warming up!", ip, port));
    }

    /**
     * 对单个实例计算权重
     *
     * @param metadata 原信息
     * @param weights 权重分配
     * @param index 当前实例索引
     * @return 当前下标实例是否预热完成
     */
    protected boolean calculate(Map<String, String> metadata, int[] weights, int index) {
        final String warmUpWeightStr = metadata.getOrDefault(GraceConstants.WARM_KEY_WEIGHT,
                String.valueOf(GraceConstants.DEFAULT_WARM_UP_WEIGHT));
        final String warmUpTimeStr = metadata
                .getOrDefault(GraceConstants.WARM_KEY_TIME, GraceConstants.DEFAULT_WARM_UP_TIME);
        final String warmUpCurveStr = metadata
                .getOrDefault(GraceConstants.WARM_KEY_CURVE, String.valueOf(GraceConstants.DEFAULT_WARM_UP_CURVE));
        String injectTimeStr = metadata.getOrDefault(GraceConstants.WARM_KEY_INJECT_TIME,
                GraceConstants.DEFAULT_WARM_UP_INJECT_TIME_GAP);
        final long injectTime = Long.parseLong(injectTimeStr);
        final long warmUpTime = Integer.parseInt(warmUpTimeStr) * ConfigConstants.SEC_DELTA;
        final int weight = this.calculateWeight(injectTime, warmUpTime, warmUpWeightStr, warmUpCurveStr);
        weights[index] = weight;
        return isWarmed(injectTime, warmUpTime);
    }

    /**
     * 是否预热完成了
     *
     * @param injectTime 注入时间， 若该值为0, 则当前实例未开启预热功能, 直接返回最大权重
     * @param warmUpTime 预热时间
     * @return 是否预热完成
     */
    private boolean isWarmed(long injectTime, long warmUpTime) {
        return injectTime == 0L || System.currentTimeMillis() - injectTime > warmUpTime;
    }

    /**
     * 计算权重
     *
     * @param injectTime 预热参数注入时间
     * @param warmUpTime 预热时间
     * @param warmUpWeightStr 预热权重
     * @param warmUpCurveStr 预热计算曲线值
     * @return 权重
     */
    protected int calculateWeight(long injectTime, long warmUpTime, String warmUpWeightStr,
            String warmUpCurveStr) {
        final int warmUpWeight = Integer.parseInt(warmUpWeightStr);
        int warmUpCurve = Integer.parseInt(warmUpCurveStr);
        if (warmUpTime <= 0 || injectTime <= 0) {
            // 未开启预热的服务默认100权重
            return warmUpWeight;
        }
        if (warmUpCurve < 0) {
            warmUpCurve = GraceConstants.DEFAULT_WARM_UP_CURVE;
        }
        final long runtime = System.currentTimeMillis() - injectTime;
        if (runtime > 0 && runtime < warmUpTime) {
            // 预热未结束
            return calculateWeight(runtime, warmUpTime, warmUpCurve, warmUpWeight);
        }
        return Math.max(0, warmUpWeight);
    }

    /**
     * 计算权重
     *
     * @param runtime 运行时间（从启动开始）
     * @param warmUpTime 预热时间
     * @param warmUpCurve 预热计算曲线
     * @param warmUpWeight 预热权重
     * @return 权重
     */
    protected int calculateWeight(double runtime, double warmUpTime, int warmUpCurve, int warmUpWeight) {
        final int round = (int) Math.round(Math.pow(runtime / warmUpTime, warmUpCurve) * warmUpWeight);
        return round < 1 ? 1 : Math.min(round, warmUpWeight);
    }

    /**
     * 选择实例
     *
     * @param totalWeight 总权重
     * @param weights 基于所有实例的权重分配
     * @param serverList 服务实例列表
     * @return 确定实例
     */
    protected Optional<Object> chooseServer(int totalWeight, int[] weights, List<?> serverList) {
        if (totalWeight <= 0) {
            return Optional.empty();
        }
        int position = new Random().nextInt(totalWeight);
        for (int i = 0; i < weights.length; i++) {
            position -= weights[i];
            if (position < 0) {
                return Optional.of(serverList.get(i));
            }
        }
        return Optional.empty();
    }

    /**
     * 获取本地ip的请求头
     *
     * @return 请求头
     */
    protected Map<String, List<String>> getGraceIpHeaders() {
        String address = RegisterContext.INSTANCE.getClientInfo().getIp() + ":" + graceConfig.getHttpServerPort();
        Map<String, List<String>> headers = new HashMap<>();
        headers.put(GraceConstants.SERMANT_GRACE_ADDRESS, Collections.singletonList(address));
        headers.put(GraceConstants.GRACE_OFFLINE_SOURCE_KEY,
                Collections.singletonList(GraceConstants.GRACE_OFFLINE_SOURCE_VALUE));
        return headers;
    }
}
