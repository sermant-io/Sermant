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

import java.util.List;
import java.util.Map;

/**
 * xDS Json Web Token Rule
 *
 * @author lilai
 * @since 2025-06-14
 */
public class XdsJwtRule {
    private String name;

    private String issuer;

    private List<String> audiences;

    private String jwks;

    private Map<String, String> fromHeaders;

    private List<String> fromParams;

    /**
     * Constructor
     */
    public XdsJwtRule() {
    }

    public String getName() {
        return name;
    }

    public String getIssuer() {
        return issuer;
    }

    public List<String> getAudiences() {
        return audiences;
    }

    public String getJwks() {
        return jwks;
    }

    public Map<String, String> getFromHeaders() {
        return fromHeaders;
    }

    public List<String> getFromParams() {
        return fromParams;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public void setAudiences(List<String> audiences) {
        this.audiences = audiences;
    }

    public void setJwks(String jwks) {
        this.jwks = jwks;
    }

    public void setFromHeaders(Map<String, String> fromHeaders) {
        this.fromHeaders = fromHeaders;
    }

    public void setFromParams(List<String> fromParams) {
        this.fromParams = fromParams;
    }
}
