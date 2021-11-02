/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.label.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.huawei.route.common.Result;
import com.huawei.route.server.labels.constant.LabelConstant;
import com.huawei.route.server.labels.exception.CustomGenericException;
import com.huawei.route.server.labels.group.service.LabelGroupService;
import com.huawei.route.server.labels.heartbeat.AgentHeartbeat;
import com.huawei.route.server.labels.send.LabelValidClient;
import com.huawei.route.server.labels.util.PathUtil;
import com.huawei.route.server.labels.util.PatternUtil;
import com.huawei.route.server.labels.vo.LabelBusinessVo;
import com.huawei.route.server.labels.vo.LabelValidVo;
import com.huawei.route.server.labels.vo.LabelVo;
import com.huawei.route.server.labels.vo.ScheduleLabelValidVo;
import com.huawei.route.server.rules.notifier.GrayTagConfigurationWrapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ConvertingCursor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.huawei.route.server.labels.constant.LabelConstant.ERROR_CODE;
import static com.huawei.route.server.labels.constant.LabelConstant.ERROR_CODE_ONE;
import static com.huawei.route.server.labels.constant.LabelConstant.INSTANCE_NAMES;
import static com.huawei.route.server.labels.constant.LabelConstant.INSTANCE_NAME_MARKING;
import static com.huawei.route.server.labels.constant.LabelConstant.LABELS;
import static com.huawei.route.server.labels.constant.LabelConstant.LABEL_GROUP_NAME_MARKING;
import static com.huawei.route.server.labels.constant.LabelConstant.LABEL_NAME_MARKING;
import static com.huawei.route.server.labels.constant.LabelConstant.SEPARATOR;
import static com.huawei.route.server.labels.constant.LabelConstant.SERVICE_NAMES_MARKING;
import static com.huawei.route.server.labels.constant.LabelConstant.SERVICE_NAME_MARKING;
import static com.huawei.route.server.labels.constant.LabelConstant.VALID_MARKING;
import static com.huawei.route.server.labels.constant.LabelConstant.VALUE_OF_LABEL;
import static com.huawei.route.server.labels.constant.LabelConstant.XPAAS_LABEL_GROUPS;

/**
 * 标签服务实现类
 *
 * @author Zhang Hu
 * @since 2021-04-12
 */
