package com.huawei.hercules.controller.perftest;

import com.alibaba.fastjson.JSONObject;
import com.huawei.hercules.controller.BaseController;
import com.huawei.hercules.service.perftest.IPerftestService;
import com.huawei.hercules.service.scenario.IScenarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/api")
public class PerftestController extends BaseController {

    Logger logger = LoggerFactory.getLogger(PerftestController.class);

    private final int imgWidth = 600;
    private final int maxChartPointCount = 10000;

    @Autowired
    private IPerftestService perftestService;

    @Autowired
    private IScenarioService scenarioService;

    /**
     * key的对应关系
     **/
    private static Map<String, String> perfTestKeys = new HashMap<>();

    /**
     * 脚本运行状态
     **/
    private static Map<String, String> perfTestStatus = new HashMap<>();

    static {
        getKeys();
        getStatus();
    }

    /**
     * 查询任务列表
     *
     * @param keywords       关键字
     * @param keywords_scope 标签
     * @param status         状态
     * @param pageSize       页数
     * @param current        页码
     * @param sorter         排序关键字
     * @param order          排序
     * @return 任务列表信息
     */
    @RequestMapping(value = {"/task"}, method = RequestMethod.GET)
    public JSONObject getAll(@RequestParam(required = false) String keywords,
                             @RequestParam(required = false) String keywords_scope, @RequestParam(required = false, name = "status[]") String[] status,
                             @RequestParam(required = false, defaultValue = "10") int pageSize,
                             @RequestParam(required = false, defaultValue = "1") int current,
                             @RequestParam(required = false) String sorter,
                             @RequestParam(required = false) String order) {

        // 1.查询条件的转换
        JSONObject pagesInfo = new JSONObject();
        pagesInfo.put("size", pageSize);
        pagesInfo.put("page", current == 0 ? 0 : current - 1);
        if (!StringUtils.isEmpty(sorter) && !StringUtils.isEmpty(order) && perfTestKeys.containsKey(sorter)) {
            StringJoiner sj = new StringJoiner(",");
            sj.add(perfTestKeys.get(sorter)).add(getOrder(order));
            pagesInfo.put("sort", sj.toString());
        }
        String queryFilter = arrayToStr(status).replaceAll("running", "R").replaceAll("pending", "S");

        // 2. 查询结果
        JSONObject result = perftestService.getAll(keywords, keywords_scope, queryFilter, pagesInfo.toString());

        // 3.结果适配
        if (result != null) {
            Map<String, Object> testListPage = (Map<String, Object>) result.get("testListPage");
            List<Map<String, Object>> files = (List<Map<String, Object>>) testListPage.get("content");
            result.put("total", testListPage.get("total"));
            for (Map<String, Object> file : files) {
                file.put("test_name", file.get(perfTestKeys.get("test_name")));
                file.put("test_type", CUSTOM_SCRIPT/*file.get(perfTestKeys.get("test_type"))*/);
                file.put("test_id", file.get(perfTestKeys.get("test_id")));
                file.put("script_path", file.get(perfTestKeys.get("script_path")));
                Map<String, Object> createdUser = (Map<String, Object>) file.get("createdUser");
                file.put("owner", createdUser.get(perfTestKeys.get("owner")));
                file.put("start_time", file.get(perfTestKeys.get("start_time")));
                file.put("duration", getDurationTime((String) file.get("duration")));
//                file.put("tps", file.get("tps"));
                file.put("mtt", file.get(perfTestKeys.get("mtt")));
                file.put("fail_rate", toPercent((String) file.get("errors"), (String) file.get("tests"))); // errors / tests
                file.put("status", perfTestStatus.get(file.get("status").toString()));

                // 格式化日期格式
                file.put("start_time", dataFormat((String)file.get("start_time")));
            }
            result.put("data", files);
        }
        return result;
    }

