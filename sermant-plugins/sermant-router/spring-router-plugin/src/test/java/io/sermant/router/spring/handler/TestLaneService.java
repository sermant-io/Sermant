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

package io.sermant.router.spring.handler;

import io.sermant.router.spring.service.LaneService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * For testing LaneService
 *
 * @author provenceee
 * @since 2023-02-28
 */
public class TestLaneService implements LaneService {
    private boolean returnEmpty;

    @Override
    public Map<String, List<String>> getLaneByParameterArray(String path, String methodName,
            Map<String, List<String>> headers, Map<String, String[]> parameters) {
        return getLane();
    }

    @Override
    public Map<String, List<String>> getLaneByParameterList(String path, String methodName,
            Map<String, List<String>> headers, Map<String, List<String>> parameters) {
        return getLane();
    }

    private Map<String, List<String>> getLane() {
        if (returnEmpty) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> map = new HashMap<>();
        map.put("sermant-flag", Collections.singletonList("flag1"));
        map.put("bar", Collections.singletonList("bar2"));
        return map;
    }

    public void setReturnEmpty(boolean returnEmpty) {
        this.returnEmpty = returnEmpty;
    }
}
