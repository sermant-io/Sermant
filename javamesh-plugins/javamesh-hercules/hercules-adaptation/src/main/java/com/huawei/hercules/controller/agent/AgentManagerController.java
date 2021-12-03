/*
 * Copyright (C) Huawei Technologies Co., Ltd. $YEAR$-$YEAR$. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hercules.controller.agent;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huawei.hercules.controller.agent.constant.AgentManagerType;
import com.huawei.hercules.controller.agent.constant.AgentPerformanceColumn;
import com.huawei.hercules.controller.agent.constant.DatabaseColumn;
import com.huawei.hercules.controller.agent.constant.ResponseColumn;
import com.huawei.hercules.exception.HerculesException;
import com.huawei.hercules.service.agent.IAgentManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.huawei.hercules.controller.agent.constant.AgentPerformanceColumn.CPU_USED_PERCENTAGE;
import static com.huawei.hercules.controller.agent.constant.AgentPerformanceColumn.FREE_MEMORY;
import static com.huawei.hercules.controller.agent.constant.AgentPerformanceColumn.TOTAL_MEMORY;
import static com.huawei.hercules.controller.agent.constant.PerformanceResponseColumn.CPU_USAGE_ELEMENT;
import static com.huawei.hercules.controller.agent.constant.PerformanceResponseColumn.DATA_ELEMENT;
import static com.huawei.hercules.controller.agent.constant.PerformanceResponseColumn.MEMORY_USAGE_ELEMENT;
import static com.huawei.hercules.util.ListUtils.listJoinBySeparator;

/**
 * 功能描述：agent管理相关rest接口
 *
 * @author z30009938
 * @since 2021-10-14
 */
@RestController
@RequestMapping("/api")
public class AgentManagerController {
    /**
     * 定义日志工具
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentManagerController.class);

    /**
     * 内存换算单位
     */
    private static final int MEMORY_CONVERSION_RATE = 1024;

    @Autowired
    private IAgentManagerService agentManagerService;

    /**
     * agent信息分页查询
     *
     * @param pageSize 页码大小
     * @param current  需要查询的页码
     * @param sorter   排序列
     * @param order    排序方式
     * @param region   agent区域
     * @return agent信息
     */
    @RequestMapping(value = "/agent", method = RequestMethod.GET)
    public JSONObject getAgents(@RequestParam(defaultValue = "10") int pageSize,
                                @RequestParam(defaultValue = "1") int current,
                                @RequestParam(required = false) String sorter,
                                @RequestParam(required = false) String order,
                                @RequestParam(required = false) String region) {
        LOGGER.info("Start to get agent paging information, pageSize={}, current={}", pageSize, current);
        String databaseSortColumn = DatabaseColumn.getDatabaseColumnName(sorter);
        JSONObject agentPage = agentManagerService.getAgentPage(pageSize, current, databaseSortColumn, order, region);
        if (agentPage == null) {
            LOGGER.error("Failed to get agent paging information, pageSize={}, current={}", pageSize, current);
            throw new HerculesException("Failed to get agent paging information.");
        }
        LOGGER.debug("Succeed to get agent paging information, info={}.", agentPage);
        if (!agentPage.containsKey(DatabaseColumn.RESPONSE_DATA_ELEMENT)) {
            LOGGER.warn("Can not find data parameter in response, info={}.", agentPage);
            return agentPage;
        }
        LOGGER.info("Start to convert agent information.");
        JSONArray agentsJsonArray = agentPage.getJSONArray(DatabaseColumn.RESPONSE_DATA_ELEMENT);
        for (int i = 0; i < agentsJsonArray.size(); i++) {
            JSONObject elementObject = agentsJsonArray.getJSONObject(i);
            convertToWebFormat(elementObject);
            LOGGER.debug("Convert agent result:{}.", elementObject);
        }
        LOGGER.info("Succeed to convert agent paging information, pageSize={}, current={}", pageSize, current);

        // 因为getJSONArray返回的是一个新的对象，所以这里要重新设置一下data
        agentPage.put(DatabaseColumn.RESPONSE_DATA_ELEMENT, agentsJsonArray);
        return agentPage;
    }

