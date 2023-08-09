/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR C¬ONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.sermant.core.utils.tag;

import com.huaweicloud.sermant.core.utils.MapUtils;

import java.util.List;
import java.util.Map;

/**
 * 流量中包含的请求信息
 *
 * @author lilai
 * @since 2023-07-17
 */
public class TrafficUtils {
    private static ThreadLocal<TrafficTag> tag = new ThreadLocal<>();

    private static ThreadLocal<TrafficData> data = new ThreadLocal<>();

    private TrafficUtils() {
    }

    /**
     * 如果开启在new Thread时跨线程传递标签，需要把ThreadLocal初始化为InheritableThreadLocal
     */
    public static void setInheritableThreadLocal() {
        if (!(tag instanceof InheritableThreadLocal)) {
            tag = new InheritableThreadLocal<>();
        }

        if (!(data instanceof InheritableThreadLocal)) {
            data = new InheritableThreadLocal<>();
        }
    }

    /**
     * 获取线程中的流量标签
     *
     * @return 流量标签
     */
    public static TrafficTag getTrafficTag() {
        return tag.get();
    }

    /**
     * 获取线程中的流量信息
     *
     * @return 流量信息
     */
    public static TrafficData getTrafficData() {
        return data.get();
    }

    /**
     * 更新线程中的流量标签
     *
     * @param tagMap 流量标签map
     */
    public static void updateTrafficTag(Map<String, List<String>> tagMap) {
        if (MapUtils.isEmpty(tagMap)) {
            return;
        }
        TrafficTag trafficTag = TrafficUtils.tag.get();
        if (trafficTag == null) {
            TrafficUtils.tag.set(new TrafficTag(tagMap));
            return;
        }
        trafficTag.updateTag(tagMap);
    }

    /**
     * 重设线程中的流量标签
     *
     * @param trafficTag 流量标签
     */
    public static void setTrafficTag(TrafficTag trafficTag) {
        if (trafficTag == null) {
            return;
        }
        tag.set(trafficTag);
    }

    /**
     * 删除线程变量
     */
    public static void removeTrafficTag() {
        tag.remove();
    }

    /**
     * 流量信息存入线程变量
     *
     * @param value 线程变量
     */
    public static void setTrafficData(TrafficData value) {
        data.set(value);
    }

    /**
     * 删除流量信息
     */
    public static void removeTrafficData() {
        data.remove();
    }
}
