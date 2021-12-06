/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.flowcontrol.console.datasource.entity.rule.kie.operator;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.fastjson.JSONObject;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.client.KieConfigClient;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.client.response.KieConfigItem;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.client.response.KieConfigResponse;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.util.KieConfig;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.util.KieConfigUtil;
import com.huawei.flowcontrol.console.entity.BaseRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * kie配置中心规则查询接口层
 *
 * @param <E> sentinel规则实体
 * @param <T> vo转换
 */
public abstract class CommonKieRuleProvider<E extends AbstractRule, T extends BaseRule<E>>
    implements RuleKieProvider<List<T>> {
    private static final int MILLISECOND = 1000;

    private final KieConfigClient kieConfigClient;

    private final KieConfig kieConfig;

    /**
     * 规则类型
     * DegradeRule 降级
     * FlowRule
     * ParamFlowRule
     * SystemRule
     */
    private final String ruleType;

    public CommonKieRuleProvider(KieConfigClient kieConfigClient, KieConfig kieConfig, String ruleType) {
        this.kieConfigClient = kieConfigClient;
        this.kieConfig = kieConfig;
        this.ruleType = ruleType;
    }

    @Override
    public List<T> getRules(String app) throws Exception {
        String url = kieConfig.getKieBaseUrl();
        Optional<KieConfigResponse> response = kieConfigClient.getConfig(url);
        return response.map(configResponse -> parseResponseToEntity(app, configResponse))
            .orElse(Collections.emptyList());
    }

    private List<T> parseResponseToEntity(String app, KieConfigResponse config) {
        if (Objects.isNull(config) || Objects.isNull(app)) {
            return Collections.emptyList();
        }

        List<T> entityList = new ArrayList<>();
        for (KieConfigItem item : config.getData()) {
            if (!KieConfigUtil.isTargetItem(ruleType, app, item)) {
                continue;
            }

            T entity = getRuleVo(item, app);
            Date createTime = new Date(Long.parseLong(item.getCreateTime()) * MILLISECOND);
            entity.setGmtCreate(createTime);
            Date modifyTime = new Date(Long.parseLong(item.getUpdateTime()) * MILLISECOND);
            entity.setGmtModified(modifyTime);
            JSONObject extInfo = new JSONObject();
            extInfo.put("ruleId", item.getId());
            entity.setExtInfo(extInfo.toJSONString());
            entityList.add(entity);
        }

        return entityList;
    }

    protected abstract T getRuleVo(KieConfigItem item, String app);

}
