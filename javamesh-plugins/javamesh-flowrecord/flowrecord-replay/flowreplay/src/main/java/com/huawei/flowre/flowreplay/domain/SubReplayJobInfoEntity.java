/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.flowreplay.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 回放子任务的处理信息
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-03-24
 */
@Getter
@Setter
public class SubReplayJobInfoEntity {
    /**
     * 子任务所属的总任务id
     */
    private String jobId;

    /**
     * 子任务的ES数据库Index
     */
    private String subJobIndex;

    /**
     * 处理该子任务的回放节点
     */
    private String workerName;

    /**
     * 处理该任务的开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    /**
     * 处理该任务的结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    /**
     * 该任务当前的状态
     */
    private String status;

    public SubReplayJobInfoEntity(String jobId,
                                  String subJobIndex, String workerName,
                                  Date startTime, Date endTime, String status) {
        this.jobId = jobId;
        this.subJobIndex = subJobIndex;
        this.workerName = workerName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getSubJobIndex() {
        return subJobIndex;
    }

    public void setSubJobIndex(String subJobIndex) {
        this.subJobIndex = subJobIndex;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
