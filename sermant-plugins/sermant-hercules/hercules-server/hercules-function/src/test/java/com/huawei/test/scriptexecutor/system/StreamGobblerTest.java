package com.huawei.test.scriptexecutor.system;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class StreamGobblerTest {
    @Test
    public void test_run_when_inputStream_is_null() {
        StreamGobbler streamGobbler = new StreamGobbler("TEST", null);
        streamGobbler.start();
        String executeInfo = null;
        try {
            executeInfo = streamGobbler.getExecuteInfo(1000L);
        } catch (InterruptedException e) {
            Assert.fail();
        }
        Assert.assertEquals("Input stream is null.", executeInfo);
    }

    @Test
    public void test_run_when_inputStream_has_content_and_set_timeout() {
        String testData = "Test data";
        byte[] bytes = testData.getBytes(StandardCharsets.UTF_8);
        StreamGobbler streamGobbler = new StreamGobbler("TEST", new ByteArrayInputStream(bytes));
        streamGobbler.start();
        String executeInfo = null;
        try {
            executeInfo = streamGobbler.getExecuteInfo(1000L);
        } catch (InterruptedException e) {
            Assert.fail();
        }
        Assert.assertEquals(testData, executeInfo);
    }

    @Test
    public void test_run_when_inputStream_has_content_and_not_set_timeout() {
        String testData = "Test data";
        byte[] bytes = testData.getBytes(StandardCharsets.UTF_8);
        StreamGobbler streamGobbler = new StreamGobbler("TEST", new ByteArrayInputStream(bytes));
        streamGobbler.start();
        String executeInfo = null;
        try {
            executeInfo = streamGobbler.getExecuteInfo(0);
        } catch (InterruptedException e) {
            Assert.fail();
        }
        Assert.assertEquals(testData, executeInfo);
    }
}
