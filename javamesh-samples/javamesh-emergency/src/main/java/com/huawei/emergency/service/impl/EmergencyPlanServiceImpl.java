/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.service.impl;

import com.huawei.common.api.CommonPage;
import com.huawei.common.api.CommonResult;
import com.huawei.common.constant.PlanStatus;
import com.huawei.emergency.dto.PlanQueryDto;
import com.huawei.emergency.dto.PlanQueryParams;
import com.huawei.emergency.dto.SceneExecDto;
import com.huawei.emergency.dto.TaskNode;
import com.huawei.emergency.entity.EmergencyExec;
import com.huawei.emergency.entity.EmergencyExecRecord;
import com.huawei.emergency.entity.EmergencyExecRecordExample;
import com.huawei.emergency.entity.EmergencyExecRecordWithBLOBs;
import com.huawei.emergency.entity.EmergencyPlan;
import com.huawei.emergency.entity.EmergencyPlanDetail;
import com.huawei.emergency.entity.EmergencyPlanDetailExample;
import com.huawei.emergency.entity.EmergencyPlanExample;
import com.huawei.emergency.entity.EmergencyTask;
import com.huawei.emergency.mapper.EmergencyExecMapper;
import com.huawei.emergency.mapper.EmergencyExecRecordMapper;
import com.huawei.emergency.mapper.EmergencyPlanDetailMapper;
import com.huawei.emergency.mapper.EmergencyPlanMapper;
import com.huawei.emergency.service.EmergencyPlanService;
import com.huawei.emergency.service.EmergencyTaskService;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Resource;

