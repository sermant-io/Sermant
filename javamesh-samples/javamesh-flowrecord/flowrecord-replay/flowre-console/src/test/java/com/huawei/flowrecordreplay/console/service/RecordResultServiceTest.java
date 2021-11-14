package com.huawei.flowrecordreplay.console.service;

import com.huawei.flowrecordreplay.console.datasource.elasticsearch.EsDataSource;
import com.huawei.flowrecordreplay.console.datasource.elasticsearch.EsDataSourceAggregate;
import com.huawei.flowrecordreplay.console.datasource.entity.recordresult.RecordInterfaceCountEntity;
import com.huawei.flowrecordreplay.console.datasource.entity.recordresult.RecordResultCountEntity;
import com.huawei.flowrecordreplay.console.datasource.entity.recordresult.RecordResultEntity;
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

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class RecordResultServiceTest {
    @Autowired
    RecordResultService recordResultService;

    @MockBean
    EsDataSource esDataSource;

    @MockBean
    EsDataSourceAggregate esDataSourceAggregate;

    @Test
    public void getRecordOverview() throws IOException {
        String jobId = "jobId";
        List<RecordInterfaceCountEntity> recordInterfaceCountEntities = new ArrayList<>();
        RecordInterfaceCountEntity recordInterfaceCountEntity = new RecordInterfaceCountEntity();
        recordInterfaceCountEntity.setMethod("method");
        recordInterfaceCountEntity.setTotal(2);
        recordInterfaceCountEntities.add(recordInterfaceCountEntity);
        recordInterfaceCountEntities.add(recordInterfaceCountEntity);
        recordInterfaceCountEntities.add(recordInterfaceCountEntity);
        IOException ioException = new IOException();
        Mockito.when(esDataSourceAggregate.recordInterfaceCount(jobId)).thenReturn(recordInterfaceCountEntities).thenThrow(ioException);
        RecordResultCountEntity recordResultCountEntity = recordResultService.getRecordOverview(jobId);
        assertEquals(6, recordResultCountEntity.getRecordTotalCount());
    }

    @Test
    public void getRecordResult() throws IOException {
        String jobId = "jobId";
        String method = "method";
        String startTime = "startTime";
        String endTime = "endTime";

        String str = "{\n" +
                "    \"recordEntityList\": [\n" +
                "        {\n" +
                "            \"jobId\": jobid,\n" +
                "            \"traceId\": \"traceId\",\n" +
                "            \"appType\": \"appType\",\n" +
                "            \"methodName\": methodName,\n" +
                "            \"subCallKey\": \"subCallKey\"\n" +
                "            \"subCallCount\": \"0\"\n" +
                "            \"requestBody\": \"requestBody\"\n" +
                "            \"requestClass\": \"requestClass\"\n" +
                "            \"responseBody\": \"responseBody\"\n" +
                "            \"responseClass\": \"responseClass\"\n" +
                "            \"timestamp\": \"2021-04-13 15:16:52\"\n" +
                "        }\n" +
                "    ],\n" +
                "}";

        List<String> recordResultString = new ArrayList<>();
        recordResultString.add(str);

        Mockito.when(esDataSource.searchByKeyEnd(jobId + "*", "methodName", method)).thenReturn(recordResultString);
        Mockito.when(esDataSource.getAllDataEnd(jobId + "*")).thenReturn(recordResultString);
        RecordResultEntity recordResultEntity = new RecordResultEntity();
        recordResultEntity = recordResultService.getRecordResult(jobId, "", "", "", "");
        assertEquals(1, recordResultEntity.getTotal());
    }
}