    /**
     * 删除任务
     *
     * @param test_id 任务ID
     * @return 删除结果
     */
    @RequestMapping(value = "/task", method = RequestMethod.DELETE)
    public HttpEntity<String> delete(@RequestParam(defaultValue = "") String test_id) {
        return perftestService.delete(test_id);
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

        // 1.查询数据ngrinder原生数据，获取perfTest默认值
        Map<String, Object> agentInfo = getAgentInfo();
        JSONObject perfTestInfos = new JSONObject();
        perfTestInfos.putAll(agentInfo);
        // 通过查询获取默认值
        JSONObject scenario = getDefaultParams(perfTestInfos, params.getString("scenario_name"));
        if (scenario == null) {
            return returnError();
        }
        // 转换数据参数
        getPerfTestInfos(perfTestInfos, params);
        // 如果是运行，没有agent数，直接返回并提醒
        if (Boolean.parseBoolean(String.valueOf(params.get("run"))) && (int) agentInfo.get("agentCount") <= 0) {
            return returnError("可运行的代理agent数为0，不能执行压测任务");
        }

        // 计算进程数和线程数，更新虚拟用户数
        getProcessAndThreads(perfTestInfos, params.getLong("vuser"));
        // 保存
        JSONObject jsonObject = perftestService.saveOne(perfTestInfos.toString(), false);

        // 保存成功之后保存任务与场景的关系
        Integer scenarioId = (Integer) scenario.get("id");
        Integer perfTestId = (Integer) jsonObject.get("id");
        JSONObject scenarioPerfTest = new JSONObject();
        scenarioPerfTest.put("scenarioId", scenarioId);
        scenarioPerfTest.put("perfTestId", perfTestId);
        scenarioService.saveScenarioPerfTest(scenarioPerfTest.toString());
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
        int agentCount = (int) agentInfo.get("agentCount");
        if ((int) agentInfo.get("agentCount") <= 0) {
            return returnError("可运行的代理agent数为0，不能执行压测任务");
        }

        String testId = (String) params.get("test_id");

        JSONObject testInfo = perftestService.getOne(Long.parseLong(testId));
        Map<String, Object> perfTestInfos = (Map<String, Object>) testInfo.get("test");
        if (perfTestInfos == null || perfTestInfos.isEmpty()) {
            return returnError("压测任务不存在");
        }
        AtomicBoolean isClone = new AtomicBoolean(true);
        if ("SAVED".equalsIgnoreCase((String) perfTestInfos.get("status"))) {
            // 删除
//            perftestService.deleteReportFile(testId);
            // 如果未运行的脚本，则直接运行
            isClone.set(false);
        }
        // 执行保存并运行接口
        perfTestInfos.put("status", "READY");
        perfTestInfos.put("agentCount", agentCount);
        perfTestInfos.put("testComment", "");
        perfTestInfos.put("finishTime", "");
        perfTestInfos.put("lastProgressMessage", "");
        perfTestInfos.put("port", "");
        perfTestInfos.put("progressMessage", "");
        perfTestInfos.put("startTime", "");
        JSONObject jsonObject = perftestService.saveOne(JSONObject.toJSONString(perfTestInfos), isClone.get());

        // 保存成功之后保存任务与场景的关系
        JSONObject allByPerfTestId = scenarioService.getAllByPerfTestId(Long.parseLong(testId));
        if (allByPerfTestId != null && isClone.get()) {
            long scenarioId = allByPerfTestId.getLong("id");
            long perfTestId = jsonObject.getInteger("id");
            JSONObject scenarioPerfTest = new JSONObject();
            scenarioPerfTest.put("scenarioId", scenarioId);
            scenarioPerfTest.put("perfTestId", perfTestId);
            scenarioService.saveScenarioPerfTest(scenarioPerfTest.toString());
        }

        // 休眠5s
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        String testId = (String) params.get("test_id");
        perftestService.stop(testId);
        return returnSuccess();
    }

    /**
     * 查询测试报告
     *
     * @param test_id  任务ID
     * @param start    开始坐标
     * @param interval 间隔
     * @return 报告信息
     */
    @RequestMapping(value = "/task/get", method = RequestMethod.GET)
    public JSONObject get(@RequestParam String test_id, @RequestParam(required = false) Integer start, @RequestParam(required = false) Integer interval) {
        if (StringUtils.isEmpty(test_id)) {
            return returnError();
        }
        if (start == null || interval == null) {
            // 查询实时数据
            return refreshTestRunningById(Long.parseLong(test_id));
        }

        JSONObject report = perftestService.getReportSectionById(Long.parseLong(test_id), imgWidth, start, interval);
        if (report == null) {
            return returnError();
        }
        Map<String, Object> test = (Map<String, Object>) report.get("test");
        parseTest(test);
        List<Object> logs = (List<Object>) report.get("logs");
        test.put("log_name", logs == null ? new ArrayList<>(0) : logs);
        List<Map<String, Object>> thisTps = (List<Map<String, Object>>) report.get("TPS");
        if (thisTps == null || thisTps.isEmpty()) {
            thisTps = getDefaultTps();
        }
        test.put("chart", thisTps);
        report.put("data", test);
        return report;
    }


