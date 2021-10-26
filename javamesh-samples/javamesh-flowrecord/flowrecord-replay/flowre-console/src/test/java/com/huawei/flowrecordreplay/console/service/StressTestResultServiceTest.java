package com.huawei.flowrecordreplay.console.service;

import com.huawei.flowrecordreplay.console.datasource.elasticsearch.EsDataSource;
import com.huawei.flowrecordreplay.console.datasource.entity.stresstest.FlowReplayMetric;
import com.huawei.flowrecordreplay.console.util.Constant;

import com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSON;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-07-27
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class StressTestResultServiceTest {
    @Autowired
    StressTestResultService stressTestResultService;

    @MockBean
    EsDataSource esDataSource;

    @Test
    public void getFlowReplayMetricList() throws IOException {
        List<String> replayMetricStrings = new ArrayList<>();
        FlowReplayMetric flowReplayMetric = new FlowReplayMetric("worker_name", "job_id", new Date().getTime(), 1000, 50);
        replayMetricStrings.add(JSON.toJSONString(flowReplayMetric));
        Mockito.when(esDataSource.searchByKey(Constant.REPLAY_METRIC, "replayJobId", "job_id")).thenReturn(replayMetricStrings);
        List<FlowReplayMetric> flowReplayMetrics = stressTestResultService.getFlowReplayMetricList("job_id");
        Assert.assertEquals(1, flowReplayMetrics.size());
    }
}