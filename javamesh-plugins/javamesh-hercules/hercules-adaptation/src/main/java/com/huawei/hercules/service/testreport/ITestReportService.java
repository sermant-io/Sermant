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

package com.huawei.hercules.service.testreport;

import com.alibaba.fastjson.JSONObject;
import com.huawei.hercules.config.FeignRequestInterceptor;
import com.huawei.hercules.fallback.TestReportServiceFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 压测报告
 */
@FeignClient(
        url = "${controller.engine.url}" + "/rest/testReport",
        name = "testreport",
        fallbackFactory = TestReportServiceFallbackFactory.class,
        configuration = FeignRequestInterceptor.class
)
public interface ITestReportService {

    /**
     * 查询压测报告
     * @param query 模糊查询关键字：测试名称
     * @param testType 压测类型
     * @param testNames 测试名称
     * @param startTime 开始时间
     * @param endTime 完成时间
     * @param pages 分页信息
     * @return 查询结果
     */
    @RequestMapping("/list")
    JSONObject getPagedAll(@RequestParam(required = false) String query,
                           @RequestParam(required = false) String testType,
                           @RequestParam(required = false) String testNames,
                           @RequestParam(required = false) String startTime,
                           @RequestParam(required = false) String endTime,
                           @RequestParam(required = false) String pages);


    /**
     * 删除报告
     * @param ids 报告ID
     * @return 删除结果
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    String delete(@RequestParam("ids") String ids);
}
