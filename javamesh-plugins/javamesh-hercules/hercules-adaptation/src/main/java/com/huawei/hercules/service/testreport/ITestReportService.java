package com.huawei.hercules.service.testreport;

import com.alibaba.fastjson.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 压测报告
 */
@FeignClient(url = "${decisionEngine.url}" + "/rest/testReport", name = "testreport")
public interface ITestReportService {
    @RequestMapping("/list")
    JSONObject getPagedAll(@RequestParam(required = false) String query,
                           @RequestParam(required = false) String testType,
                           @RequestParam(required = false) String testNames,
                           @RequestParam(required = false) String startTime,
                           @RequestParam(required = false) String endTime,
                           @RequestParam(required = false) String pages);


    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    String delete(@RequestParam("ids") String ids);
}