    /**
     * 把json对象转换成前端需要的格式类型
     *
     * @param elementObject 后端查询得到的真实agent数据
     */
    private void convertToWebFormat(JSONObject elementObject) {
        if (elementObject == null || elementObject.isEmpty()) {
            LOGGER.warn("Null agent information can not convert to web format.");
            return;
        }
        elementObject.put(ResponseColumn.AGENT_ID, elementObject.remove(DatabaseColumn.AGENT_ID));
        String statusType = elementObject.getString(DatabaseColumn.AGENT_STATE);
        elementObject.put(ResponseColumn.AGENT_STATE, AgentStatus.getShowStatus(statusType));
        elementObject.put(ResponseColumn.AGENT_LABEL, elementObject.get(DatabaseColumn.AGENT_STATE));
        elementObject.remove(DatabaseColumn.AGENT_STATE);
        elementObject.put(ResponseColumn.AGENT_IP, elementObject.remove(DatabaseColumn.AGENT_IP));
        elementObject.put(ResponseColumn.AGENT_NAME, elementObject.remove(DatabaseColumn.AGENT_NAME));
        elementObject.put(ResponseColumn.AGENT_APPROVED, elementObject.getBoolean(DatabaseColumn.AGENT_APPROVED));
        elementObject.remove(DatabaseColumn.AGENT_APPROVED);
    }

    /**
     * 根据agent的id获取agent明细
     *
     * @param agentId agent id
     * @return agent明细信息
     */
    @RequestMapping(value = "/agent/get", method = RequestMethod.GET, params = "agent_id")
    public JSONObject getAgent(@RequestParam("agent_id") Long agentId) {
        LOGGER.debug("Start to get agent information, id={}.", agentId);
        JSONObject returnJsonObject = new JSONObject();
        JSONObject responseJsonObject = agentManagerService.getOneById(agentId);
        if (responseJsonObject == null || responseJsonObject.isEmpty()) {
            LOGGER.error("The agent not exist, id = [{}].", agentId);
            throw new HerculesException("The agent not exist, id=[" + agentId + "].");
        }
        LOGGER.info("Get agent info from server:{}.", responseJsonObject);
        JSONObject agentJsonObject = responseJsonObject.getJSONObject("agent");
        convertToWebFormat(agentJsonObject);
        returnJsonObject.put(ResponseColumn.RESPONSE_DATA_ELEMENT, agentJsonObject);
        LOGGER.debug("Succeed to return agent information:{}.", returnJsonObject);
        return returnJsonObject;
    }

    /**
     * 根据agentId获取agent所在服务器性能指标
     *
     * @param agentId 需要获取性能指标的agentId
     * @return 性能数据，agent所在服务器的cpu使用率和内存使用值
     */
    @RequestMapping(value = "/agent/chart", method = RequestMethod.GET, params = "agent_id")
    public JSONObject getAgentPerformance(@RequestParam("agent_id") Long agentId) {
        LOGGER.debug("Start to get agent performance, id={}.", agentId);
        JSONObject agentInfoJSONObject = getAgentContentById(agentId);
        if (agentInfoJSONObject == null || agentInfoJSONObject.isEmpty()) {
            LOGGER.error("The agent not exist in system, get performance failed, id={}", agentId);
            throw new HerculesException("The agent not exist in system, id=" + agentId);
        }
        HttpEntity<String> httpEntity = agentManagerService.getState(agentId,
                agentInfoJSONObject.getString(DatabaseColumn.AGENT_IP),
                agentInfoJSONObject.getString(DatabaseColumn.AGENT_NAME));
        if (httpEntity == null) {
            LOGGER.error("The response is null when get agent performance, id = [{}].", agentId);
            throw new HerculesException("The response is null when get agent performance, id=[" + agentId + "].");
        }
        String performanceInfoString = httpEntity.getBody();
        if (StringUtils.isEmpty(performanceInfoString)) {
            LOGGER.error("Failed to get agent performance, id = [{}].", agentId);
            throw new HerculesException("Failed to get agent performance, id=[" + agentId + "].");
        }
        LOGGER.debug("The agent(id={}) performance:{}.", agentId, performanceInfoString);
        JSONObject agentPerformanceJson = JSONObject.parseObject(performanceInfoString);

        // data for cpu and memory
        Map<String, Object> data = new HashMap<>();
        data.put(CPU_USAGE_ELEMENT, agentPerformanceJson.get(CPU_USED_PERCENTAGE));
        long usedMemory = agentPerformanceJson.getLongValue(TOTAL_MEMORY)
                - agentPerformanceJson.getLongValue(FREE_MEMORY);
        data.put(MEMORY_USAGE_ELEMENT, usedMemory / MEMORY_CONVERSION_RATE);

        // build response message
        JSONObject returnJsonObject = new JSONObject();
        returnJsonObject.put(DATA_ELEMENT, data);
        LOGGER.info("Return agent(id={}) performance:{}.", agentId, returnJsonObject);
        return returnJsonObject;
    }