/**
 * 预案管理接口的实现类
 *
 * @author y30010171
 * @since 2021-11-02
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class EmergencyPlanServiceImpl implements EmergencyPlanService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmergencyPlanServiceImpl.class);

    @Resource(name = "scriptExecThreadPool")
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private EmergencyPlanMapper planMapper;

    @Autowired
    private EmergencyPlanDetailMapper detailMapper;

    @Autowired
    private EmergencyExecMapper execMapper;

    @Autowired
    private EmergencyExecRecordMapper execRecordMapper;

    @Autowired
    private ExecRecordHandlerFactory handlerFactory;

    @Autowired
    private EmergencyTaskService taskService;

    @Resource(name = "passwordRestTemplate")
    private RestTemplate restTemplate;

    @Override
    public CommonResult add(EmergencyPlan emergencyPlan) {
        if (StringUtils.isEmpty(emergencyPlan.getPlanName())) {
            return CommonResult.failed("请填写预案编号和预案名称");
        }
        EmergencyPlan insertPlan = new EmergencyPlan();
        insertPlan.setPlanName(emergencyPlan.getPlanName());
        insertPlan.setCreateUser(emergencyPlan.getCreateUser());
        insertPlan.setUpdateTime(new Date());
        insertPlan.setStatus(PlanStatus.NEW.getValue());
        planMapper.insertSelective(insertPlan);

        EmergencyPlan updatePlanNO = new EmergencyPlan();
        updatePlanNO.setPlanId(insertPlan.getPlanId());
        updatePlanNO.setPlanNo(String.format("%s%s%04d", "P", LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE), insertPlan.getPlanId()));
        planMapper.updateByPrimaryKeySelective(updatePlanNO);
        insertPlan.setPlanNo(updatePlanNO.getPlanNo());
        return CommonResult.success(insertPlan);
    }

    @Override
    public CommonResult delete(EmergencyPlan emergencyPlan) {
        if (emergencyPlan.getPlanId() == null) {
            return CommonResult.failed("请选择正确的预案");
        }

        // 是否正在执行
        if (haveRunning(emergencyPlan.getPlanId())) {
            return CommonResult.failed("当前预案正在执行中，无法删除。");
        }

        EmergencyPlan updatePlan = new EmergencyPlan();
        updatePlan.setIsValid("0");
        updatePlan.setPlanId(emergencyPlan.getPlanId());
        if (planMapper.updateByPrimaryKeySelective(updatePlan) == 0) {
            return CommonResult.failed("请选择正确的预案");
        }

        EmergencyPlanDetail updatePlanDetail = new EmergencyPlanDetail();
        updatePlanDetail.setIsValid("0");
        EmergencyPlanDetailExample updatePlanDetailCondition = new EmergencyPlanDetailExample();
        updatePlanDetailCondition.createCriteria().andPlanIdEqualTo(emergencyPlan.getPlanId());
        detailMapper.updateByExampleSelective(updatePlanDetail, updatePlanDetailCondition);
        return CommonResult.success();
    }

    @Override
    public CommonResult update(EmergencyPlan emergencyPlan) {
        if (emergencyPlan.getPlanId() == 0) {
            return CommonResult.failed("请选择正确的预案");
        }

        // 是否审核通过
        if (havePass(emergencyPlan.getPlanId())) {
            return CommonResult.failed("当前预案已经审核通过，无法修改。");
        }

        EmergencyPlan updatePlan = new EmergencyPlan();
        updatePlan.setPlanId(emergencyPlan.getPlanId());
        updatePlan.setPlanNo(emergencyPlan.getPlanNo());
        updatePlan.setPlanName(emergencyPlan.getPlanName());
        if (planMapper.updateByPrimaryKeySelective(updatePlan) == 0) {
            return CommonResult.failed("请选择正确的预案");
        }
        return CommonResult.success();
    }

    @Override
    public CommonResult exec(int planId, String userName) {
        if (!havePass(planId)) {
            return CommonResult.failed("当前预案未审核，无法执行。");
        }

        // 是否正在执行
        if (haveRunning(planId)) {
            return CommonResult.failed("当前预案正在执行中，请先完成之前的执行。");
        }
        EmergencyPlan updatePlan = new EmergencyPlan();
        updatePlan.setPlanId(planId);
        updatePlan.setStatus(PlanStatus.RUNNING.getValue());
        planMapper.updateByPrimaryKeySelective(updatePlan);

        // 添加预案执行记录
        EmergencyExec emergencyExec = new EmergencyExec();
        emergencyExec.setPlanId(planId);
        emergencyExec.setCreateUser(userName);
        execMapper.insertSelective(emergencyExec);
        emergencyExec.setHistoryId(emergencyExec.getExecId());

        // 获取所有的拓扑关系，添加详细的执行记录
        List<EmergencyExecRecordWithBLOBs> allExecRecords = execRecordMapper.selectAllPlanDetail(planId);
        allExecRecords.forEach(record -> {
            record.setCreateUser(userName);
            record.setExecId(emergencyExec.getExecId());
            execRecordMapper.insertSelective(record);
        });

        // 开始执行不需要任何前置条件的场景
        allExecRecords.stream()
            .filter(record -> record.getTaskId() == null && record.getPreSceneId() == null)
            .forEach(record -> {
                LOGGER.debug("Submit record_id={}. exec_id={}, task_id={}.", record.getRecordId(), record.getExecId(), record.getTaskId());
                threadPoolExecutor.execute(handlerFactory.handle(record));
            });
        LOGGER.debug("threadPoolExecutor = {} ", threadPoolExecutor);
        return CommonResult.success(emergencyExec);
    }

    @Override
    public CommonResult start(int planId, String userName) {
        return CommonResult.success();
    }

    @Override
    public CommonResult stop(int planId, String userName) {
        return CommonResult.success();
    }

    @Override
    public void onComplete(EmergencyExecRecord record) {
        LOGGER.debug("Plan exec_id={},plan_id={} is finished.", record.getExecId(), record.getPlanId());
        EmergencyPlan updatePlan = new EmergencyPlan();
        updatePlan.setPlanId(record.getPlanId());
        updatePlan.setStatus(PlanStatus.SUCCESS.getValue());
        planMapper.updateByPrimaryKeySelective(updatePlan);
    }

    @Override
    public CommonResult approve(EmergencyPlan plan, String userName) {
        if (plan.getPlanId() == null) {
            return CommonResult.failed("请选择正确的预案");
        }

        // 是否正在执行
        if (haveRunning(plan.getPlanId())) {
            return CommonResult.failed("当前预案正在执行中，无法修改审核结果。");
        }
        if (StringUtils.isEmpty(plan.getStatus())) {
            return CommonResult.failed("审核结果不能为空");
        }
        if (!PlanStatus.APPROVED.getValue().equals(plan.getStatus()) && !PlanStatus.REJECT.getValue().equals(plan.getStatus())) {
            return CommonResult.failed("审核结果不正确");
        }

        EmergencyPlan updatePlan = new EmergencyPlan();
        updatePlan.setPlanId(plan.getPlanId());
        updatePlan.setStatus(plan.getStatus());
        updatePlan.setCheckRemark(plan.getCheckRemark());
        updatePlan.setCheckTime(new Date());
        updatePlan.setCheckUser(userName);
        planMapper.updateByPrimaryKeySelective(updatePlan);
        return CommonResult.success();
    }

    @Override
    public CommonResult query(int planId) {

        List<TaskNode> taskNodes = detailMapper.selectSceneNodeByPlanId(planId);
        taskNodes.forEach(scene -> {
            List<TaskNode> children = detailMapper.selectTaskNodeBySceneId(scene.getPlanId(), scene.getSceneId());
            if (children.size() > 0) {
                scene.setChildren(children);
            }

            //查找场景下的子任务
            children.forEach(task -> {
                // 查找子任务下的子任务
                List<TaskNode> taskChildren = getChildren(task.getPlanId(), task.getSceneId(), task.getTaskId());
                if (taskChildren.size() > 0) {
                    task.setChildren(taskChildren);
                }
            });
        });
        return CommonResult.success(taskNodes);
    }

    @Override
    public CommonResult<EmergencyPlan> get(int planId) {
        EmergencyPlanExample queryExample = new EmergencyPlanExample();
        queryExample.createCriteria()
            .andPlanIdEqualTo(planId)
            .andIsValidEqualTo("1");
        List<EmergencyPlan> emergencyPlans = planMapper.selectByExample(queryExample);
        return CommonResult.success(emergencyPlans.size() > 0 ? emergencyPlans.get(0) : null);
    }

    @Override
    public CommonResult addTask(TaskNode taskNode) {
        EmergencyTask task = new EmergencyTask();
        task.setTaskName(taskNode.getTaskName());
        task.setScriptId(taskNode.getScriptId());
        task.setScriptName(taskNode.getScriptName());
        task.setChannelType(taskNode.getChannelType());
        task.setCreateUser(taskNode.getCreateUser());

        final CommonResult<EmergencyTask> addResult = taskService.add(task);
        if (addResult.getData() == null) {
            return addResult;
        } else {
            EmergencyTask data = addResult.getData();
            taskNode.setKey(data.getTaskId());
            taskNode.setSubmitInfo(data.getSubmitInfo());
            return CommonResult.success(taskNode);
        }
    }

    @Override
    public CommonResult plan(CommonPage<PlanQueryParams> params) {
        Page<PlanQueryDto> pageInfo = PageHelper
            .startPage(params.getPageIndex(), params.getPageSize(), params.getSortField() + System.lineSeparator() + params.getSortType())
            .doSelectPage(() -> {
                planMapper.queryPlanDto(params.getObject());
            });
        List<PlanQueryDto> result = pageInfo.getResult();
        // 查询明细
        result.forEach(planQueryDto -> {
            planQueryDto.setExpand(planMapper.queryPlanDetailDto(planQueryDto.getPlanId()));
        });
        return CommonResult.success(result, (int) pageInfo.getTotal());
    }

    @Override
    public CommonResult allPlanExecRecords(CommonPage<EmergencyPlan> params) {
        Page<PlanQueryDto> pageInfo = PageHelper
            .startPage(params.getPageIndex(), params.getPageSize(), params.getSortField() + System.lineSeparator() + params.getSortType())
            .doSelectPage(() -> {
                planMapper.allPlanRecords(params.getObject());
            });
        return CommonResult.success(pageInfo.getResult(), (int) pageInfo.getTotal());
    }

    @Override
    public CommonResult allSceneExecRecords(CommonPage<EmergencyExecRecord> params) {
        Page<SceneExecDto> pageInfo = PageHelper.startPage(params.getPageIndex(), params.getPageSize())
            .doSelectPage(() -> {
                planMapper.allSceneRecords(params.getObject().getExecId());
            });
        return CommonResult.success(pageInfo.getResult(), (int) pageInfo.getTotal());
    }

    @Override
    public CommonResult allTaskExecRecords(CommonPage<EmergencyExecRecord> params) {
        EmergencyExecRecord paramsObject = params.getObject();
        Page<SceneExecDto> pageInfo = PageHelper.startPage(params.getPageIndex(), params.getPageSize())
            .doSelectPage(() -> {
                planMapper.allTaskRecords(paramsObject.getExecId(), paramsObject.getSceneId());
            });
        return CommonResult.success(pageInfo.getResult(), (int) pageInfo.getTotal());
    }

    @Override
    public CommonResult save(int planId, List<TaskNode> listNodes, String userName) {
        if (listNodes == null) {
            return CommonResult.success();
        }
        if (haveRunning(planId)) {
            return CommonResult.failed("正在运行无法修改");
        }

        EmergencyPlan updatePlan = new EmergencyPlan();
        updatePlan.setPlanId(planId);
        updatePlan.setStatus(PlanStatus.NEW.getValue());
        updatePlan.setUpdateTime(new Date());
        planMapper.updateByPrimaryKeySelective(updatePlan);

        EmergencyPlanDetail oldDetails = new EmergencyPlanDetail();
        oldDetails.setIsValid("0");
        EmergencyPlanDetailExample updateCondition = new EmergencyPlanDetailExample();
        updateCondition.createCriteria().andPlanIdEqualTo(planId);
        detailMapper.updateByExampleSelective(oldDetails, updateCondition);

        Integer preSceneId = null;
        for (TaskNode scene : listNodes) {
            if (!taskService.isTaskExist(scene.getKey())) {
                continue;
            }

            // 增加关系
            EmergencyPlanDetail insertDetail = new EmergencyPlanDetail();
            insertDetail.setPlanId(planId);
            insertDetail.setSceneId(scene.getKey());
            insertDetail.setCreateUser(userName);
            if ("同步".equals(scene.getSync())) {
                insertDetail.setPreSceneId(preSceneId);
                preSceneId = scene.getKey();
            } else {
                insertDetail.setSync("异步");
            }
            detailMapper.insertSelective(insertDetail);
            handleChildren(insertDetail, scene.getChildren());
        }
        return CommonResult.success();
    }

    @Override
    public CommonResult submit(int planId) {
        EmergencyPlan plan = planMapper.selectByPrimaryKey(planId);
        if (plan == null || !"1".equals(plan.getIsValid())) {
            return CommonResult.failed("预案不存在");
        }
        if (PlanStatus.APPROVING.getValue().equals(plan.getStatus()) || PlanStatus.APPROVED.getValue().equals(plan.getStatus())) {
            return CommonResult.failed("预案已审核通过或正在审核中");
        }

        EmergencyPlan updatePlan = new EmergencyPlan();
        updatePlan.setPlanId(planId);
        updatePlan.setStatus(PlanStatus.APPROVING.getValue());
        planMapper.updateByPrimaryKeySelective(updatePlan);
        return CommonResult.success();
    }

    /**
     * 生成子任务
     *
     * @param planDetail
     * @param childrenNode
     * @return
     */
    private void handleChildren(EmergencyPlanDetail planDetail, List<TaskNode> childrenNode) {
        Integer preTaskId = null;
        if (childrenNode == null) {
            return;
        }
        for (TaskNode task : childrenNode) {
            // 任务是否存在
            if (!taskService.isTaskExist(task.getKey())) {
                continue;
            }

            EmergencyPlanDetail insertTaskDetail = new EmergencyPlanDetail();
            insertTaskDetail.setPlanId(planDetail.getPlanId());
            insertTaskDetail.setSceneId(planDetail.getSceneId());
            insertTaskDetail.setTaskId(task.getKey());
            insertTaskDetail.setPreSceneId(planDetail.getPreSceneId());
            insertTaskDetail.setParentTaskId(planDetail.getTaskId() == null ? planDetail.getSceneId() : planDetail.getTaskId());
            if ("异步".equals(task.getSync())) {
                insertTaskDetail.setSync("异步");
            } else {
                insertTaskDetail.setPreTaskId(preTaskId);
                preTaskId = task.getKey();
            }
            insertTaskDetail.setCreateUser(planDetail.getCreateUser());
            detailMapper.insertSelective(insertTaskDetail);
            handleChildren(insertTaskDetail, task.getChildren());
        }
    }


    /**
     * 迭代查找此任务的子任务
     *
     * @param taskId 任务ID
     * @return
     */
    public List<TaskNode> getChildren(int planId, int sceneId, int taskId) {
        List<TaskNode> result = detailMapper.selectTaskNodeByTaskId(planId, sceneId, taskId);
        result.forEach(detail -> {
            List<TaskNode> children = getChildren(detail.getPlanId(), detail.getSceneId(), detail.getTaskId());
            if (children.size() > 0) {
                detail.setChildren(children);
            }
        });
        return result;
    }

    /**
     * 预案是否正在运行
     *
     * @param planId 预案ID
     * @return
     */
    private boolean haveRunning(int planId) {
        EmergencyExecRecordExample isRunningCondition = new EmergencyExecRecordExample();
        isRunningCondition.createCriteria()
            .andStatusLessThan("2")
            .andPlanIdEqualTo(planId);
        return execRecordMapper.countByExample(isRunningCondition) > 0;
    }

    /**
     * 预案是否通过审核
     *
     * @param planId 预案ID
     * @return
     */
    private boolean havePass(int planId) {
        EmergencyPlanExample havePassCondition = new EmergencyPlanExample();
        havePassCondition.createCriteria()
            .andStatusNotIn(PlanStatus.UN_PASSED_STATUS)
            .andIsValidEqualTo("1")
            .andPlanIdEqualTo(planId);
        return planMapper.countByExample(havePassCondition) > 0;
    }
}
