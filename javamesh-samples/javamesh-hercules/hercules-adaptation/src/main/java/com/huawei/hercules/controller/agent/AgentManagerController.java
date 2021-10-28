/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.controller.agent;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huawei.hercules.controller.agent.constant.AgentManagerType;
import com.huawei.hercules.controller.agent.constant.DatabaseInfoKey;
import com.huawei.hercules.controller.agent.constant.PerformanceServerKey;
import com.huawei.hercules.controller.agent.constant.ResponseInfoKey;
import com.huawei.hercules.exception.HerculesException;
import com.huawei.hercules.service.agent.IAgentManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.huawei.hercules.controller.agent.constant.PerformanceResponseKey.DATA_ELEMENT;
import static com.huawei.hercules.controller.agent.constant.PerformanceResponseKey.MEMORY_USAGE_ELEMENT;
import static com.huawei.hercules.controller.agent.constant.PerformanceResponseKey.CPU_USAGE_ELEMENT;
import static com.huawei.hercules.controller.agent.constant.PerformanceServerKey.CPU_USED_PERCENTAGE;
import static com.huawei.hercules.controller.agent.constant.PerformanceServerKey.FREE_MEMORY;
import static com.huawei.hercules.controller.agent.constant.PerformanceServerKey.TOTAL_MEMORY;
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
        JSONObject agentPage = agentManagerService.getAgentPage(pageSize, current, sorter, order, region);
        if (agentPage == null) {
            LOGGER.error("Failed to get agent paging information, pageSize={}, current={}", pageSize, current);
            throw new HerculesException("Failed to get agent paging information.");
        }
        LOGGER.debug("Succeed to get agent paging information, info={}.", agentPage);
        if (!agentPage.containsKey(DatabaseInfoKey.RESPONSE_DATA_ELEMENT)) {
            LOGGER.warn("Can not find data parameter in response, info={}.", agentPage);
            return agentPage;
        }
        LOGGER.info("Start to convert agent information.");
        JSONArray agentsJsonArray = agentPage.getJSONArray(DatabaseInfoKey.RESPONSE_DATA_ELEMENT);
        for (int i = 0; i < agentsJsonArray.size(); i++) {
            JSONObject elementObject = agentsJsonArray.getJSONObject(i);
            convertToWebFormat(elementObject);
            LOGGER.debug("Convert agent result:{}.", elementObject);
        }
        LOGGER.info("Succeed to convert agent paging information, pageSize={}, current={}", pageSize, current);

        // 因为getJSONArray返回的是一个新的对象，所以这里要重新设置一下data
        agentPage.put(DatabaseInfoKey.RESPONSE_DATA_ELEMENT, agentsJsonArray);
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
        elementObject.put(ResponseInfoKey.AGENT_ID, elementObject.remove(DatabaseInfoKey.AGENT_ID));
        String statusType = elementObject.getString(DatabaseInfoKey.AGENT_STATE);
        elementObject.put(ResponseInfoKey.AGENT_STATE, AgentStatus.getShowStatus(statusType));
        elementObject.put(ResponseInfoKey.AGENT_LABEL, elementObject.get(DatabaseInfoKey.AGENT_STATE));
        elementObject.remove(DatabaseInfoKey.AGENT_STATE);
        elementObject.put(ResponseInfoKey.AGENT_IP, elementObject.remove(DatabaseInfoKey.AGENT_IP));
        elementObject.put(ResponseInfoKey.AGENT_NAME, elementObject.remove(DatabaseInfoKey.AGENT_NAME));
        elementObject.put(ResponseInfoKey.AGENT_APPROVED, elementObject.getBoolean(DatabaseInfoKey.AGENT_APPROVED));
        elementObject.remove(DatabaseInfoKey.AGENT_APPROVED);
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
        returnJsonObject.put(ResponseInfoKey.RESPONSE_DATA_ELEMENT, agentJsonObject);
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
        JSONObject responseJsonObject = agentManagerService.getOneById(agentId);
        if (responseJsonObject == null) {
            LOGGER.error("The response is null when get agent by id, id = [{}].", agentId);
            throw new HerculesException("The response is null when get agent by id, id=[" + agentId + "].");
        }
        LOGGER.info("Get agent info from server:{}.", responseJsonObject);
        JSONObject agentInfoJSONObject = responseJsonObject.getJSONObject(PerformanceServerKey.AGENT_ELEMENT);
        if (agentInfoJSONObject == null || agentInfoJSONObject.isEmpty()) {
            LOGGER.error("The agent not exist, id = [{}].", agentId);
            throw new HerculesException("The agent not exist, id=[" + agentId + "].");
        }
        HttpEntity<String> httpEntity = agentManagerService.getState(agentId,
                agentInfoJSONObject.getString(DatabaseInfoKey.AGENT_IP),
                agentInfoJSONObject.getString(DatabaseInfoKey.AGENT_NAME));
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
     * 删除一个agent
     *
     * @param agentId agentId
     * @return 删除成功为true，反之为false
     */
    @RequestMapping(value = "/agent", params = "agent_id", method = RequestMethod.DELETE)
    public JSONObject deleteOne(@RequestParam("agent_id") Long agentId) {
        HttpEntity<String> httpEntity = agentManagerService.deleteOne(agentId, AgentManagerType.AGENT_MANAGER_DELETE);
        if (httpEntity == null) {
            LOGGER.error("The response is null when delete an agent, id = [{}].", agentId);
            throw new HerculesException("The response is null when delete an agent, id=[" + agentId + "].");
        }
        LOGGER.info("Delete an agent result:{}.", httpEntity.getBody());
        return JSONObject.parseObject(httpEntity.getBody());
    }

    /**
     * 删除多个agent
     *
     * @param ids comma separated agent id list
     * @return 删除成功为true，反之为false
     */
    @RequestMapping(value = "/agent", params = "agent_ids", method = RequestMethod.DELETE)
    public JSONObject deleteMany(@RequestParam("agent_ids") String ids) {
        HttpEntity<String> httpEntity = agentManagerService.deleteMany(ids, AgentManagerType.AGENT_MANAGER_DELETE);
        if (httpEntity == null) {
            LOGGER.error("The response is null when delete agents, ids = [{}].", ids);
            throw new HerculesException("The response is null when delete agents, ids = [" + ids + "].");
        }
        LOGGER.info("Delete agents result:{}.", httpEntity.getBody());
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
        String agentIds = listJoinBySeparator(agentIdsList, ',');
        HttpEntity<String> httpEntity = agentManagerService.managerMany(agentIds, AgentManagerType.AGENT_MANAGER_STOP);
        if (httpEntity == null) {
            LOGGER.error("The response is null when stop agent, ids = [{}], action = [stop].", agentIds);
            throw new HerculesException("The response is null when stop agent, ids=[" + agentIds + "].");
        }
        LOGGER.info("Stop agent result:{}.", httpEntity.getBody());
        return JSONObject.parseObject(httpEntity.getBody());
    }

    /**
     * 根据action操作agents
     *
     * @param agentId agent ids
     * @param action  {@link AgentManagerType}
     * @return 操作成功为true，反之为false
     */
    @RequestMapping(value = "/agent/{action}", params = "agent_id", method = {RequestMethod.PUT, RequestMethod.POST})
    public JSONObject managerOne(@RequestParam(value = "agent_id") String agentId, @PathVariable String action) {
        HttpEntity<String> httpEntity = agentManagerService.managerMany(agentId, action);
        if (httpEntity == null) {
            LOGGER.error("The response is null when manager agents, ids = [{}], action = [{}].", agentId, action);
            throw new HerculesException("The response is null when manager agents, ids = [" + agentId + "].");
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
}