    /**
     * 删除多个agent
     *
     * @param idsString comma separated agent id list
     * @return 删除成功为true，反之为false
     */
    @RequestMapping(value = "/agent", params = "agent_id", method = RequestMethod.DELETE)
    public JSONObject deleteMany(@RequestParam("agent_id") String idsString) {
        if (StringUtils.isEmpty(idsString)) {
            throw new HerculesException("The param agent_id is not exist or empty.");
        }
        String[] idsArray = idsString.split(",");

        // 判断是否所有id都存在且是inactive状态，不通过直接抛出不合法异常
        List<String> invalidIds = new ArrayList<>();
        for (String id : idsArray) {
            if (isAgentStatusMatch(getAgentContentById(parseAgentId(id)), AgentStatus.INACTIVE)) {
                continue;
            }
            invalidIds.add(id);
        }
        if (!invalidIds.isEmpty()) {
            LOGGER.error("Find non-existent or non-inactive agents when remove them, invalid ids={}.", invalidIds);
            throw new HerculesException("Find non-existent or non-inactive agents when remove them, ids=" + invalidIds);
        }
        HttpEntity<String> httpEntity = agentManagerService.deleteMany(idsString, AgentManagerType.AGENT_MANAGER_DELETE);
        if (httpEntity == null) {
            LOGGER.error("No result for removing agents, ids = [{}].", idsString);
            throw new HerculesException("No result for removing agents, ids = [" + idsString + "].");
        }
        LOGGER.info("Remove agents result:{}.", httpEntity.getBody());
        return JSONObject.parseObject(httpEntity.getBody());
    }

    /**
     * 停止agent
     *
     * @param needStopAgent agent id
     * @return 操作成功为true，反之为false
     */
    @RequestMapping(value = "/agent/stop", method = RequestMethod.POST)
    public JSONObject managerOne(@RequestBody Map<String, List<String>> needStopAgent) {
        List<String> agentIdsList = needStopAgent.get("agent_id");
        List<String> invalidIds = new ArrayList<>();
        for (String id : agentIdsList) {
            if (isAgentStatusMatch(getAgentContentById(parseAgentId(id)), AgentStatus.BUSY)) {
                continue;
            }
            invalidIds.add(id);
        }
        if (!invalidIds.isEmpty()) {
            LOGGER.error("Find non-existent or non-busy agents when stop them, ids:{}", invalidIds);
            throw new HerculesException("Find non-existent or non-busy agents when stop them, ids:" + invalidIds);
        }
        String agentIds = listJoinBySeparator(agentIdsList, ',');
        HttpEntity<String> httpEntity = agentManagerService.managerMany(agentIds, AgentManagerType.AGENT_MANAGER_STOP);
        if (httpEntity == null) {
            LOGGER.error("No result for stop agents, ids = [{}], action = [stop].", agentIds);
            throw new HerculesException("No result for stop agents, ids=[" + agentIds + "].");
        }
        LOGGER.info("Stop agent result:{}.", httpEntity.getBody());
        return JSONObject.parseObject(httpEntity.getBody());
    }

