package com.huawei.flowrecordreplay.console.controller;

import com.huawei.flowrecordreplay.console.ConsoleApplication;
import com.huawei.flowrecordreplay.console.domain.CreateRecordJobRequest;
import com.huawei.flowrecordreplay.console.domain.CreateReplayJobRequest;

import com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSON;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsoleApplication.class)
public class JobControllerTest {
    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;


    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();  //构造MockMvc
    }

    @Test
    public void addRecordJob() {
        List<String> machineList = new ArrayList<>();
        List<String> methodList = new ArrayList<>();
        machineList.add("10.0.0.1");
        machineList.add("10.0.0.2");
        methodList.add("method1");
        methodList.add("method2");
        CreateRecordJobRequest queryRecordJobRequest = new CreateRecordJobRequest();
        queryRecordJobRequest.setName("test");
        queryRecordJobRequest.setApplication("test_app");
        queryRecordJobRequest.setMachineList(machineList);
        queryRecordJobRequest.setMethodList(methodList);
        queryRecordJobRequest.setStartTime(new Date());
        queryRecordJobRequest.setEndTime(new Date());
        queryRecordJobRequest.setExtra("");
        MvcResult mvcResult;
        try {
            mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/jobs/record-job")
                    .accept(MediaType.APPLICATION_JSON_UTF8)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(JSON.toJSONString(queryRecordJobRequest)))
                    .andDo(MockMvcResultHandlers.print())
                    .andReturn();
            String content = mvcResult.getResponse().getContentAsString();
            int status = mvcResult.getResponse().getStatus();
            Assert.assertEquals(200, status);
            System.out.println(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void queryAllRecordJob() {
        MvcResult mvcResult;
        try {
            mvcResult = mockMvc.perform((MockMvcRequestBuilders.get("/jobs/record-jobs"))
                    .accept(MediaType.APPLICATION_JSON_UTF8)
                    .contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(MockMvcResultHandlers.print())
                    .andReturn();
            String content = mvcResult.getResponse().getContentAsString();
            int status = mvcResult.getResponse().getStatus();
            Assert.assertEquals(200, status);
            System.out.println(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void queryRecordJobWithCondition() {
        String name = "test";
        String application = "test_app";
        String from = "2021-03-01 12:00:00";
        String to = "2021-03-01 12:00:00";
        MvcResult mvcResult;
        try {
            mvcResult = mockMvc.perform((MockMvcRequestBuilders.get("/jobs/record-jobs"))
                    .accept(MediaType.APPLICATION_JSON_UTF8)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .param("name", name)
                    .param("application", application)
                    .param("from", from)
                    .param("to", to))
                    .andDo(MockMvcResultHandlers.print())
                    .andReturn();
            String content = mvcResult.getResponse().getContentAsString();
            int status = mvcResult.getResponse().getStatus();
            Assert.assertEquals(200, status);
            System.out.println(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void queryRecordJobById() {
        String jobId = "ffec8126-af3e-4384-9259-9973ebe03511";
        MvcResult mvcResult;
        try {
            mvcResult = mockMvc.perform((MockMvcRequestBuilders.get("/jobs/record-job/{jobId}", jobId))
                    .accept(MediaType.APPLICATION_JSON_UTF8)
                    .contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(MockMvcResultHandlers.print())
                    .andReturn();
            String content = mvcResult.getResponse().getContentAsString();
            int status = mvcResult.getResponse().getStatus();
            Assert.assertEquals(200, status);
            System.out.println(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void stopRecordJob() {
        String jobId = "ffec8126-af3e-4384-9259-9973ebe03511";
        MvcResult mvcResult;
        try {
            mvcResult = mockMvc.perform((MockMvcRequestBuilders.put("/jobs/record-job/stop/{jobId}", jobId))
                    .accept(MediaType.APPLICATION_JSON_UTF8)
                    .contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(MockMvcResultHandlers.print())
                    .andReturn();
            String content = mvcResult.getResponse().getContentAsString();
            int status = mvcResult.getResponse().getStatus();
            Assert.assertEquals(200, status);
            System.out.println(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void addReplayJob() {
        CreateReplayJobRequest request = new CreateReplayJobRequest();
        request.setRecordJobId("ffec8126-af3e-4384-9259-9973ebe03511");
        MvcResult mvcResult;
        try {
            mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/jobs/replay-job")
                    .accept(MediaType.APPLICATION_JSON_UTF8)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(JSON.toJSONString(request)))
                    .andDo(MockMvcResultHandlers.print())
                    .andReturn();
            String content = mvcResult.getResponse().getContentAsString();
            int status = mvcResult.getResponse().getStatus();
            Assert.assertEquals(200, status);
            System.out.println(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void queryReplayJob() {
        String jobId = "ffec8126-af3e-4384-9259-9973ebe03511";
        MvcResult mvcResult;
        try {
            mvcResult = mockMvc.perform((MockMvcRequestBuilders.get("/jobs/replay-job/{jobId}", jobId))
                    .accept(MediaType.APPLICATION_JSON_UTF8)
                    .contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(MockMvcResultHandlers.print())
                    .andReturn();
            String content = mvcResult.getResponse().getContentAsString();
            int status = mvcResult.getResponse().getStatus();
            Assert.assertEquals(200, status);
            System.out.println(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void queryAllReplayJob() {
        MvcResult mvcResult;
        try {
            mvcResult = mockMvc.perform((MockMvcRequestBuilders.get("/jobs/replay-jobs"))
                    .accept(MediaType.APPLICATION_JSON_UTF8)
                    .contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(MockMvcResultHandlers.print())
                    .andReturn();
            String content = mvcResult.getResponse().getContentAsString();
            int status = mvcResult.getResponse().getStatus();
            Assert.assertEquals(200, status);
            System.out.println(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void queryReplayJobByApp() {
        String application = "app1";
        String name = "test_app";
        MvcResult mvcResult;
        try {
            mvcResult = mockMvc.perform((MockMvcRequestBuilders.get("/jobs/replay-jobs"))
                    .accept(MediaType.APPLICATION_JSON_UTF8)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .param("name", name)
                    .param("application", application))
                    .andDo(MockMvcResultHandlers.print())
                    .andReturn();
            String content = mvcResult.getResponse().getContentAsString();
            int status = mvcResult.getResponse().getStatus();
            Assert.assertEquals(200, status);
            System.out.println(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deleteRecordJob() {
        String jobId = "ffec8126-af3e-4384-9259-9973ebe03511";
        MvcResult mvcResult;
        try {
            mvcResult = mockMvc.perform((MockMvcRequestBuilders.delete("/jobs/record-job/{jobId}", jobId))
                    .accept(MediaType.APPLICATION_JSON_UTF8)
                    .contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(MockMvcResultHandlers.print())
                    .andReturn();
            String content = mvcResult.getResponse().getContentAsString();
            int status = mvcResult.getResponse().getStatus();
            Assert.assertEquals(200, status);
            System.out.println(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deleteReplayJob() {
        String jobId = "ffec8126-af3e-4384-9259-9973ebe03511";
        MvcResult mvcResult;
        try {
            mvcResult = mockMvc.perform((MockMvcRequestBuilders.delete("/jobs/replay-job/{jobId}", jobId))
                    .accept(MediaType.APPLICATION_JSON_UTF8)
                    .contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(MockMvcResultHandlers.print())
                    .andReturn();
            String content = mvcResult.getResponse().getContentAsString();
            int status = mvcResult.getResponse().getStatus();
            Assert.assertEquals(200, status);
            System.out.println(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void stopReplayJob() {
        String jobId = "ffec8126-af3e-4384-9259-9973ebe03511";
        MvcResult mvcResult;
        try {
            mvcResult = mockMvc.perform((MockMvcRequestBuilders.put("/jobs/replay-job/stop/{jobId}", jobId))
                    .accept(MediaType.APPLICATION_JSON_UTF8)
                    .contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(MockMvcResultHandlers.print())
                    .andReturn();
            String content = mvcResult.getResponse().getContentAsString();
            int status = mvcResult.getResponse().getStatus();
            Assert.assertEquals(200, status);
            System.out.println(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}