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

package com.huawei.flowcontrol.common.config;

import com.huawei.flowcontrol.common.enums.FlowFramework;

import com.huaweicloud.sermant.core.config.common.ConfigFieldKey;
import com.huaweicloud.sermant.core.config.common.ConfigTypeKey;
import com.huaweicloud.sermant.core.plugin.config.PluginConfig;

/**
 * flow control configuration class
 *
 * @author zhouss
 * @since 2022-01-28
 */
@ConfigTypeKey("flow.control.plugin")
public class FlowControlConfig implements PluginConfig {
    /**
     * kafka address of the flow control plugin
     */
    private String kafkaBootstrapServers = "127.0.0.1:9092,127.0.0.1:9093,127.0.0.1:9094";

    /**
     * Default interval for sending heartbeat messages, in milliseconds
     */
    private long heartbeatInterval = CommonConst.FLOW_CONTROL_HEARTBEAT_INTERVAL;

    /**
     * The default interval for sending flow control information, in milliseconds
     */
    private long metricInterval = CommonConst.FLOW_CONTROL_METRIC_INTERVAL;

    /**
     * Specifies the period during which flow control information data is initially loaded after startup
     */
    private long metricInitialDuration = CommonConst.METRIC_INITIAL_DURATION;

    /**
     * The default number of data loaded at the end time of querying flow control information is not provided
     */
    private long metricMaxLine = CommonConst.METRIC_MAX_LINE;

    /**
     * When querying flow control data, sleep for a period of time and wait until
     * the flow limiting data is written into the file
     */
    private long metricSleepTime = CommonConst.METRIC_SLEEP_TIME;

    /**
     * kafka configuration parameter: keySerialization
     */
    private String kafkaKeySerializer = "org.apache.kafka.common.serialization.StringSerializer";

    /**
     * kafka configuration parameter: valueSerialization
     */
    private String kafkaValueSerializer = "org.apache.kafka.common.serialization.StringSerializer";

    /**
     * kafka configuration parameter: The name of the topic to which flow control information is sent
     */
    private String kafkaMetricTopic = "topic-metric";

    /**
     * kafka configuration parameter: name of the topic for sending heartbeat data
     */
    private String kafkaHeartbeatTopic = "topic-heartbeat";

    /**
     * kafka configuration parameter: The producer requires a signal from the server acknowledging receipt of the data
     * ack 0,1,all
     */
    private long kafkaAcks = 1L;

    /**
     * kafka configuration parameter: Control the maximum size of requests sent by the producer. The default is 1 M
     * （This parameter is related to the message.max.bytes parameter of the Kafka host）
     */
    private long kafkaMaxRequestSize = CommonConst.KAFKA_MAX_REQUEST_SIZE;

    /**
     * kafka configuration parameter: producer memory buffer size 32m
     */
    private long kafkaBufferMemory = CommonConst.KAFKA_BUFFER_MEMORY;

    /**
     * kafka configuration parameter: the number of times the message was resold
     */
    private long kafkaRetries = 0L;

    /**
     * kafka configuration parameter: The maximum time the client will wait for a response to the request
     */
    private long kafkaRequestTimeoutMs = CommonConst.KAFKA_REQUEST_TIMEOUT_MS;

    /**
     * kafka configuration parameter: Maximum blocking time, beyond which an exception is thrown
     */
    private long kafkaMaxBlockMs = CommonConst.KAFKA_MAX_BLOCK_MS;

    /**
     * configure jaas prefixes
     */
    private String kafkaJaasConfig = ConfigConst.KAFKA_JAAS_CONFIG;

    /**
     * sasl authentication mechanism
     */
    private String kafkaSaslMechanism = ConfigConst.KAFKA_SASL_MECHANISM;

    /**
     * The encryption protocol supports the SASL_SSL protocol
     */
    private String kafkaSecurityProtocol = ConfigConst.KAFKA_SECURITY_PROTOCOL;

    /**
     * ssl truststore file Location
     */
    private String kafkaSslTruststoreLocation = ConfigConst.KAFKA_SSL_TRUSTSTORE_LOCATION;

    /**
     * ssl truststore password configuration
     */
    private String kafkaSslTruststorePassword = ConfigConst.KAFKA_SSL_TRUSTSTORE_PASSWORD;

    /**
     * domain uncheck
     */
    private String kafkaIdentificationAlgorithm = ConfigConst.KAFKA_IDENTIFICATION_ALGORITHM;

    /**
     * whether to pass the ssl authentication
     */
    private boolean isKafkaSsl = ConfigConst.IS_KAFKA_SSL;

    /**
     * servicecomb-kie address. If the configuration center is configured as a servicecomb-kie,
     * enter the correct kie service address
     */
    private String configKieAddress = "localhost:30110";

