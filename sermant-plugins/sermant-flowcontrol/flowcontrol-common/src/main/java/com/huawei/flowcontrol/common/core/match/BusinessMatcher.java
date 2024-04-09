/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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

/**
 * Based on org/apache/servicecomb/governance/marker/TrafficMarker.java from the Apache ServiceComb Java Chassis
 * project.
 */

package com.huawei.flowcontrol.common.core.match;

import com.huawei.flowcontrol.common.core.rule.Configurable;
import com.huawei.flowcontrol.common.entity.RequestEntity;

import java.util.List;

/**
 * business scenario matching
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class BusinessMatcher extends Configurable implements Matcher {
    /**
     * configuration name
     */
    private String name;

    /**
     * all matchers for the service scenario
     */
    private List<RequestMatcher> matches;

    @Override
    public boolean isInValid() {
        return matches == null || matches.isEmpty();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public List<RequestMatcher> getMatches() {
        return matches;
    }

    public void setMatches(List<RequestMatcher> matches) {
        this.matches = matches;
    }

    /**
     * if it matches
     *
     * The matching rules are as follows: If a service scenario is matched, the match is successful
     *
     * @param requestEntity request body
     * @return if it matches
     */
    @Override
    public boolean match(RequestEntity requestEntity) {
        if (requestEntity.getMethod() == null) {
            return false;
        }
        if (matches == null) {
            return false;
        }
        for (RequestMatcher matcher : matches) {
            // If one scenario is successfully matched, the requirement is met
            if (matcher.match(requestEntity)) {
                return true;
            }
        }
        return false;
    }
}
