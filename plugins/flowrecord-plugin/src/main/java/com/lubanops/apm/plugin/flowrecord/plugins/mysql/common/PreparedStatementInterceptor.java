/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.mysql.common;

import com.huawei.apm.bootstrap.common.BeforeResult;
import com.huawei.apm.bootstrap.interceptors.InstanceMethodInterceptor;
import com.lubanops.apm.bootstrap.TransformAccess;
import com.lubanops.apm.bootstrap.trace.TraceCollector;
import com.lubanops.apm.plugin.flowrecord.config.ConfigConst;
import com.lubanops.apm.plugin.flowrecord.domain.MysqlRequestEntity;
import com.lubanops.apm.plugin.flowrecord.domain.RecordStatus;
import com.lubanops.apm.plugin.flowrecord.domain.Recorder;
import com.lubanops.apm.plugin.flowrecord.plugins.redisson.v3.RedissonInterceptor;
import com.lubanops.apm.plugin.flowrecord.utils.PluginConfigUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.hash.Hashing;
import com.lubanops.apm.plugin.flowrecord.utils.KafkaProducerUtil;
import com.sun.rowset.WebRowSetImpl;
import org.apache.skywalking.apm.plugin.jdbc.define.StatementEnhanceInfos;
import org.apache.skywalking.apm.plugin.jdbc.trace.ConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.CharArrayWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * mysql prepared statement interceptor 流量录制数据处理逻辑
 *
 * @author luanwenfei
 * @version 0.1
 * @since 2021-06-01
 */
public class PreparedStatementInterceptor implements InstanceMethodInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedissonInterceptor.class);

    /**
     * 创建span的名称
     *
     * @param connectionInfo mysql的连接信息
     * @param methodName     方法名
     * @param statementName  事务名
     * @return 创建span时使用的 operation name
     */
    private String buildOperationName(ConnectionInfo connectionInfo, String methodName, String statementName) {
        return connectionInfo.getDBType() + "/JDBI/" + statementName + "/" + methodName;
    }

    /**
     * 根据返回值的类型不同封装responseBody
     *
     * @param ret after method 中的返回值
     * @return 返回结果生成的字符串
     * @throws SQLException 数据库操作异常
     */
    private String getResponseBody(Object ret) throws SQLException {
        String responseBody;
        if (ret instanceof ResultSet) {
            CharArrayWriter charArrayWriter = new CharArrayWriter();
            WebRowSetImpl webRowSet = new WebRowSetImpl();
            webRowSet.populate((ResultSet) ret);
            webRowSet.writeXml(charArrayWriter);
            char[] resultChars = charArrayWriter.toCharArray();
            charArrayWriter.close();
            ((ResultSet) ret).beforeFirst();
            responseBody = String.valueOf(resultChars);
        } else {
            responseBody = JSON.toJSONString(ret);
        }
        return responseBody;
    }

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {

    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {

        HashMap<String, String> relationContext = (HashMap<String, String>) RecordStatus.relationContext.get();

        if (relationContext.get("recordJobList") == null) {
            return result;
        }
        TransformAccess objInst = (TransformAccess) obj;
        StatementEnhanceInfos cacheObject = (StatementEnhanceInfos) objInst.getLopsAttribute();
        if (cacheObject == null || cacheObject.getConnectionInfo() == null) {
            return result;
        }
        ConnectionInfo connectInfo = cacheObject.getConnectionInfo();
        String sql = cacheObject.getSql();
        String methodName = method.getDeclaringClass().getCanonicalName() + "." + method.getName();
        String subCallKey = Hashing.sha256().hashString(TraceCollector.getVirtualTraceId()
                + methodName + connectInfo.getDBType() + connectInfo.getDatabaseName() + sql, StandardCharsets.UTF_8)
                .toString();

        // 子调用计数
        int subCallCount = 0;
        if (relationContext.get(subCallKey) != null) {
            subCallCount = Integer.parseInt(relationContext.get(subCallKey));
            subCallCount++;
        }
        relationContext.put(subCallKey, String.valueOf(subCallCount));

        // 构造Mysql请求信息
        MysqlRequestEntity mysqlRequestEntity = new MysqlRequestEntity();
        mysqlRequestEntity.setSql(sql);
        mysqlRequestEntity.setParameters(Arrays.copyOf(cacheObject.getParameters(), cacheObject.getMaxIndex()));
        for (Map.Entry<String, String> entry : RecordStatus.map.entrySet()) {
            Recorder recordRequest = Recorder.builder()
                    .traceId(TraceCollector.getVirtualTraceId()).jobId(entry.getKey())
                    .methodName(methodName).subCallKey(subCallKey)
                    .appType(ConfigConst.MYSQL_APP_TYPE).entry(false)
                    .requestBody(JSON.toJSONString(mysqlRequestEntity))
                    .responseBody(getResponseBody(result))
                    .requestClass(mysqlRequestEntity.getClass().getCanonicalName())
                    .responseClass(result.getClass().getCanonicalName())
                    .timestamp(new Date()).subCallCount(subCallCount)
                    .build();
            String serializedRequest = JSON.toJSONString(recordRequest, SerializerFeature.WriteMapNullValue);
            KafkaProducerUtil.sendMessage(PluginConfigUtil.getValueByKey(ConfigConst.KAFKA_REQUEST_TOPIC), serializedRequest);
        }
        RecordStatus.relationContext.set(relationContext);
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {
        TransformAccess objInst = (TransformAccess) obj;
        StatementEnhanceInfos cacheObject = (StatementEnhanceInfos) objInst.getLopsAttribute();
        if (cacheObject != null && cacheObject.getConnectionInfo() != null) {
            LOGGER.error("[PreparedStatementInterceptor] errpr: " + t.getMessage());
        }
    }
}
