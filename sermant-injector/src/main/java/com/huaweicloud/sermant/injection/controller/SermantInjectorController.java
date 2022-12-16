/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.injection.controller;

import com.huaweicloud.sermant.injection.dto.Response;
import com.huaweicloud.sermant.injection.dto.WebhookResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * k8s controller
 *
 * @author provenceee
 * @since 2022-07-29
 */
@RestController
public class SermantInjectorController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SermantInjectorController.class);

    private static final String UID_PATH = "uid";

    private static final String END_PATH = "/-";

    private static final String CONTAINERS_PATH = "containers";

    private static final String OBJECT_PATH = "object";

    private static final String SPEC_PATH = "spec";

    private static final String PATH_SEPARATOR = "/";

    private static final String VOLUMES_PATH = "volumes";

    private static final String VOLUMES_INJECT_PATH = PATH_SEPARATOR + SPEC_PATH + PATH_SEPARATOR + VOLUMES_PATH;

    private static final String REQUEST_PATH = "request";

    private static final String API_VERSION_PATH = "apiVersion";

    private static final String KIND_PATH = "kind";

    private static final String JSON_OPERATION_KEY = "op";

    private static final String JSON_OPERATION_ADD = "add";

    private static final String PATH_KEY = "path";

    private static final String NAME_KEY = "name";

    private static final String MOUNT_PATH = "mountPath";

    private static final String INIT_SERMANT_PATH = "/home/sermant-agent";

    private static final String VALUE_KEY = "value";

    private static final String VOLUME_MOUNTS_PATH = "volumeMounts";

    private static final String ENV_FROM_PATH = "envFrom";

    private static final String CONFIG_MAP_REF_PATH = "configMapRef";

    private static final String READINESS_PROBE_PATH = "readinessProbe";

    private static final String LIFECYCLE_PATH = "lifecycle";

    private static final String PRE_STOP_PATH = "preStop";

    private static final String ENV_PATH = "env";

    private static final String VOLUME_NAME = "sermant-agent-volume";

    private static final String IMAGE_KEY = "image";

    private static final String IMAGE_PULL_POLICY_KEY = "imagePullPolicy";

    private static final String IMAGE_NAME = "sermant-agent";

    private static final String VOLUME_DIR = "emptyDir";

    private static final String INIT_CONTAINERS_PATH = "initContainers";

    private static final String INIT_CONTAINERS_INJECT_PATH =
            PATH_SEPARATOR + SPEC_PATH + PATH_SEPARATOR + INIT_CONTAINERS_PATH;

    private static final String COMMAND_KEY = "command";

    private static final String JVM_OPTIONS_KEY = "JAVA_TOOL_OPTIONS";

    private static final String JVM_OPTIONS_VALUE_PREFIX = " -javaagent:";

    private static final String JVM_OPTIONS_VALUE_SUFFIX = "/agent/sermant-agent.jar=appName=default ";

    private static final List<JsonNode> INIT_COMMAND =
            Arrays.asList(new TextNode("tar"), new TextNode("-zxf"), new TextNode("/home/sermant-agent.tar.gz"));

    private static final String ENV_MOUNT_PATH_KEY = "SERMANT_AGENT_MOUNT_PATH";

    private static final String ENV_CONFIG_MAP_REF_KEY = "SERMANT_AGENT_CONFIG_MAP";

    private static final String K8S_READINESS_WAIT_TIME_KEY = "GRACE_RULE_K8SREADINESSWAITTIME";

    private static final String ENABLE_HEALTH_CHECK_KEY = "GRACE_RULE_ENABLEHEALTHCHECK";

    private static final String ENABLE_SPRING_KEY = "GRACE_RULE_ENABLESPRING";

    private static final String ENABLE_GRACE_SHUTDOWN_KEY = "GRACE_RULE_ENABLEGRACESHUTDOWN";

    private static final String ENABLE_OFFLINE_NOTIFY_KEY = "GRACE_RULE_ENABLEOFFLINENOTIFY";

    private static final String HTTP_SERVER_PORT_KEY = "GRACE_RULE_HTTPSERVERPORT";

    private static final String INITIAL_DELAY_SECONDS_PATH = "initialDelaySeconds";

    private static final String PERIOD_SECONDS_PATH = "periodSeconds";

    private static final String HTTP_GET_PATH = "httpGet";

    private static final String PORT_PATH = "port";

    private static final String EXEC_PATH = "exec";

    private static final List<JsonNode> PRE_STOP_COMMANDS = Arrays.asList(new TextNode("/bin/sh"), new TextNode("-c"));

    private static final String PRE_STOP_COMMAND_PREFIX = ">- curl -XPOST http://127.0.0.1:";

    private static final String PRE_STOP_COMMAND_SUFFIX = "/\\$\\$sermant\\$\\$/shutdown 2>/tmp/null;sleep 30;exit 0";

    private static final String DEFAULT_HEALTH_CHECK_PATH = "/$$sermant$$/healthCheck";

    private static final String SERMANT_CONFIG_CENTER_KEY = "DYNAMIC_CONFIG_SERVERADDRESS";

    private static final String SERMANT_CONFIG_TYPE_KEY = "DYNAMIC_CONFIG_DYNAMICCONFIGTYPE";

    private static final String SERMANT_SERVICE_CENTER_KEY = "REGISTER_SERVICE_ADDRESS";

    private static final String SERMANT_SERVICE_CENTER_TYPE_KEY = "REGISTER_SERVICE_REGISTERTYPE";

    @Autowired
    private ObjectMapper om;

    @Value("${sermant-agent.image.addr:}")
    private String imageAddr;

    @Value("${sermant-agent.image.pullPolicy:Always}")
    private String pullPolicy;

    @Value("${sermant-agent.mount.path:/home/sermant-agent}")
    private String mountPath;

    @Value("${sermant-agent.configMap:}")
    private String envFrom;

    @Value("${sermant-agent.config.type:ZOOKEEPER}")
    private String configType;

    @Value("${sermant-agent.config.address:http://localhost:2181}")
    private String configAddress;

    @Value("${sermant-agent.service.address:http://localhost:30100}")
    private String serviceAddress;

    @Value("${sermant-agent.health.check.port:16688}")
    private int healthCheckPort;

    @Value("${sermant-agent.service.type:SERVICE_COMB}")
    private String serviceType;

    /**
     * 准入控制器接口
     *
     * @param request 请求
     * @return 响应
     */
    @PostMapping(path = "/admission")
    public WebhookResponse handleAdmissionReviewRequest(@RequestBody ObjectNode request) {
        LOGGER.debug("request is {}:", request.toString().replace(System.lineSeparator(), "_"));
        return handleAdmission(request);
    }

    private WebhookResponse handleAdmission(ObjectNode body) {
        return new WebhookResponse(body.required(API_VERSION_PATH).asText(), body.required(KIND_PATH).asText(),
                modifyRequest(body));
    }

    private Response modifyRequest(ObjectNode body) {
        String uid = body.path(REQUEST_PATH).required(UID_PATH).asText();
        return inject(body)
                .map(str -> new Response(uid, Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8))))
                .orElseGet(() -> new Response(uid, null));
    }

    private Optional<String> inject(ObjectNode body) {
        JsonNode specNode = body.path(REQUEST_PATH).path(OBJECT_PATH).path(SPEC_PATH);

        // 获取容器的节点，便于遍历取值
        JsonNode containersNode = specNode.path(CONTAINERS_PATH);

        // 缓存每个容器的环境变量
        Map<Integer, Map<String, String>> containerEnv = new HashMap<>();

        // 缓存容器的env
        setEnv(containersNode, containerEnv);

        // 建一个json节点
        ArrayNode arrayNode = om.createArrayNode();

        // 新增initContainers节点
        injectInitContainer(arrayNode, specNode);

        // 新增volumes节点
        injectVolumes(arrayNode, specNode);

        // 遍历所有容器进行织入
        containerEnv.forEach((index, env) -> {
            String containerPath = Stream.of(SPEC_PATH, CONTAINERS_PATH, index.toString())
                    .collect(Collectors.joining(PATH_SEPARATOR, PATH_SEPARATOR, PATH_SEPARATOR));
            JsonNode containerNode = containersNode.path(index);

            // 向容器新增lifecycle节点
            injectLifecycle(arrayNode, env, containerNode, containerPath);

            // 向容器新增readinessProbe节点
            injectReadinessProbe(arrayNode, env, containerNode, containerPath);

            // 向容器新增configMapRef节点
            injectEnvFrom(arrayNode, env, containerNode, containerPath);

            // 向容器新增env节点
            injectEnv(arrayNode, env, containerNode, containerPath);

            // 向容器新增volumeMounts节点
            injectVolumeMounts(arrayNode, env, containerNode, containerPath);
        });
        LOGGER.info("arrayNode is: {}.", arrayNode.toString());
        return Optional.of(arrayNode.toString());
    }

    private void setEnv(JsonNode containersPath, Map<Integer, Map<String, String>> containerEnv) {
        Iterator<JsonNode> containerIterator = containersPath.elements();
        int index = 0;
        while (containerIterator.hasNext()) {
            containerEnv.put(index, new HashMap<>());
            JsonNode nextContainer = containerIterator.next();
            Iterator<JsonNode> envIterator = nextContainer.path(ENV_PATH).elements();
            while (envIterator.hasNext()) {
                JsonNode nextEnv = envIterator.next();
                containerEnv.get(index).put(nextEnv.path(NAME_KEY).asText(), nextEnv.path(VALUE_KEY).asText());
            }
            index++;
        }
    }

    private void injectInitContainer(ArrayNode arrayNode, JsonNode specPath) {
        // 建一个initContainer
        ObjectNode initContainerNode = putOrAddObject(arrayNode, specPath, INIT_CONTAINERS_PATH,
                INIT_CONTAINERS_INJECT_PATH);

        // 镜像名
        initContainerNode.put(NAME_KEY, IMAGE_NAME);

        // 镜像地址
        initContainerNode.put(IMAGE_KEY, imageAddr);

        // 镜像拉取策略
        initContainerNode.put(IMAGE_PULL_POLICY_KEY, pullPolicy);

        // 初始化命令
        initContainerNode.putArray(COMMAND_KEY).addAll(INIT_COMMAND);

        // 建一个volumeMount
        ObjectNode initContainerVolumeMountNode = initContainerNode.putArray(VOLUME_MOUNTS_PATH).addObject();

        // 磁盘名
        initContainerVolumeMountNode.put(NAME_KEY, VOLUME_NAME);

        // 磁盘路径
        initContainerVolumeMountNode.put(MOUNT_PATH, INIT_SERMANT_PATH);
    }

    private void injectVolumes(ArrayNode arrayNode, JsonNode specPath) {
        // 建一个volume
        ObjectNode volumeNode = putOrAddObject(arrayNode, specPath, VOLUMES_PATH, VOLUMES_INJECT_PATH);

        // 磁盘名
        volumeNode.put(NAME_KEY, VOLUME_NAME);

        // 磁盘
        volumeNode.putObject(VOLUME_DIR);
    }

    private void injectLifecycle(ArrayNode arrayNode, Map<String, String> env, JsonNode containerNode,
                                 String containerPath) {
        boolean enableSpring = getNotEmptyValue(env, ENABLE_SPRING_KEY, true, Boolean::parseBoolean);
        boolean enableGraceShutDown = getNotEmptyValue(env, ENABLE_GRACE_SHUTDOWN_KEY, true,
                Boolean::parseBoolean);
        boolean enableOfflineNotify = getNotEmptyValue(env, ENABLE_OFFLINE_NOTIFY_KEY, true,
                Boolean::parseBoolean);

        // 未开启spring优雅上下线/未开启优雅下线/未开启下线通知，则不注入
        if (!enableSpring || !enableGraceShutDown || !enableOfflineNotify) {
            return;
        }

        // 向新容器新增Lifecycle>preStop>exec>command节点
        ObjectNode objectNode = arrayNode.addObject();
        objectNode.put(JSON_OPERATION_KEY, JSON_OPERATION_ADD);
        int port = getNotEmptyValue(env, HTTP_SERVER_PORT_KEY, healthCheckPort, Integer::parseInt);
        if (!containerNode.hasNonNull(LIFECYCLE_PATH)) {
            // lifecycle为null，建一个lifecycle
            objectNode.put(PATH_KEY, containerPath + LIFECYCLE_PATH);

            // 建一个preStop
            ObjectNode preStopNode = objectNode.putObject(VALUE_KEY).putObject(PRE_STOP_PATH);

            // 建一个exec和command
            addExecAndCommand(preStopNode, port);
            return;
        }
        if (!containerNode.path(LIFECYCLE_PATH).hasNonNull(PRE_STOP_PATH)) {
            // lifecycle不为null, preStop为null, 建一个preStop
            objectNode.put(PATH_KEY, containerPath + LIFECYCLE_PATH + PATH_SEPARATOR + PRE_STOP_PATH);
            ObjectNode preStopNode = objectNode.putObject(VALUE_KEY);

            // 建一个exec和command
            addExecAndCommand(preStopNode, port);
            return;
        }
        if (containerNode.path(LIFECYCLE_PATH).path(PRE_STOP_PATH).hasNonNull(HTTP_GET_PATH)) {
            // lifecycle不为null, preStop不为null, 存在httpGet情况无法再织入command，直接return
            return;
        }

        // lifecycle不为null, preStop为null, 且已存在command，需要添加command
        addCommands(objectNode, containerPath, containerNode, port);
    }

    private void injectReadinessProbe(ArrayNode arrayNode, Map<String, String> env, JsonNode containerNode,
                                      String containerPath) {
        int periodSeconds = getNotEmptyValue(env, K8S_READINESS_WAIT_TIME_KEY, 1, Integer::parseInt);
        boolean enableSpring = getNotEmptyValue(env, ENABLE_SPRING_KEY, true, Boolean::parseBoolean);
        boolean enableHealthCheck = getNotEmptyValue(env, ENABLE_HEALTH_CHECK_KEY, false, Boolean::parseBoolean);

        // 原来已经有readinessProbe节点/periodSeconds<=0/未开启健康检查/未开启spring优雅上下线，则不注入
        if (containerNode.hasNonNull(READINESS_PROBE_PATH) || periodSeconds <= 0 || !enableHealthCheck
                || !enableSpring) {
            return;
        }

        // 向容器新增readinessProbe节点
        ObjectNode readinessProbeNode = arrayNode.addObject();
        readinessProbeNode.put(JSON_OPERATION_KEY, JSON_OPERATION_ADD);
        readinessProbeNode.put(PATH_KEY, containerPath + READINESS_PROBE_PATH);

        // 建一个readinessProbe
        ObjectNode readinessProbeObjectNode = readinessProbeNode.putObject(VALUE_KEY);
        readinessProbeObjectNode.put(INITIAL_DELAY_SECONDS_PATH, 1);
        readinessProbeObjectNode.put(PERIOD_SECONDS_PATH, periodSeconds);
        ObjectNode httpGetNode = readinessProbeObjectNode.putObject(HTTP_GET_PATH);
        int port = getNotEmptyValue(env, HTTP_SERVER_PORT_KEY, healthCheckPort, Integer::parseInt);
        httpGetNode.put(PORT_PATH, port);
        httpGetNode.put(PATH_KEY, DEFAULT_HEALTH_CHECK_PATH);
    }

    private void injectEnvFrom(ArrayNode arrayNode, Map<String, String> env, JsonNode containerNode,
                               String containerPath) {
        String configMap = getNotEmptyValue(env, ENV_CONFIG_MAP_REF_KEY, envFrom, value -> value);
        if (!StringUtils.hasText(configMap)) {
            return;
        }

        // 向容器新增envFrom节点
        ObjectNode envFromNode = putOrAddObject(arrayNode, containerNode, ENV_FROM_PATH,
                containerPath + ENV_FROM_PATH);

        // 新建一个configMapRef
        ObjectNode configMapRefNode = envFromNode.putObject(CONFIG_MAP_REF_PATH);
        configMapRefNode.put(NAME_KEY, configMap);
    }

    private void injectEnv(ArrayNode arrayNode, Map<String, String> env, JsonNode containerNode, String containerPath) {
        // 覆盖容器的env节点
        ObjectNode envNode = arrayNode.addObject();
        envNode.put(JSON_OPERATION_KEY, JSON_OPERATION_ADD);
        envNode.put(PATH_KEY, containerPath + ENV_PATH);
        ArrayNode envArray = envNode.putArray(VALUE_KEY);

        // 如果原来有env，则先存入原来的env
        if (containerNode.hasNonNull(ENV_PATH)) {
            Iterator<JsonNode> elements = containerNode.path(ENV_PATH).elements();
            while (elements.hasNext()) {
                envArray.add(elements.next());
            }
        }

        // agent磁盘路径
        String realMountPath = getNotEmptyValue(env, ENV_MOUNT_PATH_KEY, mountPath, value -> value);

        // jvm启动命令
        String jvmOptions = JVM_OPTIONS_VALUE_PREFIX + realMountPath + JVM_OPTIONS_VALUE_SUFFIX;
        String envJvmOptions = env.get(JVM_OPTIONS_KEY);
        if (StringUtils.hasText(envJvmOptions)) {
            jvmOptions = jvmOptions + envJvmOptions;
        }

        // 注入jvm启动命令
        addEnv(envArray, JVM_OPTIONS_KEY, jvmOptions);

        if (!StringUtils.hasText(env.get(SERMANT_CONFIG_TYPE_KEY))) {
            addEnv(envArray, SERMANT_CONFIG_TYPE_KEY, configType);
        }
        if (!StringUtils.hasText(env.get(SERMANT_CONFIG_CENTER_KEY))) {
            addEnv(envArray, SERMANT_CONFIG_CENTER_KEY, configAddress);
        }
        if (!StringUtils.hasText(env.get(SERMANT_SERVICE_CENTER_KEY))) {
            addEnv(envArray, SERMANT_SERVICE_CENTER_KEY, serviceAddress);
        }
        if (!StringUtils.hasText(env.get(SERMANT_SERVICE_CENTER_TYPE_KEY))) {
            addEnv(envArray, SERMANT_SERVICE_CENTER_TYPE_KEY, serviceType);
        }
    }

    private void injectVolumeMounts(ArrayNode arrayNode, Map<String, String> env, JsonNode containerNode,
                                    String containerPath) {
        // 向容器新增volumeMounts节点
        ObjectNode containerVolumeNode = putOrAddObject(arrayNode, containerNode, VOLUME_MOUNTS_PATH,
                containerPath + VOLUME_MOUNTS_PATH);

        // 磁盘名
        containerVolumeNode.put(NAME_KEY, VOLUME_NAME);

        // 磁盘路径
        String realMountPath = getNotEmptyValue(env, ENV_MOUNT_PATH_KEY, mountPath, value -> value);
        containerVolumeNode.put(MOUNT_PATH, realMountPath);
    }

    private <T> T getNotEmptyValue(Map<String, String> map, String key, T defaultValue, Function<String, T> mapper) {
        String value = map.get(key);
        return StringUtils.hasText(value) ? mapper.apply(value) : defaultValue;
    }

    private void addEnv(ArrayNode envArray, String key, String value) {
        // 建一个env对象
        ObjectNode envNode = envArray.addObject();

        // 注入env
        envNode.put(NAME_KEY, key);
        envNode.put(VALUE_KEY, value);
    }

    private void addCommands(ObjectNode objectNode, String containerPath, JsonNode containerNode, int port) {
        objectNode.put(PATH_KEY, containerPath + LIFECYCLE_PATH + PATH_SEPARATOR + PRE_STOP_PATH + PATH_SEPARATOR
                + EXEC_PATH + PATH_SEPARATOR + COMMAND_KEY);
        JsonNode commandsPath = containerNode.path(LIFECYCLE_PATH).path(PRE_STOP_PATH).path(EXEC_PATH)
                .path(COMMAND_KEY);
        Iterator<JsonNode> iterator = commandsPath.elements();
        ArrayNode commands = objectNode.putArray(VALUE_KEY);
        int index = 0;
        int size = commandsPath.size();
        while (iterator.hasNext()) {
            JsonNode next = iterator.next();
            String command = next.asText();
            if (index == size - 1) {
                command += ";" + PRE_STOP_COMMAND_PREFIX + port + PRE_STOP_COMMAND_SUFFIX;
            }
            commands.add(command);
            index++;
        }
    }

    private void addExecAndCommand(ObjectNode preStopNode, int port) {
        ObjectNode execNode = preStopNode.putObject(EXEC_PATH);
        ArrayNode commands = execNode.putArray(COMMAND_KEY);
        commands.addAll(PRE_STOP_COMMANDS);
        commands.add(PRE_STOP_COMMAND_PREFIX + port + PRE_STOP_COMMAND_SUFFIX);
    }

    private ObjectNode putOrAddObject(ArrayNode arrayNode, JsonNode jsonNode, String path, String injectPath) {
        // 新增一个节点
        ObjectNode node = arrayNode.addObject();
        node.put(JSON_OPERATION_KEY, JSON_OPERATION_ADD);
        ObjectNode objectNode;
        if (jsonNode.hasNonNull(path)) {
            // 如果之前有，则放到最后面
            node.put(PATH_KEY, injectPath + END_PATH);
            objectNode = node.putObject(VALUE_KEY);
        } else {
            // 没有则建一个
            node.put(PATH_KEY, injectPath);
            objectNode = node.putArray(VALUE_KEY).addObject();
        }
        return objectNode;
    }
}