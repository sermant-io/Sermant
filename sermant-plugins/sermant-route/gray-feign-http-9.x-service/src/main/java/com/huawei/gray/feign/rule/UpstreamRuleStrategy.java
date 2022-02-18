/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.gray.feign.rule;

import com.huawei.gray.feign.util.RouterUtil;
import com.huawei.route.common.gray.addr.AddrCache;
import com.huawei.route.common.gray.addr.entity.Instances;
import com.huawei.route.common.gray.constants.GrayConstant;
import com.huawei.route.common.gray.label.entity.CurrentTag;
import com.huawei.route.common.gray.label.entity.Route;
import com.huawei.sermant.core.lubanops.bootstrap.utils.StringUtils;

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
    public Instances getTargetServiceInstance(List<Route> list, String targetService,
        Map<String, Collection<String>> headers) {
        if (headers.get(GrayConstant.GRAY_TAG).isEmpty()) {
            return null;
        }
        String tagVersion;
        CurrentTag currentTag = JSONObject
            .parseObject(new ArrayList<String>(headers.get(GrayConstant.GRAY_TAG)).get(0), CurrentTag.class);
        if (currentTag != null && !StringUtils.isBlank(currentTag.getVersion())) {
            tagVersion = currentTag.getVersion();
        } else {
            tagVersion = GrayConstant.GRAY_DEFAULT_VERSION;
        }
        return AddrCache.getAddr(targetService, RouterUtil.getLdc(headers), tagVersion, null);
    }
}
