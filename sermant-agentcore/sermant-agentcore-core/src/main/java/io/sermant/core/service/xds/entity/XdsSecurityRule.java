/*
 * Copyright (C) 2025-2025 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.core.service.xds.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * xDS Authorization Rule
 *
 * @author lilai
 * @since 2025-06-14
 */
public class XdsSecurityRule {
    private Map<String, XdsJwtRule> jwtRules = new HashMap<>();

    private Map<String, XdsAuthorizationRule> allowRules = new HashMap<>();

    private Map<String, XdsAuthorizationRule> denyRules = new HashMap<>();

    public Map<String, XdsJwtRule> getJwtRules() {
        return jwtRules;
    }

    public void setJwtRules(Map<String, XdsJwtRule> jwtRules) {
        this.jwtRules = jwtRules;
    }

    public Map<String, XdsAuthorizationRule> getAllowRules() {
        return allowRules;
    }

    public void setAllowRules(Map<String, XdsAuthorizationRule> allowRules) {
        this.allowRules = allowRules;
    }

    public Map<String, XdsAuthorizationRule> getDenyRules() {
        return denyRules;
    }

    public void setDenyRules(
            Map<String, XdsAuthorizationRule> denyRules) {
        this.denyRules = denyRules;
    }
}
