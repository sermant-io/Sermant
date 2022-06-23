/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.nacos.rest.consumer;

import com.huawei.nacos.rest.consumer.aspect.StatAop;
import com.huawei.nacos.rest.consumer.stat.RequestStat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 统计类
 *
 * @author zhouss
 * @since 2022-06-17
 */
@RestController
public class StatController {
    @Autowired
    private StatAop aop;

    /**
     * 统计方法
     *
     * @param name 统计数据
     * @return Object
     */
    @RequestMapping("/stat")
    public Object getStat(@RequestParam(required = false) String name) {
        final Map<String, RequestStat> statMap = aop.getStatMap();
        if (name == null) {
            return statMap.toString();
        }
        return statMap.get(name) != null ? statMap.get(name).toString() : null;
    }
}
