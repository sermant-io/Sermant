package com.huawei.emergency.entity;

import lombok.Data;

import java.util.Date;

@Data
public class EmergencyPlanDetail {
    private Integer detailId;

    private Integer planId;

    private Integer sceneId;

    private Integer taskId;

    private Integer preSceneId;

    private Integer preTaskId;

    private Integer parentTaskId;

    private String createUser;

    private Date createTime;

    private String isValid;

    private String sync;
}