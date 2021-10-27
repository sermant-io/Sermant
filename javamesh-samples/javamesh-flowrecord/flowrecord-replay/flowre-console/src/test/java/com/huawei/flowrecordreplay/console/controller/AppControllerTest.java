package com.huawei.flowrecordreplay.console.controller;

import com.huawei.flowrecordreplay.console.ConsoleApplication;
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

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsoleApplication.class)
public class AppControllerTest {
    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();  //构造MockMvc
    }

    @Test
    public void getAppList() {
        MvcResult mvcResult;
        try {
            mvcResult = mockMvc.perform((MockMvcRequestBuilders.get("/application/apps"))
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
    public void getMachineList() {
        MvcResult mvcResult;
        String application = "test_app";
        try {
            mvcResult = mockMvc.perform((MockMvcRequestBuilders.get("/application/{application}/machines", application))
                    .accept(MediaType.APPLICATION_JSON_UTF8)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
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
}