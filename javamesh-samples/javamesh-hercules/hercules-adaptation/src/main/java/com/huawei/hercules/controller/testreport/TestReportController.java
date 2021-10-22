package com.huawei.hercules.controller.testreport;

import com.alibaba.fastjson.JSONObject;
import com.huawei.hercules.controller.BaseController;
import com.huawei.hercules.service.testreport.ITestReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api")
public class TestReportController extends BaseController {

    @Autowired
    private ITestReportService testReportService;

    /**
     * key的对应关系
     **/
    private static Map<String, String> testReportKeys = new HashMap<>();

    static {
        getKeys();
    }

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
        JSONObject result = testReportService.getPagedAll(keywords, test_type, arrayToStr(test_name),start_time, end_time, pagesInfo.toString());

        // 结果适配
        if (result != null) {
            Map<String, Object> testListPage = (Map<String, Object>) result.get("testReportListPage");
            List<Map<String, Object>> files = (List<Map<String, Object>>) testListPage.get("content");
            result.put("total", testListPage.get("total"));
            for (Map<String, Object> file : files) {
                Set<Map.Entry<String, String>> entries = testReportKeys.entrySet();
                for (Map.Entry<String, String> next : entries) {
                    file.put(next.getKey(), file.get(next.getValue()));
                }

                Map<String, Object> createdUser = (Map<String, Object>) file.get("createdUser");
                file.put("owner", createdUser.get(testReportKeys.get("owner")));
                // 格式化日期格式
                file.put("start_time", dataFormat((String)file.get("start_time")));
                file.put("end_time", dataFormat((String)file.get("end_time")));
            }
            result.put("data", files);
        }
        return result;
    }


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
