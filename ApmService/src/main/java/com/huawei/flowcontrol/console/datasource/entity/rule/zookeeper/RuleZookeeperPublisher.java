/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.datasource.entity.rule.zookeeper;

import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.fastjson.JSONObject;
import com.huawei.flowcontrol.console.rule.DynamicRulePublisherExt;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * publisher类
 *
 * @author XiaoLong Wang
 * @since 2020-12-21
 */
@Component
public class RuleZookeeperPublisher implements DynamicRulePublisherExt<List<?>> {
    /**
     * 空集合
     */
    private static final String EMPTY_ARRAY = "[]";

    @Autowired
    private CuratorFramework zkClient;

    @Override
    public void publish(String app, List<?> rules) throws Exception {
        AssertUtil.notEmpty(app, "app name cannot be empty");

        String path = ZookeeperConfigUtil.getPath(app);
        setData(path, rules);
    }

    @Override
    public void publish(String app, String entityType, List<?> rules) throws Exception {
        AssertUtil.notEmpty(app, "app name cannot be empty");

        // 获取zk中的路径
        String path = ZookeeperConfigUtil.getPath(app + entityType);
        setData(path, rules);
    }

    /**
     * 设置数据到zookeeper
     *
     * @param path  存放路劲
     * @param rules 规则集合
     * @throws Exception forpath异常
     */
    private void setData(String path, List<?> rules) throws Exception {
        // 判断路径是否存在
        Stat stat = zkClient.checkExists().forPath(path);

        // 不存在，新建
        if (stat == null) {
            zkClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, null);
        }
        byte[] data = CollectionUtils.isEmpty(rules) ? EMPTY_ARRAY.getBytes("utf-8")
            : JSONObject.toJSONString(rules).getBytes("utf-8");

        // publish 数据
        zkClient.setData().forPath(path, data);
    }
}