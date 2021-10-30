/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.entity;

import lombok.Data;

/**
 * 任务详情实体类
 *
 * @since 2021-10-30
 */
@Data
public class HistoryDetailEntity {
    private int id;

    private int historyId;

    private int relationId;

    private int sceneId;

    private int scriptId;

    private String context;

    private int executionMode;

    private String serverIp;

    private String serverPort;

    private String serverUser;

    private int status;

    private int scriptSequence;

    private String log;
}
