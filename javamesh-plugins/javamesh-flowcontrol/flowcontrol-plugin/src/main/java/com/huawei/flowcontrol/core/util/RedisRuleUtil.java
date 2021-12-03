/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.flowcontrol.core.util;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.redis.RedisDataSource;
import com.alibaba.csp.sentinel.datasource.redis.config.RedisConnectionConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.huawei.flowcontrol.core.config.CommonConst;
import com.huawei.flowcontrol.core.config.ConfigConst;
import org.apache.curator.framework.CuratorFramework;

import java.util.List;

/**
 * 加载redis的sentinel配置规则
 *
 * @author liyi
 * @since 2020-08-26
 */
public class RedisRuleUtil {
    private RedisRuleUtil() {
    }

    /**
     * 如果zookeeper连不上，则从redis中加载流控规则
     */
    public static void loadRules() {
        try {
            // 判断zookeeper异常时才从redis中读取sentinel规则
            if (!isZkConnect()) {
                initRedisRules();
            }
        } catch (Exception e) {
            RecordLog.error("[RedisRuleUtil] loading redisSentinel rule exception=" + e);
        }
    }

    /**
     * 检查连接流控规则的zookeeper连接是否不能连接
     *
     * @return true 如果能连接zookeeper，否则返回false
     */
    private static boolean isZkConnect() {
        CuratorFramework client = ZookeeperConnectionEnum.INSTANCE.getZookeeperConnection();
        if (client == null) {
            return false;
        }
        String rootPath = PluginConfigUtil.getValueByKey(ConfigConst.ZOOKEEPER_PATH);
        String appName = AppNameUtil.getAppName();
        String path = rootPath + CommonConst.SLASH_SIGN
                + appName + CommonConst.SLASH_SIGN
                + CommonConst.SENTINEL_RULE_FLOW;
        try {
            client.checkExists().forPath(path);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 加载redis中的流控配置规则
     */
    private static void initRedisRules() {
        int flowRuleSize = FlowRuleManager.getRules().size();
        int degradeRuleSize = DegradeRuleManager.getRules().size();
        int systemRuleSize = SystemRuleManager.getRules().size();
        int authorityRuleSize = AuthorityRuleManager.getRules().size();
        if (flowRuleSize == 0 || degradeRuleSize == 0
                || systemRuleSize == 0 || authorityRuleSize == 0) {
            RecordLog.info("[RedisRuleUtil] initRedisRules start");
            RedisConnectionConfig config = RedisConnectionConfig.builder()
                    .withHost(PluginConfigUtil.getValueByKey(ConfigConst.REDIS_HOST))
                    .withPort(Integer.parseInt(PluginConfigUtil.getValueByKey(ConfigConst.REDIS_PORT)))
                    .build();

            // 加载流控规则
            if (flowRuleSize == 0) {
                loadFlowRules(config);
            }

            // 加载降级规则
            if (degradeRuleSize == 0) {
                loadDegradeRules(config);
            }

            // 加载系统规则
            if (systemRuleSize == 0) {
                loadSystemRules(config);
            }

            // 加载授权规则
            if (authorityRuleSize == 0) {
                loadAuthorityRules(config);
            }
            RecordLog.info("[RedisRuleUtil] initRedisRules end");
        }
    }

    private static void loadAuthorityRules(RedisConnectionConfig config) {
        String authorityRuleKey = getCommonKey() + CommonConst.SENTINEL_RULE_AUTHORITY;
        RecordLog.info("[RedisRuleUtil] authorityRuleKey=" + authorityRuleKey);
        ReadableDataSource<String, List<AuthorityRule>> redisDataSourceAuthority =
            new RedisDataSource<List<AuthorityRule>>(config, authorityRuleKey, authorityRuleKey + CommonConst.CHANNEL,
                new Converter<String, List<AuthorityRule>>() {
                    @Override
                    public List<AuthorityRule> convert(String source) {
                        return JSON.parseObject(source, new TypeReference<List<AuthorityRule>>() {
                        });
                    }
                });
        AuthorityRuleManager.register2Property(redisDataSourceAuthority.getProperty());
        RecordLog.info("[RedisRuleUtil] AuthorityRule size=" + AuthorityRuleManager.getRules().size());
    }

    private static void loadSystemRules(RedisConnectionConfig config) {
        String systemRuleKey = getCommonKey() + CommonConst.SENTINEL_RULE_SYSTEM;
        RecordLog.info("[RedisRuleUtil] systemRuleKey=" + systemRuleKey);
        ReadableDataSource<String, List<SystemRule>> redisDataSourceSystem =
            new RedisDataSource<List<SystemRule>>(config, systemRuleKey, systemRuleKey + CommonConst.CHANNEL,
                new Converter<String, List<SystemRule>>() {
                    @Override
                    public List<SystemRule> convert(String source) {
                        return JSON.parseObject(source, new TypeReference<List<SystemRule>>() {
                        });
                    }
                });
        SystemRuleManager.register2Property(redisDataSourceSystem.getProperty());
        RecordLog.info("[RedisRuleUtil] SystemRule size=" + SystemRuleManager.getRules().size());
    }

    private static void loadDegradeRules(RedisConnectionConfig config) {
        String degradeKey = getCommonKey() + CommonConst.SENTINEL_RULE_DEGRADE;
        RecordLog.info("[RedisRuleUtil] degradeKey=" + degradeKey);
        ReadableDataSource<String, List<DegradeRule>> redisDataSourceDegrade =
            new RedisDataSource<List<DegradeRule>>(config, degradeKey, degradeKey + CommonConst.CHANNEL,
                new Converter<String, List<DegradeRule>>() {
                    @Override
                    public List<DegradeRule> convert(String source) {
                        return JSON.parseObject(source, new TypeReference<List<DegradeRule>>() {
                        });
                    }
                });
        DegradeRuleManager.register2Property(redisDataSourceDegrade.getProperty());
        RecordLog.info("[RedisRuleUtil] DegradeRule size=" + DegradeRuleManager.getRules().size());
    }

    private static void loadFlowRules(RedisConnectionConfig config) {
        String flowKey = getCommonKey() + CommonConst.SENTINEL_RULE_FLOW;
        RecordLog.info("[RedisRuleUtil] flowRuleKey=" + flowKey);
        ReadableDataSource<String, List<FlowRule>> redisDataSourceFlow =
            new RedisDataSource<List<FlowRule>>(config, flowKey, flowKey + CommonConst.CHANNEL,
                new Converter<String, List<FlowRule>>() {
                    @Override
                    public List<FlowRule> convert(String source) {
                        return JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
                        });
                    }
                });
        FlowRuleManager.register2Property(redisDataSourceFlow.getProperty());
        RecordLog.info("[RedisRuleUtil] FlowRule size=" + FlowRuleManager.getRules().size());
    }

    private static String getCommonKey() {
        String appName = AppNameUtil.getAppName();
        return CommonConst.SENTINEL + CommonConst.COLON_SIGN
            + appName + CommonConst.COLON_SIGN;
    }
}
