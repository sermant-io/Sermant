/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowrecord.init;

import com.huawei.apm.core.service.CoreServiceManager;
import com.huawei.apm.core.service.configServer.zookeeper.ZookeeperServer;
import com.huawei.flowrecord.config.CommonConst;
import com.huawei.flowrecord.config.ConfigConst;
import com.huawei.flowrecord.config.FlowRecordConfig;
import com.huawei.flowrecord.domain.RecordJob;
import com.huawei.flowrecord.utils.PluginConfigUtil;
import com.huawei.flowrecord.utils.AppNameUtil;

import com.alibaba.fastjson.JSON;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.UnhandledErrorListener;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * zookeeper监听器
 */
public class InitListener {
    private static final FlowRecordConfig flowRecordConfig = PluginConfigUtil.getFlowRecordConfig();
    private static final Logger LOGGER = LoggerFactory.getLogger(InitListener.class);

    private static final String PARENTPATH = flowRecordConfig.getZookeeperPath() +
            CommonConst.SLASH_SIGN + AppNameUtil.getAppName() + CommonConst.SLASH_SIGN + ConfigConst.CURRENT_JOB;
    private static final ZookeeperServer zkServer = CoreServiceManager.INSTANCE.getService(flowRecordConfig.getConfigServerClassName());
    private static final CuratorFramework treeCacheClient = zkServer.getClient();
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);

    public static void doinit() throws Exception {
        // 开启zookeeper监听器
        init();
        treeCache();
    }

    public static void init() {
        try {
            if (treeCacheClient.checkExists().forPath(PARENTPATH) == null) {
                treeCacheClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT)
                        .forPath(PARENTPATH, "this is parent data".getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            LOGGER.info("[flowrecord]: cannot create zookeeper node");
        }
        try {
            RecordJob.recordJob = JSON.parseObject(new String(treeCacheClient.getData().forPath(PARENTPATH)), RecordJob.class);
            String path = flowRecordConfig.getZookeeperPath() +
                    CommonConst.SLASH_SIGN + AppNameUtil.getAppName() + CommonConst.SLASH_SIGN +
                    RecordJob.recordJob.getJobId() + CommonConst.SLASH_SIGN +
                    InetAddress.getLocalHost().getHostAddress() + "_status";

            // 判断当前任务状态
            Date date = new Date();
            setEndtimer(path);
            if (treeCacheClient.checkExists().forPath(path) == null) {
                treeCacheClient.create().withMode(CreateMode.PERSISTENT).forPath(path);
                if (RecordJob.recordJob.getEndTime().after(date)) {
                    treeCacheClient.setData().forPath(path, "RUNNING".getBytes(StandardCharsets.UTF_8));
                } else {
                    treeCacheClient.setData().forPath(path, "UNHANDLED".getBytes(StandardCharsets.UTF_8));
                }
            } else {
                if (RecordJob.recordJob.isTrigger()) {
                    if (RecordJob.recordJob.getEndTime().after(date)) {
                        treeCacheClient.setData().forPath(path, "RUNNING".getBytes(StandardCharsets.UTF_8));
                    } else {
                        treeCacheClient.setData().forPath(path, "DONE".getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.info("[flowrecord]: no recordjob in zookeeper");
        }
    }

    public static void treeCache() {
        final TreeCache treeCache = new TreeCache(treeCacheClient, PARENTPATH);
        try {
            treeCache.start();
        } catch (Exception e) {
            LOGGER.info("[flowrecord]: cannot init the treecache");
        }
        treeCache.getUnhandledErrorListenable().addListener(new UnhandledErrorListener() {
            @Override
            public void unhandledError(String s, Throwable throwable) {
            }
        });
        treeCache.getListenable().addListener(new TreeCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
                switch (event.getType()) {
                    case NODE_ADDED:
                        break;
                    case NODE_REMOVED:
                        break;
                    case NODE_UPDATED:
                        LOGGER.info("[flowrecord]: zookeeper node is updated");
                        if (ZKPaths.getNodeFromPath(event.getData().getPath()).endsWith(ConfigConst.CURRENT_JOB)) {
                            try {
                                RecordJob.recordJob = JSON.parseObject(new String(event.getData().getData()), RecordJob.class);
                                String path = flowRecordConfig.getZookeeperPath() +
                                        CommonConst.SLASH_SIGN + AppNameUtil.getAppName() + CommonConst.SLASH_SIGN +
                                        RecordJob.recordJob.getJobId() + CommonConst.SLASH_SIGN +
                                        InetAddress.getLocalHost().getHostAddress() + "_status";
                                if (RecordJob.recordJob.isTrigger()) {
                                    setEndtimer(path);
                                    client.create().withMode(CreateMode.PERSISTENT).forPath(path);
                                    treeCacheClient.setData().forPath(path, "RUNNING".getBytes(StandardCharsets.UTF_8));
                                } else {
                                    treeCacheClient.setData().forPath(path, "STOPPED".getBytes(StandardCharsets.UTF_8));
                                }

                            } catch (Exception e) {
                                LOGGER.info("[flowrecord]: cannot parse recordjob");
                            }

                        }
                    default:
                }
            }
        });
    }

    private static void setEndtimer(String path) throws Exception {
        Date date = new Date();
        scheduledExecutorService.schedule(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            treeCacheClient.setData().forPath(path, "DONE".getBytes(StandardCharsets.UTF_8));
                        } catch (Exception e) {
                            LOGGER.info("[flowrecord]: cannot change the status with zookeeper path: " + path + " with finished");
                        }
                    }
                }
                , RecordJob.recordJob.getEndTime().getTime() - date.getTime(), TimeUnit.MILLISECONDS
        );
    }
}