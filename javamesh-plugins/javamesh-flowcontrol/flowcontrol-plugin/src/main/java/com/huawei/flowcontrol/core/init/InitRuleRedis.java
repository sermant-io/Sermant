/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.core.init;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.huawei.flowcontrol.core.config.CommonConst;
import com.huawei.flowcontrol.core.config.ConfigConst;
import com.huawei.flowcontrol.core.util.PluginConfigUtil;
import com.huawei.flowcontrol.core.util.RedisClient;
import com.huawei.flowcontrol.core.util.ZookeeperConnectionEnum;
import org.apache.curator.framework.CuratorFramework;

/**
 * 从注册中心获取sentinel规则存储到本地redis
 *
 * @author liyi
 * @since 2020-08-26
 */
public class InitRuleRedis {
    private InitRuleRedis() {
    }

    /**
     * 开启线程执行初始化任务
     */
    public static void doInit() {
        new RedisTask().run();
    }

    /**
     * redis任务，用于备份流控规则到redis
     *
     * @author liyi
     * @since 2020-08-26
     */
    private static class RedisTask {
        public void run() {
            CuratorFramework client = ZookeeperConnectionEnum.INSTANCE.getZookeeperConnection();
            if (client == null) {
                return;
            }
            RedisClient redisClient = new RedisClient();
            try {
                String path;
                String commonPath = getCommonPath();
                String commonKey = getCommonKey();
                try {
                    // 加载流控规则R
                    path = commonPath+ CommonConst.SENTINEL_RULE_FLOW;
                    String flowValue = new String(client.getData().forPath(path), CommonConst.PLUGIN_ENCODE);
                    String flowKey = commonKey + CommonConst.SENTINEL_RULE_FLOW;
                    redisClient.set(flowKey, flowValue);
                } catch (Exception e) {
                    RecordLog.error("[InitRuleRedis] failed to load flowRule backup to redis=" + e);
                }
                try {
                    // 加载降级规则
                    path = commonPath+ CommonConst.SENTINEL_RULE_DEGRADE;
                    String degradeValue = new String(client.getData().forPath(path), CommonConst.PLUGIN_ENCODE);
                    String degradeKey = commonKey + CommonConst.SENTINEL_RULE_DEGRADE;
                    redisClient.set(degradeKey, degradeValue);
                } catch (Exception e) {
                    RecordLog.error("[InitRuleRedis] failed to load degradeRule backup to redis=" + e);
                }
                try {
                    // 加载系统规则
                    path = commonPath+ CommonConst.SENTINEL_RULE_SYSTEM;
                    String systemRuleValue = new String(client.getData().forPath(path), CommonConst.PLUGIN_ENCODE);
                    String systemRuleKey = commonKey + CommonConst.SENTINEL_RULE_SYSTEM;
                    redisClient.set(systemRuleKey, systemRuleValue);
                } catch (Exception e) {
                    RecordLog.error("[InitRuleRedis] failed to load systemRule backup to redis=" + e);
                }
                try {
                    // 记载授权规则
                    path = commonPath+ CommonConst.SENTINEL_RULE_AUTHORITY;
                    String authorityRuleValue = new String(client.getData().forPath(path), CommonConst.PLUGIN_ENCODE);
                    String authorityRuleKey = commonKey + CommonConst.SENTINEL_RULE_AUTHORITY;
                    redisClient.set(authorityRuleKey, authorityRuleValue);
                } catch (Exception e) {
                    RecordLog.error("[InitRuleRedis] failed to load authorityRule backup to redis=" + e);
                }
            } finally {
                redisClient.close();
            }
        }

        private String getCommonPath() {
            String rootPath = PluginConfigUtil.getValueByKey(ConfigConst.ZOOKEEPER_PATH);
            String appName = AppNameUtil.getAppName();
            return rootPath + CommonConst.SLASH_SIGN
                + appName + CommonConst.SLASH_SIGN;
        }

        private String getCommonKey() {
            String appName = AppNameUtil.getAppName();
            return CommonConst.SENTINEL + CommonConst.COLON_SIGN
                + appName + CommonConst.COLON_SIGN;
        }
    }
}
