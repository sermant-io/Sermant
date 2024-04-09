/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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
 * index type set
 *
 * @author zhp
 * @since 2022-08-30
 */
public enum MetricType {
    /**
     * failure rate
     */
    FAILURE_RATE("failure_rate", "the number is failure rate in percentage"),

    /**
     * slow call rate
     */
    SLOW_CALL_RATE("slow_call_rate",
            "the number is current percentage of calls which were slower than a certain threshold"),

    /**
     * number of slow calls
     */
    SLOW_CALL_NUMBER("slow_call_number",
            "the number is current total number of calls which were slower than a certain threshold"),

    /**
     * number of successful slow calls
     */
    SLOW_CALL_SUCCESS_NUMBER("slow_call_success_number",
            "the number is current number of successful calls which were slower than a certain threshold"),

    /**
     * number of slow call failures
     */
    SLOW_CALL_FAILURE_NUMBER("slow_call_failure_number",
            "the number is current number of failed calls which were slower than a certain threshold"),

    /**
     * cache call count
     */
    BUFFERED_CALLS_NUMBER("buffered_calls_number",
            "the number is current total number of buffered calls in the ring buffer"),

    /**
     * number of failed calls
     */
    FAILED_CALLS_NUMBER("failed_calls_rate",
            "the number is current number of failed buffered calls in the ring buffer"),

    /**
     * number of calls not allowed
     */
    NOT_PERMITTED_CALLS_NUMBER("not_permitted_calls_number",
            "the number is  current number of not permitted calls, when the state is OPEN"),

    /**
     * successful calls
     */
    SUCCESSFUL_CALLS_NUMBER("successful_calls_number",
            "the number is current number of successful buffered calls in the ring buffer"),

    /**
     * number of successful requests on the server
     */
    SUCCESS_SERVER_REQUEST("successful_server_request", "the number is successful number of server request"),

    /**
     * number of successful client requests
     */
    SUCCESS_CLIENT_REQUEST("successful_client_request", "the number is successful number of client request"),

    /**
     * number of server request failures
     */
    FAILURE_SERVER_REQUEST("failure_server_request", "the number is failure number of server request"),

    /**
     * number of failed client requests
     */
    FAILURE_CLIENT_REQUEST("failure_client_request", "the number is failure number of client request"),

    /**
     * client time
     */
    CONSUMING_CLIENT_TIME("consuming_client_time", "the number is consuming time of client request"),

    /**
     * server time
     */
    CONSUMING_SERVER_TIME("consuming_server_time", "the number is consuming time of server request"),

    /**
     * number of client requests
     */
    CLIENT_REQUEST("client_request", "the number is request number of client"),

    /**
     * number of server requests
     */
    SERVER_REQUEST("server_request", "the number is request number of server"),

    /**
     * circuit breaker failure request
     */
    FAILURE_FUSE_REQUEST("failure_fuse_request", "the number is failure number of fuse"),

    /**
     * circuit breaker successful request
     */
    SUCCESS_FUSE_REQUEST("success_fuse_request", "the number is success number of fuse"),

    /**
     * circuit breaker ignored abnormal requests
     */
    IGNORE_FUSE_REQUEST("ignore_fuse_request", "the number is fuse number of ignore"),

    /**
     * slow request
     */
    SLOW_FUSE_REQUEST("slow_fuse_request", "the number is fuse number of slow"),

    /**
     * circuit breaker request
     */
    PERMITTED_FUSE_REQUEST("permitted_fuse_request", "the number is fuse number of permitted"),

    /**
     * circuit breaker request
     */
    FUSED_REQUEST("fused_request", "the number is request number of fused"),

    /**
     * circuit breaker time
     */
    FUSE_TIME("fuse_time", "the number is times number of fused"),

    /**
     * qps
     */
    QPS("qps", "the number is request number of per second"),

    /**
     * number of transactions per second
     */
    TPS("tps", "the number is number of transactions processed per second"),

    /**
     * completedRequests
     */
    FINISH_SERVER_REQUEST("finish_server_request", "the number is request number of finished"),

    /**
     * average response time
     */
    AVG_RESPONSE_TIME("avg_response_time", "the number is  number of response time"),

    /**
     * circuit breaker failure rate
     */
    FAILURE_RATE_FUSE_REQUEST("failure_rate_fuse_request", "the number is  number of response time"),

    /**
     * request
     */
    REQUEST("request", "the number is request number");

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
