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

package io.sermant.router.config.entity;

import java.util.List;

/**
 * Rules
 *
 * @author provenceee
 * @since 2021-10-27
 */
public class Rule {
    /**
     * priority, the lower the value, the lower the priority
     */
    private int precedence;

    /**
     * Matching rules
     */
    private Match match;

    /**
     * Route
     */
    private List<Route> route;

    /**
     * Downgrade routes
     */
    private List<Route> fallback;

    public void setPrecedence(int precedence) {
        this.precedence = precedence;
    }

    public int getPrecedence() {
        return this.precedence;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public Match getMatch() {
        return this.match;
    }

    public void setRoute(List<Route> route) {
        this.route = route;
    }

    public List<Route> getRoute() {
        return this.route;
    }

    public List<Route> getFallback() {
        return fallback;
    }

    public void setFallback(List<Route> fallback) {
        this.fallback = fallback;
    }
}