    /**
     * 标签监听的服务名 为空则使用IdentityConfigManager#getAppName() 否则使用配置的服务名
     */
    private String configServiceName = "sermantService";

    /**
     * Whether to enable data collection, including heartbeat and indicators
     */
    private boolean openMetricCollector = false;

    /**
     * whether to use online cse configuration rules
     */
    private boolean useCseRule = false;

    /**
     * Whether to match the UI of the source Pan-PaaS and the front and rear zookeeper
     */
    private boolean adaptPass = false;

    /**
     * Whether to throw a service exception The default is false
     */
    private boolean needThrowBizException = false;

    /**
     * Wait time unit (MS) for indicator data to be written to a file
     */
    private long metricSleepTimeMs = CommonConst.DEFAULT_METRIC_SEND_INTERVAL_MS;

    /**
     * type of flow control framework
     */
    private FlowFramework flowFramework = FlowFramework.RESILIENCE;

    /**
     * apache dubbo retry exception
     */
    private String[] apacheDubboRetryExceptions = {"org.apache.dubbo.rpc.RpcException"};

    /**
     * retry exception for alibaba dubbo
     */
    private String[] alibabaDubboRetryExceptions = {"com.alibaba.dubbo.rpc.RpcException"};

    /**
     * default retry exception for spring
     */
    private String[] springRetryExceptions = {"org.springframework.web.client.HttpServerErrorException"};

    /**
     * Whether to use the agent configuration center. The configuration center of the cse may be used instead of the
     * agent configuration center
     */
    private boolean useAgentConfigCenter = true;

    /**
     * kie namespace
     */
    private String project = "default";

    /**
     * sc app configuration
     */
    private String application = "sermant";

    /**
     * sc environment configuration
     */
    private String environment = "production";

    /**
     * default sc version
     */
    private String version = "1.0.0";

    /**
     * whether to enable sc encryption
     */
    private boolean isSslEnabled = false;

    /**
     * Whether the development is based on the servicecomb sdk.
     * You can determine whether you need to intercept service information
     */
    private boolean isBaseSdk = false;

    /**
     * cluster invoker actuator for dubbo injection. If this configuration is set to "close",
     * the agent's actuator is not applicable
     */
    private String retryClusterInvoker = "sermant";

    /**
     * maximum cache number
     */
    private int maxCacheSize = ConfigConst.DEFAULT_MAX_CACHE_SIZE;

    /**
     * cache timer check interval in seconds
     */
    private long timedCheckInterval = ConfigConst.DEFAULT_TIME_CACHE_CHECK_INTERVAL;

    /**
     * whether to enable retry
     */
    private boolean enableRetry = true;

    /**
     * connection timeout-restTemplate
     */
    private long restTemplateConnectTimeoutMs = ConfigConst.CONNECT_TIMEOUT_MS;

    /**
     * response timeout-restTemplate
     */
    private long restTemplateReadTimeoutMs = ConfigConst.SOCKET_READ_TIMEOUT_MS;

    /**
     * rest template request protocol default okhttp， if the host does not the connection mode is used
     */
    private String restTemplateRequestFactory = "okHttp";

    /**
     * Whether to replace the native ClusterInvoker for dubbo applications only;
     * <p>If true, the call logic will still follow the original dubbo cluster invoker logic when called,
     * retry only the outermost packaging</p>
     * <p>If false, when called, its cluster invoker is replaced by a custom retry Invoker retried by the plugin,
     * which defaults to false</p>
     */
    private boolean useOriginInvoker = false;

    /**
     * monitor start switch
     */
    @ConfigFieldKey("enable-start-monitor")
    private boolean enableStartMonitor;

    /**
     * system adaptive flow control switch
     */
    @ConfigFieldKey("enable-system-adaptive")
    private boolean enableSystemAdaptive;

    /**
     * system rule flow control switch
     */
    @ConfigFieldKey("enable-system-rule")
    private boolean enableSystemRule;

    public boolean isUseOriginInvoker() {
        return useOriginInvoker;
    }

    public void setUseOriginInvoker(boolean useOriginInvoker) {
        this.useOriginInvoker = useOriginInvoker;
    }

    public String getRestTemplateRequestFactory() {
        return restTemplateRequestFactory;
    }

    public void setRestTemplateRequestFactory(String restTemplateRequestFactory) {
        this.restTemplateRequestFactory = restTemplateRequestFactory;
    }

    public long getRestTemplateConnectTimeoutMs() {
        return restTemplateConnectTimeoutMs;
    }

    public void setRestTemplateConnectTimeoutMs(long restTemplateConnectTimeoutMs) {
        this.restTemplateConnectTimeoutMs = restTemplateConnectTimeoutMs;
    }

    public long getRestTemplateReadTimeoutMs() {
        return restTemplateReadTimeoutMs;
    }

