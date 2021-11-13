/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.service;

/**
 * 场景管理接口
 *
 * @author y30010171
 * @since 2021-11-04
 **/
public interface EmergencySceneService extends EmergencyCallBack {
    boolean isSceneFinished(int execId, int sceneId);
}