    /**
     * 根据action操作agents
     *
     * @param params agent 参数
     * @return 操作成功为true，反之为false
     */
    @RequestMapping(value = "/agent/license", method = RequestMethod.POST)
    public JSONObject agentLicense(@RequestBody JSONObject params) {
        Long agentId = params.getLong("agent_id");
        boolean needApproved = params.getBoolean("licensed");
        JSONObject agentContent = getAgentContentById(agentId);
        if (agentContent == null || agentContent.isEmpty()) {
            throw new HerculesException("The agent not exist.");
        }
        Boolean isApproved = agentContent.getBoolean(DatabaseColumn.AGENT_APPROVED);
        if (needApproved && isApproved) {
            throw new HerculesException("The agent already been approved.");
        }
        if (!needApproved && !isApproved) {
            throw new HerculesException("The agent already been disapproved.");
        }
        HttpEntity<String> httpEntity = null;
        if (needApproved) {
            httpEntity = agentManagerService.managerOne(agentId, "approve");
        }
        if (!needApproved) {
            httpEntity = agentManagerService.managerOne(agentId, "disapprove");
        }
        if (httpEntity == null) {
            throw new HerculesException("Approve or disapprove agent failed.");
        }
        LOGGER.info("Manager agents result:{}.", httpEntity.getBody());
        return JSONObject.parseObject(httpEntity.getBody());
    }

    /**
     * Get the number of available agents.
     *
     * @param targetRegion The name of target region
     * @return availableAgentCount Available agent count
     */
    @RequestMapping(value = {"/agent/available/count"}, method = RequestMethod.GET)
    JSONObject getAvailableAgentCount(@RequestParam(value = "region") String targetRegion) {
        HttpEntity<String> httpEntity = agentManagerService.getAvailableAgentCount(targetRegion);
        return JSONObject.parseObject(httpEntity.getBody());
    }

    /**
     * Get the number of available agents.
     *
     * @return availableAgentCount Available agent count
     */
    @RequestMapping(value = {"/agent/cleanup"}, method = RequestMethod.GET)
    JSONObject cleanupInactiveAgent() {
        HttpEntity<String> httpEntity = agentManagerService.cleanUpAgentsInInactiveRegion();
        return JSONObject.parseObject(httpEntity.getBody());
    }

    /**
     * 判断agent状态是否与指定的状态信息匹配
     *
     * @param agentInfo   agent信息
     * @param agentStatus agent状态
     * @return 匹配时返回true，不匹配时返回false
     */
    private boolean isAgentStatusMatch(JSONObject agentInfo, AgentStatus agentStatus) {
        if (agentInfo == null || agentStatus == null) {
            return false;
        }
        String status = agentInfo.getString(DatabaseColumn.AGENT_STATE);
        return agentStatus.isStatusMatched(status);
    }

    /**
     * 获取agent在数据库中保存的信息
     *
     * @param agentId agentId
     * @return agent在数据库中保存的信息
     */
    private JSONObject getAgentContentById(Long agentId) {
        JSONObject responseJsonObject = agentManagerService.getOneById(agentId);
        if (responseJsonObject == null) {
            LOGGER.error("Get the agent info failed by id, id = [{}].", agentId);
            throw new HerculesException("Get the agent info failed by id, id=[" + agentId + "].");
        }
        LOGGER.info("Get agent info from server:{}.", responseJsonObject);
        return responseJsonObject.getJSONObject(AgentPerformanceColumn.AGENT_ELEMENT);
    }

    /**
     * 把agentId字符串转换成长整型id
     *
     * @param agentIdString id字符串
     * @return 长整型id
     */
    private Long parseAgentId(String agentIdString) {
        try {
            return Long.parseLong(agentIdString);
        } catch (NumberFormatException numberFormatException) {
            throw new HerculesException("The agent id must be number, value=" + agentIdString);
        }
    }
}
