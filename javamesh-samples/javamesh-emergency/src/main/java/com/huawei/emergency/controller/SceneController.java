/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.controller;

import com.huawei.common.api.CommonResult;
import com.huawei.common.constant.FailedInfo;
import com.huawei.common.constant.ResultCode;
import com.huawei.emergency.dto.AddScriptToSceneParam;
import com.huawei.emergency.dto.SceneInfoDto;
import com.huawei.emergency.dto.SceneListParam;
import com.huawei.emergency.entity.SceneEntity;
import com.huawei.emergency.service.SceneService;
import com.huawei.script.exec.log.LogRespone;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 场景管理controller
 *
 * @since 2021-10-30
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class SceneController {
    @Value("${user_name}")
    private String userName;

    @Autowired
    private SceneService sceneService;

    @GetMapping("/scenario")
    public CommonResult listScene(@RequestParam(value = "keywords", required = false) String keywords,
                                  @RequestParam(value = "scene_user[]", required = false) String[] sceneUser,
                                  @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                  @RequestParam(value = "current", defaultValue = "1") int current,
                                  @RequestParam(value = "sorter", defaultValue = "update_time") String sorter,
                                  @RequestParam(value = "order", defaultValue = "DESC") String order) {
        SceneListParam sceneListParam = new SceneListParam(keywords, sceneUser, userName, pageSize, current, sorter,
            order);
        return sceneService.listScene(sceneListParam);
    }

    @PostMapping("/scenario")
    public CommonResult createScene(@RequestBody @Validated SceneEntity entity) {
        entity.setSceneUser(userName);
        int resultCode = sceneService.createScene(entity);
        if (resultCode == 0) {
            return CommonResult.failed("创建失败");
        } else if (resultCode == ResultCode.FAIL) {
            return CommonResult.failed("场景名已存在");
        } else {
            return CommonResult.success(resultCode);
        }
    }

    @DeleteMapping("/scenario")
    public CommonResult deleteScene(@RequestParam(value = "scene_id[]") int[] sceneId) {
        sceneService.deleteScene(sceneId);
        return CommonResult.success();
    }

    @PostMapping("/scenario/script")
    public CommonResult addScriptToScene(@RequestBody @Validated AddScriptToSceneParam param) {
        return sceneService.addScriptToScene(param);
    }

    @PutMapping("/scenario/script")
    public CommonResult updateScriptToScene(@RequestBody @Validated AddScriptToSceneParam param) {
        return sceneService.updateScriptToScene(param);
    }

    @DeleteMapping("/scenario/script")
    public CommonResult deleteScriptFromScene(@RequestParam(value = "task_id") int id) {
        int count = sceneService.deleteScriptFromScene(id);
        if (count <= 0) {
            return CommonResult.failed(FailedInfo.DELETE_SCRIPT_FROM_SCENE_FAIL);
        }
        return CommonResult.success(count);
    }

    @GetMapping("/scenario/get")
    public CommonResult getSceneInfo(@RequestParam(value = "scene_id") int sceneId) {
        SceneInfoDto sceneInfo = sceneService.getSceneInfo(sceneId);
        return CommonResult.success(sceneInfo);
    }

    @PostMapping("/scenario/run")
    public void runScene(@RequestBody Map<String, Integer> param) {
        sceneService.runScene(param.get("scene_id"), userName);
    }

    @GetMapping("/scenario/script/log")
    public LogRespone getLog(@RequestParam("scene_id") int sceneId,
                             @RequestParam("detail_id") int detailId,
                             @RequestParam(value = "line", defaultValue = "1") int lineNum) {
        int lineIndex = lineNum;
        if (lineIndex <= 0) {
            lineIndex = 1;
        }
        return sceneService.getLog(sceneId, detailId, lineIndex);
    }
}
