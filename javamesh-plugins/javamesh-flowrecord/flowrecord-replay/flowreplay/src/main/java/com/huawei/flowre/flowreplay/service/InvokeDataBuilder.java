/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.flowreplay.service;

import com.huawei.flowre.flowreplay.config.Const;
import com.huawei.flowre.flowreplay.datasource.EsDataSource;
import com.huawei.flowre.flowreplay.domain.ModifyRuleEntity;
import com.huawei.flowre.flowreplay.domain.RecordEntity;
import com.huawei.flowre.flowreplay.domain.SubReplayJobEntity;
import com.huawei.flowre.flowreplay.domain.content.DubboInvokeContent;
import com.huawei.flowre.flowreplay.domain.content.HttpInvokeContent;
import com.huawei.flowre.flowreplay.domain.message.HttpInvokeMessage;
import com.huawei.flowre.flowreplay.domain.result.HttpRequestEntity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;

import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.RpcInvocation;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 前置处理逻辑用于构造回放数据
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-06-28
 */
@Service
public class InvokeDataBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(InvokeDataBuilder.class);

    @Value("${invoke.pre.size}")
    int preInovkeSize;
    @Value("${invoke.pre.Thread}")
    int preInovkeThreadSize;
    @Value("${invoke.thread.pool.block}")
    int blockQueueSize;
    @Value("${invoke.thread.pool.core}")
    int coreThreadSize;
    @Value("${invoke.thread.pool.max}")
    int maxThreadSize;
    @Value("${invoke.thread.pool.alive}")
    int aliveTime;

    @Autowired
    EsDataSource esDataSource;
    @Autowired
    Environment environment;
    @Autowired
    private KafkaProducer<String, String> producer;

    public void replay(SubReplayJobEntity task) throws UnknownHostException, InterruptedException {
        LOGGER.info("Starting task : {}", task.getJobId());
        List<String> replayData = esDataSource.getData(task.getRecordIndex());
        InetAddress address = InetAddress.getLocalHost();
        String workerName = Const.REPLAY_DATA_TOPIC + address.getHostAddress() + '_'
                + environment.getProperty("local.server.port");
        BlockingQueue blockingQueue = new ArrayBlockingQueue(blockQueueSize);
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(coreThreadSize, maxThreadSize,
                aliveTime, TimeUnit.SECONDS, blockingQueue);
        execute(replayData, threadPoolExecutor, task, workerName);
    }

    private void execute(List<String> replayData, ThreadPoolExecutor threadPoolExecutor,
                         SubReplayJobEntity task, String workerName) throws InterruptedException {
        for (int i = 0; i < replayData.size(); i += preInovkeSize) {
            int from = i;
            int end;
            if (from + preInovkeSize > replayData.size()) {
                end = replayData.size();
            } else {
                end = from + preInovkeSize;
            }
            List<String> searchHits = replayData.subList(from, end);
            if (threadPoolExecutor.getQueue().size() > preInovkeThreadSize) {
                while (true) {
                    if (threadPoolExecutor.getQueue().size() > preInovkeThreadSize) {
                        break;
                    } else {
                        Thread.sleep(Const.THREAD_SLEEP);
                    }
                }
            }
            threadPoolExecutor.execute(
                    () -> {
                        for (String searchHit : searchHits) {
                            RecordEntity recordEntity = JSON.parseObject(searchHit, RecordEntity.class);
                            Map<String, List<ModifyRuleEntity>> modifyRule = task.getModifyRule();
                            if (modifyRule != null && modifyRule.get(recordEntity.getMethodName()) != null) {
                                modifyArguments(recordEntity, modifyRule.get(recordEntity.getMethodName()));
                            }
                            String msg;
                            switch (recordEntity.getAppType()) {
                                case "Dubbo": {
                                    sendKafka(workerName, JSON.toJSONString(buildDubboInvokeContent(recordEntity,
                                            task.getJobId(), task.getAddress())));
                                    break;
                                }
                                case "HTTP": {
                                    sendKafka(workerName, JSON.toJSONString(buildHttpInvokeContent(recordEntity,
                                            task.getJobId(), task.getAddress())));
                                    break;
                                }
                                default: {
                                    break;
                                }
                            }
                        }
                    }
            );
        }
    }

    private void sendKafka(String topic, String msg) {
        try {
            ProducerRecord<String, String> record;
            record = new ProducerRecord<>(topic, msg);
            producer.send(record, (metadata, exception) -> {
            });
        } catch (Exception e) {
            LOGGER.error("[flowreplay]: send message with kafka failed");
        } finally {
            producer.flush();
        }
    }

    private DubboInvokeContent buildDubboInvokeContent(RecordEntity recordEntity, String replayJobId, String address) {
        Invocation invocation = JSON.parseObject(recordEntity.getRequestBody(), RpcInvocation.class);
        JSONObject invocationJsonObject = JSON.parseObject(recordEntity.getRequestBody());

        // 获取参数类型列表
        JSONArray parameterTypes = (JSONArray) invocationJsonObject.get("parameterTypes");
        String[] paramTypes = new String[parameterTypes.size()];
        for (int one = 0; one < parameterTypes.size(); one++) {
            paramTypes[one] = parameterTypes.getString(one);
        }

        // 将traceId和recordJobId放入attachment 用于mock查询mock数据
        invocation.getAttachments().put(Const.RECORD_JOB_ID, recordEntity.getJobId());
        invocation.getAttachments().put(Const.TRACE_ID, recordEntity.getTraceId());

        // 创建泛化调用的实体(version 和 group 为可选属性)
        DubboInvokeContent dubboInvokeContent =
            new DubboInvokeContent(invocation.getAttachments().get(Const.FIELD_INTERFACE),
                invocation.getMethodName(), paramTypes, invocation.getArguments(), invocation.getAttachments());
        Map<String, String> attachments = invocationJsonObject.getObject("attachments",
            new ParameterizedTypeImpl(new Type[]{String.class, String.class}, null, Map.class));
        dubboInvokeContent.setVersion(attachments.getOrDefault(Const.VERSION, Const.BLANK));
        dubboInvokeContent.setGroup(attachments.getOrDefault(Const.GROUP, Const.BLANK));
        dubboInvokeContent.setAddress(address);
        return dubboInvokeContent;
    }

    private HttpInvokeMessage buildHttpInvokeContent(RecordEntity recordEntity, String replayJobId, String address) {
        HttpRequestEntity httpRequestEntity = JSON.parseObject(recordEntity.getRequestBody(), HttpRequestEntity.class);
        HttpInvokeMessage httpInvokeMessage = new HttpInvokeMessage();
        httpInvokeMessage.setMethodName(recordEntity.getMethodName());
        HttpInvokeContent httpInvokeContent = new HttpInvokeContent();
        httpInvokeContent.setData(httpRequestEntity.getHttpRequestBody());
        httpInvokeContent.setHeaders(httpRequestEntity.getHeadMap());
        httpInvokeContent.setMethod(httpRequestEntity.getMethod());
        httpInvokeContent.setUrl(address + httpRequestEntity.getUrl());
        httpInvokeMessage.setHttpInvokeContent(httpInvokeContent);
        httpInvokeMessage.setRecordTime(recordEntity.getTimestamp());
        httpInvokeMessage.setReplayJobId(replayJobId);
        httpInvokeMessage.setTraceId(recordEntity.getTraceId());
        httpInvokeMessage.setResponseBody(recordEntity.getResponseBody());
        return httpInvokeMessage;
    }

    private void modifyArguments(RecordEntity recordEntity, List<ModifyRuleEntity> modifyRule) {
        for (ModifyRuleEntity entity : modifyRule) {
            if (StringUtils.isNotBlank(entity.getType()) && StringUtils.isNotBlank(entity.getSearch())
                && StringUtils.isNotBlank(entity.getReplacement())) {
                JSONObject requestBody = JSON.parseObject(recordEntity.getRequestBody());
                if (recordEntity.getAppType().equals("Dubbo")) {
                    String requestArguments = requestBody.getJSONArray(Const.ARGUMENTS_FIELD).toString();
                    switch (entity.getType()) {
                        case Const.CONCRETE_TYPE:
                            // 具体值替换
                            requestArguments = requestArguments.replace(entity.getSearch(), entity.getReplacement());
                            break;
                        case Const.REGEX_TYPE:
                            // 正则表达式替换
                            requestArguments = requestArguments.replaceAll(entity.getSearch(), entity.getReplacement());
                            break;
                        default:
                            break;
                    }
                    requestBody.put(Const.ARGUMENTS_FIELD, JSON.parseArray(requestArguments));
                } else if (recordEntity.getAppType().equals("HTTP")) {
                    String requestArguments = recordEntity.getRequestBody();
                    switch (entity.getType()) {
                        case Const.CONCRETE_TYPE:
                            // 具体值替换
                            requestArguments = requestArguments.replace(entity.getSearch(), entity.getReplacement());
                            break;
                        case Const.REGEX_TYPE:
                            // 正则表达式替换
                            requestArguments = requestArguments.replaceAll(entity.getSearch(), entity.getReplacement());
                            break;
                        case Const.DATE_TYPE:
                            JSONArray jsonArray = requestBody.getJSONArray("httpRequestBody");
                            jsonArray = parseDate(jsonArray, entity);
                            requestBody.put("httpRequestBody", jsonArray);
                            break;
                        default:
                            break;
                    }
                }
                recordEntity.setRequestBody(requestBody.toJSONString());
            }
        }
    }

    private JSONArray parseDate(JSONArray jsonArray, ModifyRuleEntity entity) {
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String[] buff = entity.getSearch().split("\\.");
            JSONObject content;
            for (int j = 0; j < buff.length - 1; j++) {
                if (buff[j].contains("#")) {
                    String[] searchArray = buff[j].split("#", Const.LIMIT_SIZE);
                    JSONArray nextArray = (JSONArray) jsonObject.get(searchArray[1]);
                    content = (JSONObject) nextArray.get(Integer.parseInt(searchArray[0]) - 1);
                } else {
                    content = (JSONObject) jsonObject.get(buff[j]);
                }
                jsonObject = content;
            }
            Date date = new Date();
            jsonObject.put(buff[buff.length - 1], date);
        }
        return jsonArray;
    }
}
