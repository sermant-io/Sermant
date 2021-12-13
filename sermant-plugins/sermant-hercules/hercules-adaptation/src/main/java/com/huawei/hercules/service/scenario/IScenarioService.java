/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.hercules.service.scenario;

import com.alibaba.fastjson.JSONObject;
import com.huawei.hercules.config.FeignRequestInterceptor;
import com.huawei.hercules.fallback.ScenarioServiceFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 压测场景
 */
@FeignClient(
        url = "${controller.engine.url}" + "/rest/scenario",
        name = "scenario",
        fallbackFactory = ScenarioServiceFallbackFactory.class,
        configuration = FeignRequestInterceptor.class
)
public interface IScenarioService {

    /**
     * 保存
     *
     * @param scenarioInfos 场景信息
     * @return 保存结果
     */
    @RequestMapping("/save")
    String create(@RequestParam String scenarioInfos);

    /**
     * 查询
     *
     * @param query 关键字
     * @param pages 分页信息
     * @return 压测场景列表
     */
    @RequestMapping("/list")
    JSONObject getPagedAll(@RequestParam(required = false) String query,
                           @RequestParam(required = false) String appNames,
                           @RequestParam(required = false) String createBy,
                           @RequestParam(required = false) String scenarioType,
                           @RequestParam(required = false) String scenarioName,
                           @RequestParam(required = false) String pages);

    /**
     * 查询所有的
     *
     * @return 所有场景数据
     */
    @RequestMapping("/allList")
    JSONObject getAll();

    /**
     * 删除
     *
     * @param ids 场景ID
     * @return 删除结果
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    String delete(@RequestParam("ids") String ids);

    /**
     * 保存压测场景与压测任务关系
     *
     * @param scenarioPerfTestInfos 压测场景与压测任务关系JSON数据
     * @return 保存结果
     */
    @RequestMapping(value = "/saveScenarioPerfTest", method = RequestMethod.POST)
    String saveScenarioPerfTest(@RequestParam String scenarioPerfTestInfos);

    /**
     * 查询压测任务对应的场景信息
     *
     * @param perfTestId 场景ID
     * @return 场景信息
     */
    @RequestMapping(value = "/getAllByPerfTestId", method = RequestMethod.POST)
    JSONObject getAllByPerfTestId(@RequestParam Long perfTestId);

    /**
     * 查询场景对应的压测任务信息
     * @param scenarioIds 场景ID信息
     * @return 场景对应下的压测任务信息
     */
    @RequestMapping(value = "/getAllPerfTestByScenarioIds", method = RequestMethod.POST)
    JSONObject getAllPerfTestByScenarioIds(@RequestParam List<Long> scenarioIds);

    /**
     * 查询场景
     * @param scriptPaths 脚本路径
     * @return 场景信息
     */
    @RequestMapping(value = "/getAllByScriptPaths", method = RequestMethod.POST)
    JSONObject getAllByScriptPaths(@RequestParam List<String> scriptPaths);
}
