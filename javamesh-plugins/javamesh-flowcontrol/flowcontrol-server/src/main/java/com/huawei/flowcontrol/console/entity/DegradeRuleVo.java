/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.flowcontrol.console.entity;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import lombok.Getter;
import lombok.Setter;

/**
 * 此处部分引用alibaba/Sentinel开源社区代码，诚挚感谢alibaba/Sentinel开源团队的慷慨贡献
 */
@Getter
@Setter
public class DegradeRuleVo extends BaseRule<DegradeRule> {
    private String resource;
    private String limitApp;
    private Double count;
    private Integer timeWindow;
    private Integer grade;
    private Integer minRequestAmount;
    private Double slowRatioThreshold;
    private Integer statIntervalMs;

    public static DegradeRuleVo fromDegradeRule(String app, String ip, Integer port, DegradeRule rule) {
        DegradeRuleVo entity = new DegradeRuleVo();
        entity.setApp(app);
        entity.setIp(ip);
        entity.setPort(port);
        entity.setResource(rule.getResource());
        entity.setLimitApp(rule.getLimitApp());
        entity.setCount(rule.getCount());
        entity.setTimeWindow(rule.getTimeWindow());
        entity.setGrade(rule.getGrade());
        entity.setMinRequestAmount(rule.getMinRequestAmount());
        entity.setSlowRatioThreshold(rule.getSlowRatioThreshold());
        entity.setStatIntervalMs(rule.getStatIntervalMs());
        return entity;
    }

    @Override
    public DegradeRule toRule() {
        DegradeRule rule = new DegradeRule();
        rule.setResource(resource);
        rule.setLimitApp(limitApp);
        rule.setCount(count);
        rule.setTimeWindow(timeWindow);
        rule.setGrade(grade);
        if (minRequestAmount != null) {
            rule.setMinRequestAmount(minRequestAmount);
        }
        if (slowRatioThreshold != null) {
            rule.setSlowRatioThreshold(slowRatioThreshold);
        }
        if (statIntervalMs != null) {
            rule.setStatIntervalMs(statIntervalMs);
        }

        return rule;
    }
}
