/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.config.entity;

/**
 * Parameter matching
 *
 * @author provenceee
 * @since 2021-10-27
 */
public class MatchRule {
    /**
     * Value matching rules
     */
    private ValueMatch valueMatch;

    /**
     * Whether it is case-sensitive
     */
    private boolean caseInsensitive;

    /**
     * Dubbo obtains the type of the parameter: [], [.name], [.isEnabled()], [[0]], [.get(0)], [.get("key")]
     */
    private String type;

    public ValueMatch getValueMatch() {
        return valueMatch;
    }

    public void setValueMatch(ValueMatch valueMatch) {
        this.valueMatch = valueMatch;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public void setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}