    public void setRestTemplateReadTimeoutMs(long restTemplateReadTimeoutMs) {
        this.restTemplateReadTimeoutMs = restTemplateReadTimeoutMs;
    }

    public boolean isEnableRetry() {
        return enableRetry;
    }

    public void setEnableRetry(boolean enableRetry) {
        this.enableRetry = enableRetry;
    }

    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    public long getTimedCheckInterval() {
        return timedCheckInterval;
    }

    public void setTimedCheckInterval(long timedCheckInterval) {
        this.timedCheckInterval = timedCheckInterval;
    }

    public String getRetryClusterInvoker() {
        return retryClusterInvoker;
    }

    public void setRetryClusterInvoker(String retryClusterInvoker) {
        this.retryClusterInvoker = retryClusterInvoker;
    }

    public boolean isBaseSdk() {
        return isBaseSdk;
    }

    public void setBaseSdk(boolean baseSdk) {
        this.isBaseSdk = baseSdk;
    }

    public boolean isUseAgentConfigCenter() {
        return useAgentConfigCenter;
    }

    public void setUseAgentConfigCenter(boolean useAgentConfigCenter) {
        this.useAgentConfigCenter = useAgentConfigCenter;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isSslEnabled() {
        return isSslEnabled;
    }

    public void setSslEnabled(boolean sslEnabled) {
        this.isSslEnabled = sslEnabled;
    }

    public String[] getApacheDubboRetryExceptions() {
        return apacheDubboRetryExceptions;
    }

    public void setApacheDubboRetryExceptions(String[] apacheDubboRetryExceptions) {
        this.apacheDubboRetryExceptions = apacheDubboRetryExceptions;
    }

    public String[] getAlibabaDubboRetryExceptions() {
        return alibabaDubboRetryExceptions;
    }

    public void setAlibabaDubboRetryExceptions(String[] alibabaDubboRetryExceptions) {
        this.alibabaDubboRetryExceptions = alibabaDubboRetryExceptions;
    }

    public String[] getSpringRetryExceptions() {
        return springRetryExceptions;
    }

    public void setSpringRetryExceptions(String[] springRetryExceptions) {
        this.springRetryExceptions = springRetryExceptions;
    }

    public FlowFramework getFlowFramework() {
        return flowFramework;
    }

    public void setFlowFramework(FlowFramework flowFramework) {
        this.flowFramework = flowFramework;
    }

    public boolean isNeedThrowBizException() {
        return needThrowBizException;
    }

    public void setNeedThrowBizException(boolean needThrowBizException) {
        this.needThrowBizException = needThrowBizException;
    }

    public long getMetricSleepTimeMs() {
        return metricSleepTimeMs;
    }

    public void setMetricSleepTimeMs(long metricSleepTimeMs) {
        this.metricSleepTimeMs = metricSleepTimeMs;
    }

    public boolean isAdaptPass() {
        return adaptPass;
    }

    public void setAdaptPass(boolean adaptPass) {
        this.adaptPass = adaptPass;
    }

    public boolean isUseCseRule() {
        return useCseRule;
    }

    public void setUseCseRule(boolean useCseRule) {
        this.useCseRule = useCseRule;
    }

    public String getConfigServiceName() {
        return configServiceName;
    }

    public void setConfigServiceName(String configServiceName) {
        this.configServiceName = configServiceName;
    }

    public boolean isOpenMetricCollector() {
        return openMetricCollector;
    }

    public void setOpenMetricCollector(boolean openMetricCollector) {
        this.openMetricCollector = openMetricCollector;
    }

    public String getKafkaBootstrapServers() {
        return kafkaBootstrapServers;
    }

    public void setKafkaBootstrapServers(String kafkaBootstrapServers) {
        this.kafkaBootstrapServers = kafkaBootstrapServers;
    }

    public long getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(long heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public long getMetricInterval() {
        return metricInterval;
    }

    public void setMetricInterval(long metricInterval) {
        this.metricInterval = metricInterval;
    }

    public long getMetricInitialDuration() {
        return metricInitialDuration;
    }

    public void setMetricInitialDuration(long metricInitialDuration) {
        this.metricInitialDuration = metricInitialDuration;
    }

    public long getMetricMaxLine() {
        return metricMaxLine;
    }

    public void setMetricMaxLine(long metricMaxLine) {
        this.metricMaxLine = metricMaxLine;
    }

    public long getMetricSleepTime() {
        return metricSleepTime;
    }

    public void setMetricSleepTime(long metricSleepTime) {
        this.metricSleepTime = metricSleepTime;
    }

    public String getKafkaKeySerializer() {
        return kafkaKeySerializer;
    }

    public void setKafkaKeySerializer(String kafkaKeySerializer) {
        this.kafkaKeySerializer = kafkaKeySerializer;
    }

    public String getKafkaValueSerializer() {
        return kafkaValueSerializer;
    }

    public void setKafkaValueSerializer(String kafkaValueSerializer) {
        this.kafkaValueSerializer = kafkaValueSerializer;
    }

    public String getKafkaMetricTopic() {
        return kafkaMetricTopic;
    }

    public void setKafkaMetricTopic(String kafkaMetricTopic) {
        this.kafkaMetricTopic = kafkaMetricTopic;
    }

    public String getKafkaHeartbeatTopic() {
        return kafkaHeartbeatTopic;
    }

    public void setKafkaHeartbeatTopic(String kafkaHeartbeatTopic) {
        this.kafkaHeartbeatTopic = kafkaHeartbeatTopic;
    }

    public long getKafkaAcks() {
        return kafkaAcks;
    }

    public void setKafkaAcks(long kafkaAcks) {
        this.kafkaAcks = kafkaAcks;
    }

    public long getKafkaMaxRequestSize() {
        return kafkaMaxRequestSize;
    }

    public void setKafkaMaxRequestSize(long kafkaMaxRequestSize) {
        this.kafkaMaxRequestSize = kafkaMaxRequestSize;
    }

    public long getKafkaBufferMemory() {
        return kafkaBufferMemory;
    }

    public void setKafkaBufferMemory(long kafkaBufferMemory) {
        this.kafkaBufferMemory = kafkaBufferMemory;
    }

    public long getKafkaRetries() {
        return kafkaRetries;
    }

    public void setKafkaRetries(long kafkaRetries) {
        this.kafkaRetries = kafkaRetries;
    }

    public long getKafkaRequestTimeoutMs() {
        return kafkaRequestTimeoutMs;
    }

    public void setKafkaRequestTimeoutMs(long kafkaRequestTimeoutMs) {
        this.kafkaRequestTimeoutMs = kafkaRequestTimeoutMs;
    }

    public long getKafkaMaxBlockMs() {
        return kafkaMaxBlockMs;
    }

    public void setKafkaMaxBlockMs(long kafkaMaxBlockMs) {
        this.kafkaMaxBlockMs = kafkaMaxBlockMs;
    }

    public String getKafkaJaasConfig() {
        return kafkaJaasConfig;
    }

    public void setKafkaJaasConfig(String kafkaJaasConfig) {
        this.kafkaJaasConfig = kafkaJaasConfig;
    }

    public String getKafkaSaslMechanism() {
        return kafkaSaslMechanism;
    }

    public void setKafkaSaslMechanism(String kafkaSaslMechanism) {
        this.kafkaSaslMechanism = kafkaSaslMechanism;
    }

    public String getKafkaSecurityProtocol() {
        return kafkaSecurityProtocol;
    }

    public void setKafkaSecurityProtocol(String kafkaSecurityProtocol) {
        this.kafkaSecurityProtocol = kafkaSecurityProtocol;
    }

    public String getKafkaSslTruststoreLocation() {
        return kafkaSslTruststoreLocation;
    }

    public void setKafkaSslTruststoreLocation(String kafkaSslTruststoreLocation) {
        this.kafkaSslTruststoreLocation = kafkaSslTruststoreLocation;
    }

    public String getKafkaSslTruststorePassword() {
        return kafkaSslTruststorePassword;
    }

    public void setKafkaSslTruststorePassword(String kafkaSslTruststorePassword) {
        this.kafkaSslTruststorePassword = kafkaSslTruststorePassword;
    }

    public String getKafkaIdentificationAlgorithm() {
        return kafkaIdentificationAlgorithm;
    }

    public void setKafkaIdentificationAlgorithm(String kafkaIdentificationAlgorithm) {
        this.kafkaIdentificationAlgorithm = kafkaIdentificationAlgorithm;
    }

    public boolean isKafkaSsl() {
        return isKafkaSsl;
    }

    public void setKafkaSsl(boolean kafkaSsl) {
        this.isKafkaSsl = kafkaSsl;
    }

    public String getConfigKieAddress() {
        return configKieAddress;
    }

    public void setConfigKieAddress(String configKieAddress) {
        this.configKieAddress = configKieAddress;
    }

    public boolean isEnableStartMonitor() {
        return enableStartMonitor;
    }

    public void setEnableStartMonitor(boolean enableStartMonitor) {
        this.enableStartMonitor = enableStartMonitor;
    }

    public void setEnableSystemAdaptive(boolean isSystemAdaptive) {
        this.enableSystemAdaptive = isSystemAdaptive;
    }

    public boolean isEnableSystemAdaptive() {
        return enableSystemAdaptive;
    }

    public void setEnableSystemRule(boolean isSystemRule) {
        this.enableSystemRule = isSystemRule;
    }

    public boolean isEnableSystemRule() {
        return enableSystemRule;
    }
}
