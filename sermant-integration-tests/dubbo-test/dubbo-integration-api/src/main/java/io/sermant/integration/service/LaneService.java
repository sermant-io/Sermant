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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sermant.integration.service;

import io.sermant.integration.entity.LaneTestEntity;

import java.util.List;
import java.util.Map;

/**
 * 泳道测试
 *
 * @author provenceee
 * @since 2023-03-02
 */
public interface LaneService {
    /**
     * 泳道测试
     *
     * @param name 测试空策略
     * @param laneTestEntity 测试.id和.isEnabled()
     * @param arr 测试[0]
     * @param list 测试.get(0)
     * @param map 测试.get("key")
     * @return map
     */
    Map<String, Object> getLaneByDubbo(String name, LaneTestEntity laneTestEntity, String[] arr, List<Integer> list,
            Map<String, Object> map);

    /**
     * 泳道测试
     *
     * @param name 测试空策略
     * @param laneTestEntity 测试.id和.isEnabled()
     * @return map
     */
    Map<String, Object> getLaneByFeign(String name, LaneTestEntity laneTestEntity);

    /**
     * 泳道测试
     *
     * @param name 测试空策略
     * @param laneTestEntity 测试.id和.isEnabled()
     * @return map
     */
    Map<String, Object> getLaneByRest(String name, LaneTestEntity laneTestEntity);
}
