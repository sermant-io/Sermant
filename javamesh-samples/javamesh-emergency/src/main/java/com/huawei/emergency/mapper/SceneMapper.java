/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.mapper;

import com.huawei.emergency.dto.AddScriptToSceneParam;
import com.huawei.emergency.dto.SceneListParam;
import com.huawei.emergency.dto.Task;
import com.huawei.emergency.entity.SceneEntity;
import com.huawei.emergency.entity.SceneScriptRelationEntity;

import org.apache.ibatis.annotations.Mapper;

import java.sql.Timestamp;
import java.util.List;

/**
 * 场景管理Mapper
 *
 * @since 2021-10-30
 */
@Mapper
public interface SceneMapper {
    List<SceneEntity> listScene(SceneListParam sceneListParam);

    int createScene(SceneEntity entity);

    int countBySceneName(String sceneName, String sceneUser);

    int addScriptToScene(AddScriptToSceneParam param);

    int getSequenceBySceneId(int sceneId);

    SceneScriptRelationEntity selectRelationById(int id);

    int deleteScriptFromScene(int id);

    int updateSceneTime(int sceneId, Timestamp timestamp);

    int accountExecuteStatus(int sceneId);

    int countRelation(int sceneId);

    SceneEntity selectSceneBySceneId(int sceneId);

    List<Task> selectRunningSceneInfo(int sceneId,int countScript);

    List<Task> selectNotRunningSceneInfo(int sceneId,int countScript);

    void deleteScene(int[] sceneId);

    void deleteRelation(int[] sceneId);
}
