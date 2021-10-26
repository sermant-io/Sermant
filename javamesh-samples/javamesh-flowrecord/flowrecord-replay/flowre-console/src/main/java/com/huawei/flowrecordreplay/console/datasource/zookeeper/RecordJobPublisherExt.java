/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.datasource.zookeeper;

import com.huawei.flowrecordreplay.console.datasource.entity.RecordJobEntity;
import com.huawei.flowrecordreplay.console.job.JobPublisher;

/**
 * zookeeper录制任务接口定义
 *
 * @author lilai
 * @version 0.0.1
 * @param <T>
 * @since 2021-02-26
 */
public interface RecordJobPublisherExt<T> extends JobPublisher<T> {
    /**
     * 在ZK中查询录制任务
     *
     * @param application 应用名
     * @param jobId 任务ID
     * @return 任务详情
     */
    RecordJobEntity getRecordJob(String application, String jobId) throws Exception;

    /**
     * 在ZK中删除录制任务
     *
     * @param application 应用名
     * @param jobId 任务ID
     */
    void deleteRecordJob(String application, String jobId) throws Exception;

    /**
     * 更新当前任务
     *
     * @param recordJob 任务详情
     */
    void updateCurrentRecordJob(RecordJobEntity recordJob) throws Exception;

    /**
     * 往ZK中下发录制任务
     *
     * @param job 任务详情
     */
    @Override
    void publish(T job) throws Exception;
}