@Service
public class LabelServiceImpl implements LabelService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LabelServiceImpl.class);

    private static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(5, 20, 30L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(1024), new CustomizableThreadFactory("label-threadPool-"));

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${label.valid.port}")
    private int labelValidPort;

    @Autowired
    private AgentHeartbeat agentHeartbeat;

    @Autowired
    private LabelGroupService labelGroupService;

    @Autowired
    private GrayTagConfigurationWrapper grayTagConfigurationWrapper;

    @Autowired
    public LabelServiceImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Result<String> addLabel(LabelVo label) {
        if (StringUtils.isBlank(label.getOn())) {
            throw new CustomGenericException(ERROR_CODE_ONE, "是否生效不能为空");
        }
        if (!Boolean.TRUE.toString().equalsIgnoreCase(label.getOn())) {
            throw new CustomGenericException(ERROR_CODE_ONE, "新增标签时只能为true");
        }
        // 检查服务名是否重复，是否含有空元素
        checkServiceNames(label.getServiceNames());

        // 验证标签组和标签名
        String labelGroupName = label.getLabelGroupName();
        String labelName = label.getLabelName();
        validateLabel(labelGroupName, labelName, true);

        // 增加标签，给标签挂服务时，需要判断服务是否已经挂了相同的标签
        checkServiceHasLabel(label.getServiceNames(), labelGroupName, labelName);
        Result<String> result;
        try {
            // 从redis获取hash结构的标签信息的字符串
            String labelGroupPath = PathUtil.getLabelGroupPath(labelGroupName);
            String labels = (String) redisTemplate.opsForHash().get(labelGroupPath, LABELS);
            JSONArray jsonArray = Optional.ofNullable(JSONArray.parseArray(labels)).orElseGet(JSONArray::new);
            // 由于redis没有事务，所以有可能会存在labelGroupPath中有但LabelPath中没有的情况，所有要判断是否包含，不包含才add
            if (!jsonArray.contains(labelName)) {
                jsonArray.add(labelName);
                redisTemplate.opsForHash().put(labelGroupPath, LABELS, jsonArray.toJSONString());
            }
            label.setUpdateTimeStamp(System.currentTimeMillis());
            String labelPath = PathUtil.getLabelPath(labelGroupName, labelName);
            redisTemplate.opsForValue().set(labelPath, JSON.toJSONString(label, new BooleanValueFilter()));
            THREAD_POOL.execute(() -> addLabelServiceAndTakeEffect(label));
            result = Result.ofSuccessMsg("标签新增成功");
            // 通知标签更新
            grayTagConfigurationWrapper.updateTagListenPath(label);
        } catch (CustomGenericException e) {
            throw e;
        } catch (Exception exception) {
            LOGGER.error("Add label failed.", exception);
            result = Result.ofFail(ERROR_CODE_ONE, "标签新增失败");
        }
        return result;
    }

    @Override
    public Result<String> updateLabel(LabelVo label) {
        if (StringUtils.isBlank(label.getOn())) {
            label.setOn(Boolean.FALSE.toString());
        }
        if (!Boolean.TRUE.toString().equalsIgnoreCase(label.getOn()) && !Boolean.FALSE.toString()
                .equalsIgnoreCase(label.getOn())) {
            throw new CustomGenericException(ERROR_CODE_ONE, "是否生效只能为true或false");
        }
        // 检查服务名是否重复，是否含有空元素
        checkServiceNames(label.getServiceNames());

        // 验证标签组和标签名
        String labelName = label.getLabelName();
        String labelGroupName = label.getLabelGroupName();
        validateLabel(labelGroupName, labelName, false);
        String labelPath = PathUtil.getLabelPath(labelGroupName, labelName);
        String labelData = redisTemplate.opsForValue().get(labelPath);
        if (StringUtils.isBlank(labelData)) {
            throw new CustomGenericException(ERROR_CODE_ONE, "标签不存在");
        }
        // 修改标签，给标签修改服务时，需要判断服务是否已经挂了相同的标签
        checkServiceHasLabel(label.getServiceNames(), labelGroupName, labelName);
        Result<String> result;
        try {
            // 获取之前的服务和实例
            String serviceNames = JSONObject.parseObject(labelData).getString(SERVICE_NAMES_MARKING);
            JSONArray serviceArray = JSONArray.parseArray(serviceNames);
            // 前端传入的service
            List<String> serviceList = Arrays.asList(label.getServiceNames());
            // 取消勾选的服务
            List<String> cancelServices = new ArrayList<>();
            // 筛选取消勾选的服务
            serviceArray.forEach(serviceName -> {
                if (!serviceList.contains(serviceName)) {
                    cancelServices.add((String) serviceName);
                }
            });
            // 这次增加的服务
            List<String> addServices = new ArrayList<>();
            serviceList.forEach(serviceName -> {
                if (!serviceArray.contains(serviceName)) {
                    addServices.add(serviceName);
                }
            });
            // 检查取消勾选的服务是否存在生效的标签
            checkCancelServices(labelGroupName, labelName, cancelServices);
            // 改变模板不推送
//            THREAD_POOL.execute(() -> {
//                // 让新的服务实例的标签生效
//                addLabelServiceAndTakeEffect(label);
//            });
            label.setUpdateTimeStamp(System.currentTimeMillis());
            redisTemplate.opsForValue().set(labelPath, JSONObject.toJSONString(label, new BooleanValueFilter()));
            if (StringUtils.isEmpty(serviceNames)) {
                return Result.ofSuccessMsg("标签修改成功");
            }
            // 去掉取消勾选的服务
            cancelServices.forEach(serviceName -> {
                // 会把这个服务所有的标签去掉？不应该是只去掉这一个标签吗？
                deleteLabelValidInfo(serviceName, labelGroupName, labelName);
                LOGGER.info("update label {}, and delete service={}", labelName, serviceName);
            });
            // 给新加的服务保存实例标签缓存
            addLabelService(label, addServices);
            result = Result.ofSuccessMsg("标签修改成功");
            // 通知标签更新
            grayTagConfigurationWrapper.updateTagListenPath(label);
        } catch (CustomGenericException e) {
            throw e;
        } catch (Exception exception) {
            LOGGER.error("Update label failed.", exception);
            result = Result.ofFail(ERROR_CODE_ONE, "标签修改失败");
        }
        return result;
    }

    private void checkCancelServices(String labelGroupName, String labelName, List<String> services) {
        for (String serviceName : services) {
            List<String> instanceNames = agentHeartbeat.getServiceMappedInstances(String.valueOf(serviceName));
            if (CollectionUtils.isEmpty(instanceNames)) {
                continue;
            }
            for (String instanceName : instanceNames) {
                Map<Object, Object> label = getInstanceLabel(serviceName, instanceName, labelGroupName, labelName);
                if (CollectionUtils.isEmpty(label)) {
                    continue;
                }
                if (Boolean.parseBoolean((String) label.getOrDefault(VALID_MARKING, false))) {
                    throw new CustomGenericException(ERROR_CODE_ONE, "取消勾选的服务[" + serviceName + "]中存在生效的标签");
                }
            }
        }
    }

    @Override
    public Result<String> deleteLabel(String labelGroupName, String labelName) {
        // 验证标签组和标签名
        validateLabel(labelGroupName, labelName, false);
        Result<String> result;
        String labelGroupPath = PathUtil.getLabelGroupPath(labelGroupName);
        String labelPath = PathUtil.getLabelPath(labelGroupName, labelName);
        try {
            // 删除标签时删除生效的数据，但已生效的数据依然在应用中生效
            JSONObject label = JSONObject.parseObject(redisTemplate.opsForValue().get(labelPath));
            if (CollectionUtils.isEmpty(label)) {
                throw new CustomGenericException(ERROR_CODE_ONE, "标签不存在");
            }
            JSONArray servicesList = label.getJSONArray(SERVICE_NAMES_MARKING);
            if (servicesList != null) {
                // 删除服务下所有的标签？不应该是这个标签吗？
                servicesList
                        .forEach(serviceName -> deleteLabelValidInfo((String) serviceName, labelGroupName, labelName));
            }
            redisTemplate.delete(labelPath);
            String labels = (String) redisTemplate.opsForHash().get(labelGroupPath, LABELS);
            JSONArray jsonArray = Optional.ofNullable(JSONArray.parseArray(labels)).orElseGet(JSONArray::new);
            if (!CollectionUtils.isEmpty(jsonArray)) {
                jsonArray.remove(labelName);
            }
            if (CollectionUtils.isEmpty(jsonArray)) {
                redisTemplate.opsForHash().delete(labelGroupPath, LABELS);
            } else {
                redisTemplate.opsForHash().put(labelGroupPath, LABELS, jsonArray.toJSONString());
            }
            result = Result.ofSuccessMsg("标签删除成功");
            // 通知标签更新
            grayTagConfigurationWrapper.updateTagListenPath(labelGroupName, labelName);
        } catch (CustomGenericException e) {
            throw e;
        } catch (Exception exception) {
            LOGGER.error("标签删除失败", exception);
            result = Result.ofFail(ERROR_CODE_ONE, "Delete label failed.");
        }
        return result;
    }

    @Override
    public Result<Object> selectLabels(String labelGroupName) {
        Result<Object> listResult;
        List<JSONObject> listLabel = new ArrayList<>();
        try {
            if (StringUtils.isBlank(labelGroupName)) {
                List<String> labelGroups = redisTemplate.opsForList().range(XPAAS_LABEL_GROUPS, 0, -1);
                if (!CollectionUtils.isEmpty(labelGroups)) {
                    labelGroups.forEach(labelGroup -> findLabels(listLabel, labelGroup));
                    sort(listLabel);
                }
                return Result.ofSuccess(listLabel);
            }
            PatternUtil.checkLabelGroupName(labelGroupName);
            if (!labelGroupService.checkLabelGroupIsExist(labelGroupName)) {
                listResult = Result.ofFail(ERROR_CODE_ONE, "标签组不存在", Collections.emptyList());
            } else {
                findLabels(listLabel, labelGroupName);
                sort(listLabel);
                listResult = Result.ofSuccess(listLabel);
            }
        } catch (CustomGenericException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Query failed. {}", e.getMessage());
            listResult = Result.ofFail(ERROR_CODE_ONE, "查询失败", Collections.emptyList());
        }
        return listResult;
    }

    private void sort(List<JSONObject> listLabel) {
        listLabel.sort((o1, o2) -> (int) (o2.getLongValue(LabelConstant.UPDATE_TIMESTAMP) - o1
                .getLongValue(LabelConstant.UPDATE_TIMESTAMP)));
        listLabel.forEach(label -> label.remove(LabelConstant.UPDATE_TIMESTAMP));
    }

    @Override
    public Result<String> editLabelInstance(LabelBusinessVo labelBusiness) {
        // 验证标签组和标签名
        String labelGroupName = labelBusiness.getLabelGroupName();
        String labelName = labelBusiness.getLabelName();
        validateLabel(labelGroupName, labelName, false);
        String serviceName = labelBusiness.getServiceName();
        String instanceName = labelBusiness.getInstanceName();
        String instanceLabelPath = PathUtil.getInstanceLabelPath(serviceName, instanceName, labelGroupName, labelName);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(instanceLabelPath);
        entries.put(VALID_MARKING, Boolean.FALSE.toString());
        // entries的标签可能会比labelBusiness要多，所以要先查entries，然后putAll labelBusiness
        entries.putAll(JSONObject.parseObject(JSONObject.toJSONString(labelBusiness, new BooleanValueFilter())));
        String tempPath = PathUtil.getInstanceTempLabelPath(serviceName, instanceName, labelGroupName, labelName);
        redisTemplate.opsForHash().putAll(tempPath, entries);
        // 通知标签更新
        grayTagConfigurationWrapper.updateTagListenPath(labelBusiness);
        return Result.ofSuccess("修改成功");
    }

    @Override
    public Result<Object> labelValidAndInvalid(LabelValidVo labelValid) {
        if (StringUtils.isBlank(labelValid.getOn())) {
            throw new CustomGenericException(ERROR_CODE_ONE, "是否生效不能为空");
        }
        if (!Boolean.TRUE.toString().equalsIgnoreCase(labelValid.getOn()) && !Boolean.FALSE.toString()
                .equalsIgnoreCase(labelValid.getOn())) {
            throw new CustomGenericException(ERROR_CODE_ONE, "是否生效只能为true或false");
        }
        String labelGroupName = labelValid.getLabelGroupName();
        String labelName = labelValid.getLabelName();
        validateLabel(labelGroupName, labelName, false);
        String serviceName = labelValid.getServiceName();
        String instanceName = labelValid.getInstanceName();
        if (!selectServiceForLabel(labelGroupName, labelName).contains(serviceName)) {
            throw new CustomGenericException(ERROR_CODE_ONE, "该标签不适用于当前服务");
        }
        String instanceLabelPath = PathUtil.getInstanceLabelPath(serviceName, instanceName, labelGroupName, labelName);
        String tempPath = PathUtil.getInstanceTempLabelPath(serviceName, instanceName, labelGroupName, labelName);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(instanceLabelPath);
        Map<Object, Object> labelsOfRedis = redisTemplate.opsForHash().entries(tempPath);
        // entries的标签可能会比labelBusiness要多，所以要先查entries，然后putAll labelsOfRedis
        entries.putAll(labelsOfRedis);
        if (CollectionUtils.isEmpty(entries)) {
            String labelPath = PathUtil.getLabelPath(labelGroupName, labelName);
            String labelData = redisTemplate.opsForValue().get(labelPath);
            JSONObject label = JSONObject.parseObject(labelData);
            entries.put(SERVICE_NAME_MARKING, serviceName);
            entries.put(INSTANCE_NAME_MARKING, instanceName);
            entries.put(LABEL_GROUP_NAME_MARKING, labelGroupName);
            entries.put(LABEL_NAME_MARKING, labelName);
            entries.put(VALUE_OF_LABEL, label.get(VALUE_OF_LABEL));
        }
        entries.put(VALID_MARKING, labelValid.getOn().toLowerCase(Locale.ENGLISH));
        boolean flag;
        try {
            flag = sendLabelToAgent(serviceName, instanceName, entries);
        } catch (CustomGenericException e) {
            entries.put(VALID_MARKING, Boolean.FALSE.toString());
            redisTemplate.opsForHash().putAll(tempPath, entries);
            throw e;
        }
        if (flag) {
            redisTemplate.opsForHash().putAll(tempPath, entries);
            // 通知标签更新
            grayTagConfigurationWrapper.updateTagListenPath(labelValid);
            return Result.ofSuccess("操作成功");
        } else {
            // agent接收不成功则设置为false
            entries.put(VALID_MARKING, Boolean.FALSE.toString());
            redisTemplate.opsForHash().putAll(tempPath, entries);
            return Result.ofFail(ERROR_CODE_ONE, "agent接收失败");
        }
    }

    @Override
    public boolean instanceStartValid(List<ScheduleLabelValidVo> labelValidList) {
        if (CollectionUtils.isEmpty(labelValidList)) {
            LOGGER.error("labelValidList is empty.");
            return false;
        }
        labelValidList.forEach(labelValid -> {
            String labelGroupName = labelValid.getLabelGroupName();
            String labelName = labelValid.getLabelName();
            String serviceName = labelValid.getServiceName();
            String instanceName = labelValid.getInstanceName();
            try {
                validateLabel(labelGroupName, labelName, false);
                if (!selectServiceForLabel(labelGroupName, labelName).contains(serviceName)) {
                    return;
                }
            } catch (CustomGenericException e) {
                LOGGER.error("Label is invalid.", e);
                return;
            }
            Map<Object, Object> labelMap = new HashMap<>();
            labelMap.put(SERVICE_NAME_MARKING, serviceName);
            labelMap.put(INSTANCE_NAME_MARKING, instanceName);
            labelMap.put(VALID_MARKING, labelValid.getOn().toLowerCase(Locale.ENGLISH));
            labelMap.put(LABEL_GROUP_NAME_MARKING, labelGroupName);
            labelMap.put(LABEL_NAME_MARKING, labelName);
            labelMap.put(VALUE_OF_LABEL, labelValid.getValue());
            String instanceLabelPath = PathUtil
                    .getInstanceLabelPath(serviceName, instanceName, labelGroupName, labelName);
            try {
                // agent接收不成功则设置为false
                if (!sendLabelToAgent(labelValid.getIp(), labelValid.getPort(), labelMap)) {
                    labelMap.put(VALID_MARKING, Boolean.FALSE.toString());
                }
            } catch (Exception e) {
                LOGGER.error("Failed to send label to agent.", e);
                labelMap.put(VALID_MARKING, Boolean.FALSE.toString());
            }
            try {
                redisTemplate.opsForHash().putAll(instanceLabelPath, labelMap);
            } catch (Exception e) {
                LOGGER.error("Failed to send instance's label.", e);
            }
        });
        return true;
    }

    /**
     * 动态查询标签受用业务
     *
     * @param labelGroupName 标签组名
     * @param labelName 标签名
     * @return Result
     */
    @Override
    public Result<List<Object>> selectLabelInstance(String labelGroupName, String labelName) {
        validateLabel(labelGroupName, labelName, false);

        return Result.ofSuccess(selectRawLabelInstance(labelGroupName, labelName));
    }

    @Override
    public JSONArray selectRawLabelInstance(String labelGroupName, String labelName) {
        JSONObject label = getLabel(labelGroupName, labelName);
        JSONArray result = new JSONArray();
        // 改嵌套
        if (CollectionUtils.isEmpty(label)) {
            return result;
        }
        JSONArray serviceNames = label.getJSONArray(SERVICE_NAMES_MARKING);
        if (CollectionUtils.isEmpty(serviceNames)) {
            return result;
        }
        serviceNames.forEach(serviceName -> {
            try {
                final JSONObject jsonObject =
                        selectRawLabelInstance(String.valueOf(serviceName), labelGroupName, labelName, label);
                if (jsonObject != null) {
                    result.add(jsonObject);
                }
            } catch (Exception e) {
                LOGGER.error("Select label instance is fail.", e);
                throw new CustomGenericException(ERROR_CODE_ONE, "获取数据失败");
            }
        });
        return result;
    }

    @Override
    public JSONObject selectRawLabelInstance(String serviceName, String labelGroupName,
                                             String labelName, JSONObject label) {
        List<String> instanceNames = agentHeartbeat.getServiceMappedInstances(serviceName);
        if (CollectionUtils.isEmpty(instanceNames)) {
            return null;
        }
        if (label == null) {
            label = getLabel(labelGroupName, labelName);
        }
        JSONObject serviceData = new JSONObject();
        serviceData.put(SERVICE_NAME_MARKING, serviceName);
        JSONArray jsonArray = new JSONArray();
        JSONObject finalLabel = label;
        instanceNames.forEach(instance -> {
            Map<Object, Object> validValue = getInstanceLabel((String) serviceName, instance,
                    labelGroupName, labelName);
            JSONObject values = new JSONObject();
            values.put(VALID_MARKING, validValue.getOrDefault(VALID_MARKING, false));
            values.put(INSTANCE_NAME_MARKING, instance);
            values.put(VALUE_OF_LABEL, validValue.getOrDefault(VALUE_OF_LABEL, finalLabel.get(VALUE_OF_LABEL)));
            jsonArray.add(values);
        });
        serviceData.put(INSTANCE_NAMES, jsonArray);
        return serviceData;
    }

    @Override
    public JSONObject selectSingleInstance(String serviceName, String instanceName, String labelGroupName, String labelName) {
        Map<Object, Object> validValue = getInstanceLabel(serviceName, instanceName,
                labelGroupName, labelName);
        JSONObject values = new JSONObject();
        values.put(VALID_MARKING, validValue.getOrDefault(VALID_MARKING, false));
        values.put(INSTANCE_NAME_MARKING, instanceName);
        values.put(VALUE_OF_LABEL, validValue.getOrDefault(VALUE_OF_LABEL,
                getLabel(labelGroupName, labelName).get(VALUE_OF_LABEL)));
        return values;
    }

    private JSONObject getLabel(String labelGroupName, String labelName) {
        String labelPath = PathUtil.getLabelPath(labelGroupName, labelName);
        String labelData = redisTemplate.opsForValue().get(labelPath);
        return JSONObject.parseObject(labelData);
    }

    private Map<Object, Object> getInstanceLabel(String serviceName, String instance, String labelGroupName,
                                                 String labelName) {
        String tempPath = PathUtil.getInstanceTempLabelPath(serviceName, instance, labelGroupName, labelName);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(tempPath))) {
            return redisTemplate.opsForHash().entries(tempPath);
        } else {
            String instancePath = PathUtil.getInstanceLabelPath(serviceName, instance, labelGroupName, labelName);
            return redisTemplate.opsForHash().entries(instancePath);
        }
    }

    @Override
    public List<JSONObject> instanceStartLabelValid(String serviceName) {
        List<JSONObject> listLabel = new ArrayList<>();
        // 获取所有标签组名
        List<String> labelGroups = redisTemplate.opsForList().range(XPAAS_LABEL_GROUPS, 0, -1);
        if (CollectionUtils.isEmpty(labelGroups)) {
            return Collections.emptyList();
        }
        labelGroups.forEach(labelGroup -> findLabels(listLabel, labelGroup));

        // 过滤出服务的标签
        return listLabel.stream()
                .filter(k -> k.getJSONArray(SERVICE_NAMES_MARKING).contains(serviceName))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteTempLabel(String serviceName, String instanceName) {
        List<String> keys = scan(PathUtil.getInstanceLabelPath(serviceName, instanceName, LabelConstant.WILDCARD,
                LabelConstant.WILDCARD));
        if (!CollectionUtils.isEmpty(keys)) {
            redisTemplate.delete(keys);
        }
    }

    private void findLabels(List<JSONObject> listLabel, String labelGroupName) {
        String labelGroupPath = PathUtil.getLabelGroupPath(labelGroupName);
        String labels = (String) redisTemplate.opsForHash().get(labelGroupPath, LABELS);
        JSONArray jsonArrayLabels = JSON.parseArray(labels);
        if (!CollectionUtils.isEmpty(jsonArrayLabels)) {
            List<String> deleteLabels = new ArrayList<>();
            jsonArrayLabels.forEach(label -> {
                // 获取标签组下的标签
                String entries = redisTemplate.opsForValue().get(PathUtil.getLabelPath(labelGroupName, (String) label));
                if (StringUtils.isBlank(entries)) {
                    // 由于redis没有事务，所以有可能会存在labelGroupPath中有但LabelPath中没有的情况，这时候就删除掉labelGroupPath中的数据
                    deleteLabels.add((String) label);
                } else {
                    listLabel.add(JSON.parseObject(entries));
                }
            });
            if (!CollectionUtils.isEmpty(deleteLabels)) {
                jsonArrayLabels.removeAll(deleteLabels);
                redisTemplate.opsForHash().put(labelGroupPath, LABELS, jsonArrayLabels.toJSONString());
            }
        }
    }

    private void validateLabel(String labelGroupName, String labelName, boolean checkLabelNameIsExist) {
        if (StringUtils.isBlank(labelGroupName)) {
            throw new CustomGenericException(ERROR_CODE_ONE, "标签组名不能为空");
        }
        if (StringUtils.isBlank(labelName)) {
            throw new CustomGenericException(ERROR_CODE_ONE, "标签名不能为空");
        }

        // 检查标签组名和标签名是否符合规定的正则表达式
        PatternUtil.checkLabelGroupNameAndLabelName(labelGroupName, labelName);
        if (!labelGroupService.checkLabelGroupIsExist(labelGroupName)) {
            throw new CustomGenericException(ERROR_CODE_ONE, "标签组不存在");
        }
        String labelGroupPath = PathUtil.getLabelGroupPath(labelGroupName);
        JSONArray labels = JSONArray.parseArray((String) redisTemplate.opsForHash().get(labelGroupPath, LABELS));
        boolean flag = labels != null && labels.contains(labelName);
        if (!checkLabelNameIsExist && !flag) {
            throw new CustomGenericException(ERROR_CODE_ONE, "标签不存在");
        }
        if (checkLabelNameIsExist && flag) {
            String entries = redisTemplate.opsForValue().get(PathUtil.getLabelPath(labelGroupName, labelName));
            if (StringUtils.isNotBlank(entries)) {
                // 标签数据和标签列表都存在的情况下才报已存在
                throw new CustomGenericException(ERROR_CODE_ONE, "标签已存在");
            }
        }
    }

    private boolean sendLabelToAgent(String ip, Integer port, Map<Object, Object> map) {
        try {
            String response = new LabelValidClient(StringUtils.isEmpty(ip) ? "localhost" : ip,
                    port == null ? labelValidPort : port).start(JSON.toJSONString(map));
            return Boolean.TRUE.toString().equalsIgnoreCase(response);
        } catch (InterruptedException e) {
            LOGGER.error("client is interrupted.{}", e.getMessage());
            throw new CustomGenericException(ERROR_CODE, "agent响应超时");
        } catch (Exception e) {
            LOGGER.error("client has exception.{}", e.getMessage());
            throw new CustomGenericException(ERROR_CODE, "agent响应失败");
        }
    }

    private JSONArray selectServiceForLabel(String labelGroupName, String labelName) {
        String labelData = redisTemplate.opsForValue().get(PathUtil.getLabelPath(labelGroupName, labelName));
        if (StringUtils.isBlank(labelData)) {
            throw new CustomGenericException(ERROR_CODE_ONE, "标签不存在");
        }
        JSONObject label = JSONObject.parseObject(labelData);
        JSONArray serviceNames = new JSONArray();
        if (!label.isEmpty()) {
            serviceNames = label.getJSONArray(SERVICE_NAMES_MARKING);
        }
        return serviceNames;
    }

    private boolean sendLabelToAgent(String service, String instance, Map<Object, Object> map) {
        JSONObject instanceMappedHeartbeat = agentHeartbeat.getServiceHeartbeat(service);
        if (CollectionUtils.isEmpty(instanceMappedHeartbeat)) {
            LOGGER.error("Get ip failed. ");
            throw new CustomGenericException(ERROR_CODE_ONE, "找不到服务");
        }
        String heartbeat = instanceMappedHeartbeat.getString(instance);
        if (StringUtils.isEmpty(heartbeat)) {
            throw new CustomGenericException(ERROR_CODE_ONE, "找不到该实例");

        }
        JSONObject json = JSONObject.parseObject(heartbeat);
        return sendLabelToAgent(json.getString(LabelConstant.NETTY_IP), json.getInteger(LabelConstant.NETTY_PORT), map);
    }

    private void addLabelService(LabelVo label, List<String> serviceNames) {
        if (CollectionUtils.isEmpty(serviceNames)) {
            return;
        }
        String labelGroupName = label.getLabelGroupName();
        String labelName = label.getLabelName();
        for (String serviceName : serviceNames) {
            try {
                deleteLabelValidInfo(serviceName, labelGroupName, labelName);
            } catch (Exception e) {
                LOGGER.error("Failed to delete instance's old label.", e);
                // 删除失败直接return，否则下面发送标签成功后，前端会展示的标签有可能与实际发送的标签不一致
                continue;
            }
            JSONObject heartbeat = agentHeartbeat.getServiceHeartbeat(serviceName);
            if (CollectionUtils.isEmpty(heartbeat)) {
                continue;
            }
            heartbeat.entrySet().parallelStream().forEach(entry -> {
                String instanceName = entry.getKey();
                Map<Object, Object> labelMap = new HashMap<>();
                labelMap.put(SERVICE_NAME_MARKING, serviceName);
                labelMap.put(INSTANCE_NAME_MARKING, instanceName);
                labelMap.put(VALID_MARKING, Boolean.FALSE.toString());
                labelMap.put(LABEL_GROUP_NAME_MARKING, labelGroupName);
                labelMap.put(LABEL_NAME_MARKING, labelName);
                labelMap.put(VALUE_OF_LABEL, label.getValue());
                try {
                    redisTemplate.opsForHash()
                            .putAll(PathUtil.getInstanceLabelPath(serviceName, instanceName, labelGroupName, labelName),
                                    labelMap);
                } catch (Exception e) {
                    LOGGER.error("Failed to save instance's label.", e);
                }
            });
        }
    }

    private void addLabelServiceAndTakeEffect(LabelVo label) {
        if (label.getServiceNames() == null) {
            return;
        }
        String labelGroupName = label.getLabelGroupName();
        String labelName = label.getLabelName();
        for (String serviceName : label.getServiceNames()) {
            try {
                // 这个是为了修复缓存丢失可能导致的bug
                deleteLabelValidInfo(serviceName, labelGroupName, labelName);
            } catch (Exception e) {
                LOGGER.error("Failed to delete instance's old label.", e);
                // 删除失败直接return，否则下面发送标签成功后，前端会展示的标签有可能与实际发送的标签不一致
                continue;
            }
            JSONObject heartbeat = agentHeartbeat.getServiceHeartbeat(serviceName);
            if (CollectionUtils.isEmpty(heartbeat)) {
                continue;
            }
            heartbeat.entrySet().parallelStream().forEach(entry -> {
                String instanceName = entry.getKey();
                Map<Object, Object> labelMap = new HashMap<>();
                labelMap.put(SERVICE_NAME_MARKING, serviceName);
                labelMap.put(INSTANCE_NAME_MARKING, instanceName);
                labelMap.put(VALID_MARKING, label.getOn().toLowerCase(Locale.ENGLISH));
                labelMap.put(LABEL_GROUP_NAME_MARKING, labelGroupName);
                labelMap.put(LABEL_NAME_MARKING, labelName);
                labelMap.put(VALUE_OF_LABEL, label.getValue());
                try {
                    JSONObject heartbeatMsg = JSONObject.parseObject((String) entry.getValue());
                    // agent接收不成功则设置为false
                    if (!sendLabelToAgent(heartbeatMsg.getString(LabelConstant.NETTY_IP),
                            heartbeatMsg.getInteger(LabelConstant.NETTY_PORT), labelMap)) {
                        labelMap.put(VALID_MARKING, Boolean.FALSE.toString());
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to send label to agent.", e);
                    labelMap.put(VALID_MARKING, Boolean.FALSE.toString());
                }
                try {
                    redisTemplate.opsForHash()
                            .putAll(PathUtil.getInstanceLabelPath(serviceName, instanceName, labelGroupName, labelName),
                                    labelMap);
                } catch (Exception e) {
                    LOGGER.error("Failed to save instance's label.", e);
                }
            });
        }
    }

    private void deleteLabelValidInfo(String serviceName, String labelGroupName, String labelName) {
        List<String> keys = new ArrayList<>();
        // 获取这个服务下的所有实例的标签：general-paas:valid:serviceName:*:labelGroupName:labelName
        String instanceLabelPath = PathUtil.getInstanceLabelPath(serviceName, LabelConstant.WILDCARD, labelGroupName,
                labelName);
        List<String> instanceKeys = scan(instanceLabelPath);
        if (!CollectionUtils.isEmpty(instanceKeys)) {
            keys.addAll(instanceKeys);
        }

        // 获取这个服务下的所有实例的临时标签：general-paas:valid:serviceName:*:labelGroupName:labelName:temp
        String tempLabelPath = PathUtil.getInstanceTempLabelPath(serviceName, LabelConstant.WILDCARD, labelGroupName,
                labelName);
        List<String> tempKeys = scan(tempLabelPath);
        if (!CollectionUtils.isEmpty(tempKeys)) {
            keys.addAll(tempKeys);
        }
        if (!CollectionUtils.isEmpty(keys)) {
            redisTemplate.delete(keys);
        }
    }

    private List<String> scan(String pattern) {
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(1000).build();
        try (Cursor<?> cursor = redisTemplate.executeWithStickyConnection(
                redisConnection -> new ConvertingCursor<>(redisConnection.scan(options),
                        redisTemplate.getKeySerializer()::deserialize))) {
            if (cursor != null) {
                List<String> list = new ArrayList<>();
                while (cursor.hasNext()) {
                    list.add((String) cursor.next());
                }
                return list;
            }
        } catch (Exception e) {
            LOGGER.error("Scan key is fail.", e);
        }
        return Collections.emptyList();
    }

    private void checkServiceHasLabel(String[] serviceNames, String labelGroupName, String labelName) {
        if (ArrayUtils.isEmpty(serviceNames)) {
            return;
        }
        // 已存在相同标签的服务名的集合
        Map<String, String> serviceNameMap = new HashMap<>();
        // 获取所有相同label名的key
        List<String> keys = scan(PathUtil.getLabelPath(LabelConstant.WILDCARD, labelName));
        String labelPath = PathUtil.getLabelPath(labelGroupName, labelName);
        for (String key : keys) {
            if (labelPath.equals(key)) {
                // 跳过相同的组下的标签的key
                // 如果是新增的时候，这个key值已经校验过了，所以跳过。
                // 如果是修改的时候，也要跳过当前的标签。比如上次标签关联服务a，这次修改成a，b，就不需要把上次的a放到serviceNameList里面。
                continue;
            }
            // 相同名字的标签
            String str = redisTemplate.opsForValue().get(key);
            if (StringUtils.isBlank(str)) {
                continue;
            }
            String labelGroupPath = key.substring(0, key.lastIndexOf(SEPARATOR));
            JSONArray labels = JSONArray.parseArray((String) redisTemplate.opsForHash().get(labelGroupPath, LABELS));
            if (labels == null || !labels.contains(labelName)) {
                // 缓存丢失，导致标签数据存在，但标签列表没了，这时候这个标签是无效的，所以要跳过
                continue;
            }
            try {
                LabelVo labelVo = JSONObject.parseObject(str, LabelVo.class);
                if (labelVo.getServiceNames() == null) {
                    continue;
                }
                // 把每一个相同名字的标签的服务名放到serviceNames里面
                for (String serviceName : labelVo.getServiceNames()) {
                    serviceNameMap.put(serviceName, labelVo.getLabelGroupName());
                }
            } catch (JSONException e) {
                LOGGER.error("Label is invalid.", e);
            }
        }
        if (CollectionUtils.isEmpty(serviceNameMap)) {
            return;
        }
        // 所有重复关联了标签的集合
        Map<String, String> result = new HashMap<>();
        for (String serviceName : serviceNames) {
            // 如果serviceNames包含了这个标签要关联的服务名，说明服务重复关联了标签
            if (serviceNameMap.containsKey(serviceName)) {
                result.put(serviceName, serviceNameMap.get(serviceName));
            }
        }
        if (!CollectionUtils.isEmpty(result)) {
            StringBuilder msg = new StringBuilder();
            msg.append("服务");
            msg.append(JSONArray.toJSONString(result.keySet()));
            msg.append("分别在标签组");
            msg.append(JSONArray.toJSONString(result.values()));
            msg.append("下存在该标签");
            throw new CustomGenericException(ERROR_CODE_ONE, msg.toString());
        }
    }

    private void checkServiceNames(String[] serviceNames) {
        Set<String> set = new HashSet<>();
        for (String serviceName : serviceNames) {
            if (StringUtils.isBlank(serviceName)) {
                throw new CustomGenericException(ERROR_CODE_ONE, "服务名不能为空");
            }
            set.add(serviceName);
        }
        if (set.size() != serviceNames.length) {
            throw new CustomGenericException(ERROR_CODE_ONE, "服务名不能重复");
        }
    }

    private static class BooleanValueFilter implements ValueFilter {
        @Override
        public Object process(Object obj, String key, Object value) {
            if (VALID_MARKING.equals(key) && value instanceof String) {
                if (Boolean.FALSE.toString().equalsIgnoreCase((String) value)) {
                    return false;
                }
                if (Boolean.TRUE.toString().equalsIgnoreCase((String) value)) {
                    return true;
                }
            }
            return value;
        }
    }
}
