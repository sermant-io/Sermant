package com.huawei.flowrecordreplay.console.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.huawei.flowrecordreplay.console.datasource.elasticsearch.ElasticSearchIndicesInit;
import com.huawei.flowrecordreplay.console.datasource.elasticsearch.EsDataSource;
import com.huawei.flowrecordreplay.console.datasource.elasticsearch.EsDataSourceAggregate;
import com.huawei.flowrecordreplay.console.datasource.entity.replayresult.IgnoreFieldEntity;
import com.huawei.flowrecordreplay.console.datasource.entity.replayresult.ReplayInterfaceCountEntity;
import com.huawei.flowrecordreplay.console.datasource.entity.replayresult.ReplayResultCountEntity;
import com.huawei.flowrecordreplay.console.datasource.entity.replayresult.ReplayResultDetailEntity;
import com.huawei.flowrecordreplay.console.datasource.entity.replayresult.ReplayResultEntity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

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
import java.util.List;

/**
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-04-13
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ReplayResultServiceTest {
    /**
     * 忽略字段表index
     */
    private static final String IGNORE_FIELDS_INDEX = "ignore_fields";

    /**
     * 回放结果表index前缀
     */
    private static final String REPLAY_RESULT_PREFIX = "replay_result_";

    @Autowired
    ReplayResultService replayResultService;

    @MockBean
    EsDataSource esDataSource;

    @MockBean
    EsDataSourceAggregate esDataSourceAggregate;

    @MockBean
    ElasticSearchIndicesInit elasticSearchIndicesInit;

    @Test
    public void getReplayOverview() throws IOException {
        String jobId = "jobId";
        List<ReplayInterfaceCountEntity> replayInterfaceCountEntities = new ArrayList<>();
        ReplayInterfaceCountEntity replayInterfaceCountEntity = new ReplayInterfaceCountEntity();
        replayInterfaceCountEntity.setMethod("method");
        replayInterfaceCountEntity.setFailureCount(1);
        replayInterfaceCountEntity.setSuccessCount(1);
        replayInterfaceCountEntity.setTotal(2);
        replayInterfaceCountEntities.add(replayInterfaceCountEntity);
        replayInterfaceCountEntities.add(replayInterfaceCountEntity);
        replayInterfaceCountEntities.add(replayInterfaceCountEntity);
        IOException ioException = new IOException();
        Mockito.when(esDataSourceAggregate.replayInterfaceCount(jobId)).thenReturn(replayInterfaceCountEntities).thenThrow(ioException);
        ReplayResultCountEntity replayResultCountEntity = replayResultService.getReplayOverview(jobId);
        assertEquals(6, replayResultCountEntity.getReplayTotal());
        assertEquals(3, replayResultCountEntity.getReplaySuccessCount());
        assertEquals(3, replayResultCountEntity.getReplayFailureCount());
        assertFalse(replayResultCountEntity.isReCompareStatus());

        replayResultCountEntity = replayResultService.getReplayOverview(jobId);
        assertEquals(0, replayResultCountEntity.getReplayTotal());
        assertEquals(0, replayResultCountEntity.getReplaySuccessCount());
        assertEquals(0, replayResultCountEntity.getReplayFailureCount());
        assertFalse(replayResultCountEntity.isReCompareStatus());
    }

    @Test
    public void getReplayResultCompare() throws IOException {
        String jobId = "jobId";
        String strTrue = "{\n" +
                "    \"compareResult\": true,\n" +
                "    \"fieldCompare\": [\n" +
                "        {\n" +
                "            \"compare\": true,\n" +
                "            \"record\": \"record\",\n" +
                "            \"name\": \"result\",\n" +
                "            \"ignore\": true,\n" +
                "            \"replay\": \"replay\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"method\": \"method\",\n" +
                "    \"recordTime\": \"2021-04-13 15:16:52\",\n" +
                "    \"replayTime\": \"2021-04-13 15:16:52\",\n" +
                "    \"traceId\": \"c0bd111c8b7b4eeb9c2466b8c183b03b.403.16181981147910001\"\n" +
                "}";
        String strFalse = "{\n" +
                "    \"compareResult\": false,\n" +
                "    \"fieldCompare\": [\n" +
                "        {\n" +
                "            \"compare\": true,\n" +
                "            \"record\": \"record\",\n" +
                "            \"name\": \"result\",\n" +
                "            \"ignore\": true,\n" +
                "            \"replay\": \"replay\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"method\": \"method\",\n" +
                "    \"recordTime\": \"2021-04-13 15:16:52\",\n" +
                "    \"replayTime\": \"2021-04-13 15:16:52\",\n" +
                "    \"traceId\": \"c0bd111c8b7b4eeb9c2466b8c183b03b.403.16181981147910001\"\n" +
                "}";
        List<String> replayResultString = new ArrayList<>();
        replayResultString.add(strTrue);
        replayResultString.add(strFalse);
        Mockito.when(esDataSource.getAllData(REPLAY_RESULT_PREFIX + jobId)).thenReturn(replayResultString);
        Mockito.when(esDataSource.searchByKey(REPLAY_RESULT_PREFIX + jobId, "method", "method")).thenReturn(replayResultString);

        List<JSONObject> result = replayResultService.getReplayResultCompare(jobId, "", "");
        assertEquals(2, result.size());
        result = replayResultService.getReplayResultCompare(jobId, "", "true");
        assertEquals(1, result.size());
        result = replayResultService.getReplayResultCompare(jobId, "", "false");
        assertEquals(1, result.size());
    }

    @Test
    public void ignoreFiled() throws IOException {
        Mockito.when(esDataSource.checkIndexExistence(IGNORE_FIELDS_INDEX))
                .thenReturn(true).thenReturn(true)
                .thenReturn(false).thenReturn(false);
        try {
            Mockito.when(esDataSource.getDocId(IGNORE_FIELDS_INDEX, "method", "method"))
                    .thenReturn("").thenReturn("docId")
                    .thenReturn("docId").thenThrow(new IOException());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        IgnoreFieldEntity ignoreFieldEntity = new IgnoreFieldEntity();
        ignoreFieldEntity.setMethod("method");
        assertTrue(replayResultService.ignoreFiled(ignoreFieldEntity));
        assertTrue(replayResultService.ignoreFiled(ignoreFieldEntity));
        assertTrue(replayResultService.ignoreFiled(ignoreFieldEntity));
        assertFalse(replayResultService.ignoreFiled(ignoreFieldEntity));
    }

    @Test
    public void getReplayResultDetail() throws IOException {
        String jobId = "jobId";
        String strTrue = "{\n" +
                "    \"compareResult\": true,\n" +
                "    \"fieldCompare\": [\n" +
                "        {\n" +
                "            \"compare\": true,\n" +
                "            \"record\": \"record\",\n" +
                "            \"name\": \"result\",\n" +
                "            \"ignore\": true,\n" +
                "            \"replay\": \"replay\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"method\": \"method\",\n" +
                "    \"recordTime\": \"2021-04-13 15:16:52\",\n" +
                "    \"replayTime\": \"2021-04-13 15:16:52\",\n" +
                "    \"traceId\": \"traceId\"\n" +
                "}";
        String strFalse = "{\n" +
                "    \"compareResult\": false,\n" +
                "    \"fieldCompare\": [\n" +
                "        {\n" +
                "            \"compare\": true,\n" +
                "            \"record\": \"record\",\n" +
                "            \"name\": \"result\",\n" +
                "            \"ignore\": true,\n" +
                "            \"replay\": \"replay\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"method\": \"method\",\n" +
                "    \"recordTime\": \"2021-04-13 15:16:52\",\n" +
                "    \"replayTime\": \"2021-04-13 15:16:52\",\n" +
                "    \"traceId\": \"traceId\"\n" +
                "}";
        String ignore = "{\n" +
                "    \"fields\": [\n" +
                "        {\n" +
                "            \"ignore\": true,\n" +
                "            \"name\": \"result\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"method\": \"method\"\n" +
                "}";
        List<String> replayResultString = new ArrayList<>();
        List<String> fieldsIgnore = new ArrayList<>();
        replayResultString.add(strTrue);
        replayResultString.add(strFalse);
        fieldsIgnore.add(ignore);
        Mockito.when(esDataSource.searchByKey(IGNORE_FIELDS_INDEX, "method", "method"))
                .thenReturn(fieldsIgnore).thenThrow(new IOException());
        Mockito.when(esDataSource.searchByKey(REPLAY_RESULT_PREFIX + jobId, "traceId", "traceId"))
                .thenReturn(replayResultString).thenThrow(new IOException());
        ReplayResultDetailEntity replayResultDetailEntity = replayResultService.getReplayResultDetail(jobId, "traceId", "method");
        assertEquals(replayResultDetailEntity.getTraceId(), "traceId");
        assertEquals(replayResultDetailEntity.getMethod(), "method");
        replayResultDetailEntity = replayResultService.getReplayResultDetail(jobId, "traceId", "method");
        assertEquals(replayResultDetailEntity.getTraceId(), "traceId");
        assertEquals(replayResultDetailEntity.getMethod(), "method");
    }

    @Test
    public void reCompare() throws IOException {
        String jobId = "jobId";
        String strTrue = "{\n" +
                "    \"compareResult\": true,\n" +
                "    \"fieldCompare\": [\n" +
                "        {\n" +
                "            \"compare\": true,\n" +
                "            \"record\": \"record\",\n" +
                "            \"name\": \"result\",\n" +
                "            \"ignore\": true,\n" +
                "            \"replay\": \"replay\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"method\": \"method\",\n" +
                "    \"recordTime\": \"2021-04-13 15:16:52\",\n" +
                "    \"replayTime\": \"2021-04-13 15:16:52\",\n" +
                "    \"traceId\": \"traceId\"\n" +
                "}";
        String strFalse = "{\n" +
                "    \"compareResult\": false,\n" +
                "    \"fieldCompare\": [\n" +
                "        {\n" +
                "            \"compare\": true,\n" +
                "            \"record\": \"record\",\n" +
                "            \"name\": \"result\",\n" +
                "            \"ignore\": true,\n" +
                "            \"replay\": \"replay\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"method\": \"method\",\n" +
                "    \"recordTime\": \"2021-04-13 15:16:52\",\n" +
                "    \"replayTime\": \"2021-04-13 15:16:52\",\n" +
                "    \"traceId\": \"traceId\"\n" +
                "}";
        String ignore = "{\n" +
                "    \"fields\": [\n" +
                "        {\n" +
                "            \"ignore\": true,\n" +
                "            \"name\": \"result\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"method\": \"method\"\n" +
                "}";
        List<String> replayResultString = new ArrayList<>();
        List<String> fieldsIgnore = new ArrayList<>();
        replayResultString.add(strTrue);
        replayResultString.add(strFalse);
        fieldsIgnore.add(ignore);
        Mockito.when(esDataSource.searchByKey(IGNORE_FIELDS_INDEX, "method", "method"))
                .thenReturn(fieldsIgnore);
        Mockito.when(esDataSource.getAllData(REPLAY_RESULT_PREFIX + jobId)).thenReturn(replayResultString);
        Mockito.when(esDataSource.getDocId(REPLAY_RESULT_PREFIX + jobId, "traceId", "traceId")).thenReturn("");
        Mockito.when(esDataSource.getOne(IGNORE_FIELDS_INDEX, "method", "method")).thenReturn(ignore);
        replayResultService.reCompare(jobId);
    }

    @Test
    public void testReCompare() throws IOException {
        String jobId = "jobId";
        String strTrue = "{\n" +
                "    \"compareResult\": true,\n" +
                "    \"fieldCompare\": [\n" +
                "        {\n" +
                "            \"compare\": true,\n" +
                "            \"record\": \"record\",\n" +
                "            \"name\": \"result\",\n" +
                "            \"ignore\": true,\n" +
                "            \"replay\": \"replay\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"method\": \"method\",\n" +
                "    \"recordTime\": \"2021-04-13 15:16:52\",\n" +
                "    \"replayTime\": \"2021-04-13 15:16:52\",\n" +
                "    \"traceId\": \"traceId\"\n" +
                "}";
        String strFalse = "{\n" +
                "    \"compareResult\": false,\n" +
                "    \"fieldCompare\": [\n" +
                "        {\n" +
                "            \"compare\": true,\n" +
                "            \"record\": \"record\",\n" +
                "            \"name\": \"result\",\n" +
                "            \"ignore\": true,\n" +
                "            \"replay\": \"replay\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"method\": \"method\",\n" +
                "    \"recordTime\": \"2021-04-13 15:16:52\",\n" +
                "    \"replayTime\": \"2021-04-13 15:16:52\",\n" +
                "    \"traceId\": \"traceId\"\n" +
                "}";
        String ignore = "{\n" +
                "    \"fields\": [\n" +
                "        {\n" +
                "            \"ignore\": true,\n" +
                "            \"name\": \"result\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"method\": \"method\"\n" +
                "}";
        List<String> replayResultString = new ArrayList<>();
        List<String> fieldsIgnore = new ArrayList<>();
        replayResultString.add(strTrue);
        replayResultString.add(strFalse);
        fieldsIgnore.add(ignore);
        Mockito.when(esDataSource.searchByKey(IGNORE_FIELDS_INDEX, "method", "method"))
                .thenReturn(fieldsIgnore);
        Mockito.when(esDataSource.getAllData(REPLAY_RESULT_PREFIX + jobId)).thenReturn(replayResultString);
        Mockito.when(esDataSource.getDocId(REPLAY_RESULT_PREFIX + jobId, "traceId", "traceId")).thenReturn("");
        Mockito.when(esDataSource.getOne(IGNORE_FIELDS_INDEX, "method", "method")).thenReturn(ignore);
        replayResultService.reCompare(jobId, "method");
    }

    @Test
    public void compare() throws IOException {
        String jobId = "jobId";
        String strTrue = "{\n" +
                "    \"compareResult\": true,\n" +
                "    \"fieldCompare\": [\n" +
                "        {\n" +
                "            \"compare\": true,\n" +
                "            \"record\": \"record\",\n" +
                "            \"name\": \"result\",\n" +
                "            \"ignore\": true,\n" +
                "            \"replay\": \"replay\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"method\": \"method\",\n" +
                "    \"recordTime\": \"2021-04-13 15:16:52\",\n" +
                "    \"replayTime\": \"2021-04-13 15:16:52\",\n" +
                "    \"traceId\": \"traceId\"\n" +
                "}";
        String strFalse = "{\n" +
                "    \"compareResult\": false,\n" +
                "    \"fieldCompare\": [\n" +
                "        {\n" +
                "            \"compare\": true,\n" +
                "            \"record\": \"record\",\n" +
                "            \"name\": \"result\",\n" +
                "            \"ignore\": true,\n" +
                "            \"replay\": \"replay\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"method\": \"method\",\n" +
                "    \"recordTime\": \"2021-04-13 15:16:52\",\n" +
                "    \"replayTime\": \"2021-04-13 15:16:52\",\n" +
                "    \"traceId\": \"traceId\"\n" +
                "}";
        String ignore = "{\n" +
                "    \"fields\": [\n" +
                "        {\n" +
                "            \"ignore\": true,\n" +
                "            \"name\": \"result\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"method\": \"method\"\n" +
                "}";
        List<String> replayResultString = new ArrayList<>();
        List<String> fieldsIgnore = new ArrayList<>();
        replayResultString.add(strTrue);
        replayResultString.add(strFalse);
        fieldsIgnore.add(ignore);
        Mockito.when(esDataSource.searchByKey(IGNORE_FIELDS_INDEX, "method", "method"))
                .thenReturn(fieldsIgnore);
        Mockito.when(esDataSource.getAllData(REPLAY_RESULT_PREFIX + jobId)).thenReturn(replayResultString);
        Mockito.when(esDataSource.getDocId(REPLAY_RESULT_PREFIX + jobId, "traceId", "traceId")).thenReturn("").thenThrow(new IOException());
        Mockito.when(esDataSource.getOne(IGNORE_FIELDS_INDEX, "method", "method")).thenReturn(ignore).thenThrow(new IOException());
        ReplayResultEntity replayResultEntity = JSON.parseObject(strTrue, ReplayResultEntity.class);
        replayResultService.compare(jobId, replayResultEntity);
        replayResultService.compare(jobId, replayResultEntity);
        replayResultService.compare(jobId, replayResultEntity);
    }
}