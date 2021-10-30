/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.service.impl;

import com.huawei.common.api.CommonResult;
import com.huawei.common.constant.FailedInfo;
import com.huawei.common.constant.ResultCode;
import com.huawei.common.util.PageUtil;
import com.huawei.emergency.dto.AddScriptToSceneParam;
import com.huawei.emergency.dto.DownloadLogDto;
import com.huawei.emergency.dto.SceneInfoDto;
import com.huawei.emergency.dto.SceneListParam;
import com.huawei.emergency.dto.Task;
import com.huawei.emergency.entity.HistoryDetailEntity;
import com.huawei.emergency.entity.HistoryEntity;
import com.huawei.emergency.entity.SceneEntity;
import com.huawei.emergency.entity.SceneScriptRelationEntity;
import com.huawei.emergency.mapper.HistoryMapper;
import com.huawei.emergency.mapper.SceneMapper;
import com.huawei.emergency.mapper.ScriptMapper;
import com.huawei.emergency.service.SceneService;
import com.huawei.script.exec.executor.DefaultScriptCallBack;
import com.huawei.script.exec.executor.ScriptExecTask;
import com.huawei.script.exec.executor.ScriptExecutor;
import com.huawei.script.exec.log.LogMemoryStore;
import com.huawei.script.exec.log.LogRespone;

import com.github.pagehelper.PageHelper;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Resource;

/**
 * 场景管理接口实现类
 *
 * @since 2021-10-30
 **/
@Service
@Transactional
public class SceneServiceImpl implements SceneService {
    private static final int LOCAL = 0;
    private static final int REMOTE = 1;

    @Autowired
    private SceneMapper mapper;

    @Autowired
    private ScriptMapper scriptMapper;

    @Autowired
    private HistoryMapper historyMapper;

    @Resource(name = "scriptExecThreadPool")
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private Map<String, ScriptExecutor> scriptExecutorMap;

    @Autowired
    private DefaultScriptCallBack defaultScriptCallBack;

    @Override
    public CommonResult listScene(SceneListParam sceneListParam) {
        if (sceneListParam.getOrder().equals("ascend")) {
            sceneListParam.setOrder("ASC");
        } else {
            sceneListParam.setOrder("DESC");
        }
        PageHelper.orderBy(sceneListParam.getSorter() + System.lineSeparator() + sceneListParam.getOrder());
        List<SceneEntity> sceneLists = mapper.listScene(sceneListParam);
        return CommonResult.success(
            PageUtil.startPage(sceneLists, sceneListParam.getCurrent(), sceneListParam.getPageSize()),
            sceneLists.size());
    }

    @Override
    public int createScene(SceneEntity entity) {
        int count = mapper.countBySceneName(entity.getSceneName(), entity.getSceneUser());
        if (count > 0) {
            return ResultCode.FAIL;
        }
        Timestamp timestamp = getTimestamp();
        entity.setCreateTime(timestamp);
        entity.setUpdateTime(timestamp);
        return mapper.createScene(entity);
    }

    @Override
    public CommonResult addScriptToScene(AddScriptToSceneParam param) {
        int mode = getMode(param.getExecutionMode(), param.getServerUser(), param.getServerIp(), param.getServerPort());
        if (mode == ResultCode.SERVER_INFO_NULL) {
            return CommonResult.failed(FailedInfo.SERVER_INFO_NULL);
        }
        param.setExecutionModeInt(mode);
        String[] scriptNameAndUser = param.getScriptNameAndUser().split("\\(");
        param.setScriptName(scriptNameAndUser[0]);
        param.setScriptUser(scriptNameAndUser[1].substring(0, scriptNameAndUser[1].length() - 1));
        int sceneId = param.getSceneId();
        int scriptSequence;
        if (mapper.countRelation(sceneId) == 0) {
            scriptSequence = 1;
        } else {
            scriptSequence = mapper.getSequenceBySceneId(sceneId) + 1;
        }
        param.setScriptSequence(scriptSequence);
        mapper.addScriptToScene(param);
        mapper.updateSceneTime(sceneId, getTimestamp());
        return CommonResult.success();
    }

