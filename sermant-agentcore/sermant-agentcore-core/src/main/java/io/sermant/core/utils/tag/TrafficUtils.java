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

package io.sermant.core.utils.tag;

import io.sermant.core.utils.MapUtils;

import java.util.List;
import java.util.Map;

/**
 * Request information contained in the traffic
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
     * If enable cross-thread tag transmission on new threads, need to initialize ThreadLocal to
     * InheritableThreadLocal
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
     * Get traffic tag in the thread
     *
     * @return TrafficTag
     */
    public static TrafficTag getTrafficTag() {
        return tag.get();
    }

    /**
     * Get traffic data in the thread
     *
     * @return TrafficData
     */
    public static TrafficData getTrafficData() {
        return data.get();
    }

    /**
     * Update traffic tag in the thread
     *
     * @param tagMap TrafficTag map
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
     * reset Traffic Tag
     *
     * @param trafficTag TrafficTag
     */
    public static void setTrafficTag(TrafficTag trafficTag) {
        if (trafficTag == null) {
            return;
        }
        tag.set(trafficTag);
    }

    /**
     * remove traffic tag
     */
    public static void removeTrafficTag() {
        tag.remove();
    }

    /**
     * set traffic data
     *
     * @param value 线程变量
     */
    public static void setTrafficData(TrafficData value) {
        data.set(value);
    }

    /**
     * remove traffic data
     */
    public static void removeTrafficData() {
        data.remove();
    }
}
