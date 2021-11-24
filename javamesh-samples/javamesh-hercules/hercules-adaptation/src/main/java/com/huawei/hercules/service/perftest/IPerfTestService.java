package com.huawei.hercules.service.perftest;

import com.alibaba.fastjson.JSONObject;
import com.huawei.hercules.config.FeignRequestInterceptor;
import com.huawei.hercules.fallback.PerfTestServiceFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(url = "${controller.engine.url}" + "/rest/perftest",
        name = "perftest",
        fallbackFactory = PerfTestServiceFallbackFactory.class,
        configuration = FeignRequestInterceptor.class
)
public interface IPerfTestService {

    /**
     * 任务列表查询
     *
     * @param query       关键字
     * @param tag         标签
     * @param queryFilter 筛选条件
     * @param pages       分页信息
     * @return 任务列表
     */
    @RequestMapping({"/alllist"})
    JSONObject getAll(@RequestParam(required = false) String query,
                      @RequestParam(required = false) String tag, @RequestParam(required = false) String queryFilter,
                      @RequestParam(required = false) String pages,
                      @RequestParam(required = false) String testName,
                      @RequestParam(required = false) String testType,
                      @RequestParam(required = false) String scriptPath,
                      @RequestParam(required = false) String owner);

    /**
     * 任务删除
     *
     * @param ids 任务ID
     * @return 删除状态
     */
    @RequestMapping(value = "/api", method = RequestMethod.DELETE)
    HttpEntity<String> delete(@RequestParam(value = "ids", defaultValue = "") String ids);

    /**
     * / * 任务报告文件删除
     *
     * @param ids 任务ID
     * @return 删除状态
     */
    @RequestMapping(value = "/deleteReportFile", method = RequestMethod.DELETE)
    HttpEntity<String> deleteReportFile(@RequestParam(value = "ids", defaultValue = "") String ids);


    /**
     * 新建任务
     *
     * @param perfTestInfos 任务信息
     * @param isClone       是否复制克隆
     * @return 新建结果
     */
    @RequestMapping(value = "/newPerfTest", method = RequestMethod.POST)
    JSONObject saveOne(@RequestParam String perfTestInfos,
                       @RequestParam(value = "isClone", required = false, defaultValue = "false") boolean isClone);

    /**
     * 查询所有脚本路径信息
     *
     * @param ownerId 所属人，默认不传
     * @return 脚本信息
     */
    @RequestMapping("/api/script")
    HttpEntity<String> getScripts(@RequestParam(value = "ownerId", required = false) String ownerId);

    /**
     * 查询脚本资源文件
     *
     * @param scriptPath 脚本路径
     * @param ownerId    所属人，默认不传
     * @return 查询脚本资源信息
     */
    @RequestMapping("/api/resource")
    HttpEntity<String> getResources(@RequestParam String scriptPath,
                                    @RequestParam(required = false) String ownerId);

    /**
     * 查询或者创建任务
     *
     * @param id 任务ID
     * @return 任务信息
     */
    @RequestMapping("/perfTestId")
    JSONObject getOne(@RequestParam Long id);

    /**
     * 新建任务查询默认值
     *
     * @return 新建任务结果
     */
    @RequestMapping("/new")
    JSONObject openForm();

    /**
     * 查询报告+TPS
     *
     * @param id 报告ID
     * @return 报告实时信息
     */
    @RequestMapping(value = {"/basic_report"})
    JSONObject getReportSectionById(@RequestParam long id, @RequestParam int imgWidth, @RequestParam int thisDuration, @RequestParam int timeInterval);

    /**
     * 查询报告基本信息
     *
     * @param id 报告ID
     * @return 报告详细信息
     */
    @RequestMapping(value = {"/detail_report"})
    JSONObject getReportById(@RequestParam long id);

    @RequestMapping({"/api/perf"})
    HttpEntity<String> getPerfGraphById(@RequestParam("id") long id, @RequestParam(defaultValue = "") String dataType, @RequestParam(defaultValue = "false") boolean onlyTotal, @RequestParam int imgWidth);

    /**
     * 运行时数据
     *
     * @param id 任务ID
     * @return 实时测试采集数据
     */
    @RequestMapping(value = "/api/sample")
    HttpEntity<String> refreshTestRunningById(@RequestParam long id);

    /**
     * 停止任务
     *
     * @param ids 任务ID
     * @return 停止结果
     */
    @RequestMapping(value = "/api/stop", method = RequestMethod.PUT)
    HttpEntity<String> stop(@RequestParam(value = "ids", defaultValue = "") String ids);

    /**
     * 下载日志文件
     *
     * @param id   任务ID
     * @param path 日志路径
     * @return 日志内容信息
     */
    @RequestMapping(value = "/download_log")
    JSONObject downloadLogByID(@RequestParam long id, @RequestParam String path);

    /**
     * 测试注释更新
     *
     * @param id          任务ID
     * @param testComment 测试注释信息
     * @return 更新结果
     */
    @RequestMapping(value = "/leave_comment", method = RequestMethod.POST)
    String updateLeaveComment(@RequestParam Long id, @RequestParam("testComment") String testComment);

    /**
     * 查询压测任务的标签
     * @param query 查询关键字
     * @return 标签列表
     */
    @RequestMapping({"/allTags"})
    List<String> getAllTags(@RequestParam(required = false) String query);
}
