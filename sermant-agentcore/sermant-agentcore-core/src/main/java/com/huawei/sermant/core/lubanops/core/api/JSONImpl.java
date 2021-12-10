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

package com.huawei.sermant.core.lubanops.core.api;

import com.huawei.sermant.core.lubanops.bootstrap.api.JSONAPI;
import com.huawei.sermant.core.lubanops.integration.utils.JSON;

import java.util.List;

public class JSONImpl implements JSONAPI {

    @Override
    public String toJSONString(Object object) {
        return JSON.toJSONString(object);
    }

    @Override
    public String toJSONString(Object obj, List<String> excludeKeys) {
        return JSON.toJSONString(obj, excludeKeys);
    }

    @Override
    public <T> T parseObject(String text, Class<T> type) {
        return JSON.parseObject(text, type);
    }

    @Override
    public int[] parseIntArray(String s) {
        return JSON.parseIntArray(s);
    }

    @Override
    public <T> List<T> parseList(String s, Class<T> type) {
        return JSON.parseList(s, type);
    }

}
