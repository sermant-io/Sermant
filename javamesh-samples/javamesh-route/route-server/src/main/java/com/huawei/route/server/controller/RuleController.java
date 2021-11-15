/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.controller;

import com.huawei.route.server.controller.entity.RuleRequest;
import com.huawei.route.server.entity.AbstractInstance;
import com.huawei.route.server.entity.AbstractService;
import com.huawei.route.server.repository.InstanceRuleRepository;
import com.huawei.route.server.rules.InstanceTagConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 标签规则对外api
 *
 * @author zhouss
 * @since 2021-10-15
 */
@RequestMapping(value = "/route/v1/rule")
@RestController
public class RuleController<S extends AbstractService<T>, T extends AbstractInstance> {
    @Autowired
    private InstanceRuleRepository<S, T> instanceRuleRepository;

    /**
     * 查询对应实例的标签规则
     *
     * @param ruleRequest 请求参数 ip port
     * @return 规则配置
     */
    @PostMapping("/instance")
    public InstanceTagConfiguration queryInstanceRule(@RequestBody RuleRequest ruleRequest) {
        return instanceRuleRepository.queryInstanceRule(ruleRequest.getIp(), ruleRequest.getPort());
    }
}
