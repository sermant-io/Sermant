/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.service;

import com.huawei.common.api.CommonResult;
import com.huawei.emergency.dto.AddScriptToSceneParam;
import com.huawei.emergency.dto.SceneInfoDto;
import com.huawei.emergency.dto.SceneListParam;
import com.huawei.emergency.entity.SceneEntity;
import com.huawei.script.exec.log.LogRespone;

/**
 * 场景管理接口
 *
 * @since 2021-10-30
 **/
public interface SceneService {
    CommonResult listScene(SceneListParam sceneListParam);

    int createScene(SceneEntity entity);

    CommonResult addScriptToScene(AddScriptToSceneParam param);

    CommonResult updateScriptToScene(AddScriptToSceneParam param);

    int deleteScriptFromScene(int id);

    SceneInfoDto getSceneInfo(int sceneId);

    void runScene(int sceneId,String userName);

    void deleteScene(int[] sceneId);

    LogRespone getLog(int sceneId,int detailId,int line);
}
