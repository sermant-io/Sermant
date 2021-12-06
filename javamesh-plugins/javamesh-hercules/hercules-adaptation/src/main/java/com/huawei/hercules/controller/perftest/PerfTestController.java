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

package com.huawei.hercules.controller.perftest;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huawei.hercules.controller.BaseController;
import com.huawei.hercules.exception.HerculesException;
import com.huawei.hercules.service.perftest.IPerfTestService;
import com.huawei.hercules.service.scenario.IScenarioService;
import com.huawei.hercules.service.script.IScriptService;
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

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.huawei.hercules.controller.perftest.TaskInfoKey.DESC;
import static com.huawei.hercules.controller.perftest.TaskInfoKey.DURATION;
import static com.huawei.hercules.controller.perftest.TaskInfoKey.FAIL_RATE;
import static com.huawei.hercules.controller.perftest.TaskInfoKey.LABEL;
import static com.huawei.hercules.controller.perftest.TaskInfoKey.MONITORING_HOST;
import static com.huawei.hercules.controller.perftest.TaskInfoKey.MTT;
import static com.huawei.hercules.controller.perftest.TaskInfoKey.OWNER;
import static com.huawei.hercules.controller.perftest.TaskInfoKey.SCRIPT_PATH;
import static com.huawei.hercules.controller.perftest.TaskInfoKey.START_TIME;
import static com.huawei.hercules.controller.perftest.TaskInfoKey.STATUS;
import static com.huawei.hercules.controller.perftest.TaskInfoKey.STATUS_LABEL;
import static com.huawei.hercules.controller.perftest.TaskInfoKey.TEST_ID;
import static com.huawei.hercules.controller.perftest.TaskInfoKey.TEST_NAME;
import static com.huawei.hercules.controller.perftest.TaskInfoKey.TEST_TYPE;
import static com.huawei.hercules.controller.perftest.TaskInfoKey.TOTAL;
import static com.huawei.hercules.controller.perftest.TaskInfoKey.TPS;
import static com.huawei.hercules.controller.perftest.TaskInfoKey.getServerKey;

