/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.entity;

import lombok.Data;

/**
 * 场景与脚本关系的实体类
 *
 * @since 2021-10-30
 */
@Data
public class SceneScriptRelationEntity {
    private int id;

    private int sceneId;

    private String scriptNameAndUser;

    private String scriptName;

    private String scriptUser;

    private String executionMode;

    private String serverUser;

    private String serverIp;

    private String serverPort;

    private int scriptSequence;
}
