/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.rules.notifier;

import com.huawei.route.server.conditions.ZookeeperConfigCenterCondition;
import lombok.Getter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * zookeeper 路径监听管理
 *
 * @author zhouss
 * @since 2021-10-21
 */
@Component
@Conditional(ZookeeperConfigCenterCondition.class)
public class ZookeeperPathNotifierManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperPathNotifierManager.class);

    /**
     * 监听器缓存
     * key : 监听路径
     * value : 该路径的监听器
     */
    private final Map<String, PathTrigger> listenerCache = new HashMap<>();

    private final CuratorFramework zkClient;

    @Autowired
    public ZookeeperPathNotifierManager(CuratorFramework zkClient) {
        this.zkClient = zkClient;
    }

    /**
     * 注入监听触发器
     *
     * @param path zk路径
     * @param notifier 通知接口
     */
    public void registerTrigger(String path, Notifier notifier) {
        PathTrigger pathTrigger = listenerCache.get(path);
        if (pathTrigger == null) {
            pathTrigger = new PathTrigger(path, notifier);
        } else {
            pathTrigger.addListener(notifier);
        }
        listenerCache.put(path, pathTrigger);
    }

    /**
     * 关闭所有监听器
     */
    @PreDestroy
    public void close() {
        listenerCache.values().forEach(PathTrigger::close);
    }

    /**
     * 路径监听触发器
     */
    public class PathTrigger {
        @Getter
        private final String path;

        private final TreeCache treeCache;

        public PathTrigger(String path, Notifier notifier) {
            this.path = path;
            this.treeCache = new TreeCache(zkClient, path);
            this.treeCache.getListenable().addListener(new NotifierCacheListener(notifier));
            try {
                this.treeCache.start();
            } catch (Exception e) {
                LOGGER.warn("path listener path [{}] start failed!", this.path, e);
            }
        }

        /**
         * 关闭tree cache
         */
        public void close() {
            if (this.treeCache != null) {
                this.treeCache.close();
            }
        }

        /**
         * 添加监听器
         *
         * @param notifier 通知监听器
         */
        public void addListener(Notifier notifier) {
            if (this.treeCache != null) {
                treeCache.getListenable().addListener(new NotifierCacheListener(notifier));
            }
        }
    }

    static class NotifierCacheListener implements TreeCacheListener {
        private final Notifier notifier;

        public NotifierCacheListener(Notifier notifier) {
            this.notifier = notifier;
        }

        /**
         * zk事件
         *
         * @param client zk客户端
         * @param event 事件
         */
        @Override
        public void childEvent(CuratorFramework client, TreeCacheEvent event) {
            final TreeCacheEvent.Type type = event.getType();
            if (type == TreeCacheEvent.Type.NODE_ADDED
                    || type == TreeCacheEvent.Type.NODE_REMOVED
                    || type == TreeCacheEvent.Type.NODE_UPDATED) {
                final byte[] bytes = event.getData().getData();
                try {
                    notifier.notify(new String(bytes, StandardCharsets.UTF_8));
                } catch (Exception e) {
                    LOGGER.warn("notify [{}] config failed! reason: {}", notifier.getClass().getName(), e.getMessage());
                }
            }
        }
    }

}
