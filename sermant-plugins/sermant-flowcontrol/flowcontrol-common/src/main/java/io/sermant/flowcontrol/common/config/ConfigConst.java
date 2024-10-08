/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.sermant.flowcontrol.common.config;

/**
 * configuration constant class
 *
 * @author zhouss
 * @since 2022-01-28
 */
public class ConfigConst {
    /**
     * sentinel version
     */
    public static final String SENTINEL_VERSION = "sentinel.version";

    /**
     * sentinel configuration parameter: flow control rule zookeeper address
     */
    public static final String ZOOKEEPER_ADDRESS = "flowcontrol.zookeeper.address";

    /**
     * sentinel configuration parameter: zookeeper flow control rule configuration path
     */
    public static final String ZOOKEEPER_PATH = "flowcontrol.zookeeper.path";

    /**
     * sentinel configuration parameter: Default interval for sending heartbeat messages, in milliseconds
     */
    public static final String DEFAULT_HEARTBEAT_INTERVAL = "default.heartbeat.interval";

    /**
     * sentinel configuration parameter: The default interval for sending flow control information, in milliseconds
     */
    public static final String DEFAULT_METRIC_INTERVAL = "default.metric.interval";

    /**
     * sentinel configuration parameter: Specifies the period during which flow control information data
     * is initially loaded after startup
     */
    public static final String METRIC_INITIAL_DURATION = "metric.initial.duration";

    /**
     * sentinel configuration parameter: The default number of data loaded at the end time of querying
     * flow control information is not provided
     */
    public static final String METRIC_MAX_LINE = "metric.maxLine";

    /**
     * sentinel configuration parameter: When querying flow control data, sleep for a period of time and wait
     * until the flow limiting data is written into the file
     */
    public static final String METRIC_SLEEP_TIME = "metric.sleep.time";

    /**
     * kafka configuration parameter: connect to the server address
     */
    public static final String KAFKA_BOOTSTRAP_SERVERS = "kafka.bootstrap.servers";

    /**
     * kafka configuration parameter: key serialization
     */
    public static final String KAFKA_KEY_SERIALIZER = "kafka.key.serializer";

    /**
     * kafka configuration parameter: value serialization
     */
    public static final String KAFKA_VALUE_SERIALIZER = "kafka.value.serializer";

    /**
     * kafka configuration parameter: The name of the topic to which flow control information is sent
     */
    public static final String KAFKA_METRIC_TOPIC = "kafka.metric.topic";

    /**
     * kafka configuration parameter: name of the topic for sending heartbeat data
     */
    public static final String KAFKA_HEARTBEAT_TOPIC = "kafka.heartbeat.topic";

    /**
     * kafka configuration parameter: The producer requires a signal from the server acknowledging receipt of the data
     * ack 0,1,all
     */
    public static final String KAFKA_ACKS = "kafka.acks";

    /**
     * kafka configuration parameter: Control the maximum size of requests sent by the producer. The default is 1 M
     * （This parameter is related to the message.max.bytes parameter of the Kafka host）
     */
    public static final String KAFKA_MAX_REQUEST_SIZE = "kafka.max.request.size";

    /**
     * kafka configuration parameter: producer memory buffer size
     */
    public static final String KAFKA_BUFFER_MEMORY = "kafka.buffer.memory";

    /**
     * kafka configuration parameter: the number of times the message was resold
     */
    public static final String KAFKA_RETRIES = "kafka.retries";

    /**
     * kafka configuration parameter: The maximum time the client will wait for a response to the request
     */
    public static final String KAFKA_REQUEST_TIMEOUT_MS = "kafka.request.timeout.ms";

    /**
     * kafka configuration parameter: Maximum blocking time, beyond which an exception is thrown
     */
    public static final String KAFKA_MAX_BLOCK_MS = "kafka.max.block.ms";

    /**
     * jaas configuration constant
     */
    public static final String KAFKA_JAAS_CONFIG_CONST = "sasl.jaas.config";

    /**
     * kafka authentication configuration
     */
    public static final String KAFKA_JAAS_CONFIG = "";

    /**
     * sasl authentication mode constant
     */
    public static final String KAFKA_SASL_MECHANISM_CONST = "sasl.mechanism";

    /**
     * sasl authentication mechanism
     */
    public static final String KAFKA_SASL_MECHANISM = "";

    /**
     * encryption protocol constant
     */
    public static final String KAFKA_SECURITY_PROTOCOL_CONST = "security.protocol";

    /**
     * The encryption protocol supports the SASL_SSL protocol
     */
    public static final String KAFKA_SECURITY_PROTOCOL = "";

    /**
     * ssl truststore file location constant
     */
    public static final String KAFKA_SSL_TRUSTSTORE_LOCATION_CONST = "ssl.truststore.location";

    /**
     * location of the ssl truststore file
     */
    public static final String KAFKA_SSL_TRUSTSTORE_LOCATION = "";

    /**
     * ssl truststore password constant
     */
    public static final String KAFKA_SSL_TRUSTSTORE_PASSWORD_CONST = "ssl.truststore.password";

    /**
     * ssl truststore password configuration
     */
    public static final String KAFKA_SSL_TRUSTSTORE_PASSWORD = "";

    /**
     * domain constant
     */
    public static final String KAFKA_IDENTIFICATION_ALGORITHM_CONST = "ssl.endpoint.identification.algorithm";

    /**
     * domain uncheck
     */
    public static final String KAFKA_IDENTIFICATION_ALGORITHM = "";

    /**
     * whether to pass the ssl authentication
     */
    public static final boolean IS_KAFKA_SSL = false;

    /**
     * configuration center zookeeper path
     */
    public static final String CONFIG_ZOOKEEPER_PATH = "config.zookeeper.path";

    /**
     * default environment
     */
    public static final String CONFIG_PROFILE_ACTIVE_DEFAULT = "config.profile.active.default";

    /**
     * type of the connected configuration center
     */
    public static final String CONFIG_CENTER_TYPE = "flowcontrol.configCenter.type";

    /**
     * the address of the central servicecomb kie
     */
    public static final String CONFIG_KIE_ADDRESS = "config.kie.address";

    /**
     * servicecomb kie obtains the configured url
     */
    public static final String KIE_RULES_URI = "/v1/default/kie/kv";

    /**
     * spring application name
     */
    public static final String SPRING_APPLICATION_NAME = "spring.application.name";

    /**
     * project name
     */
    public static final String PROJECT_NAME = "project.name";

    /**
     * default maximum number of caches
     */
    public static final int DEFAULT_MAX_CACHE_SIZE = 1000;

    /**
     * Default cache expiration check time, in seconds, one hour by default
     */
    public static final long DEFAULT_TIME_CACHE_CHECK_INTERVAL = 3600L;

    /**
     * connection timeout
     */
    public static final long CONNECT_TIMEOUT_MS = 1000L;

    /**
     * response timeout
     */
    public static final long SOCKET_READ_TIMEOUT_MS = 1000L;

    /**
     * rest template OKHTTP protocol
     */
    public static final String REST_TEMPLATE_REQUEST_FACTORY_OK_HTTP = "okHttp";

    /**
     * rest template NETTY protocol
     */
    public static final String REST_TEMPLATE_REQUEST_FACTORY_NETTY = "netty";

    /**
     * request the httpclient protocol
     */
    public static final String REST_TEMPLATE_REQUEST_FACTORY_HTTP = "http";

    /**
     * the call passes the upstream service name
     */
    public static final String FLOW_REMOTE_SERVICE_NAME_HEADER_KEY = "sermant.flowcontrol.header.remote.service.name";

    private ConfigConst() {
    }
}
