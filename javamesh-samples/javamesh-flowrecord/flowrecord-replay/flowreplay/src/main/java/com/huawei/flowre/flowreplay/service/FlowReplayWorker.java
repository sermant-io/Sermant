/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.flowreplay.service;

import com.huawei.flowre.flowreplay.config.Const;
import com.huawei.flowre.flowreplay.config.FlowReplayConfig;
import com.huawei.flowre.flowreplay.datasource.EsDataSource;
import com.huawei.flowre.flowreplay.domain.FlowReplayStatus;
import com.huawei.flowre.flowreplay.domain.SubReplayJobEntity;
import com.huawei.flowre.flowreplay.domain.SubReplayJobInfoEntity;
import com.huawei.flowre.flowreplay.utils.WorkerStatusUtil;
import com.huawei.flowre.flowreplay.utils.ZookeeperUtil;

import com.alibaba.fastjson.JSON;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 回放主流程，接收zookeeper任务调度，执行回放流程
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-03-15
 */
@Component
@Order(3)
public class FlowReplayWorker implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlowReplayWorker.class);

    /**
     * 等待时间
     */
    private static final long WAIT_TIME = 1000L;

    @Autowired
    CuratorFramework zkClient;

    @Autowired
    InvokeDataBuilder invokeDataBuilder;

    @Autowired
    EsDataSource esDataSource;

    @Autowired
    Environment environment;

    /**
     * 监听分布式锁节点是否发生变化
     *
     * @param replayWorkerPath 回放节点路径
     * @param workerName       回放节点名字
     */
    private void startLockListener(String replayWorkerPath, String workerName) {
        // 监听 replay_lock 子节点变化
        PathChildrenCache replayLockCache = new PathChildrenCache(zkClient, Const.REPLAY_LOCK_PATH, true);
        PathChildrenCacheListener replayLockCacheListener = ((client, event) -> {
            if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)
                || event.getType().equals(PathChildrenCacheEvent.Type.CHILD_ADDED)) {
                if (isTurnToMe(workerName)) {
                    doTasksProcess(replayWorkerPath, workerName);
                }
            }
        });
        replayLockCache.getListenable().addListener(replayLockCacheListener);
        try {
            replayLockCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        } catch (Exception exception) {
            LOGGER.error("Replay lock cache start error : {}", exception.getMessage());
        }
    }

    /**
     * 监听tasks节点是否下发新的任务
     *
     * @param replayWorkerPath 回放节点路径
     * @param workerName       回放节点名字
     */
    private void startTasksListener(String replayWorkerPath, String workerName) {
        // 监听 replay_task 子节点变化
        PathChildrenCache replayTasksCache = new PathChildrenCache(zkClient, Const.REPLAY_TASKS_PATH, true);
        PathChildrenCacheListener replayTasksCacheListener = ((client, event) -> {
            if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_ADDED)) {
                activeWorker(replayWorkerPath, workerName);
            }
        });
        replayTasksCache.getListenable().addListener(replayTasksCacheListener);
        try {
            // 同步初始化
            replayTasksCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        } catch (Exception exception) {
            LOGGER.error("Replay tasks cache start error : {}", exception.getMessage());
        }
    }

    /**
     * 任务处理
     *
     * @param replayWorkerPath worker的路径
     */
    private void doTasksProcess(String replayWorkerPath, String workerName) throws InterruptedException, UnknownHostException {
        // 获取锁路径
        String lockPath = getLockPath();
        String firstWorker = JSON.parseObject(ZookeeperUtil.getData(lockPath, zkClient), String.class);
        if (Const.BLANK.equals(lockPath) || !workerName.equals(firstWorker)) {
            // 获取锁失败,重新进入锁队列
            LOGGER.error("Getting lock path error!");
            ZookeeperUtil.setData(replayWorkerPath, FlowReplayStatus.WAIT, zkClient);
            ZookeeperUtil.setNode(Const.REPLAY_LOCK_PREFIX, workerName, zkClient, CreateMode.EPHEMERAL_SEQUENTIAL);
            return;
        }

        // 获取任务
        List<String> tasksList = null;
        try {
            tasksList = zkClient.getChildren().forPath(Const.REPLAY_TASKS_PATH);
        } catch (Exception exception) {
            LOGGER.error("Getting replay tasks list error : {}", exception.getMessage());
        }
        if (tasksList != null && tasksList.size() > 0) {
            String nodName = tasksList.get(0);
            SubReplayJobEntity task = JSON.parseObject(ZookeeperUtil.getData(Const.REPLAY_TASKS_PATH
                + Const.SPLIT
                + nodName, zkClient), SubReplayJobEntity.class);

            // 删除task , 修改worker状态后释放锁
            ZookeeperUtil.deleteNode(Const.REPLAY_TASKS_PATH + Const.SPLIT + nodName, zkClient);
            ZookeeperUtil.setData(replayWorkerPath, FlowReplayStatus.BUSY, zkClient);
            ZookeeperUtil.deleteNode(lockPath, zkClient);

            // 将任务数据存入数据库
            SubReplayJobInfoEntity subReplayJobInfo = new SubReplayJobInfoEntity(task.getJobId(), task.getRecordIndex(),
                workerName, new Date(), null, FlowReplayStatus.RUNNING.toString());
            LOGGER.info("Update job status to RUNNING!");
            esDataSource.updateField(Const.REPLAY_JOB, "jobId",
                task.getJobId(), "status", FlowReplayStatus.RUNNING.toString());

            // 配置测试属性
            FlowReplayConfig.getInstance().setReplayJobId(task.getJobId());
            FlowReplayConfig.getInstance().setTestType(task.getStressTestType());
            FlowReplayConfig.getInstance().setBaseLineThroughPut(task.getBaselineThroughPut());
            FlowReplayConfig.getInstance().setMaxThreadCount(task.getMaxThreadCount());
            FlowReplayConfig.getInstance().setMaxResponseTime(task.getMaxResponseTime());
            FlowReplayConfig.getInstance().setMinSuccessRate(task.getMinSuccessRate());

            // 执行回放任务
            String docId = esDataSource.addData(Const.REPLAY_SUB_JOBS, subReplayJobInfo);
            invokeDataBuilder.replay(task);
            WorkerStatusUtil.getInstance().setReplaying(true);

            // 更新子任务状态到数据库
            subReplayJobInfo.setEndTime(new Date());
            subReplayJobInfo.setStatus(FlowReplayStatus.DONE.toString());
            esDataSource.update(Const.REPLAY_SUB_JOBS, docId, subReplayJobInfo);

            // 删除job下的任务,检查job是否完成
            ZookeeperUtil.deleteNode(Const.REPLAY_JOB_PATH + Const.SPLIT + task.getJobId()
                + Const.SPLIT + task.getRecordIndex(), zkClient);
            if (isJobEmpty(Const.REPLAY_JOB_PATH + Const.SPLIT + task.getJobId())) {
                /**
                 * 检查当前节点的回放状态和结果比对状态
                 */
                waitForDone();
                LOGGER.info("Update job status to DONE!");
                ZookeeperUtil.deleteNode(Const.REPLAY_JOB_PATH + Const.SPLIT + task.getJobId(), zkClient);

                // 更新任务状态
                FlowReplayConfig.getInstance().setReplayJobId(Const.BLANK);
                esDataSource.updateField(Const.REPLAY_JOB, "jobId",
                    task.getJobId(), "status", FlowReplayStatus.DONE.toString());
            }

            // 进入锁队列排队
            ZookeeperUtil.setData(replayWorkerPath, FlowReplayStatus.WAIT, zkClient);
            ZookeeperUtil.setNode(Const.REPLAY_LOCK_PREFIX, workerName, zkClient, CreateMode.EPHEMERAL_SEQUENTIAL);
        } else {
            /**
             * 检查回放的状态和结果比对的状态都是空闲 才能修改为空闲
             */
            waitForDone();

            // 修改worker状态为空闲，并释放锁
            ZookeeperUtil.setData(replayWorkerPath, FlowReplayStatus.IDLE, zkClient);
            ZookeeperUtil.deleteNode(lockPath, zkClient);
        }
    }

    /**
     * 当/tasks列表增加任务时 worker
     *
     * @param replayWorkerPath 回放节点在zk的临时路径
     * @param workerName       回放节点的名字
     */
    private void activeWorker(String replayWorkerPath, String workerName) {
        String workerStatus = ZookeeperUtil.getData(replayWorkerPath, zkClient);
        String status = JSON.parseObject(workerStatus, String.class);
        if (FlowReplayStatus.IDLE.toString().equals(status)) {
            // 修改 replay worker 进入等待状态
            ZookeeperUtil.setData(replayWorkerPath, FlowReplayStatus.WAIT, zkClient);

            // 进入分布式锁队列 等待获取锁
            if (!ZookeeperUtil.setNode(Const.REPLAY_LOCK_PREFIX, workerName, zkClient,
                CreateMode.EPHEMERAL_SEQUENTIAL)) {
                ZookeeperUtil.setData(replayWorkerPath, FlowReplayStatus.IDLE, zkClient);
            }
        }
    }

    /**
     * 等待回放和结果比对结束
     */
    public void waitForDone() {
        while (WorkerStatusUtil.getInstance().isRunning()) {
            try {
                Thread.sleep(WAIT_TIME);
                if (WorkerStatusUtil.getInstance().isReplaying()) {
                    LOGGER.info("Waiting for the replaying to finish!");
                }
                if (WorkerStatusUtil.getInstance().isComparing()) {
                    LOGGER.info("Waiting for the comparing to finish!");
                }
            } catch (InterruptedException interruptedException) {
                LOGGER.error("Sleep to wait replaying and comparing done error,{}",
                    interruptedException.getMessage());
            }
        }
    }

    /**
     * 判断某一个job下是否还有子任务
     *
     * @param jobPath 一个回放任务的路径
     * @return boolean
     */
    private boolean isJobEmpty(String jobPath) {
        try {
            List<String> subJobList = zkClient.getChildren().forPath(jobPath);
            if (subJobList.size() == 0) {
                return true;
            }
        } catch (Exception exception) {
            LOGGER.error("Getting subJob list error : {}", exception.getMessage());
        }
        return false;
    }

    /**
     * 获得锁时，获取锁在zookeeper节点路径，用于释放锁
     *
     * @return String
     */
    private String getLockPath() {
        List<String> lockList = null;
        try {
            lockList = zkClient.getChildren().forPath(Const.REPLAY_LOCK_PATH);
        } catch (Exception exception) {
            LOGGER.error("Getting replay lock list error : {}", exception.getMessage());
        }
        if (lockList != null && lockList.size() > 0) {
            Collections.sort(lockList);
            return Const.REPLAY_LOCK_PATH + Const.SPLIT + lockList.get(0);
        } else {
            LOGGER.error("Getting replay lock path error , replay lock list is empty !");
            return Const.BLANK;
        }
    }

    /**
     * 判断是否能获取分布式锁
     *
     * @param workerName 回放节点的名字
     * @return boolean
     */
    private boolean isTurnToMe(String workerName) {
        try {
            List<String> lockList = zkClient.getChildren().forPath(Const.REPLAY_LOCK_PATH);
            if (lockList.size() > 0) {
                // 小于 int 排序有效
                Collections.sort(lockList);
                String firstWorker = ZookeeperUtil.getData(Const.REPLAY_LOCK_PATH
                    + Const.SPLIT
                    + lockList.get(0), zkClient);
                if (workerName.equals(JSON.parseObject(firstWorker, String.class))) {
                    return true;
                }
            }
        } catch (Exception exception) {
            LOGGER.error("Getting replay lock list error : {}", exception.getMessage());
        }
        return false;
    }

    @Override
    public void run(ApplicationArguments args) {
        new Thread(() -> {
            try {
                InetAddress address = InetAddress.getLocalHost();
                String workerName = Const.WORKER_NAME_PREFIX + address.getHostAddress()
                    + Const.UNDERLINE + environment.getProperty(Const.SERVER_PORT);
                String replayWorkerPath = Const.REPLAY_WORKERS_PATH + Const.SPLIT + workerName;
                FlowReplayConfig.getInstance().setReplayWorkerName(workerName);

                // 启动对分布式锁节点的监听
                startLockListener(replayWorkerPath, workerName);

                // 启动对新增任务的监听
                startTasksListener(replayWorkerPath, workerName);

                // 在zookeeper创建回放节点
                ZookeeperUtil.setNode(replayWorkerPath, FlowReplayStatus.WAIT, zkClient, CreateMode.EPHEMERAL);

                // 等待获取锁
                ZookeeperUtil.setNode(Const.REPLAY_LOCK_PREFIX, workerName, zkClient, CreateMode.EPHEMERAL_SEQUENTIAL);
            } catch (UnknownHostException unknownHostException) {
                LOGGER.error("UnknownHost : {}", unknownHostException.getMessage());
            }
        }).start();
    }
}
