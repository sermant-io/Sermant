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
    private static final ThreadLocal<TrafficTag> TAG = new ThreadLocal<>();

    private TrafficUtils() {
    }

    /**
     * 获取线程中的流量标签
     *
     * @return 流量标签
     */
    public static TrafficTag getTrafficTag() {
        return TAG.get();
    }

    /**
     * 更新线程中的流量标签
     *
     * @param tag 流量标签map
     */
    public static void updateTrafficTag(Map<String, List<String>> tag) {
        if (MapUtils.isEmpty(tag)) {
            return;
        }
        TrafficTag trafficTag = TAG.get();
        if (trafficTag == null) {
            TAG.set(new TrafficTag(tag));
            return;
        }
        trafficTag.updateTag(tag);
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
        TAG.set(trafficTag);
    }

    /**
     * 删除线程变量
     */
    public static void removeTrafficTag() {
        TAG.remove();
    }
}
