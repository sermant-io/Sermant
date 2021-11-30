package com.huawei.hercules.controller;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class BaseController {

    public final String JSON_MSG = "msg";
    public final String JSON_RESULT_KEY = "success";
    public final boolean SUCCESS = true;
    public final boolean FAILURE = false;
    public static final String GROOVY_MAVEN_TYPE = "groovy_maven";
    public static final String CUSTOM_SCRIPT = "自定义脚本";

    protected JSONObject returnSuccess() {
        return this.returnSuccess(null);
    }

    protected JSONObject returnSuccess(String message) {
        JSONObject result = new JSONObject();
        result.put(JSON_MSG, message);
        result.put(JSON_RESULT_KEY, SUCCESS);
        return result;
    }

    protected JSONObject returnError() {
        return this.returnError(null);
    }

    protected JSONObject returnError(String message) {
        JSONObject result = new JSONObject();
        result.put(JSON_MSG, message);
        result.put(JSON_RESULT_KEY, FAILURE);
        return result;
    }

    public String longToDate(long lo) {
        Date date = new Date(lo);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    protected String arrayToStr(List<String> data) {
        if (data == null || data.isEmpty()) {
            return "";
        }
        StringJoiner sj = new StringJoiner(",");
        for (String label : data) {
            sj.add(label);
        }
        return sj.toString();
    }

    protected String arrayToStr(String[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        return arrayToStr(Arrays.asList(data));
    }

    protected String getOrder(String order) {
        if ("ascend".equals(order)) {
            return "ASC";
        }
        return "DESC";
    }

    protected CharSequence getHost(String domain, String ip) {
        StringBuilder thisHost = new StringBuilder();
        if (!StringUtils.isEmpty(domain)) {
            thisHost.append(domain);
        }
        if (!StringUtils.isEmpty(ip)) {
            if (!StringUtils.isEmpty(domain)) {
                thisHost.append(":").append(ip);
            } else {
                thisHost.append(ip);
            }
        }
        return thisHost.toString();
    }

    protected Map<String, String> parseScript(String script) {
        Map<String, String> scriptMap = new HashMap<>();
        scriptMap.put("script", script);
        return scriptMap;
    }

    protected void downloadFile(JSONObject jsonObject, HttpServletResponse response) throws Exception {
        response.reset();
        response.addHeader("Content-Disposition", jsonObject.getString("Content-Disposition"));
        response.setContentType("application/octet-stream; charset=UTF-8");
        response.addHeader("Content-Length", jsonObject.getString("Content-Length"));
        byte[] buffer = new byte[4096];
        ByteArrayInputStream fis = null;
        OutputStream toClient = null;
        try {
            fis = new ByteArrayInputStream(jsonObject.getBytes("content"));
            toClient = new BufferedOutputStream(response.getOutputStream());
            int readLength;
            while (((readLength = fis.read(buffer)) != -1)) {
                toClient.write(buffer, 0, readLength);
            }
        } catch (IOException e) {
            throw new Exception("error while download file", e);
        } finally {
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(toClient);
        }
    }

    /**
     * 将"yyyy-MM-dd HH:mm:ss E"转化成"yyyy-MM-dd HH:mm:ss"
     * @param time
     * @return
     */
    protected Object dataFormat(String time) {
        if (StringUtils.isEmpty(time) || time.trim().length() < 19) {
            return time;
        }
        return time.substring(0, 19);
    }

}
