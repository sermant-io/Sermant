/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.datasource.zookeeper;

import com.huawei.flowrecordreplay.console.datasource.entity.ReplayJobEntity;
import com.huawei.flowrecordreplay.console.job.JobPublisher;

/**
 * zookeeper回放任务接口定义
 *
 * @author lilai
 * @version 0.0.1
 * @param <T>
 * @since 2021-02-26
 */
public interface ReplayJobPublisherExt<T> extends JobPublisher<T> {
    /**
     * 在ZK中查询回放任务
     *
     * @param jobId 任务ID
     * @return 任务详情
     */
    ReplayJobEntity getReplayJob(String jobId) throws Exception;

    /**
     * 在ZK中删除回放任务
     *
     * @param jobId 任务ID
     */
    void deleteReplayJob(String jobId) throws Exception;

    /**
     * 在ZK中删除回放待执行子任务
     *
     * @param jobId 任务ID
     */
    void deleteSubReplayJob(String jobId) throws Exception;

    /**
     * 往ZK中下发回放任务
     *
     * @param job 任务详情
     */
    @Override
    void publish(T job) throws Exception;
}
