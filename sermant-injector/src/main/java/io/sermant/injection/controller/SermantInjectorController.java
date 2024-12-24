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

package io.sermant.injection.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import io.sermant.injection.dto.Response;
import io.sermant.injection.dto.WebhookResponse;

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

    private static final String METADATA_PATH = "metadata";

    private static final String ANNOTATION_PATH = "annotations";

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

    private static final String SERMANT_AGENT_INIT_CONTAINER_NAME = "sermant-agent";

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

    private static final String SERMANT_ENV_PREFIX = "env.sermant.io/";

    private static final String EXTERNAL_AGENT_INJECTION_CONFIG_KEY = "agent_config_externalAgent_injection";

    private static final String EXTERNAL_AGENT_NAME_CONFIG_KEY = "agent_config_externalAgent_name";

    private static final String EXTERNAL_AGENT_FILE_CONFIG_KEY = "agent_config_externalAgent_file";

    private static final String EXTERNAL_AGENT_INIT_CONTAINER_NAME = "external-agent";

    /**
     * if external agent injection is to be enabled, the annotations should be config as
     * 'env.sermant.io/external.agent.injection:"OTEL"' in spec -> template ->metadata -> annotations
     */
    private static final String EXTERNAL_AGENT_INJECTION_ENV_IN_ANNOTATION = "external.agent.injection";

    @Autowired
    private ObjectMapper om;

    @Value("${sermant-agent.image.addr:}")
    private String imageAddr;

    @Value("${sermant-agent.image.pullPolicy:Always}")
    private String pullPolicy;

    @Value("${sermant-agent.mount.path:/home/sermant-agent}")
    private String mountPath;

    @Value("${sermant-agent.configMap:sermant-agent-env}")
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

    @Value("${sermant-agent.inject.action:before}")
    private String action;

    @Value("${sermant-agent.externalAgent.imageAddr:}")
    private String externalAgentAddr;

    @Value("${sermant-agent.externalAgent.fileName:}")
    private String externalAgentFileName;

    /**
     * Admission controller
     *
     * @param request request
     * @return response
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

        // Get the node of the container for traversing the value
        JsonNode containersNode = specNode.path(CONTAINERS_PATH);

        // Cache environment variables for each container
        Map<Integer, Map<String, String>> containerEnv = new HashMap<>();

        // Cache env of container
        setEnv(containersNode, containerEnv);

        // Calculate env to be added based on labels
        Map<String, String> annotationEnv = new HashMap<>();
        JsonNode annotationNode = body.path(REQUEST_PATH).path(OBJECT_PATH).path(METADATA_PATH).path(ANNOTATION_PATH);
        setEnvByMap(annotationNode, annotationEnv);

        // Create a json node
        ArrayNode arrayNode = om.createArrayNode();

        // Added initContainers node
        injectSermantInitContainer(arrayNode, specNode);

        injectExternalAgentInitContainer(arrayNode, annotationEnv);

        // Add volume nodes
        injectVolumes(arrayNode, specNode);

        // Traverse all containers for weaving
        containerEnv.forEach((index, env) -> {
            String containerPath = Stream.of(SPEC_PATH, CONTAINERS_PATH, index.toString())
                    .collect(Collectors.joining(PATH_SEPARATOR, PATH_SEPARATOR, PATH_SEPARATOR));
            JsonNode containerNode = containersNode.path(index);

            // Add a lifecycle node to the container
            injectLifecycle(arrayNode, env, containerNode, containerPath);

            // Add a readinessProbe node to the container
            injectReadinessProbe(arrayNode, env, containerNode, containerPath);

            // Add a configMapRef node to the container
            injectEnvFrom(arrayNode, env, containerNode, containerPath);

            // Add an env node to the container
            injectEnv(arrayNode, env, containerNode, containerPath, annotationEnv);

            // Add a volumeMounts node to the container
            injectVolumeMounts(arrayNode, env, containerNode, containerPath);
        });
        LOGGER.info("arrayNode is: {}.", arrayNode.toString());
        return Optional.of(arrayNode.toString());
    }

    /**
     * Calculate environment variables that need to be added based on labels/annotations, taking labels as an example:
     * labels:
     *   env.sermant.io/[key1]: [value1]
     *   env.sermant.io/[key2]: [value2]
     * key1:value1 and key2:value2 are added to environment variable map. The function itself doesn't force whether
     * labels or annotations are used, just that srcMap represents the map under label/annotations.
     *
     * @param srcMap Source environment variable kv
     * @param tgtEnv The processed environment variable kv
     */
    private void setEnvByMap(JsonNode srcMap, Map<String, String> tgtEnv) {
        Iterator<String> labelIter = srcMap.fieldNames();
        int prefixLength = SERMANT_ENV_PREFIX.length();
        while (labelIter.hasNext()) {
            String labelName = labelIter.next();
            if (labelName.startsWith(SERMANT_ENV_PREFIX)) {
                String envKey = labelName.substring(prefixLength);
                String envValue = srcMap.findValue(labelName).textValue();
                tgtEnv.put(envKey, envValue);
                if (envKey.equals(EXTERNAL_AGENT_INJECTION_ENV_IN_ANNOTATION)) {
                    tgtEnv.put(EXTERNAL_AGENT_INJECTION_CONFIG_KEY, Boolean.TRUE.toString());
                    tgtEnv.put(EXTERNAL_AGENT_NAME_CONFIG_KEY, envValue);
                    tgtEnv.put(EXTERNAL_AGENT_FILE_CONFIG_KEY, mountPath + PATH_SEPARATOR + externalAgentFileName);
                }
            }
        }
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

    private void injectSermantInitContainer(ArrayNode arrayNode, JsonNode specPath) {
        ObjectNode sermantInitContainerNode = putOrAddObject(arrayNode, specPath, INIT_CONTAINERS_PATH,
                INIT_CONTAINERS_INJECT_PATH);

        constructInitContainerNode(sermantInitContainerNode, SERMANT_AGENT_INIT_CONTAINER_NAME, imageAddr,
                INIT_COMMAND);
    }

    private void injectExternalAgentInitContainer(ArrayNode arrayNode, Map<String, String> annotationEnv) {
        if (!StringUtils.hasText(annotationEnv.get(EXTERNAL_AGENT_INJECTION_ENV_IN_ANNOTATION))) {
            return;
        }

        ObjectNode node = arrayNode.addObject();
        node.put(JSON_OPERATION_KEY, JSON_OPERATION_ADD);
        node.put(PATH_KEY, INIT_CONTAINERS_INJECT_PATH + END_PATH);

        ObjectNode externalAgentInitContainerNode = node.putObject(VALUE_KEY);
        List<JsonNode> initCommandForExternalAgent =
                Arrays.asList(new TextNode("cp"), new TextNode("/home/" + externalAgentFileName),
                        new TextNode(mountPath));
        constructInitContainerNode(externalAgentInitContainerNode, EXTERNAL_AGENT_INIT_CONTAINER_NAME,
                externalAgentAddr, initCommandForExternalAgent);
    }

    private void constructInitContainerNode(ObjectNode initContainerNode, String containerName, String image,
            List<JsonNode> command) {
        // Image name
        initContainerNode.put(NAME_KEY, containerName);

        // Image address
        initContainerNode.put(IMAGE_KEY, image);

        // Image pulling policy
        initContainerNode.put(IMAGE_PULL_POLICY_KEY, pullPolicy);

        // Initialization command
        initContainerNode.putArray(COMMAND_KEY).addAll(command);

        // Create a volumeMount
        ObjectNode externalAgentVolumeMountNode = initContainerNode.putArray(VOLUME_MOUNTS_PATH)
                .addObject();

        // Disk name
        externalAgentVolumeMountNode.put(NAME_KEY, VOLUME_NAME);

        // Disk path
        externalAgentVolumeMountNode.put(MOUNT_PATH, INIT_SERMANT_PATH);
    }

    private void injectVolumes(ArrayNode arrayNode, JsonNode specPath) {
        // Create a volume
        ObjectNode volumeNode = putOrAddObject(arrayNode, specPath, VOLUMES_PATH, VOLUMES_INJECT_PATH);

        // Disk name
        volumeNode.put(NAME_KEY, VOLUME_NAME);

        // Disk
        volumeNode.putObject(VOLUME_DIR);
    }

    private void injectLifecycle(ArrayNode arrayNode, Map<String, String> env, JsonNode containerNode,
            String containerPath) {
        boolean enableSpring = getNotEmptyValue(env, ENABLE_SPRING_KEY, true, Boolean::parseBoolean);
        boolean enableGraceShutDown = getNotEmptyValue(env, ENABLE_GRACE_SHUTDOWN_KEY, true,
                Boolean::parseBoolean);
        boolean enableOfflineNotify = getNotEmptyValue(env, ENABLE_OFFLINE_NOTIFY_KEY, true,
                Boolean::parseBoolean);

        // If spring Graceful Online/Offline notification is not enabled or Graceful offline/offline notification is not
        // enabled, no injection is performed
        if (!enableSpring || !enableGraceShutDown || !enableOfflineNotify) {
            return;
        }

        // Add a Lifecycle>preStop>exec>command node to the new container
        ObjectNode objectNode = arrayNode.addObject();
        objectNode.put(JSON_OPERATION_KEY, JSON_OPERATION_ADD);
        int port = getNotEmptyValue(env, HTTP_SERVER_PORT_KEY, healthCheckPort, Integer::parseInt);
        if (!containerNode.hasNonNull(LIFECYCLE_PATH)) {
            // lifecycle is null. Create a lifecycle
            objectNode.put(PATH_KEY, containerPath + LIFECYCLE_PATH);

            // Create a preStop
            ObjectNode preStopNode = objectNode.putObject(VALUE_KEY).putObject(PRE_STOP_PATH);

            // Create an exec and command
            addExecAndCommand(preStopNode, port);
            return;
        }
        if (!containerNode.path(LIFECYCLE_PATH).hasNonNull(PRE_STOP_PATH)) {
            // lifecycle is not null and preStop is null. Create a preStop
            objectNode.put(PATH_KEY, containerPath + LIFECYCLE_PATH + PATH_SEPARATOR + PRE_STOP_PATH);
            ObjectNode preStopNode = objectNode.putObject(VALUE_KEY);

            // Create an exec and command
            addExecAndCommand(preStopNode, port);
        }
    }

    private void injectReadinessProbe(ArrayNode arrayNode, Map<String, String> env, JsonNode containerNode,
            String containerPath) {
        int periodSeconds = getNotEmptyValue(env, K8S_READINESS_WAIT_TIME_KEY, 1, Integer::parseInt);
        boolean enableSpring = getNotEmptyValue(env, ENABLE_SPRING_KEY, true, Boolean::parseBoolean);
        boolean enableHealthCheck = getNotEmptyValue(env, ENABLE_HEALTH_CHECK_KEY, false, Boolean::parseBoolean);

        // If there is already a readinessProbe node / period Seconds<=0 / If health check is not enabled/spring
        // graceful online/offline is not enabled, no injection is performed
        if (containerNode.hasNonNull(READINESS_PROBE_PATH) || periodSeconds <= 0 || !enableHealthCheck
                || !enableSpring) {
            return;
        }

        // Add a readinessProbe node to the container
        ObjectNode readinessProbeNode = arrayNode.addObject();
        readinessProbeNode.put(JSON_OPERATION_KEY, JSON_OPERATION_ADD);
        readinessProbeNode.put(PATH_KEY, containerPath + READINESS_PROBE_PATH);

        // Create a readinessProbe
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

        // Add an envFrom node to the container
        ObjectNode envFromNode = putOrAddObject(arrayNode, containerNode, ENV_FROM_PATH,
                containerPath + ENV_FROM_PATH);

        // Create a configMapRef
        ObjectNode configMapRefNode = envFromNode.putObject(CONFIG_MAP_REF_PATH);
        configMapRefNode.put(NAME_KEY, configMap);
    }

    private void injectEnv(ArrayNode arrayNode, Map<String, String> env, JsonNode containerNode, String containerPath,
            Map<String, String> annotationEnv) {
        // Override the env node of the container
        ObjectNode envNode = arrayNode.addObject();
        envNode.put(JSON_OPERATION_KEY, JSON_OPERATION_ADD);
        envNode.put(PATH_KEY, containerPath + ENV_PATH);
        ArrayNode envArray = envNode.putArray(VALUE_KEY);

        // If there is an env, store into the original env first
        if (containerNode.hasNonNull(ENV_PATH)) {
            Iterator<JsonNode> elements = containerNode.path(ENV_PATH).elements();
            while (elements.hasNext()) {
                JsonNode next = elements.next();

                // The JAVA_TOOL_OPTIONS depend on injector injection
                if (!JVM_OPTIONS_KEY.equals(next.get(NAME_KEY).asText())) {
                    envArray.add(next);
                }
            }
        }

        // agent disk path
        String realMountPath = getNotEmptyValue(env, ENV_MOUNT_PATH_KEY, mountPath, value -> value);

        // jvm startup command
        String jvmOptions = getJavaToolOptions(JVM_OPTIONS_VALUE_PREFIX + realMountPath + JVM_OPTIONS_VALUE_SUFFIX,
                env.get(JVM_OPTIONS_KEY));

        // inject the jvm startup command
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

        // add env by specifying it on /metadata/annotations
        // By default, if there is a new setting in annotations, the existing env setting will be overwritten.
        for (Map.Entry<String, String> entry : annotationEnv.entrySet()) {
            addEnv(envArray, entry.getKey(), entry.getValue());
        }
    }

    private String getJavaToolOptions(String injectOptions, String originOptions) {
        if (!StringUtils.hasText(originOptions)) {
            return injectOptions;
        }
        if ("after".equalsIgnoreCase(action)) {
            return originOptions + injectOptions;
        }
        if ("ignore".equalsIgnoreCase(action)) {
            return originOptions;
        }
        return injectOptions + originOptions;
    }

    private void injectVolumeMounts(ArrayNode arrayNode, Map<String, String> env, JsonNode containerNode,
            String containerPath) {
        // Add a volumeMounts node to the container
        ObjectNode containerVolumeNode = putOrAddObject(arrayNode, containerNode, VOLUME_MOUNTS_PATH,
                containerPath + VOLUME_MOUNTS_PATH);

        // Disk name
        containerVolumeNode.put(NAME_KEY, VOLUME_NAME);

        // Disk path
        String realMountPath = getNotEmptyValue(env, ENV_MOUNT_PATH_KEY, mountPath, value -> value);
        containerVolumeNode.put(MOUNT_PATH, realMountPath);
    }

    private <T> T getNotEmptyValue(Map<String, String> map, String key, T defaultValue, Function<String, T> mapper) {
        String value = map.get(key);
        return StringUtils.hasText(value) ? mapper.apply(value) : defaultValue;
    }

    private void addEnv(ArrayNode envArray, String key, String value) {
        // Create an env object
        ObjectNode envNode = envArray.addObject();

        // Inject env
        envNode.put(NAME_KEY, key);
        envNode.put(VALUE_KEY, value);
    }

    private void addExecAndCommand(ObjectNode preStopNode, int port) {
        ObjectNode execNode = preStopNode.putObject(EXEC_PATH);
        ArrayNode commands = execNode.putArray(COMMAND_KEY);
        commands.addAll(PRE_STOP_COMMANDS);
        commands.add(PRE_STOP_COMMAND_PREFIX + port + PRE_STOP_COMMAND_SUFFIX);
    }

    private ObjectNode putOrAddObject(ArrayNode arrayNode, JsonNode jsonNode, String path, String injectPath) {
        // Add a node
        ObjectNode node = arrayNode.addObject();
        node.put(JSON_OPERATION_KEY, JSON_OPERATION_ADD);
        ObjectNode objectNode;
        if (jsonNode.hasNonNull(path)) {
            // If exists before, put it at the end
            node.put(PATH_KEY, injectPath + END_PATH);
            objectNode = node.putObject(VALUE_KEY);
        } else {
            // If no one exists, create new one
            node.put(PATH_KEY, injectPath);
            objectNode = node.putArray(VALUE_KEY).addObject();
        }
        return objectNode;
    }
}
