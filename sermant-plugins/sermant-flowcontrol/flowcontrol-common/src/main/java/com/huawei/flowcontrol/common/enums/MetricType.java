/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.huawei.flowcontrol.common.enums;

/**
 * 指标类型集合
 *
 * @author zhp
 * @since 2022-08-30
 */
public enum MetricType {
    /**
     * 失败率
     */
    FAILURE_RATE("failure_rate", "the number is failure rate in percentage"),

    /**
     * 慢速调用率
     */
    SLOW_CALL_RATE("slow_call_rate",
            "the number is current percentage of calls which were slower than a certain threshold"),

    /**
     * 慢速调用数量
     */
    SLOW_CALL_NUMBER("slow_call_number",
            "the number is current total number of calls which were slower than a certain threshold"),

    /**
     * 慢速调用成功数量
     */
    SLOW_CALL_SUCCESS_NUMBER("slow_call_success_number",
            "the number is current number of successful calls which were slower than a certain threshold"),

    /**
     * 慢速调用失败数量
     */
    SLOW_CALL_FAILURE_NUMBER("slow_call_failure_number",
            "the number is current number of failed calls which were slower than a certain threshold"),

    /**
     * 缓存调用数
     */
    BUFFERED_CALLS_NUMBER("buffered_calls_number",
            "the number is current total number of buffered calls in the ring buffer"),

    /**
     * 调用失败个数
     */
    FAILED_CALLS_NUMBER("failed_calls_rate",
            "the number is current number of failed buffered calls in the ring buffer"),

    /**
     * 不允许呼叫数
     */
    NOT_PERMITTED_CALLS_NUMBER("not_permitted_calls_number",
            "the number is  current number of not permitted calls, when the state is OPEN"),

    /**
     * 成功调用数
     */
    SUCCESSFUL_CALLS_NUMBER("successful_calls_number",
            "the number is current number of successful buffered calls in the ring buffer"),

    /**
     * 服务端请求成功数量
     */
    SUCCESS_SERVER_REQUEST("successful_server_request", "the number is successful number of server request"),

    /**
     * 客户端请求成功数量
     */
    SUCCESS_CLIENT_REQUEST("successful_client_request", "the number is successful number of client request"),

    /**
     * 服务端请求失败数量
     */
    FAILURE_SERVER_REQUEST("failure_server_request", "the number is failure number of server request"),

    /**
     * 客户端请求失败数量
     */
    FAILURE_CLIENT_REQUEST("failure_client_request", "the number is failure number of client request"),

    /**
     * 客户端耗时
     */
    CONSUMING_CLIENT_TIME("consuming_client_time", "the number is consuming time of client request"),

    /**
     * 服务端耗时
     */
    CONSUMING_SERVER_TIME("consuming_server_time", "the number is consuming time of server request"),

    /**
     * 客户端请求数量
     */
    CLIENT_REQUEST("client_request", "the number is request number of client"),

    /**
     * 服务端请求数量
     */
    SERVER_REQUEST("server_request", "the number is request number of server"),

    /**
     * 熔断失败请求
     */
    FAILURE_FUSE_REQUEST("failure_fuse_request", "the number is failure number of fuse"),

    /**
     * 熔断成功请求
     */
    SUCCESS_FUSE_REQUEST("success_fuse_request", "the number is success number of fuse"),

    /**
     * 熔断忽视异常请求
     */
    IGNORE_FUSE_REQUEST("ignore_fuse_request", "the number is fuse number of ignore"),

    /**
     * 慢速请求
     */
    SLOW_FUSE_REQUEST("slow_fuse_request", "the number is fuse number of slow"),

    /**
     * 熔断请求
     */
    PERMITTED_FUSE_REQUEST("permitted_fuse_request", "the number is fuse number of permitted"),

    /**
     * 熔断请求
     */
    FUSED_REQUEST("fused_request", "the number is request number of fused"),

    /**
     * 熔断耗时
     */
    FUSE_TIME("fuse_time", "the number is times number of fused"),

    /**
     * 每秒请求数
     */
    QPS("qps", "the number is request number of per second"),

    /**
     * 每秒处理事务数
     */
    TPS("tps", "the number is number of transactions processed per second"),

    /**
     * 完成请求数
     */
    FINISH_SERVER_REQUEST("finish_server_request", "the number is request number of finished"),

    /**
     * 平均响应时间
     */
    AVG_RESPONSE_TIME("avg_response_time", "the number is  number of response time"),

    /**
     * 熔断失败率
     */
    FAILURE_RATE_FUSE_REQUEST("failure_rate_fuse_request", "the number is  number of response time"),

    /**
     * 请求
     */
    REQUEST("request", "the number is request number"),;

    private final String name;

    private final String desc;

    MetricType(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

}