    /**
     * 报告详情页基本信息查询
     *
     * @param test_id 任务ID
     * @return 报告结果
     */
    @RequestMapping(value = {"/report/get"}, method = RequestMethod.GET)
    public JSONObject getReports(@RequestParam String test_id) {
        JSONObject report = perftestService.getReportById(Long.parseLong(test_id));
        Map<String, Object> test = (Map<String, Object>) report.get("test");
        parseTest(test);
        report.put("data", test);
        return report;
    }


    /**
     * 报告详情页图表信息查询
     *
     * @param test_id 任务ID
     * @return 报告信息
     */
    @RequestMapping(value = {"/report/chart"}, method = RequestMethod.GET)
    public JSONObject getAllChart(@RequestParam Integer test_id) {
        String dataType = "TPS,Errors,Mean_Test_Time_(ms),Mean_time_to_first_byte,User_defined,Vuser";

        HttpEntity<String> reports = perftestService.getPerfGraphById(test_id, dataType, false, imgWidth);
        String body = reports.getBody();
        JSONObject perfGraphData = JSONObject.parseObject(body);
        // 查询任务信息
        int interval = (int) perfGraphData.get("chartInterval");
        return parseChart(interval, getChart(perfGraphData, "TPS"), getChart(perfGraphData, "Mean_Test_Time_ms")
                , getChart(perfGraphData, "Mean_time_to_first_byte"), getChart(perfGraphData, "Vuser")
                , getChart(perfGraphData, "Errors"));
    }

    @RequestMapping(value = "/task/download")
    public void downloadLogByID(@RequestParam long test_id, @RequestParam String log_name, HttpServletResponse response) throws Exception {
        if (test_id <= 0 || StringUtils.isEmpty(log_name)) {
            return;
        }
        JSONObject jsonObject = perftestService.downloadLogByID(test_id, log_name);
        if (jsonObject == null || !jsonObject.getBoolean(JSON_RESULT_KEY)) {
            return;
        }
        downloadFile(jsonObject, response);
    }


