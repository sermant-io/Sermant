/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.dto;

import lombok.Data;

import java.util.List;

/**
 * 用于描述预案修改下的每个任务节点
 *
 * @author y30010171
 * @since 2021-11-09
 **/
@Data
public class TaskNode {
    private Integer key;
    private String title;
    private String taskNo;
    private String taskName;
    private String channelType;
    private Integer scriptId;
    private String scriptName;
    private Integer[] serviceId;
    private String submitInfo;
    private String sync;
    private List<TaskNode> children;

    /**
     *  查询执行记录使用
     */
    private Integer planId;
    private Integer sceneId;
    private Integer taskId;
    private String createUser;
}
