/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.flowcontrol.adapte.cse.match.operator;

import com.huawei.flowcontrol.util.StringUtils;

/**
 * 包含
 *
 * @author zhouss
 * @since 2021-11-22
 */
public class ContainOperator implements Operator {
    @Override
    public boolean match(String targetValue, String patternValue) {
        return StringUtils.contains(targetValue, patternValue);
    }

    @Override
    public String getId() {
        return "contain";
    }
}
