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

    /** 返回信息的key **/
    public final String JSON_MSG = "msg";
    /** 操作结果状态可以 **/
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

    /**
     * 已逗号,拼接集合
     * @param data 集合
     * @return 拼接字符串结果
     */
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

    /**
     * 已逗号,拼接数组
     * @param data 集合
     * @return 拼接字符串结果
     */
    protected String arrayToStr(String[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        return arrayToStr(Arrays.asList(data));
    }

    /**
     * 转换排序方式为数据库支持的
     * @param order 排序方式
     * @return 排序方式
     */
    protected String getOrder(String order) {
        if ("ascend".equals(order)) {
            return "ASC";
        }
        return "DESC";
    }

    /**
     * 拼接主机信息：域名:IP
     * @param domain 域名
     * @param ip IP地址
     * @return 拼接结果
     */
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

    /**
     * 将脚本信息存放在Map集合中【解决feign传递时将分好;自动转变成逗号,】
     * @param script 脚本内容
     * @return 集合
     */
    protected Map<String, String> parseScript(String script) {
        Map<String, String> scriptMap = new HashMap<>();
        scriptMap.put("script", script);
        return scriptMap;
    }

    /**
     * 下载文件
     * @param jsonObject 文件内容
     * @param response 响应
     * @throws Exception 异常
     */
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

    /**
     * 将\n的字符串数据转换成数组
     * @param content 内容
     * @return 数组结果
     */
    protected String[] parseStrToArray(String content) {
        if (StringUtils.isEmpty(content)) {
            return new String[0];
        }
        if (content.startsWith("\n")) {
            return content.substring(2).split("\n");
        }
        return content.split("\n");
    }

    /**
     * 从集合中移除指定的key
     * @param map 集合
     * @param keys 待异常的key信息
     */
    protected void removeKeys(Map<String, Object> map, String[] keys) {
        if (keys == null || keys.length == 0) {
            return;
        }
        for (String key : keys) {
            map.remove(key);
        }
    }
}
