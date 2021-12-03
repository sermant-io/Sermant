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

package com.huawei.hercules.controller.testreport;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huawei.hercules.controller.BaseController;
import com.huawei.hercules.service.testreport.ITestReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

@RestController
@RequestMapping("/api")
public class TestReportController extends BaseController {

    @Autowired
    private ITestReportService testReportService;

    /**
     * key的对应关系
     **/
    private static final Map<String, String> testReportKeys = new HashMap<>();

    static {
        getKeys();
    }

    /**
     * 压测报告查询
     *
     * @param pageSize   分页信息
     * @param current    当前页
     * @param keywords   模糊查询关键字：测试名称
     * @param test_type  压测类型
     * @param start_time 开始时间
     * @param end_time   完成时间
     * @param test_name  测试名称筛选
     * @param sorter     排序关键字
     * @param order      排序方式
     * @return 压测报告查询结果
     */
    @RequestMapping(value = {"/report"}, method = RequestMethod.GET)
    public JSONObject getPagedAll(@RequestParam(required = false, defaultValue = "10") int pageSize,
                                  @RequestParam(required = false, defaultValue = "1") int current,
                                  @RequestParam(required = false) String keywords,
                                  @RequestParam(required = false) String test_type,
                                  @RequestParam(required = false) String start_time,
                                  @RequestParam(required = false) String end_time,
                                  @RequestParam(required = false, name = "test_name[]") String[] test_name,
                                  @RequestParam(required = false) String sorter,
                                  @RequestParam(required = false) String order) {

        // 1.查询条件的转换
        JSONObject pagesInfo = new JSONObject();
        pagesInfo.put("size", pageSize);
        pagesInfo.put("page", current == 0 ? 0 : current - 1);
        if (!StringUtils.isEmpty(sorter) && !StringUtils.isEmpty(order) && testReportKeys.containsKey(sorter)) {
            StringJoiner sj = new StringJoiner(",");
            sj.add(testReportKeys.get(sorter)).add(getOrder(order));
            pagesInfo.put("sort", sj.toString());
        }
        JSONObject result = testReportService.getPagedAll(keywords, test_type, arrayToStr(test_name), start_time, end_time, pagesInfo.toString());

        // 结果适配
        if (result != null) {
            JSONObject testListPage = result.getJSONObject("testReportListPage");
            JSONArray reports = testListPage.getJSONArray("content");
            result.put("total", testListPage.get("total"));
            for (int i = 0; i < reports.size(); i++) {
                JSONObject report = reports.getJSONObject(i);
                Set<Map.Entry<String, String>> entries = testReportKeys.entrySet();
                for (Map.Entry<String, String> next : entries) {
                    report.put(next.getKey(), report.get(next.getValue()));
                }

                Map<String, Object> createdUser = report.getJSONObject("createdUser");
                report.put("owner", createdUser.get(testReportKeys.get("owner")));
                // 格式化日期格式
                report.put("start_time", dataFormat((String) report.get("start_time")));
                report.put("end_time", dataFormat((String) report.get("end_time")));
            }
            result.put("data", reports);
        }
        return result;
    }

    /**
     * 删除报告
     *
     * @param test_id 报告ID
     * @return 删除结果
     */
    @RequestMapping(value = "/report", method = RequestMethod.DELETE)
    public JSONObject delete(@RequestParam(required = false, name = "test_id[]") String[] test_id) {
        testReportService.delete(arrayToStr(test_id));
        return returnSuccess();
    }

    private static void getKeys() {
        if (testReportKeys.isEmpty()) {
            testReportKeys.put("report_id", "id");
            testReportKeys.put("test_type", "testType");
            testReportKeys.put("test_name", "testName");
            testReportKeys.put("test_id", "perfTestId");
            testReportKeys.put("owner", "name");
            testReportKeys.put("start_time", "startTime");
            testReportKeys.put("end_time", "finishTime");
            testReportKeys.put("duration", "runTime");
        }
    }

}
