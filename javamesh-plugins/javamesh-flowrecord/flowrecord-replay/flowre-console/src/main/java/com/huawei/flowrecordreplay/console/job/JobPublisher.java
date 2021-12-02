/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.job;

/**
 *
 * Job configuration center interface
 *
 * @author lilai
 * @param <T>
 * @since 2021-02-26
 */
public interface JobPublisher<T> {
    /**
     * Publish jobs to remote job configuration center
     *
     * @param job job entity to push
     * @throws Exception if some error occurs
     */
    void publish(T job) throws Exception;
}
