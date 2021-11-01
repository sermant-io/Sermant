/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.configuration.service;

import com.alibaba.fastjson.JSONObject;
import com.huawei.route.server.common.Result;
import com.huawei.route.server.labels.configuration.Configuration;
import com.huawei.route.server.labels.configuration.ConfigurationVo;
import com.huawei.route.server.labels.configuration.EditEnvInfo;
import com.huawei.route.server.labels.configuration.EnvInfo;
import com.huawei.route.server.labels.exception.CustomGenericException;
import com.huawei.route.server.labels.heartbeat.AgentHeartbeat;
import com.huawei.route.server.labels.util.PatternUtil;
import com.huawei.route.server.labels.util.ZookeeperUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.curator.framework.api.transaction.TransactionOp;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.huawei.route.server.labels.constant.LabelConstant.CONFIGURATION_DESCRIPTION;
import static com.huawei.route.server.labels.constant.LabelConstant.ERROR_CODE_ONE;
import static com.huawei.route.server.labels.constant.LabelConstant.ZK_SEPARATOR;

/**
 * 配置管理实现类
 *
 * @author Zhang Hu
 * @since 2021-04-15
 */
@Service
public class ConfigurationServiceImpl implements ConfigurationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

    private static final String GENERAL_PAAS_CONFIGURATIONS = "/general-paas/configurations";

    private final CuratorFramework zkClient;

    @Autowired
    private AgentHeartbeat agentHeartbeat;

    @Autowired
    public ConfigurationServiceImpl(CuratorFramework zkClient) {
        this.zkClient = zkClient;
    }

    @PostConstruct
    public void init() {
        try {
            if (!ZookeeperUtil.isNodeExist(zkClient, GENERAL_PAAS_CONFIGURATIONS)) {
                zkClient.create().creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(GENERAL_PAAS_CONFIGURATIONS, "".getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            throw new RuntimeException("zookeeper connect failed.");
        }
    }

    @Override
    public Result<String> addConfiguration(Configuration configuration) {
        PatternUtil.checkConfigName(configuration.getConfigName());
        Result<String> result;
        String nodePath = GENERAL_PAAS_CONFIGURATIONS + ZK_SEPARATOR + configuration.getConfigName();
        String descriptionPath = nodePath + ZK_SEPARATOR + CONFIGURATION_DESCRIPTION;

        try {
            if (ZookeeperUtil.isNodeExist(zkClient, nodePath)) {
                result = Result.ofFail(ERROR_CODE_ONE, "配置名已经存在");
            } else {
                TransactionOp transactionOp = zkClient.transactionOp();
                List<CuratorOp> curatorOps = new ArrayList<>();

                // 创建配置名节点
                CuratorOp configNameNode = transactionOp.create()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(nodePath, configuration.getConfigValue().getBytes(StandardCharsets.UTF_8));
                curatorOps.add(configNameNode);

                // 创建配置下的全局节点，对所有使用访问该节点的应用生效
                CuratorOp globalNode = transactionOp.create()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(descriptionPath, configuration.getDescription().getBytes(StandardCharsets.UTF_8));
                curatorOps.add(globalNode);

                EnvInfo[] envInfos = configuration.getEnvs();

                // 将所有的环境配置在zookeeper中创建
                if (envInfos != null && envInfos.length > 0) {
                    for (EnvInfo envInfo : envInfos) {
                        String model = nodePath + ZK_SEPARATOR + envInfo.getModel();
                        CuratorOp modelNode = transactionOp.create()
                                .withMode(CreateMode.PERSISTENT)
                                .forPath(model, envInfo.getValue().getBytes(StandardCharsets.UTF_8));
                        curatorOps.add(modelNode);
                    }
                }

                zkClient.transaction().forOperations(curatorOps);
                result = Result.ofSuccessMsg("配置新增成功");
            }
        } catch (Exception exception) {
            LOGGER.error("Add Configuration failed.", exception);
            result = Result.ofFail(ERROR_CODE_ONE, "配置新增失败");
        }
        return result;
    }

    @Override
    public Result<String> updateConfiguration(Configuration configuration) {
        PatternUtil.checkConfigName(configuration.getConfigName());
        Result<String> result;
        String nodePath = GENERAL_PAAS_CONFIGURATIONS + ZK_SEPARATOR + configuration.getConfigName();
        EnvInfo[] envInfos = configuration.getEnvs();
        String descriptionPath = nodePath + ZK_SEPARATOR + CONFIGURATION_DESCRIPTION;
        try {
            if (!ZookeeperUtil.isNodeExist(zkClient, nodePath)) {
                result = Result.ofFail(ERROR_CODE_ONE, "配置名不存在");
            } else {
                // 获取configurations节点下的所有环境配置子节点
                List<String> envList = zkClient.getChildren().forPath(nodePath);
                if (!CollectionUtils.isEmpty(envList)) {
                    // 获取取消的环境配置
                    Iterator<String> it = envList.iterator();
                    List<String> envs = Arrays.stream(envInfos).map(EnvInfo::getModel).collect(Collectors.toList());
                    while (it.hasNext()) {
                        String model = it.next();
                        if (CONFIGURATION_DESCRIPTION.equals(model) || envs.contains(model)) {
                            it.remove();
                        }
                    }
                }
                TransactionOp transactionOp = zkClient.transactionOp();
                CuratorOp configNameUpdateNode = transactionOp.setData()
                        .forPath(nodePath, configuration.getConfigValue().getBytes(StandardCharsets.UTF_8));
                CuratorOp globalUpdateNode = transactionOp.setData()
                        .forPath(descriptionPath, configuration.getDescription().getBytes(StandardCharsets.UTF_8));

                List<CuratorOp> curatorOps = new ArrayList<>();
                if (envInfos.length > 0) {
                    for (EnvInfo envInfo : envInfos) {
                        String model = nodePath + ZK_SEPARATOR + envInfo.getModel();
                        if (zkClient.checkExists().forPath(model) == null) {
                            CuratorOp updateAddNode = transactionOp.create()
                                    .forPath(model, envInfo.getValue().getBytes(StandardCharsets.UTF_8));
                            curatorOps.add(updateAddNode);
                        } else {
                            CuratorOp modelUpdateNode = transactionOp.setData()
                                    .forPath(model, envInfo.getValue().getBytes(StandardCharsets.UTF_8));
                            curatorOps.add(modelUpdateNode);
                        }
                    }
                }
                if (!CollectionUtils.isEmpty(envList)) {
                    for (String env : envList) {
                        // 删除取消的环境配置
                        curatorOps.add(transactionOp.delete().forPath(nodePath + ZK_SEPARATOR + env));
                    }
                }
                curatorOps.add(globalUpdateNode);
                curatorOps.add(configNameUpdateNode);
                zkClient.transaction().forOperations(curatorOps);
                result = Result.ofSuccessMsg("配置修改成功");
            }
        } catch (Exception exception) {
            LOGGER.error("Update Configuration failed.", exception);
            result = Result.ofFail(ERROR_CODE_ONE, "配置修改失败");
        }
        return result;
    }

    @Override
    public Result<String> deleteConfiguration(String configName) {
        if (!StringUtils.hasText(configName)) {
            return Result.ofFail(ERROR_CODE_ONE, "配置名不能为空");
        }
        PatternUtil.checkConfigName(configName);
        Result<String> result;
        String nodePath = GENERAL_PAAS_CONFIGURATIONS + ZK_SEPARATOR + configName;
        try {
            if (!ZookeeperUtil.isNodeExist(zkClient, nodePath)) {
                result = Result.ofFail(ERROR_CODE_ONE, "配置名不存在");
            } else {
                zkClient.delete().deletingChildrenIfNeeded().forPath(nodePath);
                result = Result.ofSuccessMsg("配置删除成功");
            }
        } catch (Exception e) {
            LOGGER.error("Delete Configuration failed.", e);
            throw new CustomGenericException(ERROR_CODE_ONE, "配置删除失败");
        }
        return result;
    }

    @Override
    public Result<List<ConfigurationVo>> selectConfiguration() {
        Result<List<ConfigurationVo>> result;
        try {
            if (!ZookeeperUtil.isNodeExist(zkClient, GENERAL_PAAS_CONFIGURATIONS)) {
                result = Result.ofFail(ERROR_CODE_ONE, "配置名不存在", Collections.emptyList());
            } else {
                List<String> configurationList = zkClient.getChildren().forPath(GENERAL_PAAS_CONFIGURATIONS);

                // 存储所有配置信息
                List<ConfigurationVo> configurations = new ArrayList<>();
                configurationList.forEach(configType -> {
                    try {
                        // 存储单个配置的对象
                        ConfigurationVo configuration = new ConfigurationVo();
                        String config = GENERAL_PAAS_CONFIGURATIONS + ZK_SEPARATOR + configType;

                        // 获取configurations节点下的所有子节点
                        List<String> envList = zkClient.getChildren().forPath(config);

                        Stat stat = new Stat();
                        byte[] bytes = zkClient.getData().storingStatIn(stat).forPath(config);
                        configuration.setConfigValue(new String(bytes, StandardCharsets.UTF_8));
                        configuration.setConfigName(configType);
                        configuration.setUpdateTimeStamp(stat.getMtime());
                        JSONObject jsonObject = new JSONObject();
                        envList.forEach(env -> {
                            try {
                                String envPath = config + ZK_SEPARATOR + env;
                                String nodeInfo = new String(zkClient.getData()
                                        .forPath(envPath), StandardCharsets.UTF_8);

                                // description节点是配置的描述信息
                                if (CONFIGURATION_DESCRIPTION.equals(env)) {
                                    configuration.setDescription(nodeInfo);
                                } else {
                                    jsonObject.put(env, nodeInfo);
                                }
                            } catch (Exception e) {
                                throw new CustomGenericException(ERROR_CODE_ONE, "配置查询失败");
                            }
                        });
                        configuration.setEnvs(jsonObject);
                        configurations.add(configuration);
                    } catch (CustomGenericException e) {
                        throw e;
                    } catch (Exception e) {
                        LOGGER.error("Query Configuration failed.", e);
                        throw new CustomGenericException(ERROR_CODE_ONE, "配置查询失败");
                    }
                });
                configurations.sort((o1, o2) -> (int) (o2.getUpdateTimeStamp() - o1.getUpdateTimeStamp()));
                result = Result.ofSuccess(configurations);
            }
        } catch (CustomGenericException e) {
            result = Result.ofFail(e.getCode(), e.getErrMsg());
        } catch (Exception e) {
            LOGGER.error("Query Configuration failed.", e);
            result = Result.ofFail(ERROR_CODE_ONE, "配置查询失败", Collections.emptyList());
        }
        return result;
    }

    @Override
    public Result<String> editEnvConfig(EditEnvInfo editEnvInfo) {
        PatternUtil.checkConfigName(editEnvInfo.getConfigName());
        Result<String> result;
        String nodePath = GENERAL_PAAS_CONFIGURATIONS + ZK_SEPARATOR + editEnvInfo.getConfigName();
        EnvInfo envInfo = editEnvInfo.getEnv();
        if (!StringUtils.hasText(envInfo.getModel())) {
            throw new CustomGenericException(ERROR_CODE_ONE, "模式不能为空");
        }
        if (envInfo.getValue() == null) {
            envInfo.setValue("");
        }
        try {
            String modelPath = nodePath + ZK_SEPARATOR + envInfo.getModel();
            if (!ZookeeperUtil.isNodeExist(zkClient, modelPath)) {
                throw new CustomGenericException(ERROR_CODE_ONE, "配置不存在");
            }

            TransactionOp transactionOp = zkClient.transactionOp();

            CuratorOp envOp = transactionOp.setData()
                    .forPath(modelPath,
                            envInfo.getValue().getBytes(StandardCharsets.UTF_8));
            zkClient.transaction().forOperations(envOp);

            result = Result.ofSuccessMsg("修改配置成功");
        } catch (CustomGenericException e) {
            result = Result.ofFail(e.getCode(), e.getErrMsg());
        } catch (Exception e) {
            LOGGER.error("修改配置失败", e);
            throw new CustomGenericException(ERROR_CODE_ONE, "修改配置失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取服务列表，必须有实例的服务才是存活的服务
     *
     * @return 服务列表
     */
    @Override
    public Result<List<String>> getServiceList() {
        Map<String, List<String>> allServiceMappedInstances = agentHeartbeat.getAllServicesMappedInstances();
        List<String> collect = allServiceMappedInstances.entrySet().stream()
                .filter(instanceEntry -> !CollectionUtils.isEmpty(instanceEntry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        return Result.ofSuccess(collect);
    }
}