@RestController
@RequestMapping("/api")
public class PerfTestController extends BaseController {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PerfTestController.class);

    /**
     * 没有agent异常的报错信息
     */
    private static final String ERROR_AGENT_COUNT = "可运行的代理agent数为0，不能执行压测任务";

    /**
     * 压测脚本不存在的保报错信息
     */
    private static final String ERROR_SCRIPT_REVISION = "压测脚本不存在，不能执行压测任务";

    /**
     * 压测场景不存在的报错信息
     */
    private static final String ERROR_NO_SCENARIO = "关联压测场景不存在，不能执行压测任务";

    /**
     * 最大虚拟用户数
     **/
    private static final int MAX_VUSER_COUNT = 3000;

    /**
     * 超过最大虚拟用户数的报错信息
     */
    private static final String ERROR_VUSER_COUNT = "超过了最大虚拟用户数" + MAX_VUSER_COUNT;

    /**
     *
     */
    private static final int IMG_WIDTH = 600;

    /**
     * 最大图标展示节点个数
     */
    private static final int MAX_CHART_POINT_COUNT = 10000;

    @Autowired
    private IPerfTestService perfTestService;

    @Autowired
    private IScenarioService scenarioService;

    @Autowired
    IScriptService scripService;

    /**
     * 查询任务列表
     *
     * @param keywords 关键字
     * @param label    标签
     * @param status   状态
     * @param pageSize 页数
     * @param current  页码
     * @param sorter   排序关键字
     * @param order    排序
     * @return 任务列表信息
     */
    @RequestMapping(value = {"/task"}, method = RequestMethod.GET)
    public JSONObject getAll(@RequestParam(required = false) String keywords,
                             @RequestParam(required = false) String label,
                             @RequestParam(required = false, name = "status[]") String[] status,
                             @RequestParam(required = false, defaultValue = "10") int pageSize,
                             @RequestParam(required = false, defaultValue = "1") int current,
                             @RequestParam(required = false) String sorter,
                             @RequestParam(required = false) String order,
                             @RequestParam(required = false, name = "test_name[]") String[] testName,
                             @RequestParam(required = false, name = "test_type[]") String[] testType,
                             @RequestParam(required = false, name = "script_path[]") String[] scriptPath,
                             @RequestParam(required = false, name = "owner[]") String[] owner) {

        // 1.查询条件的转换
        JSONObject pagesInfo = new JSONObject();
        pagesInfo.put("size", pageSize);
        pagesInfo.put("page", current == 0 ? 0 : current - 1);
        if (!StringUtils.isEmpty(sorter)
                && !StringUtils.isEmpty(order)
                && !StringUtils.isEmpty(getServerKey(sorter))) {
            StringJoiner sj = new StringJoiner(",");
            sj.add(getServerKey(sorter)).add(getOrder(order));
            pagesInfo.put("sort", sj.toString());
        }
        String queryFilter = arrayToStr(status).replaceAll("running", "R").replaceAll("pending", "S");

        // 2. 查询结果
        JSONObject result = perfTestService.getAll(keywords, label, queryFilter, pagesInfo.toString(),
                arrayToStr(testName), arrayToStr(testType), arrayToStr(scriptPath), arrayToStr(owner));

        // 3.结果适配
        JSONObject response = new JSONObject();
        response.put(TOTAL.getShowKey(), 0);
        response.put("data", Collections.emptyList());
        if (result == null || result.isEmpty()) {
            LOGGER.error("The result is null when search tasks from server.");
            return response;
        }
        JSONObject testListPage = result.getJSONObject("testListPage");
        if (testListPage == null || testListPage.isEmpty()) {
            LOGGER.error("The element testListPage in result is empty, result:{}", result);
            return response;
        }
        JSONArray tasks = testListPage.getJSONArray("content");
        if (tasks == null || tasks.isEmpty()) {
            LOGGER.error("No task, result:{}", result);
            return response;
        }
        response.put(TOTAL.getShowKey(), testListPage.get(TOTAL.getServerKey()));
        List<JSONObject> responseTasksList = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            JSONObject task = tasks.getJSONObject(i);
            JSONObject oneResponseTask = buildResponseTask(task);
            responseTasksList.add(oneResponseTask);
        }
        response.put("data", responseTasksList);
        return response;
    }

    /**
     * 根据后端的task消息构建一个前端返回的task任务
     *
     * @param task 后端task消息
     * @return 前端task消息
     */
    private JSONObject buildResponseTask(JSONObject task) {
        JSONObject oneResponseTask = new JSONObject();
        if (task == null || task.isEmpty()) {
            return oneResponseTask;
        }
        oneResponseTask.put(TEST_NAME.getShowKey(), task.get(TEST_NAME.getServerKey()));
        oneResponseTask.put(DESC.getShowKey(), task.get(DESC.getServerKey()));
        List<String> labels = new ArrayList<>();
        if (!StringUtils.isEmpty(task.get(LABEL.getServerKey()))) {
            labels.addAll(Arrays.asList(task.getString(LABEL.getServerKey()).trim().split(",")));
        }
        oneResponseTask.put(LABEL.getShowKey(), labels);

        /*task.get(perfTestKeys.get("test_type"))*/
        oneResponseTask.put(TEST_TYPE.getShowKey(), CUSTOM_SCRIPT);
        oneResponseTask.put(TEST_ID.getShowKey(), task.get(TEST_ID.getServerKey()));
        oneResponseTask.put(SCRIPT_PATH.getShowKey(), task.get(SCRIPT_PATH.getServerKey()));
        Map<String, Object> createdUser = task.getJSONObject("createdUser");
        oneResponseTask.put(OWNER.getShowKey(), createdUser.get(OWNER.getServerKey()));
        oneResponseTask.put(DURATION.getShowKey(), getDurationTime(task.getString(DURATION.getServerKey())));
        if ("R".equalsIgnoreCase(task.getString("threshold"))) {
            oneResponseTask.put(DURATION.getShowKey(), task.get("runCount"));
        }
        oneResponseTask.put(MTT.getShowKey(), task.get(MTT.getServerKey()));
        String failPercent = toPercent(task.getString(FAIL_RATE.getServerKey()), task.getString("tests"));
        oneResponseTask.put(FAIL_RATE.getShowKey(), failPercent);
        String taskStatus = TaskStatus.getShowValue(task.getString(STATUS.getServerKey()));
        oneResponseTask.put(STATUS.getShowKey(), taskStatus);
        oneResponseTask.put(STATUS_LABEL.getShowKey(), task.getString(STATUS.getServerKey()));
        oneResponseTask.put(TPS.getShowKey(), task.getString(TPS.getServerKey()));
        oneResponseTask.put(MONITORING_HOST.getShowKey(), task.get(MONITORING_HOST.getServerKey()));

        // 格式化日期格式
        oneResponseTask.put(START_TIME.getShowKey(), dateFormat(task.getString(START_TIME.getServerKey()),
                TimeUnit.MILLISECONDS));
        return oneResponseTask;
    }

    /**
     * 删除任务
     *
     * @param taskId 任务ID
     * @return 删除结果
     */
    @RequestMapping(value = "/task", method = RequestMethod.DELETE)
    public HttpEntity<String> delete(@RequestParam(value = "test_id", defaultValue = "") String taskId) {
        return perfTestService.delete(taskId);
    }

    /**
     * 新建任务+运行
     *
     * @param params 任务信息
     * @return 新建结果
     */
    @RequestMapping(value = "/task", method = RequestMethod.POST)
    public JSONObject saveOne(@RequestBody JSONObject params) {
        if (StringUtils.isEmpty(params)) {
            return returnError();
        }
        JSONObject perfTestInfos = new JSONObject();

        // 1.查询数据ngrinder原生数据，获取perfTest默认值
        Map<String, Object> agentInfo = getAgentInfo();
        // agent数由前段输入
        int maxAgentCount = (int) agentInfo.get("agentCount");

        // 如果是运行，没有agent数，直接返回并提醒
        if (Boolean.parseBoolean(String.valueOf(params.get("run"))) && maxAgentCount <= 0) {
            return returnError(ERROR_AGENT_COUNT);
        }

        // 判断agent设置是否正确
        int agentCount = Integer.parseInt(params.get("agent").toString());
        if (!params.containsKey("agent") || agentCount > maxAgentCount) {
            return returnError("代理数只能设置为1-" + maxAgentCount);
        }

        // 通过查询获取默认值
        JSONObject sceneCheckResult = getScenarioId(perfTestInfos, params.getString("scenario_name"));
        if (!sceneCheckResult.getBoolean(JSON_RESULT_KEY)) {
            return sceneCheckResult;
        }
        // 转换数据参数
        setPerfTestInfos(perfTestInfos, params);

        // 计算进程数和线程数，更新虚拟用户数
        Long vuserCount = params.getLong("vuser");
        if (vuserCount > MAX_VUSER_COUNT) {
            return returnError(ERROR_VUSER_COUNT);
        }
        setProcessAndThreads(perfTestInfos, vuserCount);
        // 保存
        perfTestInfos.put("scheduledTime", params.get("start_time"));
        JSONObject jsonObject = perfTestService.saveOne(perfTestInfos.toString(), false);

        // 保存成功之后保存任务与场景的关系
        saveScenarioPerfTest(sceneCheckResult.getInteger("id"), jsonObject.getInteger("id"));
        return jsonObject;
    }

    /**
     * 压测任务启动
     *
     * @param params 任务信息
     * @return 启动结果
     */
    @RequestMapping(value = "/task/start", method = RequestMethod.POST)
    public JSONObject taskStart(@RequestBody JSONObject params) {
        Map<String, Object> agentInfo = getAgentInfo();
        int maxAgentCount = (int) agentInfo.get("agentCount");
        if ((int) agentInfo.get("agentCount") <= 0) {
            return returnError(ERROR_AGENT_COUNT);
        }

        Object testIdObj = params.get("test_id");
        if (testIdObj == null) {
            return returnError("压测任务不存在");
        }
        String testId = testIdObj.toString();
        JSONObject testInfo = perfTestService.getOne(Long.parseLong(testId));
        if (testInfo == null) {
            return returnError("压测任务不存在");
        }
        Map<String, Object> perfTestInfos = (Map<String, Object>) testInfo.get("test");
        if (perfTestInfos == null || perfTestInfos.isEmpty()) {
            return returnError("压测任务不存在");
        }
        int agentCount = Integer.parseInt(perfTestInfos.get("agentCount").toString());
        if (agentCount > maxAgentCount) {
            return returnError("可运行的agent数" + maxAgentCount + "小于设置的agent数" + perfTestInfos.get("agentCount"));
        }
        // 判断压测脚本是否存在
        if (!scripService.hasScript(String.valueOf(perfTestInfos.get("scriptName")))) {
            return returnError(ERROR_SCRIPT_REVISION);
        }
        // 判断压测场景
        JSONObject scenario = scenarioService.getAllByPerfTestId(Long.parseLong(testId));
        if (scenario == null) {
            return returnError(ERROR_NO_SCENARIO);
        }

        AtomicBoolean isClone = new AtomicBoolean(true);
        if ("SAVED".equalsIgnoreCase((String) perfTestInfos.get("status"))) {
            // 如果未运行的脚本，则直接运行
            isClone.set(false);
        }
        // 执行保存并运行接口
        initPerfTestFieldsValue(perfTestInfos, agentCount);
        if (isClone.get()) {
            clearPerfTestOldFieldsValue(perfTestInfos);
        }
        perfTestInfos.put("scheduledTime", params.get("start_time"));
        JSONObject jsonObject = perfTestService.saveOne(JSONObject.toJSONString(perfTestInfos), isClone.get());

        // 保存成功之后保存任务与场景的关系
        if (isClone.get()) {
            saveScenarioPerfTest(scenario.getInteger("id"), jsonObject.getInteger("id"));
        }
        return returnSuccess();
    }

    /**
     * 停止任务
     *
     * @param params 任务ID
     * @return 停止结果
     */
    @RequestMapping(value = "/task/stop", method = RequestMethod.POST)
    public JSONObject taskStop(@RequestBody JSONObject params) {
        Object testIdObj = params.get("test_id");
        if (testIdObj == null) {
            return returnError("Test id is empty.");
        }
        String testId = testIdObj.toString();
        HttpEntity<String> stopResult = perfTestService.stop(testId);
        if (stopResult == null) {
            return returnError("Stop test fail.");
        }
        String body = stopResult.getBody();
        if (StringUtils.isEmpty(body)) {
            return returnError("Stop test fail.");
        }
        JSONObject bodyJson = JSONObject.parseObject(body);
        if (bodyJson.getBoolean("success")) {
            return returnSuccess();
        }
        return returnError("Stop test fail.");
    }

    /**
     * 查询测试报告
     *
     * @param testId   任务ID
     * @param start    开始坐标
     * @param interval 间隔
     * @return 报告信息
     */
    @RequestMapping(value = "/task/get", method = RequestMethod.GET)
    public JSONObject get(@RequestParam("test_id") String testId, @RequestParam(required = false) Integer start, @RequestParam(required = false) Integer interval) {
        if (StringUtils.isEmpty(testId)) {
            return returnError();
        }
        if (start == null || interval == null) {
            // 查询实时数据
            return refreshTestRunningById(Long.parseLong(testId));
        }

        JSONObject report = perfTestService.getReportSectionById(Long.parseLong(testId), IMG_WIDTH, start, interval);
        if (report == null) {
            return returnError();
        }
        JSONObject test = report.getJSONObject("test");
        parseTest(test);
        JSONArray logs = report.getJSONArray("logs");
        test.put("log_name", logs == null ? Collections.emptyList() : logs);
        JSONArray thisTps = report.getJSONArray("TPS");
        if (thisTps == null || thisTps.isEmpty()) {
            thisTps = getDefaultTps(start, interval, Math.abs(start));
        }
        test.put("chart", thisTps);
        report.put("data", test);
        report.remove("test");
        report.remove("TPS");
        return report;
    }


    /**
     * 报告详情页基本信息查询
     *
     * @param testId 任务ID
     * @return 报告结果
     */
    @RequestMapping(value = {"/report/get"}, method = RequestMethod.GET)
    public JSONObject getReports(@RequestParam("test_id") String testId) {
        JSONObject report = perfTestService.getReportById(Long.parseLong(testId));
        if (report == null || report.isEmpty()) {
            throw new HerculesException("Report data don't found, please confirm the task status.");
        }
        Map<String, Object> test = report.getJSONObject("test");
        parseTest(test);
        report.put("data", test);
        report.remove("test");
        return report;
    }


    /**
     * 报告详情页图表信息查询
     *
     * @param testId 任务ID
     * @return 报告信息
     */
    @RequestMapping(value = {"/report/chart"}, method = RequestMethod.GET)
    public JSONObject getAllChart(@RequestParam("test_id") Integer testId) {
        String dataType = "TPS,Errors,Mean_Test_Time_(ms),Mean_time_to_first_byte,User_defined,Vuser";

        HttpEntity<String> reports = perfTestService.getPerfGraphById(testId, dataType, false, IMG_WIDTH);
        if (reports == null) {
            throw new HerculesException("Report data don't found, please confirm the task status.");
        }
        String body = reports.getBody();
        JSONObject perfGraphData = JSONObject.parseObject(body);
        if (perfGraphData == null || perfGraphData.isEmpty()) {
            throw new HerculesException("Report data don't found, please confirm the task status.");
        }
        // 查询任务信息
        int interval = (int) perfGraphData.get("chartInterval");
        return parseChart(interval, getChart(perfGraphData, "TPS"), getChart(perfGraphData, "Mean_Test_Time_ms")
                , getChart(perfGraphData, "Mean_time_to_first_byte"), getChart(perfGraphData, "Vuser")
                , getChart(perfGraphData, "Errors"));
    }

    /**
     * 下载日志文件
     *
     * @param testId  ID
     * @param logName 日志名称
     * @param response 响应
     * @throws Exception 异常
     */
    @RequestMapping(value = "/task/download")
    public void downloadLogByID(@RequestParam("test_id") long testId, @RequestParam("log_name") String logName, HttpServletResponse response) throws Exception {
        if (testId <= 0 || StringUtils.isEmpty(logName)) {
            return;
        }
        JSONObject jsonObject = perfTestService.downloadLogByID(testId, logName);
        if (jsonObject == null || !jsonObject.getBoolean(JSON_RESULT_KEY)) {
            return;
        }
        downloadFile(jsonObject, response);
    }


    /**
     * 测试注释更新
     *
     * @param params 入参
     * @return 更新结果
     */
    @RequestMapping(value = "/task/update", method = RequestMethod.PUT)
    public JSONObject updateLeaveComment(@RequestBody JSONObject params) {
        if (params == null || !params.containsKey("test_id")) {
            return returnError();
        }
        perfTestService.updateLeaveComment(Long.parseLong(params.getString("test_id")), params.getString("test_comment"));
        return returnSuccess();
    }

    /**
     * 压力预估图
     * 1.线程压力图：纵坐标=增量 * 进程数；
     * 2.进程压力图：纵坐标=增量 * 线程数；
     *
     * @param paramsInfo 压力图设置数据信息
     * @return 压力图坐标数据
     */
    @RequestMapping(value = "/task/pressurePrediction", method = RequestMethod.POST)
    public JSONObject pressurePrediction(@RequestBody JSONObject paramsInfo) {
        JSONObject vuserRampUp = new JSONObject();
        List<Map<String, Object>> data = new LinkedList<>();
        vuserRampUp.put("data", data);
        if (paramsInfo == null) {
            return vuserRampUp;
        }

        Map<String, Object> params = (Map<String, Object>) paramsInfo.get("params");
        if (params == null || !params.containsKey("vuser") || !params.containsKey("init_value")
                || !params.containsKey("increment") || !params.containsKey("init_wait")
                || !params.containsKey("growth_interval")) {
            return vuserRampUp;
        }


        int initValue = Integer.parseInt(params.get("init_value").toString());
        int increment = Integer.parseInt(params.get("increment").toString());
        int initWait = Integer.parseInt(params.get("init_wait").toString());
        int growthInterval = Integer.parseInt(params.get("growth_interval").toString());

        // 获取线程数据、进程数
        JSONObject vusers = new JSONObject();
        setProcessAndThreads(vusers, Long.parseLong(params.get("vuser").toString()));
        long processes = vusers.getLong("processes");
//        long threads = vusers.getLong("threads");
        long vuserPerAgent = vusers.getLong("vuserPerAgent");


        // 原点数据
        Map<String, Object> firstPoint = new HashMap<>();
        data.add(firstPoint);
        firstPoint.put("time", 0);
        long initCount = initValue * processes;

        initCount = Math.min(initCount, vuserPerAgent);
        if (initWait > 0) {
            firstPoint.put("time", getRampUpTime(initWait));
        }
        firstPoint.put("pressure", initCount);

        long currentCount = initCount;
        long currentTime = Math.max(initWait, 0);
        while (currentCount < vuserPerAgent) {
            Map<String, Object> point = new HashMap<>();
            data.add(point);
            currentTime += growthInterval;
            currentCount += increment * processes; // 默认显示线程压力图
            point.put("time", getRampUpTime(currentTime));
            point.put("pressure", Math.min(currentCount, vuserPerAgent));
        }

        // 往后延迟一秒的数据
        Map<String, Object> lastPoint = new HashMap<>();
        data.add(lastPoint);
        lastPoint.put("time", getRampUpTime(currentTime + 1000));
        lastPoint.put("pressure", vuserPerAgent);

        return vuserRampUp;
    }

    /**
     * 查询压测任务的标签
     *
     * @param value
     * @return
     */
    @RequestMapping(value = {"/task/tags"}, method = RequestMethod.GET)
    public JSONObject getAllTags(@RequestParam(required = false) String value) {
        JSONObject result = returnSuccess();
        result.put("data", perfTestService.getAllTags(value));
        return result;
    }

    /**
     * 获取最大的可用代理数
     *
     * @return 可用代理数
     */
    @RequestMapping(value = {"/task/maxAgent"}, method = RequestMethod.GET)
    public JSONObject getMaxAgent() {
        JSONObject result = returnSuccess();
        Map<String, Object> agentInfo = getAgentInfo();
        int agentCount = (int) agentInfo.get("agentCount");
        result.put("data", agentCount);
        return result;
    }

    /**
     * 压力图时间坐标轴数据：保留两位小数
     *
     * @param time 时间
     * @return 对应秒数据
     */
    private String getRampUpTime(long time) {
        BigDecimal timeBigDecimal = new BigDecimal(time);
        timeBigDecimal = timeBigDecimal.divide(new BigDecimal(1000), 0, RoundingMode.HALF_UP);
        return String.valueOf(timeBigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
    }

    private JSONObject refreshTestRunningById(long id) {
        HttpEntity<String> stringHttpEntity = perfTestService.refreshTestRunningById(id);
        String body = stringHttpEntity.getBody();
        JSONObject report = JSONObject.parseObject(body);
        JSONObject runningReport = new JSONObject();

        JSONObject test = report.getJSONObject("test");
        parseTest(test);
        JSONArray logs = report.getJSONArray("logs");
        test.put("log_name", logs == null ? Collections.emptyList() : logs);
        runningReport.put("data", test);
        if (!"running".equalsIgnoreCase(test.getString("status"))) {
            test.put("chart", Collections.emptyList());
            return runningReport;
        }
        // 解析实时数据
        Map<String, Object> thisTps = new HashMap<>();
        List<Map<String, Object>> tps = new ArrayList<>();
        JSONObject perf = report.getJSONObject("perf");

        // 获取累计统计
        JSONObject totalStatistics = perf == null ? null : perf.getJSONObject("totalStatistics");
        JSONArray lastSampleStatisticList = perf == null ? null : perf.getJSONArray("lastSampleStatistics");
        if (totalStatistics == null || totalStatistics.isEmpty()
                || lastSampleStatisticList == null || lastSampleStatisticList.isEmpty()
                || StringUtils.isEmpty(test.get("startTime"))) {
            thisTps.put("tps", 0);
            thisTps.put("time", "00:00:00");
            tps.add(thisTps);
            test.put("chart", tps);
            return runningReport;
        }

        // 取第一条数据：目前还未遇到多条数据，暂时适配第一条
        Map<String, Object> lastSampleStatistics = lastSampleStatisticList.getJSONObject(0);
        test.put("tps", ((BigDecimal) totalStatistics.get("TPS")).intValue());
        test.put("tps_peak", ((BigDecimal) totalStatistics.get("Peak_TPS")).intValue());
        test.put("avg_time", ((BigDecimal) totalStatistics.get("Mean_Test_Time_(ms)")).intValue());
        test.put("test_count", ((BigDecimal) totalStatistics.get("Tests")).intValue() + ((BigDecimal) totalStatistics.get("Errors")).intValue());
        test.put("success_count", totalStatistics.get("Tests"));
        test.put("fail_count", totalStatistics.get("Errors"));

        try {
            // 解析实时图表
            Date startTime = test.getDate("startTime");
            Date currentDate = new Date();
            thisTps.put("tps", Double.parseDouble(lastSampleStatistics.get("TPS").toString()));
            thisTps.put("time", getTime((currentDate.getTime() - startTime.getTime()) / 1000));
            tps.add(thisTps);
            test.put("chart", tps);
        } catch (Exception e) {
            LOGGER.error("Time format conversion failed!");
            return returnError();
        }
        return runningReport;
    }

    private JSONObject parseChart(int interval, String tpsStr, String meanTestTimeMsStr, String meanTimeToFirstByteStr, String vUserStr, String errorsStr) {
        if (StringUtils.isEmpty(tpsStr) || tpsStr.trim().length() <= 2
                || StringUtils.isEmpty(meanTestTimeMsStr) || meanTestTimeMsStr.trim().length() <= 2
                || StringUtils.isEmpty(meanTimeToFirstByteStr) || meanTimeToFirstByteStr.trim().length() <= 2
                || StringUtils.isEmpty(vUserStr) || vUserStr.trim().length() <= 2
                || StringUtils.isEmpty(errorsStr) || errorsStr.trim().length() <= 2) {
            return returnError();
        }
        List<Map<String, Object>> allChartInfo = new LinkedList<>();
        tpsStr = tpsStr.replaceAll("null", "0");
        meanTestTimeMsStr = meanTestTimeMsStr.replaceAll("null", "0");
        meanTimeToFirstByteStr = meanTimeToFirstByteStr.replaceAll("null", "0");
        vUserStr = vUserStr.replaceAll("null", "0");
        errorsStr = errorsStr.replaceAll("null", "0");
        String[] tps = tpsStr.substring(1, tpsStr.length() - 1).split(",");
        String[] meanTestTimeMs = meanTestTimeMsStr.substring(1, meanTestTimeMsStr.length() - 1).split(",");
        String[] meanTimeToFirstByte = meanTimeToFirstByteStr.substring(1, meanTimeToFirstByteStr.length() - 1).split(",");
        String[] vUser = vUserStr.substring(1, vUserStr.length() - 1).split(",");
        String[] errors = errorsStr.substring(1, errorsStr.length() - 1).split(",");
        // 就算图表的展示点数，最大不超过maxChartPointCount值
        int total = Math.min(Math.min(tps.length, meanTestTimeMs.length), Math.min(meanTimeToFirstByte.length, vUser.length));
        total = Math.min(total, errors.length);
        if (total == 0) {
            returnError();
        }
        if (total > MAX_CHART_POINT_COUNT) {
            total = MAX_CHART_POINT_COUNT;
        }
        int endTime = interval * (total - 1);
        for (int i = 0; i < total; i++) {
            Map<String, Object> thisChart = new HashMap<>();
            allChartInfo.add(0, thisChart);
            if (endTime < 0) {
                break;
            }
            // 从后取数
            thisChart.put("time", getTime(endTime));
            thisChart.put("tps", Double.parseDouble(tps[endTime / interval]));
            thisChart.put("avg_time", Double.parseDouble(meanTestTimeMs[endTime / interval]));
            thisChart.put("receive_avg", Double.parseDouble(meanTimeToFirstByte[endTime / interval]));
            thisChart.put("vuser", Double.parseDouble(vUser[endTime / interval]));
            thisChart.put("fail_count", Double.parseDouble(errors[endTime / interval]));
            endTime = endTime - interval;

        }
        JSONObject jsonObject = returnSuccess();
        jsonObject.put("data", allChartInfo);
        return jsonObject;
    }

    private String getTime(long time) {
        String format = "%s:%s:%s";
        int seconds = (int)time;
        String hourString = getHourString(seconds);
        String minuteString = getMinuteString(seconds);
        String secondsString = getSecondsString(seconds);
        return String.format(Locale.ENGLISH, format, hourString, minuteString, secondsString);
    }

    private String getChart(JSONObject perfGraphData, String key) {
        List<String> chartList = perfGraphData.get(key) != null ? (List<String>) ((Map) perfGraphData.get(key)).get("data") : null;
        return chartList == null || chartList.isEmpty() ? "" : chartList.get(0);
    }

    private void parseTest(Map<String, Object> test) {
        test.put("status_label", TaskStatus.getShowValue(test.get("status").toString()));
        test.put("status", TaskStatus.getShowValue(test.get("status").toString()));
        test.put("test_name", test.get("testName"));
        Object tagString = test.get("tagString");
        test.put("label", StringUtils.isEmpty(tagString) ? null : tagString.toString().split(","));
        test.remove("tags");
        test.put("desc", test.get("description"));
        test.put("vuser", test.get("vuserPerAgent"));
        test.put("tps_peak", test.get("peakTps"));
        test.put("avg_time", test.get("meanTestTime"));
        Object errors = test.get("errors");
        Object tests = test.get("tests");
        test.put("test_count", toTestTotal(errors == null ? "" : errors.toString(), tests == null ? "" : tests.toString()));
        test.put("success_count", test.get("tests"));
        test.put("fail_count", test.get("errors"));
        test.put("test_comment", test.get("testComment"));
        Object createdDate = test.get("createdDate");
        test.put("createdDate", dateFormat(createdDate == null ? "" : createdDate.toString(), TimeUnit.MILLISECONDS));
        Object lastModifiedDate = test.get("lastModifiedDate");
        test.put("lastModifiedDate", dateFormat(lastModifiedDate == null ? "" : lastModifiedDate.toString(), TimeUnit.MILLISECONDS));
        Object finishTime = test.get("finishTime");
        test.put("finishTime", dateFormat(finishTime == null ? "" : finishTime.toString(), TimeUnit.MILLISECONDS));
        Object scheduledTime = test.get("scheduledTime");
        test.put("scheduledTime", dateFormat(scheduledTime == null ? "" : scheduledTime.toString(), TimeUnit.MILLISECONDS));
        test.put("createdUser", userFormat(test.get("createdUser")));
        test.put("lastModifiedUser", userFormat(test.get("lastModifiedUser")));

        // 详细报告新增
        test.put("sampling_ignore", test.get("ignoreSampleCount"));
        test.put("target_host", test.get("targetHosts"));
        test.put("start_time", test.get("startTime"));
        Object duration = test.get("duration");
        test.put("test_time", getDurationTime(duration == null ? "" : duration.toString()));
        test.put("end_time", test.get("finishTime"));
        test.put("run_time", getDurationTime(duration == null ? "" : duration.toString())); //end_time-startTime 时间相减
        test.put("process", test.get("processes"));
        test.put("thread", test.get("threads"));
        test.put("tps_max", test.get("peakTps"));
        Object progressMessage = test.get("progressMessage");
        test.put("progress_message", parseStrToArray(progressMessage == null ? "" : progressMessage.toString()));

        // 格式化日期格式
        Object startTime = test.get("start_time");
        test.put("start_time", dateFormat(startTime == null ? "" : startTime.toString(), TimeUnit.MILLISECONDS));
        Object endTime = test.get("end_time");
        test.put("end_time", dataFormat(endTime == null ? "" : endTime.toString()));
    }

    /**
     * 格式化用户字符串
     *
     * @param userMap 用户信息map
     * @return 用户信息简略版
     */
    private Map<String, Object> userFormat(Object userMap) {
        if (userMap == null) {
            return Collections.emptyMap();
        }
        if (!(userMap instanceof Map)) {
            return Collections.emptyMap();
        }
        Map<?, ?> userInfo = (Map<?, ?>) userMap;
        Map<String, Object> easyUser = new HashMap<>();
        easyUser.put("Role", userInfo.get("role"));
        easyUser.put("name", userInfo.get("userName"));
        easyUser.put("ID", userInfo.get("id"));
        return easyUser;
    }

    /**
     * 把时间戳根据单位转换成时间字符串
     *
     * @param timestampString 时间戳
     * @param timeUnit        时间单位
     * @return 格式化的时间
     */
    private String dateFormat(String timestampString, TimeUnit timeUnit) {
        try {
            long timeMilliseconds = TimeUnit.MILLISECONDS.convert(Long.parseLong(timestampString), timeUnit);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            return sdf.format(new Date(timeMilliseconds));
        } catch (NumberFormatException e) {
            return "";
        }
    }

    /**
     * 根据代理数，计算进程数、线程数、更新代理数
     *
     * @param perfTestInfos 任务信息
     * @param total         总代理数
     */
    private void setProcessAndThreads(JSONObject perfTestInfos, long total) {
        long processes = getProcessCount(total);
        long threads = total / processes;

        // 前端要求用户显示必须和设置一致，所以这么改，原逻辑是
        perfTestInfos.put("processes", processes);
        perfTestInfos.put("threads", threads);
        perfTestInfos.put("vuserPerAgent", total);
    }

    /**
     * 沿用ngrinder的计算进程数的方法
     *
     * @param total 总代理数
     * @return 进程数
     */
    private long getProcessCount(long total) {
        if (total < 2) {
            return 1;
        }

        long processCount = 2;

        if (total > 80) {
            processCount = (total / 40) + 1;
        }

        if (processCount > 10) {
            processCount = 10;
        }
        return processCount;
    }

    /**
     * 沿用ngrinder的计算线程数的方法
     *
     * @param total 总代理数
     * @return 线程数
     */
    private long getThreadCount(long total) {
        long processCount = getProcessCount(total);
        return total / processCount;
    }

    private JSONObject getScenarioId(JSONObject perfTestInfos, String scenarioName) {

        // 根据压测场景查询脚本
        JSONObject scenario = scenarioService.getPagedAll("", "", "", CUSTOM_SCRIPT, scenarioName, "");
        if (scenario == null) {
            return returnError("关联压测场景不存在");
        }
        JSONObject scenarioListPage = scenario.getJSONObject("scenarioListPage");
        JSONArray files = scenarioListPage.getJSONArray("content");
        if (files != null && !files.isEmpty()) {
            String path = (String) files.getJSONObject(0).get("scriptPath");
            // 判断压测脚本是否存在
            if (!scripService.hasScript(path)) {
                return returnError("压测脚本不存在");
            }
            JSONObject success = returnSuccess();
            perfTestInfos.put("scriptName", files.getJSONObject(0).get("scriptPath"));
            success.put("id", Integer.parseInt(files.getJSONObject(0).get("id").toString()));
            return success;
        }
        return returnError("关联压测场景不存在");
    }

    private void setPerfTestInfos(JSONObject perfTestInfos, JSONObject params) {
        perfTestInfos.put("testName", params.get("test_name"));
        perfTestInfos.put("agentCount", params.get("agent"));
        perfTestInfos.put("tagString", arrayToStr((List<String>) params.get("label")));
        perfTestInfos.put("isClone", false);
        perfTestInfos.put("description", params.get("desc"));
        perfTestInfos.put("targetHosts", getTargetHosts((List<Map<String, Object>>) params.get("hosts")));
        perfTestInfos.put("threshold", getThreshold((String) params.get("basic")));
        perfTestInfos.put("runCount", params.get("by_count"));
        long duration = getLongTime((Integer) params.get("by_time_h"), (Integer) params.get("by_time_m"), (Integer) params.get("by_time_s"));
        perfTestInfos.put("duration", duration);

        // 小时数向上取整
        perfTestInfos.put("durationHour", (duration + 3599999L) / 3600000);
        perfTestInfos.put("samplingInterval", params.get("sampling_interval"));
        perfTestInfos.put("ignoreSampleCount", params.get("sampling_ignore"));
        perfTestInfos.put("safeDistribution", Boolean.parseBoolean(String.valueOf(params.get("is_safe"))) ? "true" : "false");
        perfTestInfos.put("param", params.get("test_param"));
        perfTestInfos.put("useRampUp", Boolean.parseBoolean(String.valueOf(params.get("is_increased"))) ? "true" : "false");
        perfTestInfos.put("rampUpType", "线程".equals(params.get("concurrency")) ? "THREAD" : "PROCESS");
        perfTestInfos.put("rampUpInitCount", params.get("init_value"));
        perfTestInfos.put("rampUpStep", params.get("increment"));
        perfTestInfos.put("rampUpInitSleepTime", params.get("init_wait"));
        perfTestInfos.put("rampUpIncrementInterval", params.get("growth_interval"));
        perfTestInfos.put("status", Boolean.parseBoolean(String.valueOf(params.get("run"))) ? "READY" : "SAVED");
        perfTestInfos.put("monitoringHosts", params.get("monitoring_hosts"));
    }


    private String getThreshold(String basic) {
        if ("by_time".equals(basic)) {
            return "D";
        } else if ("by_count".equals(basic)) {
            return "R";
        }
        return "";
    }

    private String getTargetHosts(List<Map<String, Object>> hosts) {
        if (hosts == null || hosts.isEmpty()) {
            return "";
        }
        StringJoiner sj = new StringJoiner(",");
        for (Map<String, Object> host : hosts) {
            String domain = (String) host.get("domain");
            String ip = (String) host.get("ip");

            sj.add(getHost(domain, ip));
        }
        return sj.toString();
    }

    private String getDurationTime(String duration) {
        if (StringUtils.isEmpty(duration)) {
            return null;
        }
        long time = Long.parseLong(duration);
        long hours = time / (1000 * 60 * 60);
        long minutes = (time - hours * (1000 * 60 * 60)) / (1000 * 60);
        long second = (time - hours * (1000 * 60 * 60) - minutes * 1000 * 60) / 1000;
        String diffTime;
        if (minutes < 10) {
            diffTime = hours + ":0" + minutes;
        } else {
            diffTime = hours + ":" + minutes;
        }
        if (second < 10) {
            diffTime = diffTime + ":0" + second;
        } else {
            diffTime = diffTime + ":" + second;
        }
        return diffTime;
    }

    private long getLongTime(Integer hours, Integer minutes, Integer seconds) {
        if (hours == null || minutes == null || seconds == null) {
            return 0L;
        }
        return ((hours * 60L + minutes) * 60L + seconds) * 1000L;
    }

    private String toPercent(String errors, String tests) {
        if (StringUtils.isEmpty(errors)) {
            return "";
        }
        try {
            int a = Integer.parseInt(errors);
            int b = Integer.parseInt(tests);
            if (a % b == 0) {
                return a / b * 100 + "%";
            } else {
                return (double) Math.round(a / (double) b * 10000) / 100 + "%";
            }
        } catch (Exception e) {
            return "";
        }
    }

    private String toTestTotal(String errors, String tests) {
        if (StringUtils.isEmpty(errors)) {
            return tests;
        }
        if (StringUtils.isEmpty(tests)) {
            return errors;
        }
        return String.valueOf(Double.parseDouble(errors) + Double.parseDouble(tests));
    }

    public Map<String, Object> getAgentInfo() {
        Map<String, Object> result = new HashMap<>();
        JSONObject defaultInfos = perfTestService.openForm();
        int agentCount = 0;
        result.put("agentCount", agentCount);
        if (defaultInfos == null) {
            return result;
        }
        JSONObject test = defaultInfos.getJSONObject("test");
        JSONObject regionAgentCountMap = defaultInfos.getJSONObject("regionAgentCountMap");
        if (test == null || test.isEmpty() || regionAgentCountMap == null || regionAgentCountMap.isEmpty()) {
            return result;
        }
        result.put("agentCount", regionAgentCountMap.get("NONE"));
        result.put("scriptRevision", test.get("scriptRevision"));
        return result;
    }

    private JSONArray getDefaultTps(int needAddedDuration, int interval, int totalDuration) {
        JSONArray tpsInfos = new JSONArray();
        for (int i = 0; i < Math.abs(needAddedDuration); i++) {
            if (i % interval != 0) {
                continue;
            }
            Map<String, Object> thisTps = new HashMap<>();
            thisTps.put("tps", 0); // 缺省值补0
            String format = "-%s:%s:%s";
            int timeIndex = totalDuration - 1 - i;
            if (timeIndex == 0) {
                thisTps.put("time", "00:00:00");
            } else {
                String time = String.format(Locale.ENGLISH, format,
                        getHourString(timeIndex), getMinuteString(timeIndex), getSecondsString(timeIndex));
                thisTps.put("time", time);
            }
            tpsInfos.add(thisTps);
        }
        return tpsInfos;
    }

    private String getHourString(int seconds) {
        int hour = seconds / (60 * 60);
        return hour < 10 ? "0" + hour : hour + "";
    }

    private String getMinuteString(int seconds) {
        int minute = (seconds % (60 * 60)) / 60;
        return minute < 10 ? "0" + minute : minute + "";
    }

    private String getSecondsString(int seconds) {
        int modSeconds = seconds % 60;
        return modSeconds < 10 ? "0" + modSeconds : modSeconds + "";
    }

    private void initPerfTestFieldsValue(Map<String, Object> perfTestInfos, int agentCount) {
        perfTestInfos.put("status", "READY");
        perfTestInfos.put("agentCount", agentCount);
        String[] keys = new String[]{"testComment", "finishTime", "lastProgressMessage", "port", "progressMessage", "startTime"};
        removeKeys(perfTestInfos, keys);
    }

    private void clearPerfTestOldFieldsValue(Map<String, Object> perfTestInfos) {
        String[] keys = new String[]{"errors", "finishTime", "lastProgressMessage", "meanTestTime", "peakTps", "scheduledTime",
                "startTime", "stopRequest", "testErrorCause", "testTimeStandardDeviation", "tests", "tps", "agentState",
                "runningSample", "monitorState", "agentIds", "monitoringConfig", "perfScene"};
        removeKeys(perfTestInfos, keys);
    }

    private void saveScenarioPerfTest(int scenarioId, int perfTestId) {
        JSONObject scenarioPerfTest = new JSONObject();
        scenarioPerfTest.put("scenarioId", scenarioId);
        scenarioPerfTest.put("perfTestId", perfTestId);
        scenarioService.saveScenarioPerfTest(scenarioPerfTest.toString());
    }
}
