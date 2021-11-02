/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.group.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huawei.route.common.Result;
import com.huawei.route.server.labels.constant.LabelConstant;
import com.huawei.route.server.labels.exception.CustomGenericException;
import com.huawei.route.server.labels.group.LabelGroup;
import com.huawei.route.server.labels.util.PathUtil;
import com.huawei.route.server.labels.util.PatternUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.huawei.route.server.labels.constant.LabelConstant.ERROR_CODE_ONE;
import static com.huawei.route.server.labels.constant.LabelConstant.LABELS;
import static com.huawei.route.server.labels.constant.LabelConstant.LABEL_GROUP_NAME_MARKING;
import static com.huawei.route.server.labels.constant.LabelConstant.XPAAS_LABEL_GROUPS;

/**
 * 标签组管理实现类
 *
 * @author Zhang Hu
 * @since 2021-04-09
 */
@Service
public class LabelGroupServiceImpl implements LabelGroupService {
    /**
     * 描述的字段
     */
    public static final String DESCRIPTION_FIELD = "description";

    private static final Logger LOGGER = LoggerFactory.getLogger(LabelGroupServiceImpl.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Resource(name = "stringRedisTemplate")
    private HashOperations<String, String, String> hashOperations;

    @Override
    public Result<LabelGroup> addLabelGroup(LabelGroup labelGroup) {
        String labelGroupName = labelGroup.getLabelGroupName();
        PatternUtil.checkLabelGroupName(labelGroupName);
        if (checkLabelGroupIsExist((labelGroupName))) {
            return Result.ofFail(ERROR_CODE_ONE, "标签组已存在");
        }
        String labelGroupPath = PathUtil.getLabelGroupPath(labelGroupName);
        redisTemplate.opsForHash().put(labelGroupPath, DESCRIPTION_FIELD, labelGroup.getDescription());
        redisTemplate.opsForHash()
                .put(labelGroupPath, LabelConstant.UPDATE_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        redisTemplate.opsForList().rightPush(XPAAS_LABEL_GROUPS, labelGroupName);
        return Result.ofSuccessMsg("添加标签组成功");
    }

    @Override
    public Result<LabelGroup> updateLabelGroup(LabelGroup labelGroup) {
        String labelGroupName = labelGroup.getLabelGroupName();
        PatternUtil.checkLabelGroupName(labelGroupName);
        if (!checkLabelGroupIsExist(labelGroupName)) {
            return Result.ofFail(ERROR_CODE_ONE, "该标签组不存在");
        }
        String key = PathUtil.getLabelGroupPath(labelGroupName);
        redisTemplate.opsForHash().put(key, DESCRIPTION_FIELD, labelGroup.getDescription());
        redisTemplate.opsForHash().put(key, LabelConstant.UPDATE_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        return Result.ofSuccessMsg("修改标签组成功");
    }

    @Override
    public Result<String> deleteLabelGroup(String labelGroupName) {
        if (!StringUtils.hasText(labelGroupName)) {
            return Result.ofFail(ERROR_CODE_ONE, "标签组名不能为空");
        }
        PatternUtil.checkLabelGroupName(labelGroupName);
        if (!checkLabelGroupIsExist(labelGroupName)) {
            return Result.ofFail(ERROR_CODE_ONE, "标签组不存在");
        }
        String key = PathUtil.getLabelGroupPath(labelGroupName);
        if (!CollectionUtils.isEmpty(JSONArray.parseArray((String) redisTemplate.opsForHash().get(key, LABELS)))) {
            throw new CustomGenericException(ERROR_CODE_ONE, "该标签组存在标签");
        }
        redisTemplate.delete(key);
        redisTemplate.opsForList().remove(XPAAS_LABEL_GROUPS, 0, labelGroupName);
        return Result.ofSuccessMsg("删除标签组成功");
    }

    @Override
    public Result<Object> getLabelGroups() {
        List<JSONObject> list = new ArrayList<>();
        List<String> labelGroups = redisTemplate.opsForList().range(XPAAS_LABEL_GROUPS, 0, -1);
        if (labelGroups == null) {
            return Result.ofSuccess(Collections.emptyList());
        }
        labelGroups.forEach(labelGroupName -> {
            String key = PathUtil.getLabelGroupPath(labelGroupName);
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            if (CollectionUtils.isEmpty(entries)) {
                // 由于redis没有事务，所以有可能会存在XPAAS_LABEL_GROUPS中有但LabelGroupPath中没有的情况，
                // 这时候就删除掉XPAAS_LABEL_GROUPS中的数据
                redisTemplate.opsForList().remove(XPAAS_LABEL_GROUPS, 0, labelGroupName);
                return;
            }
            JSONObject labelGroup = new JSONObject();
            labelGroup.put(LABEL_GROUP_NAME_MARKING, labelGroupName);
            labelGroup.put(DESCRIPTION_FIELD, entries.get(DESCRIPTION_FIELD));
            labelGroup.put(LABELS, entries.get(LABELS));
            labelGroup.put(LabelConstant.UPDATE_TIMESTAMP, entries.get(LabelConstant.UPDATE_TIMESTAMP));
            list.add(labelGroup);
        });
        list.sort((o1, o2) -> (int) (o2.getLongValue(LabelConstant.UPDATE_TIMESTAMP) - o1
                .getLongValue(LabelConstant.UPDATE_TIMESTAMP)));
        list.forEach(json -> json.remove(LabelConstant.UPDATE_TIMESTAMP));
        return Result.ofSuccess(list);
    }

    @Override
    public boolean checkLabelGroupIsExist(String labelGroupName) {
        List<String> labelGroups = redisTemplate.opsForList().range(XPAAS_LABEL_GROUPS, 0, -1);
        return !CollectionUtils.isEmpty(labelGroups) && labelGroups.contains(labelGroupName);
    }
}
