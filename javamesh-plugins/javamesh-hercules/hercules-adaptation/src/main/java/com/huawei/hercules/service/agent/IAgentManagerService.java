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

package com.huawei.hercules.service.agent;

import com.alibaba.fastjson.JSONObject;
import com.huawei.hercules.config.FeignRequestInterceptor;
import com.huawei.hercules.controller.agent.constant.AgentManagerType;
import com.huawei.hercules.fallback.AgentManagerServiceFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 功能描述：agent管理相关接口
 *
 * @author z30009938
 * @since 2021-10-14
 */
@FeignClient(
        url = "${controller.engine.url}" + "/rest/agent",
        name = "agentManager",
        fallbackFactory = AgentManagerServiceFallbackFactory.class,
        configuration = FeignRequestInterceptor.class
)
public interface IAgentManagerService {
    /**
     * 查询所有agent
     *
     * @param region 区域
     * @return 查询结果信息
     */
    @RequestMapping("/list")
    JSONObject getAll(@RequestParam(value = "region", required = false) final String region);

    /**
     * 获取所有agent的一页数据
     *
     * @param pageSize 一页数据大小
     * @param current  需要查询的页码
     * @param sorter   排序的列
     * @param order    排序的方式
     * @param region   需要查询的区域
     * @return 查询的结果信息
     */
    @RequestMapping(value = {"/list"}, params = "pageSize")
    JSONObject getAgentPage(@RequestParam int pageSize,
                            @RequestParam int current,
                            @RequestParam(required = false) String sorter,
                            @RequestParam(required = false) String order,
                            @RequestParam(value = "region", required = false) final String region);


    /**
     * Get the agent detail info for the given agent id.[方法重新命名，用于与另一个接口区分]
     *
     * @param id agent id
     * @return agent/agentDetail
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    JSONObject getOneById(@PathVariable Long id);

    /**
     * Clean up the agents in the inactive region
     */

    @RequestMapping(value = "/api", params = "action=cleanup", method = RequestMethod.POST)
    HttpEntity<String> cleanUpAgentsInInactiveRegion();

    /**
     * Get the current performance of the given agent.
     *
     * @param id   agent id
     * @param ip   agent ip
     * @param name agent name
     * @return json message
     */

    @RequestMapping("/api/{id}/state")
    HttpEntity<String> getState(@PathVariable Long id, @RequestParam String ip, @RequestParam String name);

    /**
     * Get the current all agents state.
     *
     * @return json message
     */
    @RequestMapping(value = {"/api/states"}, method = RequestMethod.GET)
    HttpEntity<String> getStates();

    /**
     * Get all agents from database.
     *
     * @return json message
     */
    @RequestMapping(value = {"/api"}, method = RequestMethod.GET)
    HttpEntity<String> getAll();

    /**
     * Get the agent for the given agent id.
     *
     * @return json message
     */
    @RequestMapping(value = "/api/{id}", method = RequestMethod.GET)
    HttpEntity<String> getOne(@PathVariable("id") Long id);

    /**
     * Delete an agent.
     *
     * @param id     agent id
     * @param action 执行类型为action,{@link AgentManagerType}
     * @return json message
     */
    @RequestMapping(value = "/api/{id}", method = RequestMethod.DELETE)
    HttpEntity<String> deleteOne(@PathVariable("id") Long id, @RequestParam("action") String action);

    /**
     * Delete the given agents.
     *
     * @param ids    comma separated agent id list
     * @param action 执行类型为action,{@link AgentManagerType}
     * @return json message
     */
    @RequestMapping(value = "/api", method = RequestMethod.DELETE)
    HttpEntity<String> deleteMany(@RequestParam("ids") String ids, @RequestParam("action") String action);

    /**
     * manager an agent.
     *
     * @param id     agent id
     * @param action 执行类型为action,{@link AgentManagerType}
     * @return json message
     */
    @RequestMapping(value = "/api/{id}", method = RequestMethod.PUT)
    HttpEntity<String> managerOne(@PathVariable("id") Long id, @RequestParam("action") String action);

    /**
     * manager the given agents.
     *
     * @param ids    comma separated agent id list
     * @param action 执行类型为action,{@link AgentManagerType}
     * @return json message
     */
    @RequestMapping(value = "/api", method = RequestMethod.PUT)
    HttpEntity<String> managerMany(@RequestParam("ids") String ids, @RequestParam("action") String action);

    /**
     * Get the number of available agents.
     *
     * @param targetRegion The name of target region
     * @return availableAgentCount Available agent count
     */
    @RequestMapping(value = {"/api/availableAgentCount"}, method = RequestMethod.GET)
    HttpEntity<String> getAvailableAgentCount(@RequestParam(value = "targetRegion") String targetRegion);
}
