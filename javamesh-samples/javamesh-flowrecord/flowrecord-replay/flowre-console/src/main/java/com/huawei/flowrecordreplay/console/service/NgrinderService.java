/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */
package com.huawei.flowrecordreplay.console.service;

import com.alibaba.fastjson.JSON;
import com.huawei.flowrecordreplay.console.datasource.elasticsearch.EsDataSource;
import com.huawei.flowrecordreplay.console.datasource.elasticsearch.EsDataSourceAggregate;
import com.huawei.flowrecordreplay.console.datasource.entity.Ngrinder.*;
import com.huawei.flowrecordreplay.console.datasource.entity.recordresult.RecordEntity;
import com.huawei.flowrecordreplay.console.util.Constant;
import com.huawei.flowrecordreplay.console.util.XmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

/**
 * 引流压测任务处理
 *
 * @author lihongjiang
 * @version 0.0.1
 * @since 2021-12-10
 */
@Service
public class NgrinderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NgrinderService.class);

    @Autowired
    EsDataSource esDataSource;
    @Autowired
    EsDataSourceAggregate esDataSourceAggregate;

    /**
     * 创建引流压测样式
     *
     * @param ngrinderJob 引流压测任务id
     * @return NgrinderModels 参数化任务模板
     */
    public NgrinderModels setNgrinderJob(NgrinderJob ngrinderJob) throws Exception {
        RecordContent recordContent = generateNgrinder(ngrinderJob.getRecordJobId());
        String replayId = UUID.randomUUID().toString();

        //generate csvs
        NgrinderModels ngrinderModels = generateCSV(recordContent, replayId);

        ngrinderModels.setCsvFileUrl(replayId);

        return ngrinderModels;
    }

    /**
     * 构造引流压测参数化文件
     *
     * @param jobid 引流压测任务id
     * @return RecordContent 录制数据解析后集合
     */
    public RecordContent generateNgrinder(String jobid) throws Exception {
        RecordContent recordContent = new RecordContent();
        List<String> recordResultString;
        recordResultString = esDataSource.getAllData(jobid + Constant.STAR);
        for (String str : recordResultString) {
            RecordEntity recordEntity = JSON.parseObject(str, RecordEntity.class);
            putIn(recordContent, recordEntity);
        }
        return recordContent;
    }

    /**
     * 单个接口解析
     *
     * @param recordContent 录制数据解析后集合
     * @param recordEntity 录制数据解析前集合
     */
    public void putIn(RecordContent recordContent, RecordEntity recordEntity) throws Exception {
        HttpRequestEntity requestBody = JSON.parseObject(recordEntity.getRequestBody(), HttpRequestEntity.class);
        MethodContent methodContent = new MethodContent();
        if (recordContent.getRecordContent().containsKey(recordEntity.getMethodName())) {
            List<String> maxParams = recordContent.getKeyList().get(recordEntity.getMethodName());
            Map<String, String> serialize = new HashMap<>();
            methodContent.setSerialized(serialize);
            serialized(maxParams, methodContent, requestBody);
            methodContent.setMethod(requestBody.getMethod());
            // url here delete the things after ?
            methodContent.setUrl(removeParams(requestBody.getUrl()));
            recordContent.getRecordContent().get(recordEntity.getMethodName()).add(methodContent);
        } else {
            List<MethodContent> methodContents = new LinkedList<>();
            List<String> maxParams = new LinkedList<>();
            Map<String, String> serialize = new HashMap<>();
            methodContent.setSerialized(serialize);
            serialized(maxParams, methodContent, requestBody);
            recordContent.getKeyList().put(recordEntity.getMethodName(), maxParams);
            methodContent.setMethod(requestBody.getMethod());
            // url here delete the things after ?
            methodContent.setUrl(removeParams(requestBody.getUrl()));
            methodContents.add(methodContent);
            recordContent.getRecordContent().put(recordEntity.getMethodName(), methodContents);
        }

        if (recordContent.getParamsCount() == null) {
            Map<String, Integer> paramsCount = new HashMap<>();
            Map<String, MethodContent> originModel = new HashMap<>();
            recordContent.setParamsCount(paramsCount);
            recordContent.setOriginModel(originModel);
        } else {
            if (recordContent.getParamsCount().containsKey(recordEntity.getMethodName())) {
                if (recordContent.getParamsCount().get(recordEntity.getMethodName()) < methodContent.getMaxParams()) {
                    recordContent.getParamsCount().replace(recordEntity.getMethodName(), methodContent.getMaxParams());
                    recordContent.getOriginModel().replace(recordEntity.getMethodName(), methodContent);
                }
            } else {
                recordContent.getParamsCount().put(recordEntity.getMethodName(), methodContent.getMaxParams());
                recordContent.getOriginModel().put(recordEntity.getMethodName(), methodContent);
            }

        }
    }

    /**
     * 参数化
     *
     * @param maxParams 接口最大参数列表
     * @param methodContent 接口内容
     * @param requestBody 单条录制数据内容
     */
    private void serialized(List<String> maxParams, MethodContent methodContent, HttpRequestEntity requestBody) throws Exception {
        int paramCount = 0;
        for (Map.Entry<String, String> entry : requestBody.getHeadMap().entrySet()) {
            methodContent.getSerialized().put(entry.getKey() + Constant.AT + Constant.HEADER, entry.getValue());
            if (!maxParams.contains(entry.getKey() + Constant.AT + Constant.HEADER)) {
                maxParams.add(entry.getKey() + Constant.AT + Constant.HEADER);
            }
        }
        // parse the param in url
        Map<String, String> params = new HashMap<>();
        parseParams(params, requestBody.getUrl());
        for (Map.Entry<String, String> entry : params.entrySet()) {
            methodContent.getSerialized().put(entry.getKey() + Constant.AT + Constant.PARAM, entry.getValue());
            if (!maxParams.contains(entry.getKey() + Constant.AT + Constant.PARAM)) {
                maxParams.add(entry.getKey() + Constant.AT + Constant.PARAM);
            }
            paramCount++;
        }

        // parse the body in http
        Map<String, String> body = new HashMap<>();
        if (requestBody.getHeadMap().containsKey("Content-Type")) {
            switch (requestBody.getHeadMap().get("Content-Type")) {
                case "application/json":
                    body = parseBody(requestBody.getHttpRequestBody());
                    break;
                case "application/xml":
                    body = parseBodyXml(requestBody.getHttpRequestBody());
                    break;
                default:
                    break;
            }
        }

        for (Map.Entry<String, String> entry : body.entrySet()) {
            methodContent.getSerialized().put(entry.getKey() + Constant.AT + Constant.BODY, entry.getValue());
            if (!maxParams.contains(entry.getKey() + Constant.AT + Constant.BODY)) {
                maxParams.add(entry.getKey() + Constant.AT + Constant.BODY);
            }
            paramCount++;
        }
        methodContent.setMaxParams(paramCount);
    }

    /**
     * json形式转换
     *
     * @param httpRequestBody 单条接口数据
     * @return Map<String, String>数据转map
     */
    private Map<String, String> parseBody(String httpRequestBody) {
        Map maps = (Map)JSON.parse(httpRequestBody);
        return maps;
    }

    /**
     * xml形式转换
     *
     * @param httpRequestBody 单条接口数据
     * @return Map<String, String>数据转map
     */
    private Map<String, String> parseBodyXml(String httpRequestBody) throws Exception {
        return XmlUtil.xmlToMap(httpRequestBody);
    }

    /**
     * 创建csv文件
     *
     * @param recordContent 录制内容
     * @param replayId 回放id
     * @return NgrinderModels 参数化模板
     */
    private NgrinderModels generateCSV(RecordContent recordContent, String replayId) throws IOException {
        NgrinderModels ngrinderModels = new NgrinderModels();
        Map<String, NgrinderModel> ngrinderModelMap = new HashMap<>();
        // this put into last line, that ngrindermodelmap has already fulled up with content
        ngrinderModels.setNgrinderModelMap(ngrinderModelMap);
        for (String methodName : recordContent.getKeyList().keySet()) {
            String [][] arr = null;
            List<String> params = recordContent.getKeyList().get(methodName);
            List<MethodContent> methodDetail = recordContent.getRecordContent().get(methodName);
            List<String> unSerialized = new ArrayList<>();
            Map<String, String> aimedTarget = new HashMap<>();

            // 第一遍循环确定哪些需要参数化的，第二遍循环形成二维数组
            for (MethodContent methodContent : methodDetail) {
                for (String key : params) {
                    if (methodContent.getSerialized().containsKey(key)) {
                        if (aimedTarget.containsKey(key)) {
                            if (!methodContent.getSerialized().get(key).equals(aimedTarget.get(key)) && !unSerialized.contains(key)) {
                                unSerialized.add(key);
                            }
                        } else {
                            aimedTarget.put(key, methodContent.getSerialized().get(key));
                        }
                    } else if (!unSerialized.contains(key)){
                        unSerialized.add(key);
                    }
                }
            }
            NgrinderModel ngrinderModel = new NgrinderModel();
            createNgrinderModel(params, aimedTarget, unSerialized, ngrinderModel);
            ngrinderModel.setMethod(methodName);
            ngrinderModel.setUrl(recordContent.getRecordContent().get(methodName).get(0).getUrl());

            ngrinderModelMap.put(methodName, ngrinderModel);
            arr = new String[methodDetail.size()][];
            int cnt = createArr(arr, unSerialized, methodDetail, params);

            String filepath = replayId + Constant.SPLIT;
            //generate to CSV file
            writeCsv(unSerialized, filepath, cnt, arr);
        }
        // to fix the return value
        return ngrinderModels;
    }

    /**
     * 创建csv所需二维数组
     *
     * @param arr 二维数组
     * @param unSerialized 无序参数化
     * @param methodDetail 参数化具体内容
     * @param params 参数列表
     * @return int 数据条数
     */
    private int createArr(String [][] arr, List<String>unSerialized,
                           List<MethodContent> methodDetail, List<String> params) {
        int cnt = 0;
        for (MethodContent methodContent : methodDetail) {
            int index = 0;
            int second = unSerialized.size();
            for (String key : params) {
                if (index == 0) {
                    arr[cnt] = new String [second];
                }
                if (!unSerialized.contains(key)) {
                    continue;
                }
                if (methodContent.getSerialized().containsKey(key)) {
                    arr[cnt][index] = methodContent.getSerialized().get(key);
                } else {
                    arr[cnt][index] = null;
                }
                index++;
            }
            cnt++;
        }
        return cnt;
    }

    /**
     * 写csv文件
     *
     * @param arr 二维数组
     * @param unSerialized 无序参数化
     * @param filepath 文件路径
     * @param cnt 文件条数
     */
    private void writeCsv(List<String>unSerialized, String filepath, int cnt, String [][] arr) throws IOException {
        BufferedWriter csvWrite = null;
        try {
            File csvFile = new File(filepath + Constant.CSV);
            if (!csvFile.getParentFile().exists()) {
                csvFile.getParentFile().mkdirs();
            }
            csvFile.createNewFile();
            csvWrite = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), "UTF-8"), 8192);
            // avoid the error code when EXCEL open it
            csvWrite.write(Constant.UFEFF);
            write(unSerialized, csvWrite);
            //TODO <= or <
            for (int i = 0; i < cnt; i++) {
                List<?> dataPerRowList = new ArrayList<>(Arrays.asList(arr[i]));
                write(dataPerRowList, csvWrite);
            }
            csvWrite.flush();
        } catch (IOException e) {
            LOGGER.error("cannot create csv file : ", e);
            throw new IOException("csv file create failed");
        } finally {
            try {
                if (null != csvWrite) {
                    csvWrite.close();
                }
            } catch (IOException e) {
                LOGGER.error("cannot close csv file: ", e);
                throw new IOException("cannot close csv file");
            }
        }
    }

    private void write(List<?> dataList, BufferedWriter csvWrite) throws IOException {
        for (Object data : dataList) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("\"");
            if (null == data) {
                buffer.append(" ");
            } else {
                buffer.append(data);
            }
            String rowStr = buffer.append("\", ").toString();
            csvWrite.write(rowStr);
        }
        csvWrite.newLine();
    }

    /**
     * 创建csv所需二维数组
     *
     * @param aimedTarget 最长目标参数
     * @param unSerialized 无序参数化
     * @param ngrinderModel 参数化模板
     * @param params 参数列表
     */
    private void createNgrinderModel(List<String> params, Map<String, String>aimedTarget,
                                     List<String>unSerialized, NgrinderModel ngrinderModel) {
        Map<String, String> header = new HashMap<>();
        Map<String, String> param = new HashMap<>();
        Map<String, String> body = new HashMap<>();

        for (String paramName : aimedTarget.keySet()) {
            boolean serializedFlag = false;
            if (unSerialized.contains(paramName)) {
                serializedFlag = true;
            }
            switch (cutOff(paramName)[1]) {
                case Constant.HEADER:
                    if (serializedFlag) {
                        header.put((cutOff(paramName)[0]), paramName);
                    } else {
                        header.put((cutOff(paramName)[0]), aimedTarget.get(paramName));
                    }
                    break;
                case Constant.PARAM:
                    if (serializedFlag) {
                        param.put((cutOff(paramName)[0]), paramName);
                    } else {
                        param.put((cutOff(paramName)[0]), aimedTarget.get(paramName));
                    }
                    break;
                case Constant.BODY:
                    if (serializedFlag) {
                        body.put((cutOff(paramName)[0]), paramName);
                    } else {
                        body.put((cutOff(paramName)[0]), aimedTarget.get(paramName));
                    }
                    break;
                default:
                    break;
            }
        }
        ngrinderModel.setBody(body);
        ngrinderModel.setHeader(header);
        ngrinderModel.setParams(param);
    }

    private String[] cutOff(String origin) {
        return origin.split("@");
    }

    private String removeParams(String url) {
        if (url.indexOf("?") != -1) {
            url = url.split("\\?")[0];
        }
        return url;
    }

    private void parseParams(Map<String, String> params, String url) {
        if (url.indexOf("?") != -1) {
            url = url.split("\\?")[1];
        } else {
            return;
        }
        String[] arrParams = url.split("&");
        for (int i = 0; i < arrParams.length; i++) {
            params.put(arrParams[i].split("=")[0], arrParams[i].split("=")[1]);
        }
    }
}