    @RequestMapping(value = "/task/update", method = RequestMethod.PUT)
    public JSONObject updateLeaveComment(@RequestBody JSONObject params) {
        if (params == null || !params.containsKey("test_id")) {
            return returnError();
        }
        perftestService.updateLeaveComment(Long.parseLong(params.getString("test_id")), params.getString("test_comment"));
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
        getProcessAndThreads(vusers, Long.parseLong(params.get("vuser").toString()));
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
            firstPoint.put("pressure", initCount);
        } else {
            firstPoint.put("pressure", initCount);
        }

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
     * 压力图时间坐标轴数据：保留两位小数
     *
     * @param time 时间
     * @return 对应秒数据
     */
    private String getRampUpTime(long time) {
        BigDecimal timeBigDecimal = new BigDecimal(time);
        timeBigDecimal = timeBigDecimal.divide(new BigDecimal(1000));
        return String.valueOf(timeBigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
    }

    private JSONObject refreshTestRunningById(long id) {
        HttpEntity<String> stringHttpEntity = perftestService.refreshTestRunningById(id);
        String body = stringHttpEntity.getBody();
        JSONObject report = JSONObject.parseObject(body);
        JSONObject runningReport = new JSONObject();

        Map<String, Object> test = (Map<String, Object>) report.get("test");
        parseTest(test);
        List<Object> logs = (List<Object>) report.get("logs");
        test.put("log_name", logs == null ? new ArrayList<>(0) : logs);
        runningReport.put("data", test);
        if (!"running".equals(test.get("status"))) {
            return runningReport;
        }
        // 解析实时数据
        Map<String, Object> perf = (Map<String, Object>) report.get("perf");
        // 获取累计统计数
        Map<String, Object> thisTps = new HashMap<>();
        thisTps.put("tps", 0);
        thisTps.put("time", "00:00");
        List<Map<String, Object>> tps = new ArrayList();
        tps.add(thisTps);
        test.put("chart", tps);

        Map<String, Object> totalStatistics = perf == null ? null : (Map<String, Object>) perf.get("totalStatistics");
        List<Map<String, Object>> lastSampleStatisticList = perf == null ? null : (List<Map<String, Object>>) perf.get("lastSampleStatistics");
        if (totalStatistics == null || totalStatistics.isEmpty()
                || lastSampleStatisticList == null || lastSampleStatisticList.isEmpty()
                || StringUtils.isEmpty(test.get("startTime"))) {
            return runningReport;
        }
        Map<String, Object> lastSampleStatistics = lastSampleStatisticList.get(0); // 取第一条数据：目前还未遇到多条数据，暂时适配第一条
        test.put("tps", ((BigDecimal) totalStatistics.get("TPS")).intValue());
        test.put("tps_peak", ((BigDecimal) totalStatistics.get("Peak_TPS")).intValue());
        test.put("avg_time", ((BigDecimal) totalStatistics.get("Mean_Test_Time_(ms)")).intValue());
        test.put("test_count", ((BigDecimal) totalStatistics.get("Tests")).intValue() + ((BigDecimal) totalStatistics.get("Errors")).intValue());
        test.put("success_count", totalStatistics.get("Tests"));
        test.put("fail_count", totalStatistics.get("Errors"));

        try {
            // 解析实时图表
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date startTime = format.parse(test.get("startTime").toString());
            Date currentDate = new Date();
            thisTps.put("tps", Double.parseDouble(lastSampleStatistics.get("TPS").toString()));
            thisTps.put("time", getTime((currentDate.getTime() - startTime.getTime()) / 1000));
            tps.add(thisTps);
            test.put("chart", tps);
        } catch (Exception e) {
            logger.error("Time format conversion failed!");
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
        if (total > maxChartPointCount) {
            total = maxChartPointCount;
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
        StringBuilder sb = new StringBuilder();
        long minutes = time / 60;
        if (minutes < 10) {
            sb.append("0");
        }
        sb.append(minutes).append(":");

        long second = time % 60;
        if (second < 10) {
            sb.append("0");
        }
        sb.append(second);
        return sb.toString();
    }

    private String getChart(JSONObject perfGraphData, String key) {
        List<String> chartList = perfGraphData.get(key) != null ? (List<String>) ((Map) perfGraphData.get(key)).get("data") : null;
        return chartList == null || chartList.isEmpty() ? "" : chartList.get(0);
    }

    private void parseTest(Map<String, Object> test) {
        test.put("status_label", perfTestStatus.get(test.get("status").toString()));
        test.put("status", perfTestStatus.get(test.get("status").toString()));
        test.put("test_name", test.get("testName"));
        test.put("label", StringUtils.isEmpty(test.get("tagString")) ? null : test.get("tagString").toString().split(","));
        test.put("desc", test.get("description"));
        test.put("vuser", test.get("vuserPerAgent"));
        test.put("tps_peak", test.get("peakTps"));
        test.put("avg_time", test.get("meanTestTime"));
        test.put("test_count", toTestTotal((String) test.get("tests"), (String) test.get("errors")));
        test.put("success_count", test.get("tests"));
        test.put("fail_count", test.get("errors"));
        test.put("test_comment", test.get("testComment"));

        // 详细报告新增
        test.put("sampling_ignore", test.get("ignoreSampleCount"));
        test.put("target_host", test.get("targetHosts"));
        test.put("start_time", test.get("startTime"));
        test.put("test_time", getDurationTime((String) test.get("duration")));
        test.put("end_time", test.get("finishTime"));
        test.put("run_time", getDurationTime((String) test.get("duration"))); //end_time-startTime 时间相减
        test.put("process", test.get("processes"));
        test.put("thread", test.get("threads"));
        test.put("tps_max", test.get("peakTps"));

        // 格式化日期格式
        test.put("start_time", dataFormat((String)test.get("start_time")));
        test.put("end_time", dataFormat((String)test.get("end_time")));
    }

    /**
     * 根据代理数，计算进程数、线程数、更新代理数
     *
     * @param perfTestInfos 任务信息
     * @param total 总代理数
     */
    private void getProcessAndThreads(JSONObject perfTestInfos, long total) {
        long processes = getProcessCount(total);
        long threads = total / processes;
        // 根据vuserPerAgent计算的
        perfTestInfos.put("processes", processes);
        perfTestInfos.put("threads", threads);
        perfTestInfos.put("vuserPerAgent", processes * threads);
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

    private JSONObject getDefaultParams(JSONObject perfTestInfos, String scenarioName) {

        // 根据压测场景查询脚本
        JSONObject scenario = scenarioService.getPagedAll("", "", "", CUSTOM_SCRIPT, scenarioName, "");
        if (scenario == null) {
            return null;
        }
        Map<String, Object> scenarioListPage = (Map<String, Object>) scenario.get("scenarioListPage");
        List<Map<String, Object>> files = (List<Map<String, Object>>) scenarioListPage.get("content");
        if (files != null || !files.isEmpty()) {
            perfTestInfos.put("scriptName", files.get(0).get("scriptPath"));
            scenario.put("id", Integer.parseInt(files.get(0).get("id").toString()));
            return scenario;
        }
        return null;
    }

    private void getPerfTestInfos(JSONObject perfTestInfos, JSONObject params) {
        perfTestInfos.put("testName", params.get("test_name"));
        perfTestInfos.put("tagString", arrayToStr((List<String>) params.get("label")));
        perfTestInfos.put("isClone", false);
        perfTestInfos.put("description", params.get("desc"));
        perfTestInfos.put("targetHosts", getTargetHosts((List<Map<String, Object>>) params.get("hosts")));
        perfTestInfos.put("threshold", getThreshold((String) params.get("basic")));
        perfTestInfos.put("runCount", params.get("by_count"));
        long duration = getLongTime((Integer) params.get("by_time_h"), (Integer) params.get("by_time_m"), (Integer) params.get("by_time_s"));
        perfTestInfos.put("duration", duration);
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

    private static void getKeys() {
        if (perfTestKeys.isEmpty()) {
            perfTestKeys.put("test_id", "id");
            perfTestKeys.put("test_name", "testName");
            perfTestKeys.put("test_type", "scenarioType");
            perfTestKeys.put("script_path", "scriptName");
            perfTestKeys.put("owner", "name");
            perfTestKeys.put("start_time", "startTime");
            perfTestKeys.put("duration", "duration");
            perfTestKeys.put("tps", "tps");
            perfTestKeys.put("mtt", "meanTestTime");
            perfTestKeys.put("fail_rate", "errors");
        }
    }

    private static void getStatus() {
        if (perfTestStatus.isEmpty()) {
            perfTestStatus.put("CANCELED", "fail");
            perfTestStatus.put("FINISHED", "success");
            perfTestStatus.put("READY", "pending");
            perfTestStatus.put("SAVED", "pending");
            perfTestStatus.put("STOP_BY_ERROR", "fail");
            perfTestStatus.put("TESTING", "running");
        }
    }

    public Map<String, Object> getAgentInfo() {
        Map<String, Object> result = new HashMap<>();
        JSONObject defaultInfos = perftestService.openForm();
        int agentCount = 0;
        result.put("agentCount", agentCount);
        if (defaultInfos == null) {
            return result;
        }
        Map<String, Object> test = (Map<String, Object>) defaultInfos.get("test");
        Map<String, Object> regionAgentCountMap = (Map<String, Object>) defaultInfos.get("regionAgentCountMap");
        if (test == null || test.isEmpty() || regionAgentCountMap == null || regionAgentCountMap.isEmpty()) {
            return result;
        }
        result.put("agentCount", regionAgentCountMap.get("NONE"));
        result.put("scriptRevision", test.get("scriptRevision"));
        return result;
    }

    private List<Map<String, Object>> getDefaultTps() {
        List<Map<String, Object>> tps = new ArrayList<>();
        for (int i = 0; i < 90; i++) {
            Map<String, Object> thisTps = new HashMap<>();
            thisTps.put("tps", 0); // 缺省值补0
            thisTps.put("time", "00:00");
            tps.add(thisTps);
        }
        return tps;
    }
}