    @Override
    public CommonResult updateScriptToScene(AddScriptToSceneParam param) {
        int mode = getMode(param.getExecutionMode(), param.getServerUser(), param.getServerIp(), param.getServerPort());
        if (mode == ResultCode.SERVER_INFO_NULL) {
            return CommonResult.failed(FailedInfo.SERVER_INFO_NULL);
        }
        param.setExecutionModeInt(mode);
        int oldId = param.getId();
        SceneScriptRelationEntity entity = mapper.selectRelationById(oldId);
        param.setScriptSequence(entity.getScriptSequence());
        String[] scriptNameAndUser = param.getScriptNameAndUser().split("\\(");
        param.setScriptName(scriptNameAndUser[0]);
        param.setScriptUser(scriptNameAndUser[1].substring(0, scriptNameAndUser[1].length() - 1));
        if (deleteScriptFromScene(oldId) <= 0) {
            return CommonResult.failed(FailedInfo.UPDATE_SCENE_FAIL);
        }
        mapper.addScriptToScene(param);
        mapper.updateSceneTime(param.getSceneId(), getTimestamp());
        return CommonResult.success();
    }

    @Override
    public int deleteScriptFromScene(int id) {
        SceneScriptRelationEntity entity = mapper.selectRelationById(id);
        int sceneId = entity.getSceneId();
        int count = mapper.deleteScriptFromScene(id);
        mapper.updateSceneTime(sceneId, getTimestamp());
        return count;
    }

    @Override
    public SceneInfoDto getSceneInfo(int sceneId) {
        // 根据场景ID查询场景名和描述
        SceneEntity sceneEntity = mapper.selectSceneBySceneId(sceneId);
        SceneInfoDto sceneInfoDto = new SceneInfoDto();
        sceneInfoDto.setSceneName(sceneEntity.getSceneName());
        sceneInfoDto.setSceneDescription(sceneEntity.getSceneDescription());

        // 根据场景ID查场景状态是否为执行状态
        int count = mapper.accountExecuteStatus(sceneId);
        int countScript = scriptMapper.countScript();
        List<Task> tasks;
        if (count > 0) {
            // 场景在执行
            sceneInfoDto.setStatus("running");
            tasks = mapper.selectRunningSceneInfo(sceneId, countScript);
        } else {
            // 场景没有在执行
            sceneInfoDto.setStatus("not_running");
            tasks = mapper.selectNotRunningSceneInfo(sceneId, countScript);
        }
        for (Task task : tasks) {
            task.setScriptNameAndUser(task.getScriptName() + "(" + task.getScriptUser() + ")");
        }
        sceneInfoDto.setTasks(tasks);
        return sceneInfoDto;
    }

    @Override
    public void runScene(int sceneId, String userName) {
        HistoryEntity historyEntity = new HistoryEntity(sceneId, userName, getTimestamp());
        int count = historyMapper.insertHistory(historyEntity);
        if (count != 1) {
            return;
        }
        int countScript = scriptMapper.countScript();
        List<HistoryDetailEntity> historyDetails = historyMapper.getDetailsFromRelation(sceneId, countScript);
        int historyId = historyEntity.getHistoryId();
        for (HistoryDetailEntity historyDetail : historyDetails) {
            historyDetail.setHistoryId(historyId);
            historyDetail.setStatus(0);
        }
        historyMapper.insertDetail(historyDetails);
        exec(historyDetails);
    }

    @Override
    public void deleteScene(int[] sceneId) {
        mapper.deleteScene(sceneId);
        mapper.deleteRelation(sceneId);
    }

    @Override
    public LogRespone getLog(int sceneId, int detailId, int line) {
        DownloadLogDto downloadLogDto = historyMapper.selectDetailById(detailId);
        if (downloadLogDto == null || StringUtils.isEmpty(downloadLogDto.getLog())) {
            return LogMemoryStore.getLog(detailId, line);
        }
        String[] split = downloadLogDto.getLog().split(System.lineSeparator());
        if (split.length >= line) {
            String[] needLogs = Arrays.copyOfRange(split, line - 1, split.length);
            return new LogRespone(null, needLogs);
        }
        return new LogRespone(null, new String[]{downloadLogDto.getLog()});
    }

    private void exec(List<HistoryDetailEntity> entity) {
        if (entity == null || entity.size() == 0) {
            return;
        }
        threadPoolExecutor.execute(new ScriptExecTask(entity, scriptExecutorMap, defaultScriptCallBack));
    }

    private Timestamp getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowDate = sdf.format(new Date());
        return Timestamp.valueOf(nowDate);
    }

    private int getMode(String executionMode, String serverUser, String serverIp, String serverPort) {
        if ("remote".equals(executionMode)) {
            if (StringUtils.isBlank(serverUser) || StringUtils.isBlank(serverIp) || StringUtils.isBlank(
                serverPort)) {
                return ResultCode.SERVER_INFO_NULL;
            }
            return REMOTE;
        }
        return LOCAL;
    }
}
