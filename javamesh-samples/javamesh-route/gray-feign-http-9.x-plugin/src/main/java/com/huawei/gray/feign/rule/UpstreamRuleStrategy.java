/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.gray.feign.rule;

import com.huawei.apm.core.lubanops.bootstrap.utils.StringUtils;
import com.huawei.gray.feign.util.RouterUtil;
import com.huawei.route.common.gray.addr.AddrCache;
import com.huawei.route.common.gray.addr.entity.Instances;
import com.huawei.route.common.gray.constants.GrayConstant;
import com.huawei.route.common.gray.label.entity.CurrentTag;
import com.huawei.route.common.gray.label.entity.Route;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 上游传递标签匹配
 *
 * @author lilai
 * @since 2021-11-03
 */
public class UpstreamRuleStrategy implements RuleStrategy {
    @Override
    public Instances getTargetServiceInstance(List<Route> list, String targetService, Map<String, Collection<String>> headers) {
        if (headers.get(GrayConstant.GRAY_TAG).isEmpty()) {
            return null;
        }
        String tagVersion;
        CurrentTag currentTag = JSONObject.parseObject(new ArrayList<String>(headers.get(GrayConstant.GRAY_TAG)).get(0), CurrentTag.class);
        if (currentTag != null && !StringUtils.isBlank(currentTag.getVersion())) {
            tagVersion = currentTag.getVersion();
        } else {
            tagVersion = GrayConstant.GRAY_DEFAULT_VERSION;
        }
        return AddrCache.getAddr(targetService, RouterUtil.getLdc(headers), tagVersion);
    }
}
