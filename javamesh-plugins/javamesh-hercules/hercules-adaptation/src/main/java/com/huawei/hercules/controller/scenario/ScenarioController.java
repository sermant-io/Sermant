/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.hercules.controller.scenario;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huawei.hercules.controller.BaseController;
import com.huawei.hercules.service.scenario.IScenarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ScenarioController extends BaseController {

    @Autowired
    private IScenarioService scenarioService;

    /**
     * key的对应关系
     **/
    private static final Map<String, String> scenarioKeys;

    static {
        scenarioKeys = new HashMap<>();
        getKeys();
    }

    /**
     * 压测场景创建
     *
     * @param params 入参
     * @return 创建状态
     */
    @RequestMapping(value = "/scenario", method = RequestMethod.POST)
    public JSONObject create(@RequestBody JSONObject params) {
        if (StringUtils.isEmpty(params) || !params.containsKey("scenario_name") && !params.containsKey("app_name")) {
            return returnError("压测场景信息缺失不完善");
        }

        if (!StringUtils.isEmpty(params.getString("scenario_id"))) {
            // 更新压测场景
            params.put("id", params.getString("scenario_id"));
        } else {
            // 新建压测场景：压测场景只能有一个
            String scenarioName = params.getString("scenario_name");
            JSONObject result = scenarioService.getPagedAll(null, null, null, null, scenarioName, null);
            if (result != null) {
                Map<String, Object> scenarioListPage = result.getJSONObject("scenarioListPage");
                if (scenarioListPage.get("total") != null && Integer.parseInt(scenarioListPage.get("total").toString()) > 0) {
                    return returnError("压测场景已存在");
                }
            }
        }

        List<String> labels = (List<String>) params.get("label");
        params.remove("label");
        params.put("label", arrayToStr(labels));

        params.put("scenario_type", CUSTOM_SCRIPT);
        scenarioService.create(params.toString());
        return returnSuccess();
    }

    /**
     * 压测场景更新
     * @param params 场景信息
     * @return 更新结果
     */
    @RequestMapping(value = "/scenario", method = RequestMethod.PUT)
    public JSONObject update(@RequestBody JSONObject params) {
        return create(params);
    }

    /**
     * 压测场景查询
     * @param keywords 模糊查询关键字：支持应用名、场景名称、标签、描述的模糊查询
     * @param appName 应用名筛选
     * @param createdBy 创建人筛选
     * @param scenarioType 场景类型筛选
     * @param scenarioName 场景名称筛选
     * @param pageSize 分页信息
     * @param currentPageNum 当前页
     * @param sorter 排序关键字
     * @param order 排序方式
     * @return 查询结果
     */
    @RequestMapping(value = "/scenario", method = RequestMethod.GET)
    public JSONObject queryScenario(@RequestParam(required = false) String keywords,
                                    @RequestParam(name = "appName", required = false) String[] appName,
                                    @RequestParam(name = "createdBy", required = false) String[] createdBy,
                                    @RequestParam(name = "scenarioType", required = false) String[] scenarioType,
                                    @RequestParam(name = "scenarioName", required = false) String[] scenarioName,
                                    @RequestParam(required = false, defaultValue = "10") int pageSize,
                                    @RequestParam(required = false, defaultValue = "1") int currentPageNum,
                                    @RequestParam(required = false) String sorter,
                                    @RequestParam(required = false) String order) {
        // 1.查询条件的转换
        JSONObject pagesInfo = new JSONObject();
        pagesInfo.put("size", pageSize);
        pagesInfo.put("page", currentPageNum == 0 ? 0 : currentPageNum - 1);
        if (!StringUtils.isEmpty(sorter) && !StringUtils.isEmpty(order)) {
            StringJoiner sj = new StringJoiner(",");
            sj.add(scenarioKeys.get(sorter)).add(getOrder(order));
            pagesInfo.put("sort", sj.toString());
        }

        JSONObject result = scenarioService.getPagedAll(keywords, arrayToStr(appName), arrayToStr(createdBy), arrayToStr(scenarioType), arrayToStr(scenarioName), pagesInfo.toString());

        // 2.结果适配
        if (result != null) {
            JSONObject scenarioListPage = result.getJSONObject("scenarioListPage");
            JSONArray files = scenarioListPage.getJSONArray("content");
            result.put("total", scenarioListPage.get("total"));
            for (int i = 0; i < files.size(); i++) {
                Set<Map.Entry<String, String>> entries = scenarioKeys.entrySet();
                JSONObject file = files.getJSONObject(i);
                for (Map.Entry<String, String> next : entries) {
                    file.put(next.getKey(), file.get(next.getValue()));
                }
                List<String> labels = new ArrayList<>();
                String label = (String) file.get("label");
                if (!StringUtils.isEmpty(label)) {
                    labels.addAll(Arrays.asList(label.trim().split(",")));
                }
                file.put("label", labels);

                Map<String, Object> user = file.getJSONObject("createBy");
                String createBy = user != null ? (String) user.get("name") : null;
                file.put("create_by", createBy);
                scenarioKeys.put("create_by", "name");

                // 格式化日期格式
                file.put("create_time", dataFormat((String)file.get("create_time")));
                file.put("update_time", dataFormat((String)file.get("update_time")));
            }
            result.put("data", files);
        }
        return result;
    }

    /**
     * 删除场景
     * @param scenarioId 场景ID
     * @return 删除结果
     */
    @RequestMapping(value = "/scenario", method = RequestMethod.DELETE)
    public JSONObject delete(@RequestParam(name = "scenarioId") String[] scenarioId) {
        if (StringUtils.isEmpty(scenarioId)) {
            return returnError();
        }
        try {
            scenarioService.delete(arrayToStr(scenarioId));
        } catch (Exception e) {
            return returnError("压测场景删除失败");
        }
        return returnSuccess();
    }

    /**
     * 校验场景下是否有压测任务
     * @param scenarioId 场景ID
     * @return 含有压测任务的场景名称结果
     */
    @RequestMapping(value = "/scenario/deleteCheck", method = RequestMethod.GET)
    public JSONObject deleteCheck(@RequestParam(name = "scenarioId") String[] scenarioId) {
        // 判断场景下是否有压测任务
        List<Long> scenarioIds = Arrays.stream(scenarioId)
                .map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
        JSONObject result = new JSONObject();
        Set<Object> scenarioNames = new HashSet<>();
        result.put("data", scenarioNames);
        if (scenarioIds.isEmpty()) {
            return result;
        }
        JSONObject allPerfTestByScenarioIds = scenarioService.getAllPerfTestByScenarioIds(scenarioIds);
        JSONArray perfTests = allPerfTestByScenarioIds.getJSONArray("scenarioInfos");
        if (perfTests != null && !perfTests.isEmpty()) {
            for (int i = 0; i < perfTests.size(); i++ ) {
                JSONObject test = perfTests.getJSONObject(i);
                scenarioNames.add(test.getString("scenarioName"));
            }
        }
        return result;
    }
    /**
     * 下拉列表查询
     *
     * @param value 关键字
     * @return 场景列表
     */
    @RequestMapping(value = {"/scenario/search"}, method = RequestMethod.GET)
    public JSONObject search(@RequestParam(required = false) String value) {
        JSONObject pagesInfo = new JSONObject();
        pagesInfo.put("size", Integer.MAX_VALUE);
        pagesInfo.put("page", 0);
        // 只查询脚本场景
        JSONObject result = scenarioService.getPagedAll(value, "", "", CUSTOM_SCRIPT, "", pagesInfo.toString());

        List<String> scenarioInfos = new ArrayList<>();
        if (result != null) {
            JSONObject scenarioListPage = result.getJSONObject("scenarioListPage");
            JSONArray content = scenarioListPage.getJSONArray("content");

            for (int i = 0; i < content.size(); i++ ) {
                JSONObject file = content.getJSONObject(i);
                scenarioInfos.add(file.getString("scenarioName"));
            }
            result.put("data", scenarioInfos);
        }
        return result;
    }

    private static void getKeys() {
        if (scenarioKeys.isEmpty()) {
            scenarioKeys.put("scenario_id", "id");
            scenarioKeys.put("app_name", "appName");
            scenarioKeys.put("scenario_name", "scenarioName");
            scenarioKeys.put("scenario_type", "scenarioType");
            scenarioKeys.put("script_path", "scriptPath");
            scenarioKeys.put("create_time", "createTime");
            scenarioKeys.put("update_time", "updateTime");
        }
    }
}
