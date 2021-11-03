/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.strategy.rule;

import com.huawei.apm.bootstrap.lubanops.utils.StringUtils;
import com.huawei.gray.dubbo.cache.DubboCache;
import com.huawei.gray.dubbo.strategy.RuleStrategy;
import com.huawei.gray.dubbo.utils.RouterUtil;
import com.huawei.route.common.constants.GrayConstant;
import com.huawei.route.common.gray.addr.AddrCache;
import com.huawei.route.common.gray.addr.entity.Instances;
import com.huawei.route.common.gray.label.LabelCache;
import com.huawei.route.common.gray.label.entity.CurrentTag;
import com.huawei.route.common.gray.label.entity.Route;

import com.alibaba.fastjson.JSONObject;

import org.apache.dubbo.rpc.Invocation;

import java.util.List;

/**
 * 上游传递标签匹配
 *
 * @author pengyuyi
 * @date 2021/10/15
 */
public class UpstreamRuleStrategy implements RuleStrategy {
    @Override
    public String getTargetServiceIp(List<Route> list, String targetService, String interfaceName, String version,
            Invocation invocation) {
        String tagVersion;
        CurrentTag currentTag = JSONObject
                .parseObject(invocation.getAttachment(GrayConstant.GRAY_TAG), CurrentTag.class);
        if (currentTag != null && StringUtils.isNotBlank(currentTag.getVersion())) {
            tagVersion = currentTag.getVersion();
        } else {
            tagVersion = LabelCache.getLabel(DubboCache.getAppName()).getCurrentTag().getVersion();
        }
        Instances instances = AddrCache.getAddr(targetService, RouterUtil.getLdc(invocation), tagVersion);
        return RouterUtil.getTargetAndSetAttachment(instances, invocation, tagVersion, RouterUtil.getLdc(invocation));
    }
}
